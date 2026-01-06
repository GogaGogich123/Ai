import torch
import sys
from pathlib import Path

def test_imports():
    print("Testing imports...")
    try:
        from mcbuilder import (
            BLOCKS_ARRAY, BuildPasteAPI, BuildPasteDataset, 
            VQVAE3D, MaskedLatentModel, export_to_litematic
        )
        from mcbuilder.text_encoder import TextConditionedTransformer
        print("✓ All imports successful")
        return True
    except Exception as e:
        print(f"✗ Import failed: {e}")
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
        from mcbuilder import VQVAE3D, MaskedLatentModel, BLOCKS_ARRAY
        from mcbuilder.text_encoder import TextConditionedTransformer
        
        device = torch.device('cpu')
        
        vqvae = VQVAE3D(
            num_blocks=len(BLOCKS_ARRAY),
            embedding_dim=64,
            num_embeddings=512,
            hidden_dims=[32, 64],
            commitment_cost=0.25
        ).to(device)
        
        print(f"✓ VQ-VAE created: {sum(p.numel() for p in vqvae.parameters()):,} params")
        
        transformer = MaskedLatentModel(
            num_embeddings=512,
            d_model=256,
            nhead=4,
            num_layers=2,
            dim_feedforward=512,
            dropout=0.1
        ).to(device)
        
        print(f"✓ Transformer created: {sum(p.numel() for p in transformer.parameters()):,} params")
        
        print("✓ Testing text-conditioned transformer...")
        text_transformer = TextConditionedTransformer(
            num_embeddings=512,
            d_model=256,
            nhead=4,
            num_layers=2,
            dim_feedforward=512,
            dropout=0.1
        ).to(device)
        
        print(f"✓ Text transformer created: {sum(p.numel() for p in text_transformer.parameters()):,} params")
        
        return True
    except Exception as e:
        print(f"✗ Model test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_forward_pass():
    print("\nTesting forward pass...")
    try:
        from mcbuilder import VQVAE3D, BLOCKS_ARRAY
        from mcbuilder.text_encoder import TextConditionedTransformer
        
        device = torch.device('cpu')
        
        vqvae = VQVAE3D(
            num_blocks=len(BLOCKS_ARRAY),
            embedding_dim=64,
            num_embeddings=512,
            hidden_dims=[32, 64],
            commitment_cost=0.25
        ).to(device)
        
        dummy_input = torch.randint(0, len(BLOCKS_ARRAY), (1, 8, 8, 8), device=device)
        
        with torch.no_grad():
            recon, vq_loss, indices = vqvae(dummy_input)
        
        print(f"✓ VQ-VAE forward pass: input {dummy_input.shape} -> recon {recon.shape}")
        
        text_transformer = TextConditionedTransformer(
            num_embeddings=512,
            d_model=256,
            nhead=4,
            num_layers=2,
            dim_feedforward=512,
            dropout=0.1
        ).to(device)
        
        dummy_latents = torch.randint(0, 512, (1, 64), device=device)
        
        with torch.no_grad():
            logits = text_transformer(dummy_latents, ["a test building"])
        
        print(f"✓ Text transformer forward pass: input {dummy_latents.shape} -> logits {logits.shape}")
        
        return True
    except Exception as e:
        print(f"✗ Forward pass test failed: {e}")
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
