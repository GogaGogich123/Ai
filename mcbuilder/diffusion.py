import torch
import torch.nn as nn
import torch.nn.functional as F
import math
from typing import Optional, Tuple

class SinusoidalPositionEmbeddings(nn.Module):
    def __init__(self, dim: int):
        super().__init__()
        self.dim = dim

    def forward(self, time: torch.Tensor) -> torch.Tensor:
        device = time.device
        half_dim = self.dim // 2
        embeddings = math.log(10000) / (half_dim - 1)
        embeddings = torch.exp(torch.arange(half_dim, device=device) * -embeddings)
        embeddings = time[:, None] * embeddings[None, :]
        embeddings = torch.cat((embeddings.sin(), embeddings.cos()), dim=-1)
        return embeddings

class AttentionBlock3D(nn.Module):
    def __init__(self, channels: int, num_heads: int = 8):
        super().__init__()
        self.channels = channels
        self.num_heads = num_heads
        
        self.norm = nn.GroupNorm(8, channels)
        self.qkv = nn.Conv3d(channels, channels * 3, 1)
        self.proj_out = nn.Conv3d(channels, channels, 1)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        b, c, d, h, w = x.shape
        residual = x
        
        x = self.norm(x)
        qkv = self.qkv(x)
        
        qkv = qkv.reshape(b, 3, self.num_heads, c // self.num_heads, d * h * w)
        qkv = qkv.permute(1, 0, 2, 4, 3)
        q, k, v = qkv[0], qkv[1], qkv[2]
        
        attn = torch.matmul(q, k.transpose(-2, -1)) / math.sqrt(c // self.num_heads)
        attn = F.softmax(attn, dim=-1)
        
        out = torch.matmul(attn, v)
        out = out.permute(0, 1, 3, 2).reshape(b, c, d, h, w)
        
        out = self.proj_out(out)
        return out + residual

class ResBlock3D(nn.Module):
    def __init__(self, in_channels: int, out_channels: int, time_emb_dim: int, dropout: float = 0.1):
        super().__init__()
        
        self.norm1 = nn.GroupNorm(8, in_channels)
        self.conv1 = nn.Conv3d(in_channels, out_channels, 3, padding=1)
        
        self.time_emb = nn.Sequential(
            nn.SiLU(),
            nn.Linear(time_emb_dim, out_channels)
        )
        
        self.norm2 = nn.GroupNorm(8, out_channels)
        self.dropout = nn.Dropout(dropout)
        self.conv2 = nn.Conv3d(out_channels, out_channels, 3, padding=1)
        
        if in_channels != out_channels:
            self.skip = nn.Conv3d(in_channels, out_channels, 1)
        else:
            self.skip = nn.Identity()

    def forward(self, x: torch.Tensor, time_emb: torch.Tensor) -> torch.Tensor:
        h = self.norm1(x)
        h = F.silu(h)
        h = self.conv1(h)
        
        time_emb = self.time_emb(time_emb)
        h = h + time_emb[:, :, None, None, None]
        
        h = self.norm2(h)
        h = F.silu(h)
        h = self.dropout(h)
        h = self.conv2(h)
        
        return h + self.skip(x)

class UNet3D(nn.Module):
    def __init__(
        self,
        in_channels: int,
        model_channels: int = 128,
        out_channels: int = None,
        num_res_blocks: int = 2,
        attention_resolutions: Tuple[int, ...] = (8, 16),
        dropout: float = 0.1,
        channel_mult: Tuple[int, ...] = (1, 2, 4, 8),
        num_heads: int = 8,
        time_emb_dim: int = None
    ):
        super().__init__()
        
        if out_channels is None:
            out_channels = in_channels
        
        if time_emb_dim is None:
            time_emb_dim = model_channels * 4
        
        self.time_embed = nn.Sequential(
            SinusoidalPositionEmbeddings(model_channels),
            nn.Linear(model_channels, time_emb_dim),
            nn.SiLU(),
            nn.Linear(time_emb_dim, time_emb_dim)
        )
        
        self.input_conv = nn.Conv3d(in_channels, model_channels, 3, padding=1)
        
        self.down_blocks = nn.ModuleList([])
        self.down_samples = nn.ModuleList([])
        
        ch = model_channels
        ds = 1
        for level, mult in enumerate(channel_mult):
            for _ in range(num_res_blocks):
                layers = [
                    ResBlock3D(ch, model_channels * mult, time_emb_dim, dropout)
                ]
                ch = model_channels * mult
                if ds in attention_resolutions:
                    layers.append(AttentionBlock3D(ch, num_heads))
                self.down_blocks.append(nn.ModuleList(layers))
            
            if level != len(channel_mult) - 1:
                self.down_samples.append(nn.Conv3d(ch, ch, 3, stride=2, padding=1))
                ds *= 2
            else:
                self.down_samples.append(nn.Identity())
        
        self.middle_blocks = nn.ModuleList([
            ResBlock3D(ch, ch, time_emb_dim, dropout),
            AttentionBlock3D(ch, num_heads),
            ResBlock3D(ch, ch, time_emb_dim, dropout)
        ])
        
        self.up_blocks = nn.ModuleList([])
        self.up_samples = nn.ModuleList([])
        
        for level, mult in reversed(list(enumerate(channel_mult))):
            for i in range(num_res_blocks + 1):
                layers = [
                    ResBlock3D(
                        ch + (model_channels * mult if i == 0 else 0),
                        model_channels * mult,
                        time_emb_dim,
                        dropout
                    )
                ]
                ch = model_channels * mult
                if ds in attention_resolutions:
                    layers.append(AttentionBlock3D(ch, num_heads))
                self.up_blocks.append(nn.ModuleList(layers))
            
            if level != 0:
                self.up_samples.append(nn.ConvTranspose3d(ch, ch, 4, stride=2, padding=1))
                ds //= 2
            else:
                self.up_samples.append(nn.Identity())
        
        self.output_norm = nn.GroupNorm(8, ch)
        self.output_conv = nn.Conv3d(ch, out_channels, 3, padding=1)

    def forward(self, x: torch.Tensor, t: torch.Tensor) -> torch.Tensor:
        time_emb = self.time_embed(t)
        
        h = self.input_conv(x)
        
        down_features = []
        for blocks, downsample in zip(self.down_blocks, self.down_samples):
            for block in blocks:
                if isinstance(block, ResBlock3D):
                    h = block(h, time_emb)
                else:
                    h = block(h)
            down_features.append(h)
            h = downsample(h)
        
        for block in self.middle_blocks:
            if isinstance(block, ResBlock3D):
                h = block(h, time_emb)
            else:
                h = block(h)
        
        for blocks, upsample in zip(self.up_blocks, self.up_samples):
            skip = down_features.pop()
            if h.shape != skip.shape:
                h = F.interpolate(h, size=skip.shape[2:], mode='trilinear', align_corners=False)
            h = torch.cat([h, skip], dim=1)
            
            for block in blocks:
                if isinstance(block, ResBlock3D):
                    h = block(h, time_emb)
                else:
                    h = block(h)
            
            h = upsample(h)
        
        h = self.output_norm(h)
        h = F.silu(h)
        h = self.output_conv(h)
        
        return h

class LatentDiffusion3D(nn.Module):
    def __init__(
        self,
        latent_channels: int,
        model_channels: int = 128,
        num_res_blocks: int = 2,
        attention_resolutions: Tuple[int, ...] = (4, 8),
        dropout: float = 0.1,
        channel_mult: Tuple[int, ...] = (1, 2, 4, 8),
        num_heads: int = 8,
        timesteps: int = 1000
    ):
        super().__init__()
        
        self.timesteps = timesteps
        
        self.unet = UNet3D(
            in_channels=latent_channels,
            model_channels=model_channels,
            out_channels=latent_channels,
            num_res_blocks=num_res_blocks,
            attention_resolutions=attention_resolutions,
            dropout=dropout,
            channel_mult=channel_mult,
            num_heads=num_heads
        )
        
        betas = self._cosine_beta_schedule(timesteps)
        alphas = 1.0 - betas
        alphas_cumprod = torch.cumprod(alphas, dim=0)
        
        self.register_buffer('betas', betas)
        self.register_buffer('alphas', alphas)
        self.register_buffer('alphas_cumprod', alphas_cumprod)
        self.register_buffer('sqrt_alphas_cumprod', torch.sqrt(alphas_cumprod))
        self.register_buffer('sqrt_one_minus_alphas_cumprod', torch.sqrt(1.0 - alphas_cumprod))

    def _cosine_beta_schedule(self, timesteps: int, s: float = 0.008) -> torch.Tensor:
        steps = timesteps + 1
        x = torch.linspace(0, timesteps, steps)
        alphas_cumprod = torch.cos(((x / timesteps) + s) / (1 + s) * math.pi * 0.5) ** 2
        alphas_cumprod = alphas_cumprod / alphas_cumprod[0]
        betas = 1 - (alphas_cumprod[1:] / alphas_cumprod[:-1])
        return torch.clip(betas, 0.0001, 0.9999)

    def q_sample(self, x_start: torch.Tensor, t: torch.Tensor, noise: torch.Tensor = None) -> torch.Tensor:
        if noise is None:
            noise = torch.randn_like(x_start)
        
        sqrt_alphas_cumprod_t = self.sqrt_alphas_cumprod[t]
        sqrt_one_minus_alphas_cumprod_t = self.sqrt_one_minus_alphas_cumprod[t]
        
        while len(sqrt_alphas_cumprod_t.shape) < len(x_start.shape):
            sqrt_alphas_cumprod_t = sqrt_alphas_cumprod_t.unsqueeze(-1)
            sqrt_one_minus_alphas_cumprod_t = sqrt_one_minus_alphas_cumprod_t.unsqueeze(-1)
        
        return sqrt_alphas_cumprod_t * x_start + sqrt_one_minus_alphas_cumprod_t * noise

    def p_sample(self, x: torch.Tensor, t: torch.Tensor) -> torch.Tensor:
        with torch.no_grad():
            pred_noise = self.unet(x, t)
            
            alpha = self.alphas[t]
            alpha_cumprod = self.alphas_cumprod[t]
            beta = self.betas[t]
            
            while len(alpha.shape) < len(x.shape):
                alpha = alpha.unsqueeze(-1)
                alpha_cumprod = alpha_cumprod.unsqueeze(-1)
                beta = beta.unsqueeze(-1)
            
            pred_x0 = (x - torch.sqrt(1 - alpha_cumprod) * pred_noise) / torch.sqrt(alpha_cumprod)
            pred_x0 = torch.clamp(pred_x0, -1, 1)
            
            mean = (x - beta * pred_noise / torch.sqrt(1 - alpha_cumprod)) / torch.sqrt(alpha)
            
            if t[0] > 0:
                noise = torch.randn_like(x)
                variance = beta
                return mean + torch.sqrt(variance) * noise
            else:
                return mean

    def forward(self, x: torch.Tensor, t: torch.Tensor) -> torch.Tensor:
        return self.unet(x, t)

    @torch.no_grad()
    def sample(
        self, 
        shape: Tuple[int, ...], 
        device: torch.device, 
        num_inference_steps: int = None,
        init_latent: torch.Tensor = None
    ) -> torch.Tensor:
        if num_inference_steps is None:
            num_inference_steps = self.timesteps
        
        if init_latent is not None:
            x = init_latent
            start_step = self.timesteps - num_inference_steps
        else:
            x = torch.randn(shape, device=device)
            start_step = 0
        
        step_size = max(1, self.timesteps // num_inference_steps)
        
        for i in reversed(range(start_step, self.timesteps, step_size)):
            t = torch.full((shape[0],), i, device=device, dtype=torch.long)
            x = self.p_sample(x, t)
        
        return x
    
    @torch.no_grad()
    def sample_with_context(
        self,
        shape: Tuple[int, ...],
        device: torch.device,
        context: torch.Tensor,
        num_inference_steps: int = None,
        guidance_scale: float = 1.5
    ) -> torch.Tensor:
        if num_inference_steps is None:
            num_inference_steps = self.timesteps
        
        x = torch.randn(shape, device=device)
        
        step_size = max(1, self.timesteps // num_inference_steps)
        
        for i in reversed(range(0, self.timesteps, step_size)):
            t = torch.full((shape[0],), i, device=device, dtype=torch.long)
            
            pred_noise = self.unet(x, t)
            
            if context is not None and guidance_scale > 1.0:
                context_channels = min(context.shape[1], x.shape[1])
                context_resized = F.adaptive_avg_pool3d(
                    context[:, :context_channels],
                    output_size=x.shape[2:]
                )
                
                x_with_context = x.clone()
                x_with_context[:, :context_channels] += context_resized * 0.1
                
                pred_noise_context = self.unet(x_with_context, t)
                
                pred_noise = pred_noise + guidance_scale * (pred_noise_context - pred_noise)
            
            alpha = self.alphas[t]
            alpha_cumprod = self.alphas_cumprod[t]
            beta = self.betas[t]
            
            while len(alpha.shape) < len(x.shape):
                alpha = alpha.unsqueeze(-1)
                alpha_cumprod = alpha_cumprod.unsqueeze(-1)
                beta = beta.unsqueeze(-1)
            
            pred_x0 = (x - torch.sqrt(1 - alpha_cumprod) * pred_noise) / torch.sqrt(alpha_cumprod)
            pred_x0 = torch.clamp(pred_x0, -1, 1)
            
            mean = (x - beta * pred_noise / torch.sqrt(1 - alpha_cumprod)) / torch.sqrt(alpha)
            
            if t[0] > 0:
                noise = torch.randn_like(x)
                variance = beta
                x = mean + torch.sqrt(variance) * noise
            else:
                x = mean
        
        return x
