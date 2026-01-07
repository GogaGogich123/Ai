import torch
import torch.nn as nn
import numpy as np
from typing import Dict, Tuple, List
from dataclasses import dataclass

@dataclass
class ValidationResult:
    valid: bool
    issues: List[str]
    score: float

class PhysicsValidator(nn.Module):
    def __init__(self):
        super().__init__()
        
        self.solid_blocks = {
            'stone', 'cobblestone', 'planks', 'bricks', 'log', 'wood',
            'concrete', 'terracotta', 'wool', 'glass', 'obsidian'
        }
    
    def check_support(self, blocks: torch.Tensor, block_names: List[str]) -> ValidationResult:
        issues = []
        
        blocks_np = blocks.cpu().numpy()
        h, w, d = blocks_np.shape
        
        unsupported_count = 0
        total_blocks = 0
        
        for y in range(1, h):
            for x in range(w):
                for z in range(d):
                    block_id = blocks_np[y, x, z]
                    
                    if block_id == 0:
                        continue
                    
                    total_blocks += 1
                    block_name = block_names[block_id] if block_id < len(block_names) else 'air'
                    
                    is_solid = any(solid in block_name for solid in self.solid_blocks)
                    
                    if is_solid:
                        below_id = blocks_np[y-1, x, z]
                        if below_id == 0:
                            has_adjacent_support = False
                            for dx, dz in [(-1,0), (1,0), (0,-1), (0,1)]:
                                nx, nz = x + dx, z + dz
                                if 0 <= nx < w and 0 <= nz < d:
                                    if blocks_np[y, nx, nz] != 0:
                                        has_adjacent_support = True
                                        break
                            
                            if not has_adjacent_support:
                                unsupported_count += 1
        
        if total_blocks == 0:
            return ValidationResult(False, ["Build is empty"], 0.0)
        
        support_ratio = 1.0 - (unsupported_count / total_blocks)
        
        if unsupported_count > total_blocks * 0.1:
            issues.append(f"Too many floating blocks: {unsupported_count}/{total_blocks}")
        
        return ValidationResult(
            valid=support_ratio > 0.9,
            issues=issues,
            score=support_ratio
        )

class InteriorValidator(nn.Module):
    def __init__(self):
        super().__init__()
        
        self.furniture_blocks = {
            'bed', 'chest', 'crafting_table', 'furnace', 'table', 
            'chair', 'door', 'trapdoor', 'ladder', 'torch', 'lantern'
        }
    
    def check_interior(self, blocks: torch.Tensor, block_names: List[str]) -> ValidationResult:
        issues = []
        
        blocks_np = blocks.cpu().numpy()
        h, w, d = blocks_np.shape
        
        enclosed_spaces = self._find_enclosed_spaces(blocks_np)
        
        if len(enclosed_spaces) == 0:
            return ValidationResult(
                valid=False,
                issues=["No enclosed interior spaces found"],
                score=0.0
            )
        
        furnished_rooms = 0
        total_rooms = len(enclosed_spaces)
        
        for space in enclosed_spaces:
            has_furniture = False
            for y, x, z in space:
                block_id = blocks_np[y, x, z]
                if block_id < len(block_names):
                    block_name = block_names[block_id]
                    if any(furniture in block_name for furniture in self.furniture_blocks):
                        has_furniture = True
                        break
            
            if has_furniture:
                furnished_rooms += 1
        
        furniture_ratio = furnished_rooms / total_rooms if total_rooms > 0 else 0
        
        if furniture_ratio < 0.5:
            issues.append(f"Only {furnished_rooms}/{total_rooms} rooms have furniture")
        
        return ValidationResult(
            valid=furniture_ratio > 0.5,
            issues=issues,
            score=furniture_ratio
        )
    
    def _find_enclosed_spaces(self, blocks: np.ndarray) -> List[List[Tuple[int, int, int]]]:
        h, w, d = blocks.shape
        visited = np.zeros_like(blocks, dtype=bool)
        spaces = []
        
        def is_enclosed(y, x, z):
            if blocks[y, x, z] != 0:
                return False
            
            if y == 0 or y == h-1 or x == 0 or x == w-1 or z == 0 or z == d-1:
                return False
            
            return True
        
        def flood_fill(start_y, start_x, start_z):
            space = []
            stack = [(start_y, start_x, start_z)]
            touches_boundary = False
            
            while stack and len(space) < 1000:
                y, x, z = stack.pop()
                
                if visited[y, x, z]:
                    continue
                
                if blocks[y, x, z] != 0:
                    continue
                
                visited[y, x, z] = True
                space.append((y, x, z))
                
                if y == 0 or y == h-1 or x == 0 or x == w-1 or z == 0 or z == d-1:
                    touches_boundary = True
                
                for dy, dx, dz in [(-1,0,0), (1,0,0), (0,-1,0), (0,1,0), (0,0,-1), (0,0,1)]:
                    ny, nx, nz = y + dy, x + dx, z + dz
                    if 0 <= ny < h and 0 <= nx < w and 0 <= nz < d:
                        if not visited[ny, nx, nz]:
                            stack.append((ny, nx, nz))
            
            return space if not touches_boundary and len(space) >= 8 else []
        
        for y in range(1, h-1):
            for x in range(1, w-1):
                for z in range(1, d-1):
                    if not visited[y, x, z] and is_enclosed(y, x, z):
                        space = flood_fill(y, x, z)
                        if space:
                            spaces.append(space)
        
        return spaces

class BuildQualityValidator:
    def __init__(self):
        self.physics_validator = PhysicsValidator()
        self.interior_validator = InteriorValidator()
    
    def validate(self, blocks: torch.Tensor, block_names: List[str]) -> Dict[str, ValidationResult]:
        results = {}
        
        results['physics'] = self.physics_validator.check_support(blocks, block_names)
        results['interior'] = self.interior_validator.check_interior(blocks, block_names)
        
        return results
    
    def get_overall_score(self, results: Dict[str, ValidationResult]) -> float:
        weights = {
            'physics': 0.6,
            'interior': 0.4
        }
        
        score = 0.0
        for key, weight in weights.items():
            if key in results:
                score += results[key].score * weight
        
        return score

def fix_floating_blocks(blocks: torch.Tensor) -> torch.Tensor:
    blocks = blocks.clone()
    blocks_np = blocks.cpu().numpy()
    h, w, d = blocks_np.shape
    
    changed = True
    iterations = 0
    max_iterations = 10
    
    while changed and iterations < max_iterations:
        changed = False
        iterations += 1
        
        for y in range(h-2, 0, -1):
            for x in range(w):
                for z in range(d):
                    block_id = blocks_np[y, x, z]
                    
                    if block_id == 0:
                        continue
                    
                    below = blocks_np[y-1, x, z]
                    
                    if below == 0:
                        has_support = False
                        
                        for dy in range(y-1, -1, -1):
                            if blocks_np[dy, x, z] != 0:
                                has_support = True
                                break
                        
                        if not has_support:
                            for dx, dz in [(-1,0), (1,0), (0,-1), (0,1)]:
                                nx, nz = x + dx, z + dz
                                if 0 <= nx < w and 0 <= nz < d:
                                    if blocks_np[y, nx, nz] != 0:
                                        has_support = True
                                        break
                        
                        if not has_support:
                            blocks_np[y, x, z] = 0
                            changed = True
    
    return torch.from_numpy(blocks_np).to(blocks.device)
