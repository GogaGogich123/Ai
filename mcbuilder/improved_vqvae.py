import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import Tuple

from .vqvae import VectorQuantizer, ResidualBlock3D

class ImprovedEncoder3D(nn.Module):
    def __init__(self, in_channels: int, hidden_dims: list, latent_dim: int, num_res_blocks: int = 3):
        super().__init__()
        
        modules = []
        for h_dim in hidden_dims:
            modules.append(
                nn.Sequential(
                    nn.Conv3d(in_channels, h_dim, kernel_size=4, stride=2, padding=1),
                    nn.GroupNorm(8, h_dim),
                    nn.SiLU()
                )
            )
            
            for _ in range(num_res_blocks):
                modules.append(ResidualBlock3D(h_dim))
            
            in_channels = h_dim
        
        self.encoder = nn.Sequential(*modules)
        
        self.conv_out = nn.Sequential(
            nn.Conv3d(hidden_dims[-1], latent_dim, kernel_size=3, padding=1),
            nn.GroupNorm(8, latent_dim),
            nn.SiLU(),
            nn.Conv3d(latent_dim, latent_dim, kernel_size=1)
        )
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.encoder(x)
        return self.conv_out(x)

class ImprovedDecoder3D(nn.Module):
    def __init__(self, latent_dim: int, hidden_dims: list, out_channels: int, num_res_blocks: int = 3):
        super().__init__()
        
        self.conv_in = nn.Sequential(
            nn.Conv3d(latent_dim, hidden_dims[0], kernel_size=3, padding=1),
            nn.GroupNorm(8, hidden_dims[0]),
            nn.SiLU()
        )
        
        modules = []
        for i in range(len(hidden_dims)):
            for _ in range(num_res_blocks):
                modules.append(ResidualBlock3D(hidden_dims[i]))
            
            if i < len(hidden_dims) - 1:
                modules.append(
                    nn.Sequential(
                        nn.ConvTranspose3d(hidden_dims[i], hidden_dims[i+1], kernel_size=4, stride=2, padding=1),
                        nn.GroupNorm(8, hidden_dims[i+1]),
                        nn.SiLU()
                    )
                )
        
        self.decoder = nn.Sequential(*modules)
        
        self.conv_out = nn.Sequential(
            ResidualBlock3D(hidden_dims[-1]),
            nn.GroupNorm(8, hidden_dims[-1]),
            nn.SiLU(),
            nn.Conv3d(hidden_dims[-1], out_channels, kernel_size=3, padding=1)
        )
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.conv_in(x)
        x = self.decoder(x)
        return self.conv_out(x)

class ImprovedVQVAE3D(nn.Module):
    def __init__(
        self,
        num_blocks: int,
        embedding_dim: int = 128,
        num_embeddings: int = 1024,
        hidden_dims: list = [64, 128, 256],
        num_res_blocks: int = 3,
        commitment_cost: float = 0.25
    ):
        super().__init__()
        
        self.num_blocks = num_blocks
        self.embedding_dim = embedding_dim
        
        self.block_embedding = nn.Embedding(num_blocks, 32)
        
        self.encoder = ImprovedEncoder3D(
            in_channels=32,
            hidden_dims=hidden_dims,
            latent_dim=embedding_dim,
            num_res_blocks=num_res_blocks
        )
        
        self.vq = VectorQuantizer(
            num_embeddings=num_embeddings,
            embedding_dim=embedding_dim,
            commitment_cost=commitment_cost
        )
        
        self.decoder = ImprovedDecoder3D(
            latent_dim=embedding_dim,
            hidden_dims=list(reversed(hidden_dims)),
            out_channels=num_blocks,
            num_res_blocks=num_res_blocks
        )
    
    def forward(self, x: torch.Tensor) -> Tuple[torch.Tensor, torch.Tensor, torch.Tensor]:
        x_embedded = self.block_embedding(x).permute(0, 4, 1, 2, 3)
        
        z = self.encoder(x_embedded)
        
        z = z.permute(0, 2, 3, 4, 1)
        quantized, vq_loss, encoding_indices = self.vq(z)
        quantized = quantized.permute(0, 4, 1, 2, 3)
        
        x_recon = self.decoder(quantized)
        
        return x_recon, vq_loss, encoding_indices
    
    def encode(self, x: torch.Tensor) -> torch.Tensor:
        x_embedded = self.block_embedding(x).permute(0, 4, 1, 2, 3)
        z = self.encoder(x_embedded)
        z = z.permute(0, 2, 3, 4, 1)
        _, _, encoding_indices = self.vq(z)
        return encoding_indices
    
    def encode_to_latent(self, x: torch.Tensor) -> torch.Tensor:
        x_embedded = self.block_embedding(x).permute(0, 4, 1, 2, 3)
        z = self.encoder(x_embedded)
        z = z.permute(0, 2, 3, 4, 1)
        quantized, _, _ = self.vq(z)
        return quantized.permute(0, 4, 1, 2, 3)
    
    def decode_latent(self, z: torch.Tensor) -> torch.Tensor:
        return self.decoder(z)
    
    def decode_indices(self, indices: torch.Tensor, shape: Tuple[int, int, int]) -> torch.Tensor:
        quantized = self.vq.embedding(indices)
        quantized = quantized.view(-1, *shape, self.embedding_dim).permute(0, 4, 1, 2, 3)
        x_recon = self.decoder(quantized)
        return x_recon
