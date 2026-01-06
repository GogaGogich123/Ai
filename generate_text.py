import torch
import numpy as np
import argparse
from pathlib import Path

from mcbuilder.vqvae import VQVAE3D
from mcbuilder.text_encoder import TextConditionedTransformer
from mcbuilder.blocks import BLOCKS_ARRAY
from mcbuilder.litematic_export import export_to_litematic

def load_models(vqvae_path: str, transformer_path: str, device):
    print("Loading VQ-VAE...")
    vqvae_checkpoint = torch.load(vqvae_path, map_location=device)
    vqvae = VQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        **vqvae_checkpoint['config']
    ).to(device)
    vqvae.load_state_dict(vqvae_checkpoint['model_state_dict'])
    vqvae.eval()
    
    print("Loading Text-Conditioned Transformer...")
    transformer_checkpoint = torch.load(transformer_path, map_location=device)
    transformer = TextConditionedTransformer(
        **transformer_checkpoint['config']
    ).to(device)
    transformer.load_state_dict(transformer_checkpoint['model_state_dict'])
    transformer.eval()
    
    return vqvae, transformer

def generate_from_text(
    vqvae,
    transformer,
    device,
    prompt: str,
    size: tuple = (32, 32, 32),
    num_iterations: int = 10,
    temperature: float = 1.0,
    top_k: int = 50
):
    print(f"Generating build from prompt: '{prompt}'")
    print(f"Size: {size}")
    
    latent_size = tuple(s // 4 for s in size)
    
    with torch.no_grad():
        generated_latents = transformer.generate_from_text(
            text_prompt=prompt,
            shape=latent_size,
            device=device,
            num_iterations=num_iterations,
            temperature=temperature,
            top_k=top_k
        )
        
        blocks_logits = vqvae.decode_indices(generated_latents, latent_size)
        blocks = blocks_logits.argmax(dim=1).squeeze(0).cpu().numpy()
    
    return blocks

def main(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    vqvae, transformer = load_models(
        args.vqvae_checkpoint,
        args.transformer_checkpoint,
        device
    )
    
    size = tuple(map(int, args.size.split(',')))
    
    blocks = generate_from_text(
        vqvae,
        transformer,
        device,
        prompt=args.prompt,
        size=size,
        num_iterations=args.num_iterations,
        temperature=args.temperature,
        top_k=args.top_k
    )
    
    block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
    
    export_to_litematic(
        blocks=blocks,
        block_names=block_names,
        output_path=args.output,
        name=args.name,
        author="MinecraftAI",
        description=f"Prompt: {args.prompt}\nTemp: {args.temperature}, Iter: {args.num_iterations}"
    )
    
    print(f"\nBuild generated successfully: {args.output}")
    print(f"You can import this .litematic file into Minecraft using Litematica mod")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate Minecraft builds from text prompts")
    parser.add_argument('--vqvae_checkpoint', type=str, required=True, help='Path to VQ-VAE checkpoint')
    parser.add_argument('--transformer_checkpoint', type=str, required=True, help='Path to text-conditioned transformer checkpoint')
    parser.add_argument('--prompt', type=str, required=True, help='Text prompt describing the build')
    parser.add_argument('--size', type=str, default='32,32,32', help='Build size as x,y,z')
    parser.add_argument('--output', type=str, default='generated_build.litematic', help='Output .litematic file path')
    parser.add_argument('--name', type=str, default=None, help='Build name (defaults to prompt)')
    parser.add_argument('--num_iterations', type=int, default=10, help='Number of generation iterations')
    parser.add_argument('--temperature', type=float, default=1.0, help='Sampling temperature')
    parser.add_argument('--top_k', type=int, default=50, help='Top-k sampling')
    
    args = parser.parse_args()
    
    if args.name is None:
        args.name = args.prompt[:50]
    
    main(args)
