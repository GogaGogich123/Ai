#!/usr/bin/env python3
"""
Example script for generating builds with AI descriptions using Gemini.
"""

import sys
import os

GEMINI_API_KEY = "AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw"

def generate_with_description():
    """Generate a build with AI description."""
    
    print("=" * 60)
    print("Minecraft AI Builder - Generate with Description")
    print("=" * 60)
    
    checkpoints_exist = (
        os.path.exists("./checkpoints_improved/improved_vqvae_final.pt") and
        os.path.exists("./checkpoints_diffusion/diffusion_final.pt")
    )
    
    if not checkpoints_exist:
        print("\n⚠️  Checkpoints not found!")
        print("Please train the models first:")
        print("  1. python mcbuilder/train_improved_vqvae.py --epochs 100")
        print("  2. python mcbuilder/train_diffusion.py --epochs 100")
        return 1
    
    examples = [
        {
            "name": "Small House",
            "size": "32,32,32",
            "style": "detailed",
            "lang": "en"
        },
        {
            "name": "Medium Castle",
            "size": "64,64,64",
            "style": "creative",
            "lang": "en"
        },
        {
            "name": "Large Fortress",
            "size": "96,64,96",
            "style": "detailed",
            "lang": "ru"
        }
    ]
    
    print("\nAvailable examples:")
    for i, ex in enumerate(examples, 1):
        print(f"  {i}. {ex['name']} ({ex['size']}) - {ex['style']} style, {ex['lang']}")
    
    print("\nSelect example (1-3) or 'q' to quit: ", end="")
    choice = input().strip()
    
    if choice.lower() == 'q':
        return 0
    
    try:
        idx = int(choice) - 1
        if idx < 0 or idx >= len(examples):
            print("Invalid choice")
            return 1
        
        example = examples[idx]
    except:
        print("Invalid input")
        return 1
    
    cmd = f"""python generate_hq.py \\
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \\
    --diffusion_checkpoint ./checkpoints_diffusion/diffusion_final.pt \\
    --size {example['size']} \\
    --output {example['name'].lower().replace(' ', '_')}.litematic \\
    --name "{example['name']}" \\
    --num_samples 3 \\
    --validate \\
    --gemini_api_key {GEMINI_API_KEY} \\
    --description_style {example['style']} \\
    --description_language {example['lang']}"""
    
    print(f"\n🚀 Generating {example['name']}...\n")
    print(f"Command:\n{cmd}\n")
    
    import subprocess
    result = subprocess.run(cmd, shell=True)
    
    return result.returncode

if __name__ == "__main__":
    sys.exit(generate_with_description())
