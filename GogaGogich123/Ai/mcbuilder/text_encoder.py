import torch
import torch.nn as nn
from transformers import AutoTokenizer, AutoModel
from typing import Optional, Dict

class SimpleTextEncoder(nn.Module):
    def __init__(
        self, 
        embed_dim: int = 512,
        max_seq_length: int = 77,
        vocab_size: int = 10000,
        num_layers: int = 6,
        num_heads: int = 8,
        dropout: float = 0.1
    ):
        super().__init__()
        self.embed_dim = embed_dim
        self.max_seq_length = max_seq_length
        
        self.token_embedding = nn.Embedding(vocab_size, embed_dim)
        self.position_embedding = nn.Parameter(torch.randn(1, max_seq_length, embed_dim))
        
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=embed_dim,
            nhead=num_heads,
            dim_feedforward=embed_dim * 4,
            dropout=dropout,
            activation='gelu',
            batch_first=True,
            norm_first=True
        )
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        
        self.ln_final = nn.LayerNorm(embed_dim)
        
    def forward(
        self, 
        input_ids: torch.Tensor, 
        attention_mask: Optional[torch.Tensor] = None
    ) -> Dict[str, torch.Tensor]:
        batch_size, seq_length = input_ids.shape
        
        x = self.token_embedding(input_ids)
        x = x + self.position_embedding[:, :seq_length, :]
        
        if attention_mask is not None:
            attention_mask = attention_mask.bool()
            attention_mask = ~attention_mask
        
        x = self.transformer(x, src_key_padding_mask=attention_mask)
        x = self.ln_final(x)
        
        pooled = x[:, 0]
        
        return {
            'last_hidden_state': x,
            'pooler_output': pooled
        }


class CLIPTextEncoder(nn.Module):
    def __init__(
        self,
        model_name: str = "sentence-transformers/all-MiniLM-L6-v2",
        projection_dim: Optional[int] = None,
        freeze: bool = False
    ):
        super().__init__()
        
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = AutoModel.from_pretrained(model_name)
        
        if freeze:
            for param in self.model.parameters():
                param.requires_grad = False
        
        self.embed_dim = self.model.config.hidden_size
        
        if projection_dim is not None:
            self.projection = nn.Sequential(
                nn.Linear(self.embed_dim, projection_dim),
                nn.GELU(),
                nn.Linear(projection_dim, projection_dim)
            )
            self.embed_dim = projection_dim
        else:
            self.projection = None
    
    def encode_text(self, texts: list[str], device: torch.device) -> torch.Tensor:
        encoded = self.tokenizer(
            texts,
            padding=True,
            truncation=True,
            max_length=77,
            return_tensors="pt"
        )
        
        input_ids = encoded['input_ids'].to(device)
        attention_mask = encoded['attention_mask'].to(device)
        
        return input_ids, attention_mask
    
    def forward(
        self,
        input_ids: Optional[torch.Tensor] = None,
        attention_mask: Optional[torch.Tensor] = None,
        texts: Optional[list[str]] = None
    ) -> Dict[str, torch.Tensor]:
        if texts is not None:
            device = next(self.parameters()).device
            input_ids, attention_mask = self.encode_text(texts, device)
        
        outputs = self.model(
            input_ids=input_ids,
            attention_mask=attention_mask
        )
        
        last_hidden_state = outputs.last_hidden_state
        
        pooled = last_hidden_state[:, 0]
        
        if self.projection is not None:
            pooled = self.projection(pooled)
            last_hidden_state = self.projection(last_hidden_state.reshape(-1, last_hidden_state.shape[-1]))
            last_hidden_state = last_hidden_state.reshape(
                outputs.last_hidden_state.shape[0],
                outputs.last_hidden_state.shape[1],
                -1
            )
        
        return {
            'last_hidden_state': last_hidden_state,
            'pooler_output': pooled,
            'attention_mask': attention_mask
        }


class TextTokenizer:
    def __init__(self, vocab_size: int = 10000, max_length: int = 77):
        self.vocab_size = vocab_size
        self.max_length = max_length
        self.word_to_idx = {'<PAD>': 0, '<UNK>': 1, '<SOS>': 2, '<EOS>': 3}
        self.idx_to_word = {v: k for k, v in self.word_to_idx.items()}
        self.next_idx = 4
        
    def build_vocab(self, texts: list[str]):
        word_freq = {}
        for text in texts:
            words = text.lower().split()
            for word in words:
                word_freq[word] = word_freq.get(word, 0) + 1
        
        sorted_words = sorted(word_freq.items(), key=lambda x: x[1], reverse=True)
        
        for word, _ in sorted_words[:self.vocab_size - 4]:
            if word not in self.word_to_idx:
                self.word_to_idx[word] = self.next_idx
                self.idx_to_word[self.next_idx] = word
                self.next_idx += 1
    
    def encode(self, text: str, add_special_tokens: bool = True) -> Dict[str, torch.Tensor]:
        words = text.lower().split()
        
        if add_special_tokens:
            tokens = [self.word_to_idx['<SOS>']]
        else:
            tokens = []
        
        for word in words[:self.max_length - 2]:
            tokens.append(self.word_to_idx.get(word, self.word_to_idx['<UNK>']))
        
        if add_special_tokens:
            tokens.append(self.word_to_idx['<EOS>'])
        
        attention_mask = [1] * len(tokens)
        
        while len(tokens) < self.max_length:
            tokens.append(self.word_to_idx['<PAD>'])
            attention_mask.append(0)
        
        return {
            'input_ids': torch.tensor(tokens, dtype=torch.long),
            'attention_mask': torch.tensor(attention_mask, dtype=torch.long)
        }
    
    def batch_encode(self, texts: list[str]) -> Dict[str, torch.Tensor]:
        encoded = [self.encode(text) for text in texts]
        
        return {
            'input_ids': torch.stack([e['input_ids'] for e in encoded]),
            'attention_mask': torch.stack([e['attention_mask'] for e in encoded])
        }
    
    def save(self, path: str):
        import json
        with open(path, 'w', encoding='utf-8') as f:
            json.dump({
                'word_to_idx': self.word_to_idx,
                'vocab_size': self.vocab_size,
                'max_length': self.max_length
            }, f, ensure_ascii=False, indent=2)
    
    def load(self, path: str):
        import json
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        self.word_to_idx = data['word_to_idx']
        self.vocab_size = data['vocab_size']
        self.max_length = data['max_length']
        self.idx_to_word = {int(v): k for k, v in self.word_to_idx.items()}
        self.next_idx = max(self.word_to_idx.values()) + 1
