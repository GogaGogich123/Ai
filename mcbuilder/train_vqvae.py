import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader
from pathlib import Path
import argparse
from tqdm import tqdm
import wandb
from datetime import datetime

from dataset import BuildPasteDataset
from vqvae import VQVAE3D
from blocks import BLOCKS_ARRAY

def train_epoch(model, dataloader, optimizer, device, epoch):
    model.train()
    total_loss = 0
    total_recon_loss = 0
    total_vq_loss = 0
    num_batches = 0
    
    pbar = tqdm(dataloader, desc=f"Epoch {epoch}")
    
    for batch_idx, batch in enumerate(pbar):
        blocks = batch['blocks'].to(device)
        
        optimizer.zero_grad()
        
        recon, vq_loss, _ = model(blocks)
        
        recon_loss = F.cross_entropy(
            recon.permute(0, 2, 3, 4, 1).reshape(-1, model.num_blocks),
            blocks.reshape(-1),
            ignore_index=0
        )
        
        loss = recon_loss + vq_loss
        
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()
        
        total_loss += loss.item()
        total_recon_loss += recon_loss.item()
        total_vq_loss += vq_loss.item()
        num_batches += 1
        
        pbar.set_postfix({
            'loss': f'{loss.item():.4f}',
            'recon': f'{recon_loss.item():.4f}',
            'vq': f'{vq_loss.item():.4f}'
        })
        
        if batch_idx % 100 == 0:
            wandb.log({
                'train/loss': loss.item(),
                'train/recon_loss': recon_loss.item(),
                'train/vq_loss': vq_loss.item(),
                'train/step': epoch * len(dataloader) + batch_idx
            })
    
    return total_loss / num_batches, total_recon_loss / num_batches, total_vq_loss / num_batches

def main(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    wandb.init(
        project="minecraft-vqvae",
        config={
            "chunk_size": args.chunk_size,
            "embedding_dim": args.embedding_dim,
            "num_embeddings": args.num_embeddings,
            "batch_size": args.batch_size,
            "lr": args.lr,
            "epochs": args.epochs,
            "min_blocks": args.min_blocks,
            "max_blocks": args.max_blocks
        }
    )
    
    dataset = BuildPasteDataset(
        cache_dir=args.cache_dir,
        chunk_size=args.chunk_size,
        overlap=args.overlap,
        min_blocks=args.min_blocks,
        max_blocks=args.max_blocks,
        categories=args.categories.split(',') if args.categories else None,
        download=True
    )
    
    dataloader = DataLoader(
        dataset,
        batch_size=args.batch_size,
        num_workers=args.num_workers,
        pin_memory=True if device.type == 'cuda' else False
    )
    
    model = VQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        embedding_dim=args.embedding_dim,
        num_embeddings=args.num_embeddings,
        hidden_dims=[32, 64, 128],
        commitment_cost=0.25
    ).to(device)
    
    total_params = sum(p.numel() for p in model.parameters())
    print(f"Total parameters: {total_params:,}")
    
    optimizer = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=0.01)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=args.epochs)
    
    checkpoint_dir = Path(args.checkpoint_dir)
    checkpoint_dir.mkdir(parents=True, exist_ok=True)
    
    for epoch in range(args.epochs):
        print(f"\nEpoch {epoch+1}/{args.epochs}")
        
        loss, recon_loss, vq_loss = train_epoch(model, dataloader, optimizer, device, epoch)
        
        print(f"Average Loss: {loss:.4f}, Recon: {recon_loss:.4f}, VQ: {vq_loss:.4f}")
        
        wandb.log({
            'epoch/loss': loss,
            'epoch/recon_loss': recon_loss,
            'epoch/vq_loss': vq_loss,
            'epoch/lr': optimizer.param_groups[0]['lr'],
            'epoch': epoch
        })
        
        scheduler.step()
        
        if (epoch + 1) % args.save_every == 0:
            checkpoint_path = checkpoint_dir / f"vqvae_epoch_{epoch+1}.pt"
            torch.save({
                'epoch': epoch,
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'scheduler_state_dict': scheduler.state_dict(),
                'loss': loss,
            }, checkpoint_path)
            print(f"Saved checkpoint to {checkpoint_path}")
    
    final_path = checkpoint_dir / "vqvae_final.pt"
    torch.save({
        'model_state_dict': model.state_dict(),
        'config': {
            'num_blocks': len(BLOCKS_ARRAY),
            'embedding_dim': args.embedding_dim,
            'num_embeddings': args.num_embeddings,
            'hidden_dims': [32, 64, 128],
            'commitment_cost': 0.25
        }
    }, final_path)
    print(f"Saved final model to {final_path}")
    
    wandb.finish()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--cache_dir', type=str, default='./data/cache')
    parser.add_argument('--checkpoint_dir', type=str, default='./checkpoints')
    parser.add_argument('--chunk_size', type=int, default=32)
    parser.add_argument('--overlap', type=int, default=4)
    parser.add_argument('--min_blocks', type=int, default=800)
    parser.add_argument('--max_blocks', type=int, default=None)
    parser.add_argument('--categories', type=str, default=None)
    parser.add_argument('--batch_size', type=int, default=4)
    parser.add_argument('--num_workers', type=int, default=2)
    parser.add_argument('--embedding_dim', type=int, default=64)
    parser.add_argument('--num_embeddings', type=int, default=512)
    parser.add_argument('--lr', type=float, default=1e-4)
    parser.add_argument('--epochs', type=int, default=50)
    parser.add_argument('--save_every', type=int, default=5)
    
    args = parser.parse_args()
    main(args)
