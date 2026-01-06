import torch
import numpy as np
import argparse
from pathlib import Path

from mcbuilder.vqvae import VQVAE3D
from mcbuilder.transformer import MaskedLatentModel
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
    
    print("Loading Transformer...")
    transformer_checkpoint = torch.load(transformer_path, map_location=device)
    transformer = MaskedLatentModel(
        **transformer_checkpoint['config']
    ).to(device)
    transformer.load_state_dict(transformer_checkpoint['model_state_dict'])
    transformer.eval()
    
    return vqvae, transformer

def generate_build(
    vqvae,
    transformer,
    device,
    size: tuple = (32, 32, 32),
    num_iterations: int = 10,
    temperature: float = 1.0,
    top_k: int = 50
):
    print(f"Generating build of size {size}...")
    
    latent_size = tuple(s // 4 for s in size)
    
    partial = torch.zeros(1, np.prod(latent_size), dtype=torch.long, device=device)
    mask = torch.ones(1, np.prod(latent_size), dtype=torch.bool, device=device)
    
    with torch.no_grad():
        generated_latents = transformer.generate(
            partial=partial,
            mask=mask,
            num_iterations=num_iterations,
            temperature=temperature,
            top_k=top_k
        )
        
        generated_latents = generated_latents.view(1, *latent_size)
        
        blocks_logits = vqvae.decode_indices(generated_latents, latent_size)
        blocks = blocks_logits.argmax(dim=1).squeeze(0).cpu().numpy()
    
    return blocks

def extend_build(
    vqvae,
    transformer,
    device,
    existing_blocks: np.ndarray,
    mask: np.ndarray,
    num_iterations: int = 10,
    temperature: float = 1.0,
    top_k: int = 50
):
    print("Extending existing build...")
    
    existing_tensor = torch.from_numpy(existing_blocks).unsqueeze(0).long().to(device)
    mask_tensor = torch.from_numpy(mask).unsqueeze(0).bool().to(device)
    
    with torch.no_grad():
        existing_latents = vqvae.encode(existing_tensor)
        
        generated_latents = transformer.generate(
            partial=existing_latents,
            mask=mask_tensor,
            num_iterations=num_iterations,
            temperature=temperature,
            top_k=top_k
        )
        
        latent_size = tuple(s // 4 for s in existing_blocks.shape)
        
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
    
    if args.mode == "generate":
        size = tuple(map(int, args.size.split(',')))
        
        blocks = generate_build(
            vqvae,
            transformer,
            device,
            size=size,
            num_iterations=args.num_iterations,
            temperature=args.temperature,
            top_k=args.top_k
        )
        
    elif args.mode == "extend":
        print("Extension mode not yet implemented")
        return
    
    block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
    
    export_to_litematic(
        blocks=blocks,
        block_names=block_names,
        output_path=args.output,
        name=args.name,
        author="MinecraftAI",
        description=f"Generated with temperature={args.temperature}, iterations={args.num_iterations}"
    )
    
    print(f"Build generated successfully: {args.output}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--vqvae_checkpoint', type=str, required=True)
    parser.add_argument('--transformer_checkpoint', type=str, required=True)
    parser.add_argument('--mode', type=str, default='generate', choices=['generate', 'extend'])
    parser.add_argument('--size', type=str, default='32,32,32')
    parser.add_argument('--output', type=str, default='generated_build.litematic')
    parser.add_argument('--name', type=str, default='AI Generated Build')
    parser.add_argument('--num_iterations', type=int, default=10)
    parser.add_argument('--temperature', type=float, default=1.0)
    parser.add_argument('--top_k', type=int, default=50)
    
    args = parser.parse_args()
    main(args)
