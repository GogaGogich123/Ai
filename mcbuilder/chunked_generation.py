import torch
import torch.nn.functional as F
import numpy as np
from typing import Tuple, List, Optional
from dataclasses import dataclass
from tqdm import tqdm

@dataclass
class ChunkConfig:
    chunk_size: int = 32
    overlap: int = 8
    blend_width: int = 4

class MultiScaleChunkedGenerator:
    def __init__(
        self,
        vqvae,
        diffusion,
        device,
        chunk_config: ChunkConfig = None
    ):
        self.vqvae = vqvae
        self.diffusion = diffusion
        self.device = device
        self.config = chunk_config or ChunkConfig()
        
        self.vqvae.eval()
        self.diffusion.eval()
    
    def generate_large_build(
        self,
        target_size: Tuple[int, int, int],
        guidance_scale: float = 1.0,
        num_inference_steps: int = 50,
        seed: Optional[int] = None
    ) -> torch.Tensor:
        if seed is not None:
            torch.manual_seed(seed)
            np.random.seed(seed)
        
        h, w, d = target_size
        chunk_size = self.config.chunk_size
        overlap = self.config.overlap
        
        if h <= chunk_size and w <= chunk_size and d <= chunk_size:
            return self._generate_single_chunk(target_size, num_inference_steps)
        
        print(f"Generating large build {target_size} with chunked generation...")
        
        num_chunks_h = (h - overlap) // (chunk_size - overlap) + (1 if (h - overlap) % (chunk_size - overlap) > 0 else 0)
        num_chunks_w = (w - overlap) // (chunk_size - overlap) + (1 if (w - overlap) % (chunk_size - overlap) > 0 else 0)
        num_chunks_d = (d - overlap) // (chunk_size - overlap) + (1 if (d - overlap) % (chunk_size - overlap) > 0 else 0)
        
        total_chunks = num_chunks_h * num_chunks_w * num_chunks_d
        print(f"Total chunks: {total_chunks} ({num_chunks_h}x{num_chunks_w}x{num_chunks_d})")
        
        full_build = torch.zeros(h, w, d, dtype=torch.long, device=self.device)
        weight_map = torch.zeros(h, w, d, dtype=torch.float32, device=self.device)
        
        chunk_positions = self._calculate_chunk_positions(target_size)
        
        context_chunks = {}
        
        for idx, (h_start, w_start, d_start, h_end, w_end, d_end) in enumerate(tqdm(chunk_positions, desc="Generating chunks")):
            chunk_h = h_end - h_start
            chunk_w = w_end - w_start
            chunk_d = d_end - d_start
            
            context_latent = self._gather_context(
                context_chunks, 
                h_start, w_start, d_start,
                chunk_h, chunk_w, chunk_d
            )
            
            chunk_blocks = self._generate_chunk_with_context(
                (chunk_h, chunk_w, chunk_d),
                context_latent,
                num_inference_steps
            )
            
            weights = self._create_blend_weights(chunk_h, chunk_w, chunk_d)
            
            full_build[h_start:h_end, w_start:w_end, d_start:d_end] += (chunk_blocks * weights).long()
            weight_map[h_start:h_end, w_start:w_end, d_start:d_end] += weights
            
            with torch.no_grad():
                chunk_tensor = F.one_hot(chunk_blocks, num_classes=self.vqvae.num_blocks).float()
                chunk_tensor = chunk_tensor.permute(3, 0, 1, 2).unsqueeze(0)
                
                block_emb = self.vqvae.block_embedding(chunk_blocks.unsqueeze(0))
                block_emb = block_emb.permute(0, 4, 1, 2, 3)
                
                latent = self.vqvae.encoder(block_emb)
                
                context_chunks[(h_start, w_start, d_start)] = latent.detach()
        
        weight_map = torch.clamp(weight_map, min=1e-6)
        full_build = (full_build.float() / weight_map).long()
        
        return full_build
    
    def _calculate_chunk_positions(self, target_size: Tuple[int, int, int]) -> List[Tuple[int, int, int, int, int, int]]:
        h, w, d = target_size
        chunk_size = self.config.chunk_size
        overlap = self.config.overlap
        stride = chunk_size - overlap
        
        positions = []
        
        h_starts = list(range(0, h - chunk_size + 1, stride))
        if not h_starts or h_starts[-1] + chunk_size < h:
            h_starts.append(max(0, h - chunk_size))
        
        w_starts = list(range(0, w - chunk_size + 1, stride))
        if not w_starts or w_starts[-1] + chunk_size < w:
            w_starts.append(max(0, w - chunk_size))
        
        d_starts = list(range(0, d - chunk_size + 1, stride))
        if not d_starts or d_starts[-1] + chunk_size < d:
            d_starts.append(max(0, d - chunk_size))
        
        for h_start in h_starts:
            for w_start in w_starts:
                for d_start in d_starts:
                    h_end = min(h_start + chunk_size, h)
                    w_end = min(w_start + chunk_size, w)
                    d_end = min(d_start + chunk_size, d)
                    
                    positions.append((h_start, w_start, d_start, h_end, w_end, d_end))
        
        return positions
    
    def _gather_context(
        self,
        context_chunks: dict,
        h_start: int,
        w_start: int,
        d_start: int,
        chunk_h: int,
        chunk_w: int,
        chunk_d: int
    ) -> Optional[torch.Tensor]:
        if not context_chunks:
            return None
        
        overlap = self.config.overlap
        chunk_size = self.config.chunk_size
        
        neighboring_positions = [
            (h_start - chunk_size + overlap, w_start, d_start),
            (h_start, w_start - chunk_size + overlap, d_start),
            (h_start, w_start, d_start - chunk_size + overlap),
        ]
        
        context_latents = []
        for pos in neighboring_positions:
            if pos in context_chunks:
                context_latents.append(context_chunks[pos])
        
        if context_latents:
            return torch.cat(context_latents, dim=1)
        
        return None
    
    def _generate_chunk_with_context(
        self,
        chunk_size: Tuple[int, int, int],
        context_latent: Optional[torch.Tensor],
        num_inference_steps: int
    ) -> torch.Tensor:
        h, w, d = chunk_size
        latent_h, latent_w, latent_d = h // 4, w // 4, d // 4
        
        latent_shape = (1, self.vqvae.embedding_dim, latent_h, latent_w, latent_d)
        
        with torch.no_grad():
            if context_latent is not None:
                latent = self.diffusion.sample_with_context(
                    latent_shape,
                    self.device,
                    context_latent,
                    num_inference_steps=num_inference_steps
                )
            else:
                latent = self.diffusion.sample(
                    latent_shape,
                    self.device,
                    num_inference_steps=num_inference_steps
                )
            
            blocks_logits = self.vqvae.decode_latent(latent)
            blocks = blocks_logits.argmax(dim=1).squeeze(0)
            
            if blocks.shape != chunk_size:
                blocks = F.interpolate(
                    blocks.unsqueeze(0).unsqueeze(0).float(),
                    size=chunk_size,
                    mode='nearest'
                ).long().squeeze(0).squeeze(0)
        
        return blocks
    
    def _generate_single_chunk(
        self,
        size: Tuple[int, int, int],
        num_inference_steps: int
    ) -> torch.Tensor:
        h, w, d = size
        latent_h, latent_w, latent_d = h // 4, w // 4, d // 4
        
        latent_shape = (1, self.vqvae.embedding_dim, latent_h, latent_w, latent_d)
        
        with torch.no_grad():
            latent = self.diffusion.sample(
                latent_shape,
                self.device,
                num_inference_steps=num_inference_steps
            )
            
            blocks_logits = self.vqvae.decode_latent(latent)
            blocks = blocks_logits.argmax(dim=1).squeeze(0)
        
        return blocks
    
    def _create_blend_weights(self, h: int, w: int, d: int) -> torch.Tensor:
        blend_width = self.config.blend_width
        
        weights = torch.ones(h, w, d, device=self.device)
        
        for i in range(blend_width):
            weight = (i + 1) / (blend_width + 1)
            
            if i < h:
                weights[i, :, :] *= weight
                weights[-(i+1), :, :] *= weight
            
            if i < w:
                weights[:, i, :] *= weight
                weights[:, -(i+1), :] *= weight
            
            if i < d:
                weights[:, :, i] *= weight
                weights[:, :, -(i+1)] *= weight
        
        return weights
    
    def generate_hierarchical(
        self,
        target_size: Tuple[int, int, int],
        num_inference_steps: int = 50,
        seed: Optional[int] = None
    ) -> torch.Tensor:
        if seed is not None:
            torch.manual_seed(seed)
            np.random.seed(seed)
        
        h, w, d = target_size
        
        scales = []
        current_size = [h, w, d]
        while any(s > 32 for s in current_size):
            scales.append(tuple(current_size))
            current_size = [max(32, s // 2) for s in current_size]
        scales.append(tuple(current_size))
        scales.reverse()
        
        print(f"Hierarchical generation with {len(scales)} scales: {scales}")
        
        current_build = None
        
        for scale_idx, scale_size in enumerate(scales):
            print(f"\nGenerating scale {scale_idx + 1}/{len(scales)}: {scale_size}")
            
            if scale_idx == 0:
                current_build = self.generate_large_build(
                    scale_size,
                    num_inference_steps=num_inference_steps,
                    seed=seed
                )
            else:
                upscaled = F.interpolate(
                    current_build.unsqueeze(0).unsqueeze(0).float(),
                    size=scale_size,
                    mode='nearest'
                ).long().squeeze(0).squeeze(0)
                
                refined = self._refine_build(
                    upscaled,
                    scale_size,
                    num_inference_steps=num_inference_steps // 2
                )
                
                current_build = refined
        
        return current_build
    
    def _refine_build(
        self,
        coarse_build: torch.Tensor,
        target_size: Tuple[int, int, int],
        num_inference_steps: int
    ) -> torch.Tensor:
        h, w, d = target_size
        chunk_size = self.config.chunk_size
        overlap = self.config.overlap
        
        if h <= chunk_size and w <= chunk_size and d <= chunk_size:
            return coarse_build
        
        refined_build = coarse_build.clone()
        
        chunk_positions = self._calculate_chunk_positions(target_size)
        
        for h_start, w_start, d_start, h_end, w_end, d_end in tqdm(chunk_positions, desc="Refining chunks"):
            coarse_chunk = coarse_build[h_start:h_end, w_start:w_end, d_start:d_end]
            
            with torch.no_grad():
                block_emb = self.vqvae.block_embedding(coarse_chunk.unsqueeze(0))
                block_emb = block_emb.permute(0, 4, 1, 2, 3)
                
                latent = self.vqvae.encoder(block_emb)
                
                noise_level = 0.3
                noise = torch.randn_like(latent) * noise_level
                latent_noisy = latent + noise
                
                latent_shape = latent.shape
                latent_refined = self.diffusion.sample(
                    latent_shape,
                    self.device,
                    num_inference_steps=num_inference_steps,
                    init_latent=latent_noisy
                )
                
                blocks_logits = self.vqvae.decode_latent(latent_refined)
                refined_chunk = blocks_logits.argmax(dim=1).squeeze(0)
            
            refined_build[h_start:h_end, w_start:w_end, d_start:d_end] = refined_chunk
        
        return refined_build
