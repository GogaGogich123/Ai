import requests
import json
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
import time

FIREBASE_API_KEY = "AIzaSyBg9qoQQxPQ2vGh7vU_DpTK6pxGU1oXh-Y"
FIRESTORE_ENDPOINT = "https://firestore.googleapis.com/v1/projects/buildpastemod/databases/(default)/documents:runQuery"
BUILD_DOWNLOAD_ENDPOINT = "https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/get/{build_id}"

@dataclass
class BuildMetadata:
    build_id: str
    name: str
    description: str
    category: str
    block_count: int
    premium: bool
    date: Optional[str] = None
    creator_uuid: Optional[str] = None
    thumbnail: Optional[str] = None

@dataclass
class BuildData:
    metadata: BuildMetadata
    size: List[int]
    blocks: List[int]
    data: List[str]
    direction: str
    nbt: Dict[str, str]

class BuildPasteAPI:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })
    
    def list_builds(
        self, 
        category: Optional[str] = None,
        limit: int = 100,
        order_by: str = "date",
        order_direction: str = "DESCENDING"
    ) -> List[BuildMetadata]:
        query = {
            "structuredQuery": {
                "from": [{"collectionId": "builds"}],
                "where": {
                    "compositeFilter": {
                        "op": "AND",
                        "filters": [
                            {
                                "fieldFilter": {
                                    "field": {"fieldPath": "private"},
                                    "op": "EQUAL",
                                    "value": {"booleanValue": False}
                                }
                            },
                            {
                                "fieldFilter": {
                                    "field": {"fieldPath": "published"},
                                    "op": "EQUAL",
                                    "value": {"booleanValue": True}
                                }
                            }
                        ]
                    }
                },
                "orderBy": [
                    {
                        "field": {"fieldPath": order_by},
                        "direction": order_direction
                    }
                ],
                "limit": limit
            }
        }
        
        if category:
            query["structuredQuery"]["where"]["compositeFilter"]["filters"].append({
                "fieldFilter": {
                    "field": {"fieldPath": "category"},
                    "op": "EQUAL",
                    "value": {"stringValue": category}
                }
            })
        
        try:
            response = self.session.post(
                f"{FIRESTORE_ENDPOINT}?key={FIREBASE_API_KEY}",
                json=query,
                timeout=30
            )
            response.raise_for_status()
            
            builds = []
            for item in response.json():
                if 'document' not in item:
                    continue
                    
                doc = item['document']
                fields = doc.get('fields', {})
                
                build_id = doc['name'].split('/')[-1]
                
                def get_field(field_name: str, field_type: str, default=None):
                    field = fields.get(field_name, {})
                    return field.get(field_type, default)
                
                builds.append(BuildMetadata(
                    build_id=build_id,
                    name=get_field('name', 'stringValue', ''),
                    description=get_field('description', 'stringValue', ''),
                    category=get_field('category', 'stringValue', ''),
                    block_count=int(get_field('blockCount', 'integerValue', 0)),
                    premium=get_field('premium', 'booleanValue', False),
                    date=get_field('date', 'stringValue'),
                    creator_uuid=get_field('creatoruuid', 'stringValue'),
                    thumbnail=get_field('thumbnail', 'stringValue')
                ))
            
            return builds
            
        except Exception as e:
            print(f"Error listing builds: {e}")
            return []
    
    def download_build(self, build_id: str, version: str = "1.19.2") -> Optional[BuildData]:
        from .blocks import BLOCK_TO_ID
        
        url = BUILD_DOWNLOAD_ENDPOINT.format(build_id=build_id)
        params = {
            'version': version,
            'member': 'free'
        }
        
        try:
            response = self.session.get(url, params=params, timeout=60)
            response.raise_for_status()
            
            data = response.json()
            raw_blocks = data.get('blocks', [])
            
            if not raw_blocks:
                return None
            
            converted_blocks = []
            unknown_count = 0
            
            for block in raw_blocks:
                if isinstance(block, str):
                    clean_block = block.replace('minecraft:', '')
                    block_id = BLOCK_TO_ID.get(clean_block, None)
                    
                    if block_id is None:
                        unknown_count += 1
                        converted_blocks.append(0)
                    else:
                        converted_blocks.append(block_id)
                elif isinstance(block, (int, float)):
                    block_id = int(block)
                    if block_id < 0 or block_id >= len(BLOCK_TO_ID):
                        unknown_count += 1
                        converted_blocks.append(0)
                    else:
                        converted_blocks.append(block_id)
                else:
                    unknown_count += 1
                    converted_blocks.append(0)
            
            total_blocks = len(converted_blocks)
            if total_blocks == 0:
                return None
            
            unknown_ratio = unknown_count / total_blocks
            if unknown_ratio > 0.5:
                return None
            
            return BuildData(
                metadata=BuildMetadata(
                    build_id=build_id,
                    name="",
                    description="",
                    category="",
                    block_count=len(converted_blocks),
                    premium=False
                ),
                size=data.get('size', [0, 0, 0]),
                blocks=converted_blocks,
                data=data.get('data', []),
                direction=data.get('direction', 'up'),
                nbt=data.get('nbt', {})
            )
            
        except Exception as e:
            print(f"Error downloading build {build_id}: {e}")
            return None
    
    def iter_all_builds(
        self,
        categories: Optional[List[str]] = None,
        min_blocks: int = 800,
        max_blocks: Optional[int] = None,
        exclude_premium: bool = True,
        batch_size: int = 100
    ):
        if categories is None:
            categories = [None]
        
        for category in categories:
            offset = 0
            while True:
                builds = self.list_builds(
                    category=category,
                    limit=batch_size
                )
                
                if not builds:
                    break
                
                for build in builds:
                    if build.block_count < min_blocks:
                        continue
                    if max_blocks and build.block_count > max_blocks:
                        continue
                    if exclude_premium and build.premium:
                        continue
                    
                    yield build
                
                if len(builds) < batch_size:
                    break
                
                time.sleep(0.5)
