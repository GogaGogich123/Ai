#!/usr/bin/env python3
"""
Generate AI descriptions for all builds in the dataset cache.
This creates a text-to-build dataset for future training.
"""

import argparse
import torch
from pathlib import Path
from tqdm import tqdm
import time

from mcbuilder.buildpaste_api import BuildPasteAPI
from mcbuilder.build_analyzer import BuildAnalyzer
from mcbuilder.gemini_describer import GeminiDescriber
from mcbuilder.blocks import BLOCKS_ARRAY
import numpy as np

def generate_descriptions_for_dataset(
    cache_dir: str,
    gemini_api_key: str,
    language: str = "en",
    style: str = "detailed",
    limit: int = None,
    delay: float = 1.0
):
    cache_path = Path(cache_dir)
    builds_cache = cache_path / "builds"
    descriptions_cache = cache_path / "descriptions"
    descriptions_cache.mkdir(exist_ok=True)
    
    if not builds_cache.exists():
        print(f"Error: Builds cache not found at {builds_cache}")
        print("Please run training first to download builds.")
        return
    
    analyzer = BuildAnalyzer()
    describer = GeminiDescriber(gemini_api_key)
    block_names = ["minecraft:" + block for block in BLOCKS_ARRAY]
    
    build_files = sorted(list(builds_cache.glob("*.npz")))
    
    if limit:
        build_files = build_files[:limit]
    
    print(f"Found {len(build_files)} builds in cache")
    print(f"Generating {language} descriptions in {style} style...")
    print(f"Rate limit delay: {delay}s between requests\n")
    
    generated = 0
    skipped = 0
    errors = 0
    
    for build_file in tqdm(build_files, desc="Processing builds"):
        build_id = build_file.stem
        desc_path = descriptions_cache / f"{build_id}.txt"
        
        if desc_path.exists():
            skipped += 1
            continue
        
        try:
            data = np.load(build_file, allow_pickle=True)
            blocks = data['blocks']
            
            metadata_json = data.get('metadata', None)
            build_name = "Unknown"
            build_category = "Unknown"
            
            if metadata_json:
                import json
                metadata = json.loads(str(metadata_json))
                build_name = metadata.get('name', 'Unknown')
                build_category = metadata.get('category', 'Unknown')
            
            blocks_tensor = torch.from_numpy(blocks)
            analysis = analyzer.analyze_build(blocks_tensor, block_names)
            
            analysis_prompt = analyzer.format_analysis_for_prompt(analysis)
            
            enhanced_prompt = f"{analysis_prompt}\n\nOriginal name: {build_name}\nCategory: {build_category}\n"
            
            description = describer.generate_description(
                analysis,
                enhanced_prompt,
                style=style,
                language=language
            )
            
            with open(desc_path, 'w', encoding='utf-8') as f:
                f.write(description)
            
            generated += 1
            
            time.sleep(delay)
        
        except Exception as e:
            print(f"\nError processing {build_id}: {e}")
            errors += 1
            continue
    
    print(f"\n{'='*60}")
    print(f"Summary:")
    print(f"  Generated: {generated}")
    print(f"  Skipped (already cached): {skipped}")
    print(f"  Errors: {errors}")
    print(f"  Total descriptions: {generated + skipped}")
    print(f"{'='*60}")
    
    if generated > 0:
        print(f"\n✓ Descriptions saved to: {descriptions_cache}")
        print(f"✓ Ready for text-to-build training!")

def preview_descriptions(cache_dir: str, num_samples: int = 5):
    descriptions_cache = Path(cache_dir) / "descriptions"
    builds_cache = Path(cache_dir) / "builds"
    
    if not descriptions_cache.exists():
        print("No descriptions found")
        return
    
    desc_files = sorted(list(descriptions_cache.glob("*.txt")))[:num_samples]
    
    print(f"\n{'='*60}")
    print(f"Sample Descriptions ({len(desc_files)} shown):")
    print(f"{'='*60}\n")
    
    for desc_file in desc_files:
        build_id = desc_file.stem
        
        with open(desc_file, 'r', encoding='utf-8') as f:
            description = f.read().strip()
        
        build_file = builds_cache / f"{build_id}.npz"
        if build_file.exists():
            data = np.load(build_file, allow_pickle=True)
            metadata_json = data.get('metadata', None)
            if metadata_json:
                import json
                metadata = json.loads(str(metadata_json))
                build_name = metadata.get('name', 'Unknown')
                print(f"Build: {build_name} ({build_id})")
            else:
                print(f"Build ID: {build_id}")
        else:
            print(f"Build ID: {build_id}")
        
        print(f"Description: {description}\n")
        print("-" * 60 + "\n")

def main():
    parser = argparse.ArgumentParser(description="Generate AI descriptions for dataset builds")
    parser.add_argument('--cache_dir', type=str, default='./data/cache', help='Dataset cache directory')
    parser.add_argument('--gemini_api_key', type=str, required=True, help='Gemini API key')
    parser.add_argument('--language', type=str, default='en', choices=['en', 'ru'], help='Description language')
    parser.add_argument('--style', type=str, default='detailed', choices=['detailed', 'concise', 'creative'], help='Description style')
    parser.add_argument('--limit', type=int, default=None, help='Limit number of builds to process')
    parser.add_argument('--delay', type=float, default=1.0, help='Delay between API calls (seconds)')
    parser.add_argument('--preview', action='store_true', help='Preview existing descriptions')
    parser.add_argument('--num_preview', type=int, default=5, help='Number of descriptions to preview')
    
    args = parser.parse_args()
    
    if args.preview:
        preview_descriptions(args.cache_dir, args.num_preview)
    else:
        generate_descriptions_for_dataset(
            args.cache_dir,
            args.gemini_api_key,
            args.language,
            args.style,
            args.limit,
            args.delay
        )

if __name__ == "__main__":
    main()
