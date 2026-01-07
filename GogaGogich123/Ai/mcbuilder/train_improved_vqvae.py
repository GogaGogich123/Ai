import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent.absolute()))

import torch
import torch.nn.functional as F
from torch.utils.data import DataLoader
import argparse
from tqdm import tqdm
import wandb

from mcbuilder.dataset import BuildPasteDataset
from mcbuilder.improved_vqvae import ImprovedVQVAE3D
from mcbuilder.blocks import BLOCKS_ARRAY

def perceptual_loss(recon, target, model):
    recon_feat = model.encoder(recon)
    target_feat = model.encoder(target)
    return F.mse_loss(recon_feat, target_feat)

def train_epoch(model, dataloader, optimizer, device, epoch, use_perceptual=True):
    model.train()
    total_loss = 0
    total_recon_loss = 0
    total_vq_loss = 0
    total_perceptual = 0
    num_batches = 0
    
    pbar = tqdm(dataloader, desc=f"Epoch {epoch}")
    
    for batch_idx, batch in enumerate(pbar):
        blocks = batch['blocks'].to(device)
        
        optimizer.zero_grad()
        
        recon, vq_loss, _ = model(blocks)
        
        recon_loss = F.cross_entropy(
            recon.permute(0, 2, 3, 4, 1).reshape(-1, model.num_blocks),
            blocks.reshape(-1),
            ignore_index=0,
            label_smoothing=0.1
        )
        
        loss = recon_loss + vq_loss
        
        if use_perceptual:
            with torch.no_grad():
                target_embedded = model.block_embedding(blocks).permute(0, 4, 1, 2, 3)
            recon_probs = F.softmax(recon, dim=1)
            recon_embedded = torch.matmul(
                recon_probs.permute(0, 2, 3, 4, 1).reshape(-1, model.num_blocks),
                model.block_embedding.weight
            ).reshape(*blocks.shape, -1).permute(0, 4, 1, 2, 3)
            
            perc_loss = F.mse_loss(recon_embedded, target_embedded)
            loss = loss + 0.1 * perc_loss
            total_perceptual += perc_loss.item()
        
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()
        
        total_loss += loss.item()
        total_recon_loss += recon_loss.item()
        total_vq_loss += vq_loss.item()
        num_batches += 1
        
        log_dict = {
            'loss': f'{loss.item():.4f}',
            'recon': f'{recon_loss.item():.4f}',
            'vq': f'{vq_loss.item():.4f}'
        }
        if use_perceptual:
            log_dict['perc'] = f'{perc_loss.item():.4f}'
        pbar.set_postfix(log_dict)
        
        if batch_idx % 100 == 0:
            wandb_log = {
                'train/loss': loss.item(),
                'train/recon_loss': recon_loss.item(),
                'train/vq_loss': vq_loss.item(),
                'train/step': epoch * len(dataloader) + batch_idx
            }
            if use_perceptual:
                wandb_log['train/perceptual_loss'] = perc_loss.item()
            wandb.log(wandb_log)
    
    avg_perceptual = total_perceptual / num_batches if use_perceptual else 0
    return (
        total_loss / num_batches,
        total_recon_loss / num_batches,
        total_vq_loss / num_batches,
        avg_perceptual
    )

def main(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    wandb.init(
        project="minecraft-improved-vqvae",
        config={
            "chunk_size": args.chunk_size,
            "embedding_dim": args.embedding_dim,
            "num_embeddings": args.num_embeddings,
            "num_res_blocks": args.num_res_blocks,
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
        download=True,
        generate_descriptions=args.generate_descriptions,
        gemini_api_key=args.gemini_api_key if args.generate_descriptions else None,
        description_language=args.description_language
    )
    
    dataloader = DataLoader(
        dataset,
        batch_size=args.batch_size,
        num_workers=args.num_workers,
        pin_memory=True if device.type == 'cuda' else False
    )
    
    model = ImprovedVQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        embedding_dim=args.embedding_dim,
        num_embeddings=args.num_embeddings,
        hidden_dims=[64, 128, 256],
        num_res_blocks=args.num_res_blocks,
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
        
        loss, recon_loss, vq_loss, perc_loss = train_epoch(
            model, dataloader, optimizer, device, epoch, use_perceptual=True
        )
        
        print(f"Avg Loss: {loss:.4f}, Recon: {recon_loss:.4f}, VQ: {vq_loss:.4f}, Perc: {perc_loss:.4f}")
        
        wandb.log({
            'epoch/loss': loss,
            'epoch/recon_loss': recon_loss,
            'epoch/vq_loss': vq_loss,
            'epoch/perceptual_loss': perc_loss,
            'epoch/lr': optimizer.param_groups[0]['lr'],
            'epoch': epoch
        })
        
        scheduler.step()
        
        if (epoch + 1) % args.save_every == 0:
            checkpoint_path = checkpoint_dir / f"improved_vqvae_epoch_{epoch+1}.pt"
            torch.save({
                'epoch': epoch,
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'scheduler_state_dict': scheduler.state_dict(),
                'loss': loss,
            }, checkpoint_path)
            print(f"Saved checkpoint to {checkpoint_path}")
    
    final_path = checkpoint_dir / "improved_vqvae_final.pt"
    torch.save({
        'model_state_dict': model.state_dict(),
        'config': {
            'num_blocks': len(BLOCKS_ARRAY),
            'embedding_dim': args.embedding_dim,
            'num_embeddings': args.num_embeddings,
            'hidden_dims': [64, 128, 256],
            'num_res_blocks': args.num_res_blocks,
            'commitment_cost': 0.25
        }
    }, final_path)
    print(f"Saved final model to {final_path}")
    
    wandb.finish()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--cache_dir', type=str, default='./data/cache')
    parser.add_argument('--checkpoint_dir', type=str, default='./checkpoints_improved')
    parser.add_argument('--chunk_size', type=int, default=32)
    parser.add_argument('--overlap', type=int, default=4)
    parser.add_argument('--min_blocks', type=int, default=800)
    parser.add_argument('--max_blocks', type=int, default=None)
    parser.add_argument('--categories', type=str, default=None)
    parser.add_argument('--batch_size', type=int, default=4)
    parser.add_argument('--num_workers', type=int, default=2)
    parser.add_argument('--embedding_dim', type=int, default=128)
    parser.add_argument('--num_embeddings', type=int, default=1024)
    parser.add_argument('--num_res_blocks', type=int, default=3)
    parser.add_argument('--lr', type=float, default=1e-4)
    parser.add_argument('--epochs', type=int, default=100)
    parser.add_argument('--save_every', type=int, default=10)
    
    parser.add_argument('--generate_descriptions', action='store_true', help='Generate AI descriptions for dataset builds')
    parser.add_argument('--gemini_api_key', type=str, default=None, help='Gemini API key for description generation')
    parser.add_argument('--description_language', type=str, default='en', choices=['en', 'ru'], help='Language for descriptions')
    
    args = parser.parse_args()
    main(args)
