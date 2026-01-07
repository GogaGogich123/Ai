import torch
import sys
from pathlib import Path

def test_imports():
    print("Testing imports...")
    try:
        from mcbuilder import (
            BLOCKS_ARRAY, BuildPasteAPI, BuildPasteDataset, 
            ImprovedVQVAE3D, LatentDiffusion3D, export_to_litematic,
            BuildQualityValidator, fix_floating_blocks,
            MultiScaleChunkedGenerator, ChunkConfig
        )
        print("✓ All imports successful")
        return True
    except Exception as e:
        print(f"✗ Import failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_api():
    print("\nTesting BuildPaste API...")
    try:
        from mcbuilder import BuildPasteAPI
        api = BuildPasteAPI()
        builds = api.list_builds(limit=5)
        print(f"✓ API working: Found {len(builds)} builds")
        if builds:
            print(f"  First build: {builds[0].name} ({builds[0].block_count} blocks)")
        return True
    except Exception as e:
        print(f"✗ API test failed: {e}")
        return False

def test_models():
    print("\nTesting model creation...")
    try:
        from mcbuilder import ImprovedVQVAE3D, LatentDiffusion3D, BLOCKS_ARRAY
        
        device = torch.device('cpu')
        
        vqvae = ImprovedVQVAE3D(
            num_blocks=len(BLOCKS_ARRAY),
            embedding_dim=128,
            num_embeddings=1024,
            hidden_dims=[64, 128, 256],
            num_res_blocks=3,
            commitment_cost=0.25
        ).to(device)
        
        print(f"✓ Improved VQ-VAE created: {sum(p.numel() for p in vqvae.parameters()):,} params")
        
        diffusion = LatentDiffusion3D(
            latent_channels=128,
            model_channels=64,
            num_res_blocks=2,
            attention_resolutions=(4, 8),
            dropout=0.1,
            channel_mult=(1, 2, 4),
            num_heads=4,
            timesteps=1000
        ).to(device)
        
        print(f"✓ Diffusion model created: {sum(p.numel() for p in diffusion.parameters()):,} params")
        
        return True
    except Exception as e:
        print(f"✗ Model test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_forward_pass():
    print("\nTesting forward pass...")
    try:
        from mcbuilder import ImprovedVQVAE3D, LatentDiffusion3D, BLOCKS_ARRAY
        
        device = torch.device('cpu')
        
        vqvae = ImprovedVQVAE3D(
            num_blocks=len(BLOCKS_ARRAY),
            embedding_dim=128,
            num_embeddings=1024,
            hidden_dims=[64, 128, 256],
            num_res_blocks=3,
            commitment_cost=0.25
        ).to(device)
        
        dummy_input = torch.randint(0, len(BLOCKS_ARRAY), (1, 8, 8, 8), device=device)
        
        with torch.no_grad():
            recon, vq_loss, indices = vqvae(dummy_input)
        
        print(f"✓ VQ-VAE forward pass: input {dummy_input.shape} -> recon {recon.shape}")
        print(f"  VQ loss: {vq_loss.item():.4f}")
        
        diffusion = LatentDiffusion3D(
            latent_channels=128,
            model_channels=64,
            num_res_blocks=2,
            attention_resolutions=(4, 8),
            dropout=0.1,
            channel_mult=(1, 2, 4),
            num_heads=4,
            timesteps=1000
        ).to(device)
        
        dummy_latent = torch.randn(1, 128, 2, 2, 2, device=device)
        dummy_t = torch.tensor([500], device=device)
        
        with torch.no_grad():
            noise_pred = diffusion(dummy_latent, dummy_t)
        
        print(f"✓ Diffusion forward pass: latent {dummy_latent.shape} -> noise {noise_pred.shape}")
        
        return True
    except Exception as e:
        print(f"✗ Forward pass test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_validators():
    print("\nTesting validators...")
    try:
        from mcbuilder import BuildQualityValidator, fix_floating_blocks, BLOCKS_ARRAY
        
        validator = BuildQualityValidator()
        print("✓ Validator created")
        
        dummy_blocks = torch.randint(0, len(BLOCKS_ARRAY), (16, 16, 16))
        dummy_blocks[0, :, :] = 1
        
        block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
        results = validator.validate(dummy_blocks, block_names)
        
        print(f"✓ Validation complete:")
        print(f"  Physics score: {results['physics'].score:.2f}")
        print(f"  Interior score: {results['interior'].score:.2f}")
        
        fixed_blocks = fix_floating_blocks(dummy_blocks)
        print(f"✓ Auto-fix applied: {dummy_blocks.shape} -> {fixed_blocks.shape}")
        
        return True
    except Exception as e:
        print(f"✗ Validator test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_chunked_generator():
    print("\nTesting chunked generator...")
    try:
        from mcbuilder import (
            ImprovedVQVAE3D, LatentDiffusion3D, 
            MultiScaleChunkedGenerator, ChunkConfig, BLOCKS_ARRAY
        )
        
        device = torch.device('cpu')
        
        vqvae = ImprovedVQVAE3D(
            num_blocks=len(BLOCKS_ARRAY),
            embedding_dim=64,
            num_embeddings=256,
            hidden_dims=[32, 64],
            num_res_blocks=2,
            commitment_cost=0.25
        ).to(device)
        
        diffusion = LatentDiffusion3D(
            latent_channels=64,
            model_channels=32,
            num_res_blocks=1,
            attention_resolutions=(4,),
            dropout=0.1,
            channel_mult=(1, 2),
            num_heads=2,
            timesteps=100
        ).to(device)
        
        chunk_config = ChunkConfig(
            chunk_size=16,
            overlap=4,
            blend_width=2
        )
        
        generator = MultiScaleChunkedGenerator(
            vqvae, diffusion, device, chunk_config
        )
        
        print(f"✓ Chunked generator created")
        print(f"  Chunk size: {chunk_config.chunk_size}")
        print(f"  Overlap: {chunk_config.overlap}")
        print(f"  Blend width: {chunk_config.blend_width}")
        
        return True
    except Exception as e:
        print(f"✗ Chunked generator test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    print("=" * 60)
    print("Minecraft AI Builder - Test Suite")
    print("=" * 60)
    
    results = []
    
    results.append(("Imports", test_imports()))
    results.append(("API", test_api()))
    results.append(("Models", test_models()))
    results.append(("Forward Pass", test_forward_pass()))
    results.append(("Validators", test_validators()))
    results.append(("Chunked Generator", test_chunked_generator()))
    
    print("\n" + "=" * 60)
    print("Test Results:")
    print("=" * 60)
    
    for name, result in results:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"{status}: {name}")
    
    all_passed = all(result for _, result in results)
    
    print("=" * 60)
    if all_passed:
        print("All tests passed! ✓")
        return 0
    else:
        print("Some tests failed ✗")
        return 1

if __name__ == "__main__":
    sys.exit(main())
