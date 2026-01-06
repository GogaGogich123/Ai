import torch
import numpy as np
from typing import Dict, List, Tuple
from collections import Counter

class BuildAnalyzer:
    def __init__(self):
        self.furniture_blocks = {
            'bed', 'chest', 'crafting_table', 'furnace', 'table', 
            'chair', 'door', 'trapdoor', 'ladder', 'torch', 'lantern',
            'barrel', 'bookshelf', 'enchanting_table', 'anvil'
        }
        
        self.structural_blocks = {
            'stone', 'cobblestone', 'bricks', 'planks', 'log', 'wood',
            'concrete', 'terracotta', 'sandstone', 'prismarine'
        }
        
        self.decorative_blocks = {
            'glass', 'stained_glass', 'wool', 'carpet', 'banner',
            'flower', 'leaves', 'vine', 'fence', 'wall'
        }
    
    def analyze_build(
        self, 
        blocks: np.ndarray, 
        block_names: List[str]
    ) -> Dict:
        if isinstance(blocks, torch.Tensor):
            blocks = blocks.cpu().numpy()
        
        h, w, d = blocks.shape
        total_voxels = h * w * d
        
        block_ids = blocks.flatten()
        non_air = block_ids[block_ids != 0]
        
        if len(non_air) == 0:
            return {
                'size': (h, w, d),
                'total_blocks': 0,
                'block_types': {},
                'categories': {},
                'density': 0.0,
                'height': 0,
                'footprint': (0, 0)
            }
        
        block_counts = Counter(non_air.tolist())
        
        block_type_counts = {}
        for block_id, count in block_counts.items():
            if block_id < len(block_names):
                block_name = block_names[block_id].replace('minecraft:', '')
                block_type_counts[block_name] = count
        
        top_blocks = sorted(block_type_counts.items(), key=lambda x: x[1], reverse=True)[:10]
        
        categories = {
            'structural': 0,
            'decorative': 0,
            'furniture': 0,
            'other': 0
        }
        
        for block_name, count in block_type_counts.items():
            if any(s in block_name for s in self.structural_blocks):
                categories['structural'] += count
            elif any(d in block_name for d in self.decorative_blocks):
                categories['decorative'] += count
            elif any(f in block_name for f in self.furniture_blocks):
                categories['furniture'] += count
            else:
                categories['other'] += count
        
        occupied_height = 0
        for y in range(h):
            if np.any(blocks[y, :, :] != 0):
                occupied_height = y + 1
        
        footprint_x = 0
        footprint_z = 0
        for x in range(w):
            if np.any(blocks[:, x, :] != 0):
                footprint_x += 1
        for z in range(d):
            if np.any(blocks[:, :, z] != 0):
                footprint_z += 1
        
        density = len(non_air) / total_voxels
        
        rooms = self._estimate_rooms(blocks)
        
        return {
            'size': (h, w, d),
            'total_blocks': len(non_air),
            'unique_block_types': len(block_type_counts),
            'top_blocks': top_blocks,
            'categories': categories,
            'density': density,
            'height': occupied_height,
            'footprint': (footprint_x, footprint_z),
            'estimated_rooms': rooms,
            'has_furniture': categories['furniture'] > 0,
            'primary_material': top_blocks[0][0] if top_blocks else 'unknown'
        }
    
    def _estimate_rooms(self, blocks: np.ndarray) -> int:
        h, w, d = blocks.shape
        
        if h < 3 or w < 3 or d < 3:
            return 0
        
        enclosed_spaces = 0
        checked = np.zeros_like(blocks, dtype=bool)
        
        for y in range(1, h - 1):
            for x in range(1, w - 1):
                for z in range(1, d - 1):
                    if blocks[y, x, z] == 0 and not checked[y, x, z]:
                        if self._is_enclosed(blocks, x, y, z, checked):
                            enclosed_spaces += 1
        
        return enclosed_spaces
    
    def _is_enclosed(
        self, 
        blocks: np.ndarray, 
        x: int, 
        y: int, 
        z: int, 
        checked: np.ndarray
    ) -> bool:
        h, w, d = blocks.shape
        
        if x <= 0 or x >= w - 1 or y <= 0 or y >= h - 1 or z <= 0 or z >= d - 1:
            return False
        
        has_walls = (
            blocks[y, x-1, z] != 0 and blocks[y, x+1, z] != 0 and
            blocks[y, x, z-1] != 0 and blocks[y, x, z+1] != 0
        )
        
        has_floor = blocks[y-1, x, z] != 0
        has_ceiling = blocks[y+1, x, z] != 0
        
        checked[y, x, z] = True
        
        return has_walls and has_floor and has_ceiling
    
    def format_analysis_for_prompt(self, analysis: Dict) -> str:
        h, w, d = analysis['size']
        
        prompt = f"""Analyze this Minecraft build structure:

Size: {w}x{h}x{d} blocks
Total blocks used: {analysis['total_blocks']:,}
Density: {analysis['density']:.1%}
Actual height: {analysis['height']} blocks
Footprint: {analysis['footprint'][0]}x{analysis['footprint'][1]} blocks

Block composition:
- Structural blocks: {analysis['categories']['structural']:,}
- Decorative blocks: {analysis['categories']['decorative']:,}
- Furniture/functional: {analysis['categories']['furniture']:,}
- Other: {analysis['categories']['other']:,}

Top materials used:
"""
        
        for block_name, count in analysis['top_blocks'][:5]:
            percentage = (count / analysis['total_blocks']) * 100
            prompt += f"- {block_name}: {count:,} ({percentage:.1f}%)\n"
        
        prompt += f"\nEstimated rooms/spaces: {analysis['estimated_rooms']}\n"
        prompt += f"Has furniture: {'Yes' if analysis['has_furniture'] else 'No'}\n"
        prompt += f"Primary material: {analysis['primary_material']}\n"
        
        return prompt
