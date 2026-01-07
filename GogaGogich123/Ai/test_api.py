import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.absolute()))

from mcbuilder import BuildPasteAPI

def test_api():
    print("Testing BuildPaste API...")
    api = BuildPasteAPI()
    
    print("\nFetching builds list...")
    builds = api.list_builds(limit=10)
    
    print(f"Found {len(builds)} builds:")
    for build in builds[:5]:
        print(f"  - {build.name} ({build.category}): {build.block_count} blocks")
    
    if builds:
        print(f"\nDownloading build: {builds[0].name}")
        build_data = api.download_build(builds[0].build_id)
        
        if build_data:
            print(f"  Size: {build_data.size}")
            print(f"  Total blocks: {len(build_data.blocks)}")
            print(f"  Direction: {build_data.direction}")
            print(f"  NBT entries: {len(build_data.nbt)}")
            print("\nAPI test successful!")
        else:
            print("Failed to download build")
    else:
        print("No builds found")

if __name__ == "__main__":
    test_api()
