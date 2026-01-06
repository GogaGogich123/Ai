import torch
import torch.nn as nn
from transformers import CLIPTextModel, CLIPTokenizer

class TextEncoder(nn.Module):
    def __init__(self, model_name: str = "openai/clip-vit-base-patch32", freeze: bool = True):
        super().__init__()
        
        self.tokenizer = CLIPTokenizer.from_pretrained(model_name)
        self.text_model = CLIPTextModel.from_pretrained(model_name)
        
        if freeze:
            for param in self.text_model.parameters():
                param.requires_grad = False
        
        self.embed_dim = self.text_model.config.hidden_size
    
    def forward(self, text_prompts: list) -> torch.Tensor:
        tokens = self.tokenizer(
            text_prompts,
            padding=True,
            truncation=True,
            max_length=77,
            return_tensors="pt"
        ).to(self.text_model.device)
        
        outputs = self.text_model(**tokens)
        
        return outputs.pooler_output

class ConditionalTransformerBlock(nn.Module):
    def __init__(self, d_model: int, nhead: int, dim_feedforward: int, text_dim: int, dropout: float = 0.1):
        super().__init__()
        
        self.self_attn = nn.MultiheadAttention(d_model, nhead, dropout=dropout, batch_first=True)
        self.norm1 = nn.LayerNorm(d_model)
        
        self.cross_attn = nn.MultiheadAttention(d_model, nhead, dropout=dropout, batch_first=True)
        self.norm2 = nn.LayerNorm(d_model)
        
        self.text_proj = nn.Linear(text_dim, d_model)
        
        self.ffn = nn.Sequential(
            nn.Linear(d_model, dim_feedforward),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(dim_feedforward, d_model),
            nn.Dropout(dropout)
        )
        self.norm3 = nn.LayerNorm(d_model)
    
    def forward(self, x: torch.Tensor, text_embed: torch.Tensor) -> torch.Tensor:
        attn_out, _ = self.self_attn(x, x, x)
        x = self.norm1(x + attn_out)
        
        text_embed_proj = self.text_proj(text_embed).unsqueeze(1)
        
        cross_attn_out, _ = self.cross_attn(x, text_embed_proj, text_embed_proj)
        x = self.norm2(x + cross_attn_out)
        
        ffn_out = self.ffn(x)
        x = self.norm3(x + ffn_out)
        
        return x

class TextConditionedTransformer(nn.Module):
    def __init__(
        self,
        num_embeddings: int,
        d_model: int = 512,
        nhead: int = 8,
        num_layers: int = 12,
        dim_feedforward: int = 2048,
        dropout: float = 0.1,
        text_model: str = "openai/clip-vit-base-patch32",
        max_seq_len: int = 4096
    ):
        super().__init__()
        
        self.num_embeddings = num_embeddings
        self.d_model = d_model
        
        self.text_encoder = TextEncoder(text_model, freeze=True)
        
        self.token_embedding = nn.Embedding(num_embeddings + 1, d_model)
        
        from .transformer import PositionalEncoding3D
        self.pos_encoding = PositionalEncoding3D(d_model, max_seq_len)
        
        self.mask_token_id = num_embeddings
        
        self.transformer_blocks = nn.ModuleList([
            ConditionalTransformerBlock(
                d_model, 
                nhead, 
                dim_feedforward, 
                self.text_encoder.embed_dim,
                dropout
            )
            for _ in range(num_layers)
        ])
        
        self.output_head = nn.Linear(d_model, num_embeddings)
        
        self.dropout = nn.Dropout(dropout)
    
    def forward(self, x: torch.Tensor, text_prompts: list) -> torch.Tensor:
        text_embed = self.text_encoder(text_prompts)
        
        x = self.token_embedding(x)
        x = self.pos_encoding(x)
        x = self.dropout(x)
        
        for block in self.transformer_blocks:
            x = block(x, text_embed)
        
        logits = self.output_head(x)
        
        return logits
    
    def generate_from_text(
        self,
        text_prompt: str,
        shape: tuple,
        device: torch.device,
        num_iterations: int = 10,
        temperature: float = 1.0,
        top_k: int = 50
    ) -> torch.Tensor:
        self.eval()
        
        seq_len = torch.prod(torch.tensor(shape)).item()
        
        x = torch.full((1, seq_len), self.mask_token_id, dtype=torch.long, device=device)
        
        mask = torch.ones(seq_len, dtype=torch.bool, device=device)
        
        for iteration in range(num_iterations):
            with torch.no_grad():
                logits = self.forward(x, [text_prompt])
                logits = logits / temperature
                
                if top_k is not None:
                    v, _ = torch.topk(logits, top_k, dim=-1)
                    logits[logits < v[..., [-1]]] = float('-inf')
                
                probs = torch.nn.functional.softmax(logits, dim=-1)
                
                mask_confidence = 1.0 - (iteration / num_iterations)
                
                num_to_generate = max(1, int(mask.sum() * 0.2))
                
                masked_probs = probs[0, mask]
                entropy = -(masked_probs * torch.log(masked_probs + 1e-10)).sum(dim=-1)
                
                _, indices_to_fill = torch.topk(-entropy, num_to_generate)
                
                mask_indices = torch.where(mask)[0]
                fill_indices = mask_indices[indices_to_fill]
                
                samples = torch.multinomial(probs[0, fill_indices], 1).squeeze(-1)
                x[0, fill_indices] = samples
                
                mask[fill_indices] = False
        
        return x.view(1, *shape)
