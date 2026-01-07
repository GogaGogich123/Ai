from setuptools import setup, find_packages

setup(
    name="mcbuilder",
    version="1.0.0",
    description="AI-powered Minecraft builder with text-to-build generation",
    author="GogaGogich123",
    packages=find_packages(),
    python_requires=">=3.10",
    install_requires=[
        "torch>=2.0.0",
        "numpy>=1.24.0",
        "requests>=2.31.0",
        "tqdm>=4.65.0",
        "wandb>=0.15.0",
        "einops>=0.7.0",
        "transformers>=4.30.0",
        "sentence-transformers>=2.2.0",
        "pillow>=10.0.0",
        "google-generativeai>=0.3.0",
    ],
)
