# 🎮 Examples: Text-to-Build Generation

Quick examples for generating different types of Minecraft builds from text prompts.

## 🏰 Architecture

### Medieval Castle
```bash
python generate_from_text.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --text_to_build_checkpoint ./checkpoints_text_to_build/text_to_build_final.pt \
    --prompt "medieval stone castle with tall towers and fortified walls" \
    --size 64,48,64 \
    --guidance_scale 8.0 \
    --num_samples 3 \
    --validate \
    --output medieval_castle.litematic
```

### Japanese Pagoda
```bash
python generate_from_text.py \
    --prompt "traditional japanese pagoda with wooden beams and curved roofs" \
    --size 32,48,32 \
    --guidance_scale 7.5 \
    --validate \
    --output pagoda.litematic
```

### Gothic Cathedral
```bash
python generate_from_text.py \
    --prompt "gothic cathedral with stained glass windows and stone arches" \
    --size 48,64,48 \
    --guidance_scale 9.0 \
    --num_samples 5 \
    --validate \
    --output cathedral.litematic
```

## 🏡 Houses & Buildings

### Cozy Cottage
```bash
python generate_from_text.py \
    --prompt "cozy cottage with oak planks stone fireplace and wooden furniture" \
    --size 24,20,24 \
    --guidance_scale 7.0 \
    --validate \
    --output cottage.litematic
```

### Modern House
```bash
python generate_from_text.py \
    --prompt "modern suburban house with white concrete walls and large glass windows" \
    --size 32,24,32 \
    --guidance_scale 7.5 \
    --validate \
    --output modern_house.litematic
```

### Victorian Mansion
```bash
python generate_from_text.py \
    --prompt "victorian mansion with brick walls tall windows and elegant architecture" \
    --size 48,32,48 \
    --guidance_scale 8.0 \
    --num_samples 3 \
    --validate \
    --output mansion.litematic
```

## 🌳 Natural & Fantasy

### Treehouse
```bash
python generate_from_text.py \
    --prompt "fantasy treehouse with wooden platforms bridges and leaf decorations" \
    --size 32,40,32 \
    --guidance_scale 7.5 \
    --validate \
    --output treehouse.litematic
```

### Desert Temple
```bash
python generate_from_text.py \
    --prompt "ancient desert temple with sandstone pillars and hieroglyphs" \
    --size 40,32,40 \
    --guidance_scale 8.5 \
    --validate \
    --output desert_temple.litematic
```

### Underwater Base
```bash
python generate_from_text.py \
    --prompt "underwater research base with glass domes and coral decorations" \
    --size 48,32,48 \
    --guidance_scale 8.0 \
    --num_samples 3 \
    --validate \
    --output underwater_base.litematic
```

## 🏭 Functional Buildings

### Blacksmith Workshop
```bash
python generate_from_text.py \
    --prompt "blacksmith workshop with anvils furnaces and storage chests" \
    --size 20,16,20 \
    --guidance_scale 8.0 \
    --validate \
    --output blacksmith.litematic
```

### Wizard Tower
```bash
python generate_from_text.py \
    --prompt "wizard tower with bookshelves potion brewing stand and enchanting table" \
    --size 16,48,16 \
    --guidance_scale 9.0 \
    --validate \
    --output wizard_tower.litematic
```

### Library
```bash
python generate_from_text.py \
    --prompt "grand library with tall bookshelves reading desks and enchanting area" \
    --size 32,24,32 \
    --guidance_scale 7.5 \
    --validate \
    --output library.litematic
```

## 🎨 Themed Structures

### Sci-Fi Building
```bash
python generate_from_text.py \
    --prompt "futuristic sci-fi building with smooth quartz walls and cyan glass" \
    --size 40,32,40 \
    --guidance_scale 8.5 \
    --num_samples 3 \
    --validate \
    --output scifi_building.litematic
```

### Pirate Ship
```bash
python generate_from_text.py \
    --prompt "wooden pirate ship with sails masts and treasure storage" \
    --size 48,32,24 \
    --guidance_scale 8.0 \
    --validate \
    --output pirate_ship.litematic
```

### Elven Palace
```bash
python generate_from_text.py \
    --prompt "elegant elven palace with white marble arches and nature integration" \
    --size 64,40,64 \
    --guidance_scale 9.0 \
    --num_samples 5 \
    --validate \
    --output elven_palace.litematic
```

## 🎯 Interior Rooms

### Bedroom
```bash
python generate_from_text.py \
    --prompt "cozy bedroom with bed carpets wardrobe and windows with curtains" \
    --size 12,12,12 \
    --guidance_scale 7.0 \
    --validate \
    --output bedroom.litematic
```

### Kitchen
```bash
python generate_from_text.py \
    --prompt "modern kitchen with crafting table furnaces chests and dining area" \
    --size 16,12,16 \
    --guidance_scale 7.5 \
    --validate \
    --output kitchen.litematic
```

### Throne Room
```bash
python generate_from_text.py \
    --prompt "royal throne room with red carpet golden decorations and grand throne" \
    --size 32,20,24 \
    --guidance_scale 8.5 \
    --validate \
    --output throne_room.litematic
```

## 🚀 Advanced Techniques

### High Quality Generation
```bash
python generate_from_text.py \
    --prompt "detailed medieval fortress with stone walls towers and courtyard" \
    --size 64,48,64 \
    --num_inference_steps 100 \
    --guidance_scale 10.0 \
    --num_samples 10 \
    --validate \
    --output fortress_hq.litematic
```

### Creative/Diverse Results
```bash
python generate_from_text.py \
    --prompt "unique fantasy building with magical elements" \
    --size 32,32,32 \
    --guidance_scale 3.0 \
    --num_samples 5 \
    --validate \
    --output fantasy_creative.litematic
```

### Fast Preview
```bash
python generate_from_text.py \
    --prompt "simple wooden house" \
    --size 16,16,16 \
    --num_inference_steps 20 \
    --guidance_scale 5.0 \
    --num_samples 1 \
    --no_validate \
    --output quick_preview.litematic
```

### Reproducible Results
```bash
python generate_from_text.py \
    --prompt "castle with towers" \
    --size 32,32,32 \
    --seed 42 \
    --guidance_scale 7.5 \
    --validate \
    --output castle_seed42.litematic
```

## 📊 Parameter Guide

### Guidance Scale
- **1.0-3.0**: Very creative, may diverge from prompt
- **5.0-7.0**: Balanced, good diversity
- **7.5-10.0**: Follows prompt closely (recommended)
- **10.0+**: Very strict, may lose quality

### Inference Steps
- **20-30**: Fast, lower quality
- **50**: Balanced (default)
- **75-100**: High quality, slower

### Number of Samples
- **1**: Fast, single result
- **3**: Good selection (default)
- **5-10**: Best quality selection

### Size Guidelines
- **Small (16³-24³)**: Fast, interior rooms
- **Medium (32³-48³)**: Buildings, houses
- **Large (64³+)**: Castles, complexes

## 🎨 Batch Generation Script

Create multiple variations:

```bash
#!/bin/bash

VQVAE="./checkpoints_improved/improved_vqvae_final.pt"
TEXT_MODEL="./checkpoints_text_to_build/text_to_build_final.pt"

prompts=(
    "medieval castle with towers"
    "cozy cottage with fireplace"
    "modern glass house"
    "fantasy wizard tower"
    "japanese temple"
)

for i in "${!prompts[@]}"; do
    echo "Generating: ${prompts[$i]}"
    python generate_from_text.py \
        --vqvae_checkpoint "$VQVAE" \
        --text_to_build_checkpoint "$TEXT_MODEL" \
        --prompt "${prompts[$i]}" \
        --size 32,32,32 \
        --validate \
        --output "build_${i}.litematic"
done

echo "Done! Generated ${#prompts[@]} builds."
```

Save as `batch_generate.sh`, make executable with `chmod +x batch_generate.sh`, run with `./batch_generate.sh`.

## 💡 Tips

1. **Be specific**: "stone castle with towers" > "castle"
2. **Mention materials**: oak, stone, glass, concrete
3. **Use Minecraft terms**: planks, cobblestone, bricks
4. **Describe key features**: towers, bridges, windows, rooms
5. **Experiment with guidance**: Try 5.0-10.0 range
6. **Generate multiple**: Use `--num_samples 3-5`
7. **Always validate**: Use `--validate` for best quality

---

Happy building! 🎮✨
