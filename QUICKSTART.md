# Minecraft AI Builder - Quick Start

## Two Training Pipelines

### Option A: BASIC (Faster, Good Quality)
- VQ-VAE → Text-Conditioned Transformer
- Training time: ~8-12 hours
- Quality: Good

### Option B: HIGH-QUALITY (Slower, Best Quality) ⭐ RECOMMENDED
- Improved VQ-VAE → Latent Diffusion
- Training time: ~20-28 hours  
- Quality: Excellent
- See **[HQ_PIPELINE.md](HQ_PIPELINE.md)** for details

---

## Basic Pipeline (Option A)

### 1. Test API Connection
```bash
python test_api.py
```

### 2. Train VQ-VAE (Stage 1)
```bash
python mcbuilder/train_vqvae.py \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints \
    --chunk_size 32 \
    --batch_size 4 \
    --epochs 50
```

### 3. Train Text-Conditioned Transformer (Stage 2)
```bash
python mcbuilder/train_text_conditioned.py \
    --vqvae_checkpoint ./checkpoints/vqvae_final.pt \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints_text \
    --batch_size 8 \
    --epochs 50
```

### 4. Generate from Text
```bash
python generate_text.py \
    --vqvae_checkpoint ./checkpoints/vqvae_final.pt \
    --transformer_checkpoint ./checkpoints_text/text_transformer_final.pt \
    --prompt "a cozy medieval cottage" \
    --size 32,32,32 \
    --output my_build.litematic
```

---

## High-Quality Pipeline (Option B) ⭐

See **[HQ_PIPELINE.md](HQ_PIPELINE.md)** for complete guide.

**Quick commands:**

```bash
# Stage 1: Improved VQ-VAE
python mcbuilder/train_improved_vqvae.py \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100

# Stage 2: Latent Diffusion
python mcbuilder/train_diffusion.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --checkpoint_dir ./checkpoints_diffusion \
    --epochs 100

# Generate with validation
python generate_hq.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --diffusion_checkpoint ./checkpoints_diffusion/diffusion_final.pt \
    --validate \
    --num_samples 5 \
    --output castle.litematic
```

## Google Colab

For free GPU training:
1. Open `colab_train.ipynb` in Google Colab
2. Run cells in order
3. Download checkpoints when done

## Key Parameters

**VQ-VAE Training:**
- `chunk_size`: Size of chunks (32x32x32 recommended)
- `embedding_dim`: Latent dimension (64 default)
- `num_embeddings`: Codebook size (512 default)
- `min_blocks`: Minimum blocks per build (800 default)

**Transformer Training:**
- `d_model`: Model dimension (512 default)
- `num_layers`: Transformer layers (12 default)
- `mask_ratio`: Masking ratio for training (0.15 default)

**Generation:**
- `temperature`: Sampling temperature (1.0 = default, lower = more conservative)
- `num_iterations`: Refinement iterations (10 default)
- `top_k`: Top-k sampling (50 default)
