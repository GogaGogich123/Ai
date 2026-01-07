# 🔧 Installation Guide

## Quick Install (Recommended)

### Option 1: Install as Python Package

From the project root directory:

```bash
pip install -e .
```

This installs `mcbuilder` as an editable package, making all imports work correctly.

### Option 2: Manual Setup

If you prefer not to install:

```bash
# Add to PYTHONPATH
export PYTHONPATH="${PYTHONPATH}:/path/to/Ai"

# Or in Python scripts (already included in all scripts):
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.absolute()))
```

## Google Colab Installation

In Colab, after cloning the repository:

```python
# Clone repository
!git clone https://github.com/GogaGogich123/Ai.git
%cd Ai

# Install package
!pip install -e .

# Install dependencies
!pip install -r requirements.txt
```

## Troubleshooting

### "ModuleNotFoundError: No module named 'mcbuilder'"

**Solution 1:** Install the package
```bash
cd /path/to/Ai
pip install -e .
```

**Solution 2:** Run from correct directory
```bash
# Always run from project root
cd /path/to/Ai
python mcbuilder/train_improved_vqvae.py [args]
```

**Solution 3:** Already fixed in scripts!
All training and generation scripts now automatically add the project directory to `sys.path`, so they should work from any location.

### "ImportError" or other import issues

Make sure all dependencies are installed:
```bash
pip install -r requirements.txt
```

### Colab-specific issues

In Colab, make sure to:
1. Clone the repo: `!git clone ...`
2. Change directory: `%cd Ai`
3. Install package: `!pip install -e .`
4. Install dependencies: `!pip install -r requirements.txt`

## Verify Installation

Test that everything works:

```bash
python test_api.py
```

You should see BuildPaste API successfully connecting and listing builds.

## Next Steps

After successful installation, see:
- **[QUICKSTART.md](QUICKSTART.md)** - Training guide
- **[TEXT_TO_BUILD.md](TEXT_TO_BUILD.md)** - Text-to-build guide
- **[colab_train.ipynb](colab_train.ipynb)** - Interactive Colab notebook
