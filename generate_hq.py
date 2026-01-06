import torch
import numpy as np
import argparse
from pathlib import Path

from mcbuilder.improved_vqvae import ImprovedVQVAE3D
from mcbuilder.diffusion import LatentDiffusion3D
from mcbuilder.validators import BuildQualityValidator, fix_floating_blocks
from mcbuilder.blocks import BLOCKS_ARRAY
from mcbuilder.litematic_export import export_to_litematic
from mcbuilder.chunked_generation import MultiScaleChunkedGenerator, ChunkConfig

def load_models(vqvae_path: str, diffusion_path: str, device):
    print("Loading Improved VQ-VAE...")
    vqvae_checkpoint = torch.load(vqvae_path, map_location=device)
    vqvae = ImprovedVQVAE3D(
        num_blocks=len(BLOCKS_ARRAY),
        **vqvae_checkpoint['config']
    ).to(device)
    vqvae.load_state_dict(vqvae_checkpoint['model_state_dict'])
    vqvae.eval()
    
    print("Loading Diffusion Model...")
    diffusion_checkpoint = torch.load(diffusion_path, map_location=device)
    diffusion = LatentDiffusion3D(
        **diffusion_checkpoint['config']
    ).to(device)
    diffusion.load_state_dict(diffusion_checkpoint['model_state_dict'])
    diffusion.eval()
    
    return vqvae, diffusion

def generate_single_chunk(
    vqvae,
    diffusion,
    device,
    size: tuple = (32, 32, 32),
    num_samples: int = 3,
    validate: bool = True,
    num_inference_steps: int = 50
):
    print(f"Generating {num_samples} candidates of size {size}...")
    
    latent_size = tuple(s // 4 for s in size)
    latent_shape = (1, vqvae.embedding_dim, *latent_size)
    
    candidates = []
    scores = []
    
    validator = BuildQualityValidator() if validate else None
    
    for i in range(num_samples):
        print(f"\nGenerating candidate {i+1}/{num_samples}...")
        
        with torch.no_grad():
            latent = diffusion.sample(latent_shape, device, num_inference_steps=num_inference_steps)
            
            blocks_logits = vqvae.decode_latent(latent)
            blocks = blocks_logits.argmax(dim=1).squeeze(0)
            
            if validate:
                print("Validating build quality...")
                block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
                
                results = validator.validate(blocks, block_names)
                
                print(f"Physics score: {results['physics'].score:.2f}")
                print(f"Interior score: {results['interior'].score:.2f}")
                
                for key, result in results.items():
                    if result.issues:
                        print(f"  {key} issues: {', '.join(result.issues)}")
                
                score = validator.get_overall_score(results)
                print(f"Overall score: {score:.2f}")
                
                if results['physics'].score < 0.8:
                    print("Fixing floating blocks...")
                    blocks = fix_floating_blocks(blocks)
                    
                    results_after = validator.validate(blocks, block_names)
                    print(f"Physics score after fix: {results_after['physics'].score:.2f}")
                
                candidates.append(blocks.cpu().numpy())
                scores.append(score)
            else:
                candidates.append(blocks.cpu().numpy())
                scores.append(1.0)
    
    if validate and scores:
        best_idx = np.argmax(scores)
        print(f"\nBest candidate: #{best_idx+1} with score {scores[best_idx]:.2f}")
        return candidates[best_idx]
    else:
        return candidates[0]

def generate_high_quality(
    vqvae,
    diffusion,
    device,
    size: tuple = (32, 32, 32),
    num_samples: int = 3,
    validate: bool = True,
    use_chunked: bool = False,
    use_hierarchical: bool = False,
    chunk_size: int = 32,
    overlap: int = 8,
    num_inference_steps: int = 50
):
    if use_chunked or any(s > chunk_size for s in size):
        print(f"\n🔷 Using multi-scale chunked generation for large build {size}")
        
        chunk_config = ChunkConfig(
            chunk_size=chunk_size,
            overlap=overlap,
            blend_width=4
        )
        
        generator = MultiScaleChunkedGenerator(
            vqvae, diffusion, device, chunk_config
        )
        
        if use_hierarchical:
            blocks = generator.generate_hierarchical(
                size,
                num_inference_steps=num_inference_steps
            )
        else:
            blocks = generator.generate_large_build(
                size,
                num_inference_steps=num_inference_steps
            )
        
        if validate:
            print("\nValidating large build quality...")
            validator = BuildQualityValidator()
            block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
            results = validator.validate(blocks, block_names)
            
            print(f"Physics score: {results['physics'].score:.2f}")
            print(f"Interior score: {results['interior'].score:.2f}")
            
            if results['physics'].score < 0.8:
                print("Fixing floating blocks...")
                blocks = fix_floating_blocks(blocks)
        
        return blocks.cpu().numpy() if isinstance(blocks, torch.Tensor) else blocks
    else:
        return generate_single_chunk(
            vqvae, diffusion, device, size, num_samples, validate, num_inference_steps
        )

def main(args):
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    size = tuple(map(int, args.size.split(',')))
    
    vqvae, diffusion = load_models(
        args.vqvae_checkpoint,
        args.diffusion_checkpoint,
        device
    )
    
    blocks = generate_high_quality(
        vqvae,
        diffusion,
        device,
        size=size,
        num_samples=args.num_samples,
        validate=args.validate,
        use_chunked=args.chunked,
        use_hierarchical=args.hierarchical,
        chunk_size=args.chunk_size,
        overlap=args.overlap,
        num_inference_steps=args.num_inference_steps
    )
    
    block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
    
    export_to_litematic(
        blocks=blocks,
        block_names=block_names,
        output_path=args.output,
        name=args.name,
        author="MinecraftAI-HQ",
        description=f"High-quality diffusion model, validated={args.validate}"
    )
    
    print(f"\n✓ High-quality build generated: {args.output}")
    print("You can import this .litematic file into Minecraft using Litematica mod")
    
    if args.generate_multiple:
        print(f"\nGenerating {args.generate_multiple} additional variations...")
        for i in range(args.generate_multiple):
            output_path = Path(args.output)
            variant_path = output_path.parent / f"{output_path.stem}_variant_{i+1}{output_path.suffix}"
            
            blocks = generate_high_quality(
                vqvae,
                diffusion,
                device,
                size=size,
                num_samples=1,
                validate=False,
                use_chunked=args.chunked,
                chunk_size=args.chunk_size,
                overlap=args.overlap,
                num_inference_steps=args.num_inference_steps
            )
            
            export_to_litematic(
                blocks=blocks,
                block_names=block_names,
                output_path=str(variant_path),
                name=f"{args.name} - Variant {i+1}",
                author="MinecraftAI-HQ",
                description=f"Variant {i+1}"
            )
            
            print(f"✓ Generated variant {i+1}: {variant_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate high-quality Minecraft builds with diffusion")
    parser.add_argument('--vqvae_checkpoint', type=str, required=True, help='Path to improved VQ-VAE checkpoint')
    parser.add_argument('--diffusion_checkpoint', type=str, required=True, help='Path to diffusion model checkpoint')
    parser.add_argument('--size', type=str, default='32,32,32', help='Build size as x,y,z')
    parser.add_argument('--output', type=str, default='hq_build.litematic', help='Output .litematic file path')
    parser.add_argument('--name', type=str, default='HQ AI Build', help='Build name')
    parser.add_argument('--num_samples', type=int, default=3, help='Number of candidates to generate (best will be selected)')
    parser.add_argument('--validate', action='store_true', help='Enable quality validation')
    parser.add_argument('--generate_multiple', type=int, default=0, help='Generate N additional unvalidated variants')
    parser.add_argument('--chunked', action='store_true', help='Force chunked generation even for small builds')
    parser.add_argument('--hierarchical', action='store_true', help='Use hierarchical multi-scale generation')
    parser.add_argument('--chunk_size', type=int, default=32, help='Size of each chunk (default: 32)')
    parser.add_argument('--overlap', type=int, default=8, help='Overlap between chunks (default: 8)')
    parser.add_argument('--num_inference_steps', type=int, default=50, help='Number of diffusion steps (default: 50)')
    
    args = parser.parse_args()
    main(args)
