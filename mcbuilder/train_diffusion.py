import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader
from pathlib import Path
import argparse
from tqdm import tqdm
import wandb

from mcbuilder.dataset import BuildPasteDataset
from mcbuilder.improved_vqvae import ImprovedVQVAE3D
from mcbuilder.diffusion import LatentDiffusion3D
from mcbuilder.blocks import BLOCKS_ARRAY

class LatentDatasetForDiffusion(torch.utils.data.IterableDataset):
    def __init__(self, base_dataset, vqvae, device):
        super().__init__()
        self.base_dataset = base_dataset
        self.vqvae = vqvae
        self.device = device
    
    def __iter__(self):
        self.vqvae.eval()
        with torch.no_grad():
            for sample in self.base_dataset:
                blocks = sample['blocks'].unsqueeze(0).to(self.device)
                
                latent = self.vqvae.encode_to_latent(blocks)
                
                latent = (latent - latent.mean()) / (latent.std() + 1e-8)
                
                yield {
                    'latent': latent.squeeze(0).cpu(),
                    'build_id': sample['build_id'],
                    'category': sample['category'],
                    'description': sample.get('description'),
                    'build_name': sample.get('build_name')
                }

def train_epoch(model, dataloader, optimizer, device, epoch):
    model.train()
    total_loss = 0
    num_batches = 0
    
    pbar = tqdm(dataloader, desc=f"Epoch {epoch}")
    
    for batch_idx, batch in enumerate(pbar):
        latent = batch['latent'].to(device)
        
        batch_size = latent.shape[0]
        
        t = torch.randint(0, model.timesteps, (batch_size,), device=device).long()
        
        noise = torch.randn_like(latent)
        
        noisy_latent = model.q_sample(latent, t, noise)
        
        optimizer.zero_grad()
        
        pred_noise = model(noisy_latent, t)
        
        loss = F.mse_loss(pred_noise, noise)
        
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()
        
        total_loss += loss.item()
        num_batches += 1
        
        pbar.set_postfix({'loss': f'{loss.item():.4f}'})
        
        if batch_idx % 100 == 0:
            wandb.log({
                'train/loss': loss.item(),
                'train/step': epoch * len(dataloader) + batch_idx
            })
    
    return total_loss / num_batches

def main(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    wandb.init(
        project="minecraft-diffusion",
        config={
            "model_channels": args.model_channels,
            "num_res_blocks": args.num_res_blocks,
            "batch_size": args.batch_size,
            "lr": args.lr,
            "epochs": args.epochs,
            "timesteps": args.timesteps
        }
    )
    
    print("Loading VQ-VAE...")
    checkpoint = torch.load(args.vqvae_checkpoint, map_location=device)
    
    vqvae = ImprovedVQVAE3D(
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
        download=True,
        generate_descriptions=args.generate_descriptions,
        gemini_api_key=args.gemini_api_key if args.generate_descriptions else None,
        description_language=args.description_language
    )
    
    latent_dataset = LatentDatasetForDiffusion(
        base_dataset=base_dataset,
        vqvae=vqvae,
        device=device
    )
    
    dataloader = DataLoader(
        latent_dataset,
        batch_size=args.batch_size,
        num_workers=args.num_workers,
        pin_memory=True if device.type == 'cuda' else False
    )
    
    print("Creating diffusion model...")
    model = LatentDiffusion3D(
        latent_channels=vqvae.embedding_dim,
        model_channels=args.model_channels,
        num_res_blocks=args.num_res_blocks,
        attention_resolutions=(4, 8),
        dropout=args.dropout,
        channel_mult=(1, 2, 4, 8),
        num_heads=args.num_heads,
        timesteps=args.timesteps
    ).to(device)
    
    total_params = sum(p.numel() for p in model.parameters())
    print(f"Total parameters: {total_params:,}")
    
    optimizer = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=0.01)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=args.epochs)
    
    checkpoint_dir = Path(args.checkpoint_dir)
    checkpoint_dir.mkdir(parents=True, exist_ok=True)
    
    for epoch in range(args.epochs):
        print(f"\nEpoch {epoch+1}/{args.epochs}")
        
        loss = train_epoch(model, dataloader, optimizer, device, epoch)
        
        print(f"Average Loss: {loss:.4f}")
        
        wandb.log({
            'epoch/loss': loss,
            'epoch/lr': optimizer.param_groups[0]['lr'],
            'epoch': epoch
        })
        
        scheduler.step()
        
        if (epoch + 1) % args.save_every == 0:
            checkpoint_path = checkpoint_dir / f"diffusion_epoch_{epoch+1}.pt"
            torch.save({
                'epoch': epoch,
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'scheduler_state_dict': scheduler.state_dict(),
                'loss': loss,
            }, checkpoint_path)
            print(f"Saved checkpoint to {checkpoint_path}")
    
    final_path = checkpoint_dir / "diffusion_final.pt"
    torch.save({
        'model_state_dict': model.state_dict(),
        'config': {
            'latent_channels': vqvae.embedding_dim,
            'model_channels': args.model_channels,
            'num_res_blocks': args.num_res_blocks,
            'attention_resolutions': (4, 8),
            'dropout': args.dropout,
            'channel_mult': (1, 2, 4, 8),
            'num_heads': args.num_heads,
            'timesteps': args.timesteps
        }
    }, final_path)
    print(f"Saved final model to {final_path}")
    
    wandb.finish()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--vqvae_checkpoint', type=str, required=True)
    parser.add_argument('--cache_dir', type=str, default='./data/cache')
    parser.add_argument('--checkpoint_dir', type=str, default='./checkpoints_diffusion')
    parser.add_argument('--chunk_size', type=int, default=32)
    parser.add_argument('--overlap', type=int, default=4)
    parser.add_argument('--min_blocks', type=int, default=800)
    parser.add_argument('--max_blocks', type=int, default=None)
    parser.add_argument('--categories', type=str, default=None)
    parser.add_argument('--batch_size', type=int, default=4)
    parser.add_argument('--num_workers', type=int, default=2)
    parser.add_argument('--model_channels', type=int, default=128)
    parser.add_argument('--num_res_blocks', type=int, default=2)
    parser.add_argument('--num_heads', type=int, default=8)
    parser.add_argument('--dropout', type=float, default=0.1)
    parser.add_argument('--timesteps', type=int, default=1000)
    parser.add_argument('--lr', type=float, default=1e-4)
    parser.add_argument('--epochs', type=int, default=100)
    parser.add_argument('--save_every', type=int, default=10)
    
    parser.add_argument('--generate_descriptions', action='store_true', help='Generate AI descriptions for dataset builds')
    parser.add_argument('--gemini_api_key', type=str, default=None, help='Gemini API key for description generation')
    parser.add_argument('--description_language', type=str, default='en', choices=['en', 'ru'], help='Language for descriptions')
    
    args = parser.parse_args()
    main(args)
