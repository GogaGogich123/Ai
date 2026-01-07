import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.absolute()))

import torch
import torch.nn.functional as F
import numpy as np
import argparse
from typing import Optional

from mcbuilder.improved_vqvae import ImprovedVQVAE3D
from mcbuilder.text_conditioned_diffusion import TextConditionedLatentDiffusion3D
from mcbuilder.text_encoder import CLIPTextEncoder, SimpleTextEncoder, TextTokenizer
from mcbuilder.validators import BuildQualityValidator, fix_floating_blocks
from mcbuilder.blocks import BLOCKS_ARRAY
from mcbuilder.litematic_export import export_to_litematic

def load_models(args, device):
    print("Loading VQ-VAE...")
    vqvae_checkpoint = torch.load(args.vqvae_checkpoint, map_location=device)
    vqvae = ImprovedVQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        **vqvae_checkpoint['config']
    ).to(device)
    vqvae.load_state_dict(vqvae_checkpoint['model_state_dict'])
    vqvae.eval()
    
    print("Loading text-to-build model...")
    text_checkpoint = torch.load(args.text_to_build_checkpoint, map_location=device)
    
    config = text_checkpoint['config']
    text_encoder_type = config.get('text_encoder_type', 'clip')
    
    if text_encoder_type == 'clip':
        text_encoder = CLIPTextEncoder(
            model_name=args.clip_model_name,
            projection_dim=config['context_dim']
        ).to(device)
    else:
        text_encoder = SimpleTextEncoder(
            embed_dim=config['context_dim'],
            max_seq_length=args.max_seq_length,
            vocab_size=args.vocab_size
        ).to(device)
    
    diffusion = TextConditionedLatentDiffusion3D(
        latent_channels=config['latent_channels'],
        context_dim=config['context_dim'],
        model_channels=config['model_channels'],
        num_res_blocks=config['num_res_blocks'],
        attention_resolutions=tuple(config['attention_resolutions']),
        dropout=config['dropout'],
        channel_mult=tuple(config['channel_mult']),
        num_heads=config['num_heads'],
        timesteps=config['timesteps']
    ).to(device)
    
    diffusion.load_state_dict(text_checkpoint['diffusion_state_dict'])
    text_encoder.load_state_dict(text_checkpoint['text_encoder_state_dict'])
    
    diffusion.eval()
    text_encoder.eval()
    
    tokenizer = None
    if text_encoder_type == 'simple':
        tokenizer_path = Path(args.text_to_build_checkpoint).parent / "tokenizer_vocab.json"
        if tokenizer_path.exists():
            tokenizer = TextTokenizer()
            tokenizer.load(str(tokenizer_path))
            print(f"✓ Loaded tokenizer from {tokenizer_path}")
        else:
            print("Warning: Tokenizer not found, using random vocab")
            tokenizer = TextTokenizer(vocab_size=args.vocab_size)
    
    return vqvae, diffusion, text_encoder, tokenizer, text_encoder_type

def generate_from_prompt(
    prompt: str,
    vqvae,
    diffusion,
    text_encoder,
    tokenizer,
    text_encoder_type: str,
    device,
    size: tuple = (32, 32, 32),
    num_inference_steps: int = 50,
    guidance_scale: float = 7.5,
    num_samples: int = 1,
    validate: bool = True
):
    print(f"\n{'='*60}")
    print(f"Prompt: {prompt}")
    print(f"Size: {size}")
    print(f"Guidance scale: {guidance_scale}")
    print(f"Inference steps: {num_inference_steps}")
    print(f"{'='*60}\n")
    
    with torch.no_grad():
        if text_encoder_type == 'clip':
            text_outputs = text_encoder(texts=[prompt])
        else:
            if tokenizer is None:
                raise ValueError("Tokenizer required for simple text encoder")
            encoded = tokenizer.encode(prompt)
            input_ids = encoded['input_ids'].unsqueeze(0).to(device)
            attention_mask = encoded['attention_mask'].unsqueeze(0).to(device)
            text_outputs = text_encoder(input_ids, attention_mask)
        
        context = text_outputs['last_hidden_state']
        
        latent_size = tuple(s // 4 for s in size)
        latent_shape = (1, vqvae.embedding_dim, *latent_size)
        
        best_build = None
        best_score = -1
        
        validator = BuildQualityValidator() if validate else None
        
        for i in range(num_samples):
            print(f"Generating sample {i+1}/{num_samples}...")
            
            latent = diffusion.sample(
                shape=latent_shape,
                device=device,
                context=context,
                num_inference_steps=num_inference_steps,
                guidance_scale=guidance_scale
            )
            
            logits = vqvae.decode(latent)
            
            blocks = torch.argmax(logits, dim=1).squeeze(0)
            
            if blocks.shape != size:
                blocks = F.interpolate(
                    blocks.unsqueeze(0).unsqueeze(0).float(),
                    size=size,
                    mode='nearest'
                ).squeeze().long()
            
            if validate:
                physics_score, interior_score = validator.validate_build(blocks)
                total_score = physics_score * 0.6 + interior_score * 0.4
                
                print(f"  Physics: {physics_score:.3f} | Interior: {interior_score:.3f} | Total: {total_score:.3f}")
                
                if total_score > best_score:
                    best_score = total_score
                    best_build = blocks
            else:
                best_build = blocks
                break
        
        if validate and best_build is not None:
            print(f"\nBest build score: {best_score:.3f}")
            print("Applying auto-fix...")
            best_build = fix_floating_blocks(best_build)
        
        return best_build

def main():
    parser = argparse.ArgumentParser(description='Generate Minecraft builds from text prompts')
    
    parser.add_argument('--vqvae_checkpoint', type=str, required=True,
                      help='Path to trained VQ-VAE checkpoint')
    parser.add_argument('--text_to_build_checkpoint', type=str, required=True,
                      help='Path to trained text-to-build model checkpoint')
    
    parser.add_argument('--prompt', type=str, required=True,
                      help='Text prompt for generation (e.g., "medieval castle with towers")')
    
    parser.add_argument('--size', type=str, default='32,32,32',
                      help='Build size as X,Y,Z (default: 32,32,32)')
    parser.add_argument('--num_inference_steps', type=int, default=50,
                      help='Number of denoising steps (default: 50)')
    parser.add_argument('--guidance_scale', type=float, default=7.5,
                      help='Classifier-free guidance scale (default: 7.5)')
    parser.add_argument('--num_samples', type=int, default=3,
                      help='Number of candidates to generate and select best (default: 3)')
    
    parser.add_argument('--validate', action='store_true',
                      help='Enable quality validation and auto-fix')
    parser.add_argument('--no_validate', action='store_false', dest='validate',
                      help='Disable validation (faster)')
    parser.set_defaults(validate=True)
    
    parser.add_argument('--output', type=str, default='generated_build.litematic',
                      help='Output .litematic file path')
    parser.add_argument('--name', type=str, default=None,
                      help='Build name (defaults to prompt)')
    
    parser.add_argument('--seed', type=int, default=None,
                      help='Random seed for reproducibility')
    
    parser.add_argument('--clip_model_name', type=str, default='sentence-transformers/all-MiniLM-L6-v2',
                      help='CLIP model name')
    parser.add_argument('--vocab_size', type=int, default=10000,
                      help='Vocabulary size for simple encoder')
    parser.add_argument('--max_seq_length', type=int, default=77,
                      help='Maximum sequence length')
    
    args = parser.parse_args()
    
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    if args.seed is not None:
        torch.manual_seed(args.seed)
        np.random.seed(args.seed)
        print(f"Random seed: {args.seed}")
    
    size = tuple(map(int, args.size.split(',')))
    if len(size) != 3:
        raise ValueError("Size must be X,Y,Z format (e.g., 32,32,32)")
    
    vqvae, diffusion, text_encoder, tokenizer, text_encoder_type = load_models(args, device)
    
    build = generate_from_prompt(
        prompt=args.prompt,
        vqvae=vqvae,
        diffusion=diffusion,
        text_encoder=text_encoder,
        tokenizer=tokenizer,
        text_encoder_type=text_encoder_type,
        device=device,
        size=size,
        num_inference_steps=args.num_inference_steps,
        guidance_scale=args.guidance_scale,
        num_samples=args.num_samples,
        validate=args.validate
    )
    
    if build is None:
        print("Failed to generate build!")
        return
    
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    build_name = args.name if args.name else args.prompt[:50]
    
    print(f"\nExporting to {output_path}...")
    export_to_litematic(build.cpu().numpy(), str(output_path), build_name)
    
    print(f"\n✓ Build generated successfully!")
    print(f"  Output: {output_path}")
    print(f"  Size: {build.shape}")
    print(f"  Name: {build_name}")
    
    non_air = (build != 0).sum().item()
    total = build.numel()
    print(f"  Blocks: {non_air:,} / {total:,} ({non_air/total*100:.1f}% filled)")
    
    print("\nTo use in Minecraft:")
    print("  1. Install Litematica mod")
    print(f"  2. Place {output_path.name} in .minecraft/schematics/")
    print("  3. Load schematic in-game with Litematica")

if __name__ == '__main__':
    main()
