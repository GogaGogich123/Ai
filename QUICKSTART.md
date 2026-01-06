# Minecraft AI Builder - Quick Start

## Training Pipeline

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

### 3. Train Transformer (Stage 2)
```bash
python mcbuilder/train_transformer.py \
    --vqvae_checkpoint ./checkpoints/vqvae_final.pt \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints_transformer \
    --batch_size 8 \
    --epochs 50
```

### 4. Generate Builds
```bash
python generate.py \
    --vqvae_checkpoint ./checkpoints/vqvae_final.pt \
    --transformer_checkpoint ./checkpoints_transformer/transformer_final.pt \
    --size 32,32,32 \
    --output my_build.litematic \
    --temperature 1.0 \
    --num_iterations 10
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
