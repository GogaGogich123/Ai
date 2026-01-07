from setuptools import setup, find_packages

setup(
    name="mcbuilder",
    version="1.0.0",
    packages=find_packages(),
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
        "mistralai>=1.0.0",
    ],
    python_requires=">=3.10",
    author="GogaGogich123",
    description="Minecraft AI Builder - Generate builds from text using neural networks",
    long_description=open("README.md", encoding="utf-8").read(),
    long_description_content_type="text/markdown",
)
