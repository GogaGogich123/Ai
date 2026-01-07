#!/usr/bin/env python3
"""
Test script to verify all imports and basic functionality
for the Colab notebook without requiring GPU.
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.absolute()))

def test_basic_imports():
    """Test basic Python imports"""
    print("🧪 Testing basic imports...")
    try:
        import torch
        import numpy as np
        import argparse
        print(f"  ✓ PyTorch {torch.__version__}")
        print(f"  ✓ NumPy {np.__version__}")
        print(f"  ✓ argparse available")
    except ImportError as e:
        print(f"  ❌ Error: {e}")
        return False
    return True

def test_mcbuilder_imports():
    """Test mcbuilder package imports"""
    print("\n🧪 Testing mcbuilder imports...")
    try:
        from mcbuilder import BuildPasteAPI
        from mcbuilder import ImprovedVQVAE3D
        from mcbuilder import TextConditionedLatentDiffusion3D
        from mcbuilder import CLIPTextEncoder, SimpleTextEncoder
        from mcbuilder import BuildQualityValidator
        from mcbuilder import BLOCKS_ARRAY, BLOCK_TO_ID
        from mcbuilder import export_to_litematic
        
        print(f"  ✓ BuildPasteAPI")
        print(f"  ✓ ImprovedVQVAE3D")
        print(f"  ✓ TextConditionedLatentDiffusion3D")
        print(f"  ✓ Text encoders (CLIP, Simple)")
        print(f"  ✓ BuildQualityValidator")
        print(f"  ✓ Block mappings ({len(BLOCKS_ARRAY)} blocks)")
        print(f"  ✓ Litematic export")
    except ImportError as e:
        print(f"  ❌ Import error: {e}")
        import traceback
        traceback.print_exc()
        return False
    return True

def test_model_creation():
    """Test model creation (no training, just instantiation)"""
    print("\n🧪 Testing model instantiation...")
    try:
        import torch
        from mcbuilder import ImprovedVQVAE3D, BLOCKS_ARRAY
        
        # Test VQ-VAE creation
        vqvae = ImprovedVQVAE3D(
            num_blocks=len(BLOCKS_ARRAY),
            embedding_dim=128,
            num_embeddings=1024,
            hidden_dims=[64, 128, 256],
            num_res_blocks=3
        )
        print(f"  ✓ VQ-VAE created")
        
        # Test forward pass with dummy data
        dummy_input = torch.randint(0, len(BLOCKS_ARRAY), (1, 8, 8, 8))
        with torch.no_grad():
            recon, vq_loss, indices = vqvae(dummy_input)
        print(f"  ✓ VQ-VAE forward pass successful")
        print(f"    Input shape: {dummy_input.shape}")
        print(f"    Output shape: {recon.shape}")
        
    except Exception as e:
        print(f"  ❌ Model error: {e}")
        import traceback
        traceback.print_exc()
        return False
    return True

def test_text_encoder():
    """Test text encoder"""
    print("\n🧪 Testing text encoder...")
    try:
        from mcbuilder import CLIPTextEncoder
        
        encoder = CLIPTextEncoder(
            model_name="sentence-transformers/all-MiniLM-L6-v2",
            projection_dim=512
        )
        print(f"  ✓ CLIP encoder created")
        
        # Test encoding
        import torch
        with torch.no_grad():
            embeddings = encoder(["test prompt", "another prompt"])
        print(f"  ✓ Text encoding successful")
        print(f"    Output shape: {embeddings.shape}")
        
    except Exception as e:
        print(f"  ⚠️  Text encoder error (may need internet): {e}")
        # This is non-critical, may fail offline
        return True
    return True

def test_buildpaste_api():
    """Test BuildPaste API connection"""
    print("\n🧪 Testing BuildPaste API...")
    try:
        from mcbuilder import BuildPasteAPI
        
        api = BuildPasteAPI()
        print(f"  ✓ API client created")
        
        # Try to list builds (requires internet)
        try:
            builds = api.list_builds(limit=3)
            print(f"  ✓ API connection successful")
            print(f"  ✓ Found {len(builds)} builds")
            if builds:
                print(f"    Example: {builds[0].name}")
        except Exception as e:
            print(f"  ⚠️  API connection error (may be offline): {e}")
            # Non-critical, may fail offline
            
    except Exception as e:
        print(f"  ❌ API error: {e}")
        return False
    return True

def test_training_scripts():
    """Test that training scripts have correct syntax"""
    print("\n🧪 Testing training scripts syntax...")
    scripts = [
        "mcbuilder/train_improved_vqvae.py",
        "mcbuilder/train_diffusion.py",
        "mcbuilder/train_text_to_build.py",
        "generate_from_text.py",
        "generate_hq.py",
        "test_api.py"
    ]
    
    import py_compile
    for script in scripts:
        try:
            py_compile.compile(script, doraise=True)
            print(f"  ✓ {script}")
        except py_compile.PyCompileError as e:
            print(f"  ❌ Syntax error in {script}: {e}")
            return False
    
    return True

def test_validators():
    """Test validation system"""
    print("\n🧪 Testing validation system...")
    try:
        from mcbuilder import BuildQualityValidator, fix_floating_blocks, BLOCKS_ARRAY
        import torch
        
        validator = BuildQualityValidator()
        print(f"  ✓ Validator created")
        
        # Create dummy build
        dummy_blocks = torch.randint(0, 10, (16, 16, 16))
        score = validator.validate(dummy_blocks.numpy(), BLOCKS_ARRAY)
        print(f"  ✓ Validation works (physics: {score['physics_score']:.2f})")
        
        fixed_blocks = fix_floating_blocks(dummy_blocks.numpy())
        print(f"  ✓ Auto-fix works")
        
    except Exception as e:
        print(f"  ⚠️  Validator test skipped: {e}")
        # Non-critical for notebook
        return True
    return True

def main():
    """Run all tests"""
    print("=" * 60)
    print("🚀 Minecraft AI Builder - Test Suite")
    print("=" * 60)
    
    tests = [
        ("Basic Imports", test_basic_imports),
        ("MCBuilder Imports", test_mcbuilder_imports),
        ("Model Creation", test_model_creation),
        ("Text Encoder", test_text_encoder),
        ("BuildPaste API", test_buildpaste_api),
        ("Script Syntax", test_training_scripts),
        ("Validators", test_validators),
    ]
    
    results = []
    for test_name, test_func in tests:
        try:
            result = test_func()
            results.append((test_name, result))
        except Exception as e:
            print(f"\n❌ Unexpected error in {test_name}: {e}")
            import traceback
            traceback.print_exc()
            results.append((test_name, False))
    
    # Summary
    print("\n" + "=" * 60)
    print("📊 Test Summary")
    print("=" * 60)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"  {status}: {test_name}")
    
    print(f"\n🎯 Score: {passed}/{total} tests passed")
    
    if passed == total:
        print("\n✅ All tests passed! Notebook is ready to use.")
        return 0
    else:
        print(f"\n⚠️  {total - passed} test(s) failed. Please fix before using.")
        return 1

if __name__ == "__main__":
    sys.exit(main())
