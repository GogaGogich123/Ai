import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import Tuple

class VectorQuantizer(nn.Module):
    def __init__(self, num_embeddings: int, embedding_dim: int, commitment_cost: float = 0.25):
        super().__init__()
        self.num_embeddings = num_embeddings
        self.embedding_dim = embedding_dim
        self.commitment_cost = commitment_cost
        
        self.embedding = nn.Embedding(num_embeddings, embedding_dim)
        self.embedding.weight.data.uniform_(-1/num_embeddings, 1/num_embeddings)
    
    def forward(self, inputs: torch.Tensor) -> Tuple[torch.Tensor, torch.Tensor, torch.Tensor]:
        input_shape = inputs.shape
        flat_input = inputs.view(-1, self.embedding_dim)
        
        distances = (
            torch.sum(flat_input**2, dim=1, keepdim=True) 
            + torch.sum(self.embedding.weight**2, dim=1)
            - 2 * torch.matmul(flat_input, self.embedding.weight.t())
        )
        
        encoding_indices = torch.argmin(distances, dim=1).unsqueeze(1)
        encodings = torch.zeros(encoding_indices.shape[0], self.num_embeddings, device=inputs.device)
        encodings.scatter_(1, encoding_indices, 1)
        
        quantized = torch.matmul(encodings, self.embedding.weight).view(input_shape)
        
        e_latent_loss = F.mse_loss(quantized.detach(), inputs)
        q_latent_loss = F.mse_loss(quantized, inputs.detach())
        loss = q_latent_loss + self.commitment_cost * e_latent_loss
        
        quantized = inputs + (quantized - inputs).detach()
        
        return quantized, loss, encoding_indices.view(input_shape[0], -1)

class ResidualBlock3D(nn.Module):
    def __init__(self, channels: int):
        super().__init__()
        self.conv1 = nn.Conv3d(channels, channels, kernel_size=3, padding=1)
        self.conv2 = nn.Conv3d(channels, channels, kernel_size=3, padding=1)
        self.norm1 = nn.GroupNorm(8, channels)
        self.norm2 = nn.GroupNorm(8, channels)
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        residual = x
        x = F.relu(self.norm1(self.conv1(x)))
        x = self.norm2(self.conv2(x))
        return F.relu(x + residual)

class Encoder3D(nn.Module):
    def __init__(self, in_channels: int, hidden_dims: list, latent_dim: int):
        super().__init__()
        
        modules = []
        for h_dim in hidden_dims:
            modules.append(
                nn.Sequential(
                    nn.Conv3d(in_channels, h_dim, kernel_size=4, stride=2, padding=1),
                    nn.GroupNorm(8, h_dim),
                    nn.ReLU(),
                    ResidualBlock3D(h_dim)
                )
            )
            in_channels = h_dim
        
        self.encoder = nn.Sequential(*modules)
        self.conv_out = nn.Conv3d(hidden_dims[-1], latent_dim, kernel_size=3, padding=1)
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.encoder(x)
        return self.conv_out(x)

class Decoder3D(nn.Module):
    def __init__(self, latent_dim: int, hidden_dims: list, out_channels: int):
        super().__init__()
        
        self.conv_in = nn.Conv3d(latent_dim, hidden_dims[0], kernel_size=3, padding=1)
        
        modules = []
        for i in range(len(hidden_dims) - 1):
            modules.append(
                nn.Sequential(
                    ResidualBlock3D(hidden_dims[i]),
                    nn.ConvTranspose3d(hidden_dims[i], hidden_dims[i+1], kernel_size=4, stride=2, padding=1),
                    nn.GroupNorm(8, hidden_dims[i+1]),
                    nn.ReLU()
                )
            )
        
        self.decoder = nn.Sequential(*modules)
        self.conv_out = nn.Conv3d(hidden_dims[-1], out_channels, kernel_size=3, padding=1)
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.conv_in(x)
        x = self.decoder(x)
        return self.conv_out(x)

class VQVAE3D(nn.Module):
    def __init__(
        self,
        num_blocks: int,
        embedding_dim: int = 64,
        num_embeddings: int = 512,
        hidden_dims: list = [32, 64, 128],
        commitment_cost: float = 0.25
    ):
        super().__init__()
        
        self.num_blocks = num_blocks
        self.embedding_dim = embedding_dim
        
        self.embedding = nn.Embedding(num_blocks, embedding_dim)
        
        self.encoder = Encoder3D(
            in_channels=embedding_dim,
            hidden_dims=hidden_dims,
            latent_dim=embedding_dim
        )
        
        self.vq = VectorQuantizer(
            num_embeddings=num_embeddings,
            embedding_dim=embedding_dim,
            commitment_cost=commitment_cost
        )
        
        self.decoder = Decoder3D(
            latent_dim=embedding_dim,
            hidden_dims=list(reversed(hidden_dims)),
            out_channels=num_blocks
        )
    
    def forward(self, x: torch.Tensor) -> Tuple[torch.Tensor, torch.Tensor, torch.Tensor]:
        x_embedded = self.embedding(x).permute(0, 4, 1, 2, 3)
        
        z = self.encoder(x_embedded)
        
        z = z.permute(0, 2, 3, 4, 1)
        quantized, vq_loss, encoding_indices = self.vq(z)
        quantized = quantized.permute(0, 4, 1, 2, 3)
        
        x_recon = self.decoder(quantized)
        
        return x_recon, vq_loss, encoding_indices
    
    def encode(self, x: torch.Tensor) -> torch.Tensor:
        x_embedded = self.embedding(x).permute(0, 4, 1, 2, 3)
        z = self.encoder(x_embedded)
        z = z.permute(0, 2, 3, 4, 1)
        _, _, encoding_indices = self.vq(z)
        return encoding_indices
    
    def decode_indices(self, indices: torch.Tensor, shape: Tuple[int, int, int]) -> torch.Tensor:
        quantized = self.vq.embedding(indices)
        quantized = quantized.view(-1, *shape, self.embedding_dim).permute(0, 4, 1, 2, 3)
        x_recon = self.decoder(quantized)
        return x_recon
