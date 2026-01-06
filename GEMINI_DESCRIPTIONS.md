# 🤖 AI-Generated Build Descriptions with Gemini

Автоматическая генерация описаний для построек Minecraft с использованием Google Gemini API.

## ✨ Возможности

- 📝 **Автоматический анализ** постройки (размер, материалы, комнаты)
- 🤖 **AI-описания** через Gemini API (3 стиля: detailed, concise, creative)
- 🌍 **Мультиязычность** (английский и русский)
- 🎨 **Кастомизация** стиля и детальности описаний
- 📊 **Статистика** построек (блоки, материалы, плотность)

## 🚀 Быстрый старт

### 1. Установка зависимостей

```bash
pip install google-generativeai
```

### 2. Генерация с описанием

```bash
python generate_hq.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --diffusion_checkpoint ./checkpoints_diffusion/diffusion_final.pt \
    --size 32,32,32 \
    --output castle.litematic \
    --validate \
    --gemini_api_key YOUR_API_KEY \
    --description_style detailed \
    --description_language en
```

### 3. Интерактивный скрипт

```bash
python generate_with_description.py
```

## 📋 Параметры

### Gemini API

- `--gemini_api_key KEY` - API ключ Gemini (обязательно)
- `--description_style STYLE` - Стиль описания:
  - `detailed` - Детальное (3-5 предложений)
  - `concise` - Краткое (1-2 предложения)
  - `creative` - Творческое/атмосферное
- `--description_language LANG` - Язык:
  - `en` - Английский
  - `ru` - Русский
- `--describe_variants` - Генерировать описания и для вариантов

## 🎨 Примеры

### Детальное описание (English)

```bash
python generate_hq.py \
    --size 32,32,32 \
    --validate \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style detailed \
    --description_language en \
    --output detailed_house.litematic
```

**Результат:**
```
A charming medieval cottage built with oak planks and cobblestone, 
featuring multiple cozy rooms with wooden furniture. The 32x32x32 
structure showcases traditional craftsmanship with decorative glass 
windows and a sturdy stone foundation. Contains 3 functional rooms 
including a furnished living area and bedroom.
```

### Краткое описание (Русский)

```bash
python generate_hq.py \
    --size 64,64,64 \
    --validate \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style concise \
    --description_language ru \
    --output castle.litematic
```

**Результат:**
```
Средневековый замок 64x64x64 из каменных блоков с 8 комнатами 
и оборонительными башнями.
```

### Творческое описание

```bash
python generate_hq.py \
    --size 96,64,96 \
    --hierarchical \
    --validate \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style creative \
    --description_language en \
    --output fortress.litematic
```

**Результат:**
```
Rising from the earth like a sentinel of ages past, this magnificent 
stone fortress stands as a testament to architectural mastery. Its 
weathered walls tell stories of countless generations, while torch-lit 
chambers within offer warm refuge from the world beyond. A place where 
legends are born and ancient secrets sleep.
```

### Множественные варианты с описаниями

```bash
python generate_hq.py \
    --size 32,32,32 \
    --validate \
    --generate_multiple 3 \
    --describe_variants \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style detailed \
    --output house.litematic
```

Создаст:
- `house.litematic` - с детальным описанием
- `house_variant_1.litematic` - с кратким описанием
- `house_variant_2.litematic` - с кратким описанием
- `house_variant_3.litematic` - с кратким описанием

## 📊 Анализ построек

Система автоматически анализирует:

### Структурная информация
- Размер постройки (H x W x D)
- Общее количество блоков
- Плотность заполнения
- Фактическая высота
- Площадь основания

### Материалы
- Топ-10 используемых блоков
- Процентное соотношение
- Основной материал
- Категории (структурные, декоративные, мебель)

### Функциональность
- Количество комнат
- Наличие мебели
- Функциональные элементы

## 🛠️ API Reference

### BuildAnalyzer

```python
from mcbuilder import BuildAnalyzer

analyzer = BuildAnalyzer()
analysis = analyzer.analyze_build(blocks, block_names)

# Результат:
{
    'size': (32, 32, 32),
    'total_blocks': 5432,
    'unique_block_types': 15,
    'top_blocks': [('oak_planks', 1200), ...],
    'categories': {'structural': 3000, 'decorative': 800, ...},
    'density': 0.53,
    'height': 28,
    'footprint': (30, 30),
    'estimated_rooms': 4,
    'has_furniture': True,
    'primary_material': 'oak_planks'
}
```

### GeminiDescriber

```python
from mcbuilder import GeminiDescriber

describer = GeminiDescriber(api_key="YOUR_KEY")

# Генерация описания
description = describer.generate_description(
    analysis,
    analysis_prompt,
    style="detailed",  # or "concise", "creative"
    language="en"      # or "ru"
)

# Множественные стили
descriptions = describer.generate_multiple_descriptions(
    analysis,
    analysis_prompt,
    styles=["detailed", "concise", "creative"],
    language="en"
)

# Улучшение пользовательского описания
enhanced = describer.enhance_user_description(
    "My cool castle",
    analysis,
    language="en"
)
```

## 🎯 Лучшие практики

### Для детального описания:
- Используйте `--validate` для точной статистики
- Генерируйте несколько кандидатов (`--num_samples 5`)
- Стиль `detailed` + английский язык

### Для быстрых превью:
- Стиль `concise`
- Отключите `--describe_variants`
- Меньше candidates (`--num_samples 1`)

### Для творческих проектов:
- Стиль `creative`
- Большие постройки (`--hierarchical`)
- Высокое качество (`--validate`)

## 💡 Советы

1. **API ключ**: Получите бесплатно на [Google AI Studio](https://makersuite.google.com/app/apikey)
2. **Лимиты**: Gemini API имеет rate limits - используйте паузы между запросами
3. **Качество**: Лучшие описания получаются для validated построек
4. **Язык**: Русский язык работает отлично для всех стилей
5. **Варианты**: Используйте `--describe_variants` только для важных построек

## 🐛 Troubleshooting

**Ошибка API:**
```
Error generating description with Gemini: ...
```
- Проверьте API ключ
- Проверьте интернет соединение
- Проверьте квоты API

**Некорректное описание:**
- Попробуйте другой стиль
- Используйте `--validate` для точной статистики
- Увеличьте `--num_samples` для лучшего качества

**Описание на английском вместо русского:**
- Убедитесь: `--description_language ru`
- Проверьте версию `google-generativeai`

## 📚 Примеры интеграции

### Python скрипт

```python
from mcbuilder import (
    BuildAnalyzer, GeminiDescriber,
    ImprovedVQVAE3D, LatentDiffusion3D
)

# Загрузка моделей...
# Генерация постройки...

# Анализ
analyzer = BuildAnalyzer()
analysis = analyzer.analyze_build(blocks, block_names)

# Генерация описания
describer = GeminiDescriber("YOUR_KEY")
description = describer.generate_description(
    analysis,
    analyzer.format_analysis_for_prompt(analysis),
    style="detailed",
    language="en"
)

print(description)
```

### Batch генерация

```bash
#!/bin/bash

for i in {1..10}; do
    python generate_hq.py \
        --size 32,32,32 \
        --validate \
        --gemini_api_key YOUR_KEY \
        --description_style detailed \
        --output "build_$i.litematic"
    
    sleep 2  # Rate limiting
done
```

## 🔮 Планы

- [ ] Кэширование описаний
- [ ] Поддержка других LLM (Claude, GPT-4)
- [ ] Генерация по описанию (text→build)
- [ ] Улучшение анализа интерьеров
- [ ] Распознавание архитектурных стилей

---

**Теперь ваши постройки получают профессиональные описания автоматически! 🤖✨**
