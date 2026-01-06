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
        download: bool = True
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
        
        self.api = BuildPasteAPI()
        
        self.metadata_cache = self.cache_dir / "metadata.json"
        self.builds_cache = self.cache_dir / "builds"
        self.builds_cache.mkdir(exist_ok=True)
    
    def _get_build_cache_path(self, build_id: str) -> Path:
        return self.builds_cache / f"{build_id}.npz"
    
    def _is_cached(self, build_id: str) -> bool:
        return self._get_build_cache_path(build_id).exists()
    
    def _cache_build(self, build_data: BuildData):
        cache_path = self._get_build_cache_path(build_data.metadata.build_id)
        
        size = build_data.size
        blocks = np.array(build_data.blocks, dtype=np.int16).reshape(size)
        
        np.savez_compressed(
            cache_path,
            blocks=blocks,
            size=size,
            direction=build_data.direction,
            block_count=build_data.metadata.block_count
        )
    
    def _load_cached_build(self, build_id: str) -> Optional[np.ndarray]:
        cache_path = self._get_build_cache_path(build_id)
        if not cache_path.exists():
            return None
        
        try:
            data = np.load(cache_path)
            return data['blocks']
        except Exception as e:
            print(f"Error loading cached build {build_id}: {e}")
            return None
    
    def _download_and_cache_build(self, metadata: BuildMetadata) -> Optional[np.ndarray]:
        if self._is_cached(metadata.build_id):
            return self._load_cached_build(metadata.build_id)
        
        if not self.download:
            return None
        
        build_data = self.api.download_build(metadata.build_id)
        if build_data is None:
            return None
        
        self._cache_build(build_data)
        return self._load_cached_build(metadata.build_id)
    
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
            
            blocks = self._download_and_cache_build(metadata)
            if blocks is None:
                continue
            
            chunks = self._extract_chunks(blocks)
            
            for chunk in chunks:
                chunk_tensor = torch.from_numpy(chunk).long()
                
                if self.transform:
                    chunk_tensor = self.transform(chunk_tensor)
                
                yield {
                    'blocks': chunk_tensor,
                    'build_id': metadata.build_id,
                    'category': metadata.category
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
                'category': sample['category']
            }
