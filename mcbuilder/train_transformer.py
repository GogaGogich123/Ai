import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader
from pathlib import Path
import argparse
from tqdm import tqdm
import wandb
import random

from dataset import BuildPasteDataset, MaskedChunkDataset
from vqvae import VQVAE3D
from transformer import MaskedLatentModel
from blocks import BLOCKS_ARRAY

class LatentDataset(torch.utils.data.IterableDataset):
    def __init__(self, base_dataset, vqvae, device, mask_ratio=0.15):
        super().__init__()
        self.base_dataset = base_dataset
        self.vqvae = vqvae
        self.device = device
        self.mask_ratio = mask_ratio
        self.mask_token_id = vqvae.vq.num_embeddings
    
    def __iter__(self):
        self.vqvae.eval()
        with torch.no_grad():
            for sample in self.base_dataset:
                blocks = sample['blocks'].unsqueeze(0).to(self.device)
                
                encoding_indices = self.vqvae.encode(blocks)
                encoding_indices = encoding_indices.squeeze(0)
                
                mask = torch.rand(encoding_indices.shape, device=self.device) < self.mask_ratio
                
                masked_indices = encoding_indices.clone()
                masked_indices[mask] = self.mask_token_id
                
                yield {
                    'input': masked_indices.cpu(),
                    'target': encoding_indices.cpu(),
                    'mask': mask.cpu(),
                    'build_id': sample['build_id'],
                    'category': sample['category']
                }

def train_epoch(model, dataloader, optimizer, device, epoch):
    model.train()
    total_loss = 0
    total_accuracy = 0
    num_batches = 0
    
    pbar = tqdm(dataloader, desc=f"Epoch {epoch}")
    
    for batch_idx, batch in enumerate(pbar):
        input_indices = batch['input'].to(device)
        target_indices = batch['target'].to(device)
        mask = batch['mask'].to(device)
        
        optimizer.zero_grad()
        
        logits = model(input_indices)
        
        loss = F.cross_entropy(
            logits.reshape(-1, logits.size(-1)),
            target_indices.reshape(-1),
            reduction='mean'
        )
        
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()
        
        with torch.no_grad():
            pred = logits.argmax(dim=-1)
            accuracy = (pred[mask] == target_indices[mask]).float().mean()
        
        total_loss += loss.item()
        total_accuracy += accuracy.item()
        num_batches += 1
        
        pbar.set_postfix({
            'loss': f'{loss.item():.4f}',
            'acc': f'{accuracy.item():.4f}'
        })
        
        if batch_idx % 100 == 0:
            wandb.log({
                'train/loss': loss.item(),
                'train/accuracy': accuracy.item(),
                'train/step': epoch * len(dataloader) + batch_idx
            })
    
    return total_loss / num_batches, total_accuracy / num_batches

def main(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    wandb.init(
        project="minecraft-transformer",
        config={
            "d_model": args.d_model,
            "nhead": args.nhead,
            "num_layers": args.num_layers,
            "batch_size": args.batch_size,
            "lr": args.lr,
            "epochs": args.epochs,
            "mask_ratio": args.mask_ratio
        }
    )
    
    print("Loading VQ-VAE...")
    checkpoint = torch.load(args.vqvae_checkpoint, map_location=device)
    
    vqvae = VQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        **checkpoint['config']
    ).to(device)
    vqvae.load_state_dict(checkpoint['model_state_dict'])
    vqvae.eval()
    
    for param in vqvae.parameters():
        param.requires_grad = False
    
    print("Creating dataset...")
    base_dataset = BuildPasteDataset(
        cache_dir=args.cache_dir,
        chunk_size=args.chunk_size,
        overlap=args.overlap,
        min_blocks=args.min_blocks,
        max_blocks=args.max_blocks,
        categories=args.categories.split(',') if args.categories else None,
        download=True
    )
    
    latent_dataset = LatentDataset(
        base_dataset=base_dataset,
        vqvae=vqvae,
        device=device,
        mask_ratio=args.mask_ratio
    )
    
    dataloader = DataLoader(
        latent_dataset,
        batch_size=args.batch_size,
        num_workers=args.num_workers,
        pin_memory=True if device.type == 'cuda' else False
    )
    
    print("Creating transformer model...")
    model = MaskedLatentModel(
        num_embeddings=vqvae.vq.num_embeddings,
        d_model=args.d_model,
        nhead=args.nhead,
        num_layers=args.num_layers,
        dim_feedforward=args.dim_feedforward,
        dropout=args.dropout
    ).to(device)
    
    total_params = sum(p.numel() for p in model.parameters())
    print(f"Total parameters: {total_params:,}")
    
    optimizer = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=0.01)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=args.epochs)
    
    checkpoint_dir = Path(args.checkpoint_dir)
    checkpoint_dir.mkdir(parents=True, exist_ok=True)
    
    for epoch in range(args.epochs):
        print(f"\nEpoch {epoch+1}/{args.epochs}")
        
        loss, accuracy = train_epoch(model, dataloader, optimizer, device, epoch)
        
        print(f"Average Loss: {loss:.4f}, Accuracy: {accuracy:.4f}")
        
        wandb.log({
            'epoch/loss': loss,
            'epoch/accuracy': accuracy,
            'epoch/lr': optimizer.param_groups[0]['lr'],
            'epoch': epoch
        })
        
        scheduler.step()
        
        if (epoch + 1) % args.save_every == 0:
            checkpoint_path = checkpoint_dir / f"transformer_epoch_{epoch+1}.pt"
            torch.save({
                'epoch': epoch,
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'scheduler_state_dict': scheduler.state_dict(),
                'loss': loss,
            }, checkpoint_path)
            print(f"Saved checkpoint to {checkpoint_path}")
    
    final_path = checkpoint_dir / "transformer_final.pt"
    torch.save({
        'model_state_dict': model.state_dict(),
        'config': {
            'num_embeddings': vqvae.vq.num_embeddings,
            'd_model': args.d_model,
            'nhead': args.nhead,
            'num_layers': args.num_layers,
            'dim_feedforward': args.dim_feedforward,
            'dropout': args.dropout
        }
    }, final_path)
    print(f"Saved final model to {final_path}")
    
    wandb.finish()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--vqvae_checkpoint', type=str, required=True)
    parser.add_argument('--cache_dir', type=str, default='./data/cache')
    parser.add_argument('--checkpoint_dir', type=str, default='./checkpoints_transformer')
    parser.add_argument('--chunk_size', type=int, default=32)
    parser.add_argument('--overlap', type=int, default=4)
    parser.add_argument('--min_blocks', type=int, default=800)
    parser.add_argument('--max_blocks', type=int, default=None)
    parser.add_argument('--categories', type=str, default=None)
    parser.add_argument('--batch_size', type=int, default=8)
    parser.add_argument('--num_workers', type=int, default=2)
    parser.add_argument('--mask_ratio', type=float, default=0.15)
    parser.add_argument('--d_model', type=int, default=512)
    parser.add_argument('--nhead', type=int, default=8)
    parser.add_argument('--num_layers', type=int, default=12)
    parser.add_argument('--dim_feedforward', type=int, default=2048)
    parser.add_argument('--dropout', type=float, default=0.1)
    parser.add_argument('--lr', type=float, default=1e-4)
    parser.add_argument('--epochs', type=int, default=50)
    parser.add_argument('--save_every', type=int, default=5)
    
    args = parser.parse_args()
    main(args)
