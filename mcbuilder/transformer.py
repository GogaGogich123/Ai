import torch
import torch.nn as nn
import torch.nn.functional as F
import math
from typing import Optional

class PositionalEncoding3D(nn.Module):
    def __init__(self, d_model: int, max_len: int = 5000):
        super().__init__()
        self.d_model = d_model
        
        pe = torch.zeros(max_len, d_model)
        position = torch.arange(0, max_len, dtype=torch.float).unsqueeze(1)
        div_term = torch.exp(torch.arange(0, d_model, 2).float() * (-math.log(10000.0) / d_model))
        
        pe[:, 0::2] = torch.sin(position * div_term)
        pe[:, 1::2] = torch.cos(position * div_term)
        
        self.register_buffer('pe', pe)
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return x + self.pe[:x.size(1), :]

class TransformerBlock(nn.Module):
    def __init__(self, d_model: int, nhead: int, dim_feedforward: int, dropout: float = 0.1):
        super().__init__()
        
        self.attention = nn.MultiheadAttention(d_model, nhead, dropout=dropout, batch_first=True)
        self.norm1 = nn.LayerNorm(d_model)
        
        self.ffn = nn.Sequential(
            nn.Linear(d_model, dim_feedforward),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(dim_feedforward, d_model),
            nn.Dropout(dropout)
        )
        self.norm2 = nn.LayerNorm(d_model)
    
    def forward(self, x: torch.Tensor, mask: Optional[torch.Tensor] = None) -> torch.Tensor:
        attn_out, _ = self.attention(x, x, x, attn_mask=mask)
        x = self.norm1(x + attn_out)
        
        ffn_out = self.ffn(x)
        x = self.norm2(x + ffn_out)
        
        return x

class LatentTransformer(nn.Module):
    def __init__(
        self,
        num_embeddings: int,
        d_model: int = 512,
        nhead: int = 8,
        num_layers: int = 12,
        dim_feedforward: int = 2048,
        dropout: float = 0.1,
        max_seq_len: int = 4096
    ):
        super().__init__()
        
        self.num_embeddings = num_embeddings
        self.d_model = d_model
        
        self.token_embedding = nn.Embedding(num_embeddings + 1, d_model)
        self.pos_encoding = PositionalEncoding3D(d_model, max_seq_len)
        
        self.mask_token_id = num_embeddings
        
        self.transformer_blocks = nn.ModuleList([
            TransformerBlock(d_model, nhead, dim_feedforward, dropout)
            for _ in range(num_layers)
        ])
        
        self.output_head = nn.Linear(d_model, num_embeddings)
        
        self.dropout = nn.Dropout(dropout)
    
    def forward(self, x: torch.Tensor, mask: Optional[torch.Tensor] = None) -> torch.Tensor:
        x = self.token_embedding(x)
        x = self.pos_encoding(x)
        x = self.dropout(x)
        
        for block in self.transformer_blocks:
            x = block(x, mask)
        
        logits = self.output_head(x)
        
        return logits
    
    def generate_masked(
        self, 
        x: torch.Tensor, 
        mask: torch.Tensor,
        temperature: float = 1.0,
        top_k: Optional[int] = None
    ) -> torch.Tensor:
        self.eval()
        with torch.no_grad():
            masked_x = x.clone()
            masked_x[mask] = self.mask_token_id
            
            logits = self.forward(masked_x)
            logits = logits / temperature
            
            if top_k is not None:
                v, _ = torch.topk(logits, top_k, dim=-1)
                logits[logits < v[..., [-1]]] = float('-inf')
            
            probs = F.softmax(logits, dim=-1)
            samples = torch.multinomial(probs.view(-1, probs.size(-1)), 1).view(probs.shape[:-1])
            
            result = x.clone()
            result[mask] = samples[mask]
            
            return result

class MaskedLatentModel(nn.Module):
    def __init__(
        self,
        num_embeddings: int,
        d_model: int = 512,
        nhead: int = 8,
        num_layers: int = 12,
        dim_feedforward: int = 2048,
        dropout: float = 0.1
    ):
        super().__init__()
        
        self.transformer = LatentTransformer(
            num_embeddings=num_embeddings,
            d_model=d_model,
            nhead=nhead,
            num_layers=num_layers,
            dim_feedforward=dim_feedforward,
            dropout=dropout
        )
    
    def forward(self, x: torch.Tensor, mask: Optional[torch.Tensor] = None) -> torch.Tensor:
        return self.transformer(x, mask)
    
    def generate(
        self,
        partial: torch.Tensor,
        mask: torch.Tensor,
        num_iterations: int = 10,
        temperature: float = 1.0,
        top_k: Optional[int] = 50
    ) -> torch.Tensor:
        current = partial.clone()
        
        for i in range(num_iterations):
            current = self.transformer.generate_masked(
                current,
                mask,
                temperature=temperature,
                top_k=top_k
            )
        
        return current
