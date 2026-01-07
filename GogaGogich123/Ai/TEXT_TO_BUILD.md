# 📝 Text-to-Build Generation

Generate Minecraft builds from text prompts using AI!

## 🎯 Overview

The text-to-build system allows you to create Minecraft structures by simply describing them in natural language:

```bash
python generate_from_text.py \
    --prompt "medieval castle with stone towers and wooden bridges" \
    --output castle.litematic
```

**Result:** A complete Minecraft build matching your description!

## 🏗️ Architecture

The system uses three main components:

1. **VQ-VAE** - Compresses 3D voxels into latent space (32³ → 8³ × 128d)
2. **Text Encoder** - Encodes prompts into embeddings (CLIP or Transformer)
3. **Text-Conditioned Diffusion** - Generates latents conditioned on text via cross-attention

### Cross-Attention Mechanism

```
Text Prompt → Text Encoder → Context Embeddings
                                    ↓
Noisy Latent → UNet3D (with Cross-Attention) → Denoised Latent
                                    ↓
                              VQ-VAE Decoder → Blocks
```

## 🚀 Training Pipeline

### Prerequisites

1. **Trained VQ-VAE** (from Stage 1)
2. **Dataset with descriptions** (generated via Mistral)

### Stage 1: VQ-VAE with Descriptions (8-12 hours)

```bash
python mcbuilder/train_improved_vqvae.py \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100 \
    --generate_descriptions \
    --mistral_api_key YOUR_GEMINI_KEY \
    --description_language en
```

This creates a dataset with AI-generated descriptions for each build.

### Stage 2: Text-Conditioned Diffusion (15-20 hours)

```bash
python mcbuilder/train_text_to_build.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --checkpoint_dir ./checkpoints_text_to_build \
    --cache_dir ./data/cache \
    --text_encoder_type clip \
    --context_dim 512 \
    --model_channels 128 \
    --batch_size 4 \
    --epochs 100 \
    --learning_rate 1e-4 \
    --save_every 10
```

**Parameters:**
- `--text_encoder_type`: `clip` (pretrained) or `simple` (train from scratch)
- `--context_dim`: Dimension of text embeddings (512 recommended)
- `--freeze_text_encoder`: Freeze text encoder (faster, uses less memory)

## 🎮 Generation

### Basic Usage

```bash
python generate_from_text.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --text_to_build_checkpoint ./checkpoints_text_to_build/text_to_build_final.pt \
    --prompt "cozy wooden cottage with fireplace" \
    --output cottage.litematic
```

### Advanced Options

```bash
python generate_from_text.py \
    --prompt "futuristic sci-fi building with glass walls" \
    --size 48,48,48 \
    --num_inference_steps 100 \
    --guidance_scale 7.5 \
    --num_samples 5 \
    --validate \
    --output scifi.litematic
```

**Key Parameters:**

- `--prompt`: Text description of the build
- `--size`: Build dimensions (X,Y,Z)
- `--guidance_scale`: How strongly to follow prompt (1.0-15.0, default: 7.5)
  - Lower = more creative/diverse
  - Higher = closer to prompt
- `--num_inference_steps`: Denoising steps (20-100, default: 50)
- `--num_samples`: Generate N candidates and select best (default: 3)
- `--validate`: Enable physics/interior validation
- `--seed`: Random seed for reproducibility

## 📝 Writing Good Prompts

### Effective Prompts

✅ **Good:**
- "medieval stone castle with tall towers and wooden gates"
- "cozy cottage with oak planks and stone chimney"
- "modern house with glass windows and concrete walls"
- "fantasy treehouse with wooden bridges and leaves"

❌ **Too vague:**
- "house"
- "building"
- "something cool"

### Prompt Tips

1. **Be specific about style:** medieval, modern, fantasy, sci-fi
2. **Mention materials:** stone, wood, glass, concrete, brick
3. **Describe key features:** towers, bridges, windows, rooms
4. **Keep it concise:** 5-15 words work best
5. **Use Minecraft terminology:** blocks, planks, cobblestone

### Example Prompts

**Architecture:**
- "gothic cathedral with stained glass windows and stone arches"
- "japanese pagoda with wooden beams and curved roofs"
- "desert temple with sandstone pillars and hieroglyphs"

**Nature:**
- "enchanted forest with giant mushrooms and glowing flowers"
- "mountain fortress built into cliffs with waterfalls"
- "underwater base with glass domes and coral decorations"

**Functional:**
- "blacksmith workshop with anvils furnaces and storage chests"
- "wizard tower with bookshelves potion stand and enchanting table"
- "cozy bedroom with bed carpets and windows"

## 🎛️ Text Encoder Options

### Option 1: CLIP (Recommended)

**Pros:**
- Pretrained on millions of images/text
- Better understanding of concepts
- No vocabulary building needed
- Works out of the box

**Cons:**
- Slightly larger model
- Requires transformers library

```bash
python mcbuilder/train_text_to_build.py \
    --text_encoder_type clip \
    --clip_model_name sentence-transformers/all-MiniLM-L6-v2 \
    --freeze_text_encoder  # Optional: freeze for speed
```

### Option 2: Simple Transformer

**Pros:**
- Lightweight
- Can be trained end-to-end
- Fully customizable

**Cons:**
- Requires vocabulary building
- Needs more training data
- Less semantic understanding

```bash
python mcbuilder/train_text_to_build.py \
    --text_encoder_type simple \
    --vocab_size 10000 \
    --max_seq_length 77 \
    --text_encoder_layers 6 \
    --build_vocab_first  # Build vocab from dataset
```

## 📊 Training Tips

### Hyperparameters

**For high quality:**
```bash
--model_channels 128 \
--num_res_blocks 2 \
--attention_resolutions 4 8 \
--channel_mult 1 2 4 8 \
--num_heads 8 \
--guidance_scale 7.5
```

**For speed (lower quality):**
```bash
--model_channels 96 \
--num_res_blocks 1 \
--attention_resolutions 8 \
--channel_mult 1 2 4 \
--num_heads 4
```

### Memory Optimization

**Out of memory?**
- Reduce `--batch_size` (try 2 or 1)
- Reduce `--model_channels` (try 96 or 64)
- Use `--freeze_text_encoder`
- Reduce `--context_dim` (try 256)

### Training Time

**Colab Free GPU (T4):**
- CLIP encoder: ~15-20 hours (100 epochs)
- Simple encoder: ~12-15 hours (100 epochs)

**Local GPU (RTX 3090):**
- CLIP encoder: ~8-10 hours
- Simple encoder: ~6-8 hours

## 🎯 Quality Expectations

With full training (100 epochs):

**Prompt Following:**
- ✅ Matches architectural style
- ✅ Uses correct materials
- ✅ Captures key features
- ⚠️ Not perfect - some interpretation

**Build Quality:**
- Physics score: 0.8-0.9 (good support)
- Interior score: 0.5-0.7 (functional rooms)
- Coherence: Structures make sense
- Diversity: Different results per prompt

## 🔧 Troubleshooting

### "Build doesn't match prompt"

- Increase `--guidance_scale` (try 10-15)
- Use more `--num_inference_steps` (try 100)
- Generate more samples with `--num_samples 5`
- Check if training data has similar examples
- Try rephrasing prompt

### "Build quality is poor"

- Enable `--validate` for auto-fix
- Increase `--num_samples` to select best
- Train longer (150-200 epochs)
- Use CLIP encoder instead of simple

### "Generation is slow"

- Reduce `--num_inference_steps` (min 20)
- Reduce `--num_samples` (try 1)
- Disable `--no_validate`
- Use smaller `--size`

### "Out of memory during generation"

- Reduce `--size` (try 24,24,24)
- Generate on CPU (slower)
- Close other applications

## 📚 Examples

### Generate a Castle

```bash
python generate_from_text.py \
    --prompt "medieval stone castle with multiple towers and fortified walls" \
    --size 64,48,64 \
    --guidance_scale 8.0 \
    --num_samples 3 \
    --validate \
    --output castle.litematic
```

### Generate a House

```bash
python generate_from_text.py \
    --prompt "modern suburban house with white walls and large windows" \
    --size 32,24,32 \
    --guidance_scale 7.5 \
    --output house.litematic
```

### Generate Interior

```bash
python generate_from_text.py \
    --prompt "cozy library with bookshelves enchanting table and reading area" \
    --size 16,16,16 \
    --guidance_scale 9.0 \
    --validate \
    --output library.litematic
```

### Batch Generation

```bash
for prompt in "cottage" "tower" "bridge" "temple"; do
    python generate_from_text.py \
        --prompt "medieval $prompt made of stone and wood" \
        --output "${prompt}.litematic"
done
```

## 🔮 Future Improvements

- [ ] Support for longer/more complex prompts
- [ ] Style transfer (combine prompt + reference build)
- [ ] Negative prompts ("castle without towers")
- [ ] Multi-scale text-to-build (unlimited sizes)
- [ ] Fine-tuning on specific styles
- [ ] Interactive prompt refinement

## 💡 Use Cases

**Creative Building:**
- Quick prototypes for complex structures
- Inspiration for manual builds
- Automated city generation

**Map Making:**
- Generate unique structures for adventure maps
- Populate worlds with diverse buildings
- Create themed areas (medieval village, sci-fi city)

**Education:**
- Teach architectural styles
- Demonstrate material combinations
- Experiment with design concepts

---

## 📝 Quick Reference

### Training (Stage 1 + 2)

```bash
# Stage 1: VQ-VAE + Descriptions (8-12h)
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --mistral_api_key YOUR_KEY

# Stage 2: Text-to-Build (15-20h)
python mcbuilder/train_text_to_build.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --text_encoder_type clip \
    --epochs 100
```

### Generation

```bash
python generate_from_text.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --text_to_build_checkpoint ./checkpoints_text_to_build/text_to_build_final.pt \
    --prompt "YOUR DESCRIPTION HERE" \
    --output build.litematic
```

---

**Now you can generate Minecraft builds with just words! 🎮✨**
