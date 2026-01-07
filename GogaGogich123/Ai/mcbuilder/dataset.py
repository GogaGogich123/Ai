import torch
from torch.utils.data import Dataset, IterableDataset
import numpy as np
import os
import json
import hashlib
from pathlib import Path
from typing import Optional, List, Tuple, Dict
import random

from .buildpaste_api import BuildPasteAPI, BuildData, BuildMetadata
from .blocks import BLOCKS_ARRAY, is_blacklisted
from .build_analyzer import BuildAnalyzer
from .mistral_describer import MistralDescriber

class BuildPasteDataset(IterableDataset):
    def __init__(
        self,
        cache_dir: str,
        chunk_size: int = 32,
        overlap: int = 4,
        min_blocks: int = 800,
        max_blocks: Optional[int] = None,
        categories: Optional[List[str]] = None,
        transform=None,
        download: bool = True,
        generate_descriptions: bool = False,
        mistral_api_key: Optional[str] = None,
        description_language: str = "en"
    ):
        super().__init__()
        self.cache_dir = Path(cache_dir)
        self.cache_dir.mkdir(parents=True, exist_ok=True)
        
        self.chunk_size = chunk_size
        self.overlap = overlap
        self.min_blocks = min_blocks
        self.max_blocks = max_blocks
        self.categories = categories
        self.transform = transform
        self.download = download
        
        self.generate_descriptions = generate_descriptions
        self.description_language = description_language
        
        self.api = BuildPasteAPI()
        
        if generate_descriptions:
            if not mistral_api_key:
                raise ValueError("mistral_api_key required when generate_descriptions=True")
            self.analyzer = BuildAnalyzer()
            self.describer = MistralDescriber(mistral_api_key)
            print(f"✓ Mistral descriptions enabled (language: {description_language})")
        else:
            self.analyzer = None
            self.describer = None
        
        self.metadata_cache = self.cache_dir / "metadata.json"
        self.builds_cache = self.cache_dir / "builds"
        self.descriptions_cache = self.cache_dir / "descriptions"
        self.builds_cache.mkdir(exist_ok=True)
        self.descriptions_cache.mkdir(exist_ok=True)
    
    def _get_build_cache_path(self, build_id: str) -> Path:
        return self.builds_cache / f"{build_id}.npz"
    
    def _get_description_cache_path(self, build_id: str) -> Path:
        return self.descriptions_cache / f"{build_id}.txt"
    
    def _is_cached(self, build_id: str) -> bool:
        return self._get_build_cache_path(build_id).exists()
    
    def _is_description_cached(self, build_id: str) -> bool:
        return self._get_description_cache_path(build_id).exists()
    
    def _cache_build(self, build_data: BuildData, description: Optional[str] = None):
        cache_path = self._get_build_cache_path(build_data.metadata.build_id)
        
        size = build_data.size
        blocks = np.array(build_data.blocks, dtype=np.int16).reshape(size)
        
        metadata_dict = {
            'name': build_data.metadata.name,
            'category': build_data.metadata.category,
            'block_count': build_data.metadata.block_count
        }
        
        np.savez_compressed(
            cache_path,
            blocks=blocks,
            size=size,
            direction=build_data.direction,
            block_count=build_data.metadata.block_count,
            metadata=json.dumps(metadata_dict)
        )
        
        if description:
            desc_path = self._get_description_cache_path(build_data.metadata.build_id)
            with open(desc_path, 'w', encoding='utf-8') as f:
                f.write(description)
    
    def _load_cached_build(self, build_id: str) -> Optional[Tuple[np.ndarray, Optional[str]]]:
        cache_path = self._get_build_cache_path(build_id)
        if not cache_path.exists():
            return None
        
        try:
            data = np.load(cache_path, allow_pickle=True)
            blocks = data['blocks']
            
            description = None
            desc_path = self._get_description_cache_path(build_id)
            if desc_path.exists():
                with open(desc_path, 'r', encoding='utf-8') as f:
                    description = f.read().strip()
            
            return blocks, description
        except Exception as e:
            print(f"Error loading cached build {build_id}: {e}")
            return None
    
    def _generate_description_for_build(
        self, 
        blocks: np.ndarray, 
        metadata: BuildMetadata
    ) -> str:
        try:
            block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
            
            blocks_tensor = torch.from_numpy(blocks)
            analysis = self.analyzer.analyze_build(blocks_tensor, block_names)
            
            analysis_prompt = self.analyzer.format_analysis_for_prompt(analysis)
            
            original_name = metadata.name if metadata.name else "Unknown Build"
            original_category = metadata.category if metadata.category else "Unknown"
            
            enhanced_prompt = f"{analysis_prompt}\n\nOriginal name: {original_name}\nCategory: {original_category}\n"
            
            description = self.describer.generate_description(
                analysis,
                enhanced_prompt,
                style="detailed",
                language=self.description_language
            )
            
            return description
        
        except Exception as e:
            print(f"Warning: Failed to generate description: {e}")
            return f"{metadata.name} - {metadata.category} build with {metadata.block_count} blocks"
    
    def _download_and_cache_build(self, metadata: BuildMetadata) -> Optional[Tuple[np.ndarray, Optional[str]]]:
        if self._is_cached(metadata.build_id):
            return self._load_cached_build(metadata.build_id)
        
        if not self.download:
            return None
        
        print(f"Downloading build: {metadata.name} ({metadata.build_id})...")
        build_data = self.api.download_build(metadata.build_id)
        if build_data is None:
            return None
        
        try:
            size = build_data.size
            blocks_raw = np.array(build_data.blocks, dtype=np.int16)
            
            max_valid_idx = len(BLOCKS_ARRAY) - 1
            if np.any(blocks_raw > max_valid_idx) or np.any(blocks_raw < 0):
                invalid_count = np.sum((blocks_raw > max_valid_idx) | (blocks_raw < 0))
                print(f"  ⚠ Skipping build: {invalid_count} invalid block indices (max valid: {max_valid_idx})")
                return None
            
            blocks = blocks_raw.reshape(size)
        except (ValueError, TypeError) as e:
            print(f"  ⚠ Skipping build with modded blocks: {e}")
            return None
        
        description = None
        if self.generate_descriptions and not self._is_description_cached(metadata.build_id):
            print(f"  Generating AI description...")
            description = self._generate_description_for_build(blocks, metadata)
            print(f"  ✓ Description: {description[:80]}...")
        elif self._is_description_cached(metadata.build_id):
            desc_path = self._get_description_cache_path(metadata.build_id)
            with open(desc_path, 'r', encoding='utf-8') as f:
                description = f.read().strip()
        
        self._cache_build(build_data, description)
        
        return blocks, description
    
    def _extract_chunks(self, blocks: np.ndarray) -> List[np.ndarray]:
        sx, sy, sz = blocks.shape
        chunks = []
        
        stride = self.chunk_size - self.overlap
        
        for x in range(0, sx, stride):
            for y in range(0, sy, stride):
                for z in range(0, sz, stride):
                    x_end = min(x + self.chunk_size, sx)
                    y_end = min(y + self.chunk_size, sy)
                    z_end = min(z + self.chunk_size, sz)
                    
                    chunk = blocks[x:x_end, y:y_end, z:z_end]
                    
                    if chunk.shape[0] < 8 or chunk.shape[1] < 8 or chunk.shape[2] < 8:
                        continue
                    
                    padded = np.zeros((self.chunk_size, self.chunk_size, self.chunk_size), dtype=np.int16)
                    padded[:chunk.shape[0], :chunk.shape[1], :chunk.shape[2]] = chunk
                    
                    non_air = np.sum(padded != 0)
                    if non_air < 50:
                        continue
                    
                    chunks.append(padded)
        
        return chunks
    
    def __iter__(self):
        worker_info = torch.utils.data.get_worker_info()
        
        build_iter = self.api.iter_all_builds(
            categories=self.categories,
            min_blocks=self.min_blocks,
            max_blocks=self.max_blocks,
            exclude_premium=True
        )
        
        for idx, metadata in enumerate(build_iter):
            if worker_info is not None:
                if idx % worker_info.num_workers != worker_info.id:
                    continue
            
            result = self._download_and_cache_build(metadata)
            if result is None:
                continue
            
            blocks, description = result
            
            chunks = self._extract_chunks(blocks)
            
            for chunk in chunks:
                chunk_tensor = torch.from_numpy(chunk).long()
                
                if self.transform:
                    chunk_tensor = self.transform(chunk_tensor)
                
                yield {
                    'blocks': chunk_tensor,
                    'build_id': metadata.build_id,
                    'category': metadata.category,
                    'description': description,
                    'build_name': metadata.name
                }

class MaskedChunkDataset(IterableDataset):
    def __init__(
        self,
        base_dataset: BuildPasteDataset,
        mask_ratio: float = 0.15,
        mask_token_id: int = -1
    ):
        super().__init__()
        self.base_dataset = base_dataset
        self.mask_ratio = mask_ratio
        self.mask_token_id = mask_token_id
    
    def __iter__(self):
        for sample in self.base_dataset:
            blocks = sample['blocks']
            
            original = blocks.clone()
            
            mask = torch.rand(blocks.shape) < self.mask_ratio
            
            masked = blocks.clone()
            masked[mask] = self.mask_token_id
            
            yield {
                'input': masked,
                'target': original,
                'mask': mask,
                'build_id': sample['build_id'],
                'category': sample['category'],
                'description': sample.get('description'),
                'build_name': sample.get('build_name')
            }
