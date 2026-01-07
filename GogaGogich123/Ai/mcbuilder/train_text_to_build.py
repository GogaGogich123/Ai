import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent.absolute()))

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader
import argparse
import os
from tqdm import tqdm
import json

from mcbuilder.improved_vqvae import ImprovedVQVAE3D
from mcbuilder.text_conditioned_diffusion import TextConditionedLatentDiffusion3D
from mcbuilder.text_encoder import CLIPTextEncoder, SimpleTextEncoder, TextTokenizer
from mcbuilder.dataset import BuildPasteDataset
from mcbuilder.blocks import BLOCKS_ARRAY

def train_text_to_build(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    checkpoint_dir = Path(args.checkpoint_dir)
    checkpoint_dir.mkdir(parents=True, exist_ok=True)
    
    print("Loading VQ-VAE...")
    vqvae_checkpoint = torch.load(args.vqvae_checkpoint, map_location=device)
    vqvae = ImprovedVQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        **vqvae_checkpoint['config']
    ).to(device)
    vqvae.load_state_dict(vqvae_checkpoint['model_state_dict'])
    vqvae.eval()
    
    for param in vqvae.parameters():
        param.requires_grad = False
    
    print("Initializing text encoder...")
    if args.text_encoder_type == 'clip':
        text_encoder = CLIPTextEncoder(
            model_name=args.clip_model_name,
            projection_dim=args.context_dim,
            freeze=args.freeze_text_encoder
        ).to(device)
        tokenizer = None
    else:
        text_encoder = SimpleTextEncoder(
            embed_dim=args.context_dim,
            max_seq_length=args.max_seq_length,
            vocab_size=args.vocab_size,
            num_layers=args.text_encoder_layers,
            num_heads=args.text_encoder_heads,
            dropout=args.dropout
        ).to(device)
        tokenizer = TextTokenizer(vocab_size=args.vocab_size, max_length=args.max_seq_length)
        
        if args.build_vocab_first:
            print("Building vocabulary from dataset...")
            dataset_for_vocab = BuildPasteDataset(
                cache_dir=args.cache_dir,
                chunk_size=args.chunk_size,
                download=False,
                generate_descriptions=False
            )
            
            descriptions = []
            for i, sample in enumerate(dataset_for_vocab):
                if sample.get('description'):
                    descriptions.append(sample['description'])
                if i >= 1000:
                    break
            
            tokenizer.build_vocab(descriptions)
            
            vocab_path = checkpoint_dir / "tokenizer_vocab.json"
            tokenizer.save(str(vocab_path))
            print(f"✓ Vocabulary saved to {vocab_path}")
    
    print("Initializing text-conditioned diffusion model...")
    diffusion = TextConditionedLatentDiffusion3D(
        latent_channels=vqvae.embedding_dim,
        context_dim=text_encoder.embed_dim,
        model_channels=args.model_channels,
        num_res_blocks=args.num_res_blocks,
        attention_resolutions=tuple(args.attention_resolutions),
        dropout=args.dropout,
        channel_mult=tuple(args.channel_mult),
        num_heads=args.num_heads,
        timesteps=args.timesteps,
        use_cross_attention=True
    ).to(device)
    
    print(f"Diffusion model parameters: {sum(p.numel() for p in diffusion.parameters()):,}")
    print(f"Text encoder parameters: {sum(p.numel() for p in text_encoder.parameters()):,}")
    
    optimizer = torch.optim.AdamW(
        list(diffusion.parameters()) + list(text_encoder.parameters()),
        lr=args.learning_rate,
        weight_decay=args.weight_decay
    )
    
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(
        optimizer,
        T_max=args.epochs,
        eta_min=args.learning_rate * 0.1
    )
    
    start_epoch = 0
    if args.resume_checkpoint and os.path.exists(args.resume_checkpoint):
        print(f"Resuming from checkpoint: {args.resume_checkpoint}")
        checkpoint = torch.load(args.resume_checkpoint, map_location=device)
        diffusion.load_state_dict(checkpoint['diffusion_state_dict'])
        text_encoder.load_state_dict(checkpoint['text_encoder_state_dict'])
        optimizer.load_state_dict(checkpoint['optimizer_state_dict'])
        start_epoch = checkpoint['epoch'] + 1
        print(f"Resumed from epoch {start_epoch}")
    
    print("Setting up dataset...")
    dataset = BuildPasteDataset(
        cache_dir=args.cache_dir,
        chunk_size=args.chunk_size,
        download=True,
        generate_descriptions=args.generate_descriptions,
        gemini_api_key=args.gemini_api_key if args.generate_descriptions else None,
        description_language=args.description_language
    )
    
    dataloader = DataLoader(
        dataset,
        batch_size=args.batch_size,
        num_workers=args.num_workers,
        pin_memory=True
    )
    
    print(f"\nStarting training for {args.epochs} epochs...")
    print(f"Batch size: {args.batch_size}")
    print(f"Learning rate: {args.learning_rate}")
    print(f"Text encoder: {args.text_encoder_type}")
    
    for epoch in range(start_epoch, args.epochs):
        diffusion.train()
        text_encoder.train()
        
        epoch_loss = 0.0
        num_batches = 0
        
        progress_bar = tqdm(dataloader, desc=f"Epoch {epoch+1}/{args.epochs}")
        
        for batch in progress_bar:
            blocks = batch['blocks'].to(device)
            descriptions = batch.get('description', None)
            
            if descriptions is None or all(d is None for d in descriptions):
                continue
            
            descriptions = [d if d is not None else "" for d in descriptions]
            
            with torch.no_grad():
                blocks_one_hot = F.one_hot(blocks, num_classes=len(BLOCKS_ARRAY)).float()
                blocks_one_hot = blocks_one_hot.permute(0, 4, 1, 2, 3)
                
                latents = vqvae.encode(blocks_one_hot)
            
            if args.text_encoder_type == 'clip':
                text_outputs = text_encoder(texts=descriptions)
            else:
                encoded = tokenizer.batch_encode(descriptions)
                input_ids = encoded['input_ids'].to(device)
                attention_mask = encoded['attention_mask'].to(device)
                text_outputs = text_encoder(input_ids, attention_mask)
            
            context = text_outputs['last_hidden_state']
            
            t = torch.randint(0, diffusion.timesteps, (latents.shape[0],), device=device)
            noise = torch.randn_like(latents)
            noisy_latents = diffusion.q_sample(latents, t, noise)
            
            pred_noise = diffusion(noisy_latents, t, context)
            
            loss = F.mse_loss(pred_noise, noise)
            
            optimizer.zero_grad()
            loss.backward()
            
            torch.nn.utils.clip_grad_norm_(diffusion.parameters(), 1.0)
            torch.nn.utils.clip_grad_norm_(text_encoder.parameters(), 1.0)
            
            optimizer.step()
            
            epoch_loss += loss.item()
            num_batches += 1
            
            progress_bar.set_postfix({
                'loss': f'{loss.item():.4f}',
                'avg_loss': f'{epoch_loss/num_batches:.4f}',
                'lr': f'{optimizer.param_groups[0]["lr"]:.6f}'
            })
        
        scheduler.step()
        
        avg_loss = epoch_loss / max(num_batches, 1)
        print(f"\nEpoch {epoch+1} completed - Average loss: {avg_loss:.4f}")
        
        if (epoch + 1) % args.save_every == 0:
            checkpoint_path = checkpoint_dir / f"text_to_build_epoch_{epoch+1}.pt"
            torch.save({
                'epoch': epoch,
                'diffusion_state_dict': diffusion.state_dict(),
                'text_encoder_state_dict': text_encoder.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'loss': avg_loss,
                'config': {
                    'latent_channels': vqvae.embedding_dim,
                    'context_dim': text_encoder.embed_dim,
                    'model_channels': args.model_channels,
                    'num_res_blocks': args.num_res_blocks,
                    'attention_resolutions': args.attention_resolutions,
                    'dropout': args.dropout,
                    'channel_mult': args.channel_mult,
                    'num_heads': args.num_heads,
                    'timesteps': args.timesteps,
                    'text_encoder_type': args.text_encoder_type
                }
            }, checkpoint_path)
            print(f"✓ Checkpoint saved: {checkpoint_path}")
    
    final_path = checkpoint_dir / "text_to_build_final.pt"
    torch.save({
        'epoch': args.epochs - 1,
        'diffusion_state_dict': diffusion.state_dict(),
        'text_encoder_state_dict': text_encoder.state_dict(),
        'loss': avg_loss,
        'config': {
            'latent_channels': vqvae.embedding_dim,
            'context_dim': text_encoder.embed_dim,
            'model_channels': args.model_channels,
            'num_res_blocks': args.num_res_blocks,
            'attention_resolutions': args.attention_resolutions,
            'dropout': args.dropout,
            'channel_mult': args.channel_mult,
            'num_heads': args.num_heads,
            'timesteps': args.timesteps,
            'text_encoder_type': args.text_encoder_type
        }
    }, final_path)
    print(f"\n✓ Final model saved: {final_path}")
    print("Training completed!")

def main():
    parser = argparse.ArgumentParser(description='Train Text-to-Build Model')
    
    parser.add_argument('--vqvae_checkpoint', type=str, required=True,
                      help='Path to trained VQ-VAE checkpoint')
    parser.add_argument('--cache_dir', type=str, default='./data/cache',
                      help='Directory for caching builds')
    parser.add_argument('--checkpoint_dir', type=str, default='./checkpoints_text_to_build',
                      help='Directory to save checkpoints')
    
    parser.add_argument('--text_encoder_type', type=str, default='clip', choices=['clip', 'simple'],
                      help='Type of text encoder to use')
    parser.add_argument('--clip_model_name', type=str, default='sentence-transformers/all-MiniLM-L6-v2',
                      help='CLIP model name (only for clip encoder)')
    parser.add_argument('--freeze_text_encoder', action='store_true',
                      help='Freeze text encoder weights')
    parser.add_argument('--vocab_size', type=int, default=10000,
                      help='Vocabulary size (only for simple encoder)')
    parser.add_argument('--max_seq_length', type=int, default=77,
                      help='Maximum sequence length')
    parser.add_argument('--text_encoder_layers', type=int, default=6,
                      help='Number of transformer layers (only for simple encoder)')
    parser.add_argument('--text_encoder_heads', type=int, default=8,
                      help='Number of attention heads (only for simple encoder)')
    parser.add_argument('--build_vocab_first', action='store_true',
                      help='Build vocabulary from dataset before training (only for simple encoder)')
    
    parser.add_argument('--context_dim', type=int, default=512,
                      help='Dimension of text context embeddings')
    parser.add_argument('--model_channels', type=int, default=128,
                      help='Base number of channels in UNet')
    parser.add_argument('--num_res_blocks', type=int, default=2,
                      help='Number of residual blocks per level')
    parser.add_argument('--attention_resolutions', nargs='+', type=int, default=[4, 8],
                      help='Resolutions to apply attention')
    parser.add_argument('--dropout', type=float, default=0.1,
                      help='Dropout rate')
    parser.add_argument('--channel_mult', nargs='+', type=int, default=[1, 2, 4, 8],
                      help='Channel multipliers for each level')
    parser.add_argument('--num_heads', type=int, default=8,
                      help='Number of attention heads')
    parser.add_argument('--timesteps', type=int, default=1000,
                      help='Number of diffusion timesteps')
    
    parser.add_argument('--chunk_size', type=int, default=32,
                      help='Size of build chunks')
    parser.add_argument('--batch_size', type=int, default=4,
                      help='Batch size')
    parser.add_argument('--epochs', type=int, default=100,
                      help='Number of training epochs')
    parser.add_argument('--learning_rate', type=float, default=1e-4,
                      help='Learning rate')
    parser.add_argument('--weight_decay', type=float, default=0.01,
                      help='Weight decay')
    parser.add_argument('--save_every', type=int, default=10,
                      help='Save checkpoint every N epochs')
    parser.add_argument('--num_workers', type=int, default=4,
                      help='Number of data loader workers')
    
    parser.add_argument('--generate_descriptions', action='store_true',
                      help='Generate AI descriptions during training')
    parser.add_argument('--gemini_api_key', type=str,
                      help='Gemini API key for description generation')
    parser.add_argument('--description_language', type=str, default='en',
                      help='Language for descriptions (en/ru)')
    
    parser.add_argument('--resume_checkpoint', type=str,
                      help='Path to checkpoint to resume from')
    
    args = parser.parse_args()
    
    train_text_to_build(args)

if __name__ == '__main__':
    main()
