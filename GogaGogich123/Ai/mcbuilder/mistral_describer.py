from mistralai import Mistral
from typing import Dict, Optional
import time

class MistralDescriber:
    def __init__(self, api_key: str, model_name: str = "mistral-small-latest"):
        self.client = Mistral(api_key=api_key)
        self.model_name = model_name
        
        self.generation_config = {
            "temperature": 0.7,
            "top_p": 0.95,
            "max_tokens": 500,
        }
    
    def generate_description(
        self, 
        analysis: Dict,
        analysis_prompt: str,
        style: str = "detailed",
        language: str = "en"
    ) -> str:
        try:
            if style == "detailed":
                system_prompt = self._get_detailed_prompt(language)
            elif style == "concise":
                system_prompt = self._get_concise_prompt(language)
            elif style == "creative":
                system_prompt = self._get_creative_prompt(language)
            else:
                system_prompt = self._get_detailed_prompt(language)
            
            full_prompt = f"{system_prompt}\n\n{analysis_prompt}"
            
            response = self.client.chat.complete(
                model=self.model_name,
                messages=[
                    {
                        "role": "user",
                        "content": full_prompt
                    }
                ],
                temperature=self.generation_config["temperature"],
                top_p=self.generation_config["top_p"],
                max_tokens=self.generation_config["max_tokens"]
            )
            
            if response.choices and response.choices[0].message.content:
                return response.choices[0].message.content.strip()
            else:
                return self._fallback_description(analysis, language)
        
        except Exception as e:
            print(f"Error generating description with Mistral: {e}")
            return self._fallback_description(analysis, language)
    
    def _get_detailed_prompt(self, language: str) -> str:
        if language == "ru":
            return """Ты - эксперт по архитектуре Minecraft. На основе технических характеристик постройки, создай детальное и интересное описание.

Описание должно включать:
1. Тип постройки (дом, замок, башня, храм и т.д.)
2. Архитектурный стиль (средневековый, современный, фэнтези и т.д.)
3. Основные материалы и их использование
4. Ключевые особенности (размер, комнаты, декор)
5. Назначение и функциональность

Пиши живо и интересно, как описание в каталоге построек. Длина: 3-5 предложений."""
        else:
            return """You are a Minecraft architecture expert. Based on the technical specifications of a build, create a detailed and engaging description.

The description should include:
1. Build type (house, castle, tower, temple, etc.)
2. Architectural style (medieval, modern, fantasy, etc.)
3. Primary materials and their usage
4. Key features (size, rooms, decoration)
5. Purpose and functionality

Write in an engaging style, like a catalog description. Length: 3-5 sentences."""
    
    def _get_concise_prompt(self, language: str) -> str:
        if language == "ru":
            return """Создай краткое описание постройки Minecraft (1-2 предложения). Укажи тип постройки, основной материал и размер."""
        else:
            return """Create a concise Minecraft build description (1-2 sentences). Mention the build type, primary material, and size."""
    
    def _get_creative_prompt(self, language: str) -> str:
        if language == "ru":
            return """Ты - творческий писатель. Создай вдохновляющее и атмосферное описание постройки Minecraft, как будто это место из фэнтезийной истории. Сделай его запоминающимся и красивым."""
        else:
            return """You are a creative writer. Create an inspiring and atmospheric description of this Minecraft build, as if it's a location from a fantasy story. Make it memorable and beautiful."""
    
    def _fallback_description(self, analysis: Dict, language: str) -> str:
        size = analysis.get('size') or analysis.get('dimensions', (32, 32, 32))
        h, w, d = size
        blocks = analysis.get('total_blocks', 0)
        material = analysis.get('primary_material', 'various materials')
        
        if language == "ru":
            return f"Постройка размером {w}x{h}x{d} блоков, использовано {blocks:,} блоков. Основной материал: {material}. " \
                   f"Содержит {analysis['estimated_rooms']} комнат{'у' if analysis['estimated_rooms'] == 1 else ''}."
        else:
            return f"A build of {w}x{h}x{d} blocks using {blocks:,} blocks. Primary material: {material}. " \
                   f"Contains {analysis['estimated_rooms']} room{'s' if analysis['estimated_rooms'] != 1 else ''}."
    
    def generate_multiple_descriptions(
        self,
        analysis: Dict,
        analysis_prompt: str,
        styles: list = ["detailed", "concise", "creative"],
        language: str = "en"
    ) -> Dict[str, str]:
        descriptions = {}
        
        for style in styles:
            description = self.generate_description(
                analysis, 
                analysis_prompt, 
                style=style, 
                language=language
            )
            descriptions[style] = description
            time.sleep(0.5)
        
        return descriptions
    
    def enhance_user_description(
        self, 
        user_description: str, 
        analysis: Dict,
        language: str = "en"
    ) -> str:
        try:
            if language == "ru":
                prompt = f"""Улучши это описание постройки Minecraft, добавив технические детали:

Исходное описание: {user_description}

Технические характеристики:
- Размер: {analysis['size'][0]}x{analysis['size'][1]}x{analysis['size'][2]}
- Блоков: {analysis['total_blocks']:,}
- Комнат: {analysis['estimated_rooms']}
- Основной материал: {analysis.get('primary_material', 'unknown')}

Создай улучшенную версию описания, сохраняя стиль автора, но добавляя важные детали."""
            else:
                prompt = f"""Enhance this Minecraft build description by adding technical details:

Original description: {user_description}

Technical specs:
- Size: {analysis['size'][0]}x{analysis['size'][1]}x{analysis['size'][2]}
- Blocks: {analysis['total_blocks']:,}
- Rooms: {analysis['estimated_rooms']}
- Primary material: {analysis.get('primary_material', 'unknown')}

Create an enhanced version that preserves the author's style while adding important details."""
            
            response = self.client.chat.complete(
                model=self.model_name,
                messages=[
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=self.generation_config["temperature"],
                top_p=self.generation_config["top_p"],
                max_tokens=self.generation_config["max_tokens"]
            )
            
            if response.choices and response.choices[0].message.content:
                return response.choices[0].message.content.strip()
            else:
                return user_description
        
        except Exception as e:
            print(f"Error enhancing description: {e}")
            return user_description
