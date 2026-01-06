# 📝 Dataset Description Generation

Автоматическая генерация AI-описаний для построек из датасета BuildPaste. Это создаёт **text-to-build датасет** для будущего обучения моделей генерации по текстовым промптам.

## 🎯 Зачем это нужно?

При обучении модели скачиваются постройки из BuildPaste API. Теперь для каждой постройки можно автоматически сгенерировать детальное описание через Gemini API.

**Результат:** Датасет с парами (постройка, описание) готовый для text-to-build обучения!

```
./data/cache/
  ├── builds/
  │   ├── build_001.npz          ← постройка (3D voxels)
  │   ├── build_002.npz
  │   └── ...
  └── descriptions/
      ├── build_001.txt          ← AI описание
      ├── build_002.txt
      └── ...
```

## 🚀 Два способа использования

### Способ 1: Во время обучения (Рекомендую)

Генерировать описания автоматически при первой загрузке построек:

```bash
# Stage 1: VQ-VAE с генерацией описаний
python mcbuilder/train_improved_vqvae.py \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru
```

**Что происходит:**
1. Скачивается постройка из BuildPaste
2. Анализируется структура (блоки, материалы, комнаты)
3. Gemini генерирует описание
4. Описание сохраняется в `./data/cache/descriptions/`
5. Постройка используется для обучения

**Преимущества:**
- Всё автоматически
- Описания создаются один раз
- Кэшируются навсегда

**Недостатки:**
- Обучение идёт медленнее (Gemini API ~1 сек на запрос)
- Нужен API ключ сразу

### Способ 2: После обучения (Batch режим)

Сначала обучаешь модель, потом генерируешь описания для уже скачанных построек:

```bash
# 1. Обычное обучение (без описаний)
python mcbuilder/train_improved_vqvae.py \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100

# 2. Генерация описаний для всего кэша
python generate_dataset_descriptions.py \
    --cache_dir ./data/cache \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --language ru \
    --style detailed \
    --delay 1.0
```

**Что происходит:**
1. Скрипт находит все .npz файлы в кэше
2. Для каждого анализирует постройку
3. Генерирует описание через Gemini
4. Сохраняет в `./data/cache/descriptions/`

**Преимущества:**
- Обучение идёт с нормальной скоростью
- Можно контролировать процесс
- Можно остановить и продолжить

**Недостатки:**
- Два шага вместо одного

## 📊 Параметры генерации

### Training скрипты (train_improved_vqvae.py, train_diffusion.py)

```bash
--generate_descriptions      # Включить генерацию описаний
--gemini_api_key KEY         # API ключ Gemini
--description_language LANG  # Язык: en или ru
```

### Batch генератор (generate_dataset_descriptions.py)

```bash
--cache_dir PATH            # Путь к кэшу (default: ./data/cache)
--gemini_api_key KEY        # API ключ Gemini (обязательно)
--language LANG             # en или ru (default: en)
--style STYLE               # detailed/concise/creative (default: detailed)
--limit N                   # Ограничить N построек (для тестирования)
--delay SECONDS             # Задержка между запросами (default: 1.0)
--preview                   # Просмотр существующих описаний
--num_preview N             # Сколько показать (default: 5)
```

## 🎨 Стили описаний

### Detailed (Рекомендую для датасета)
**Длина:** 3-5 предложений  
**Содержит:**
- Тип постройки и архитектурный стиль
- Основные материалы
- Ключевые особенности
- Функциональность

**Пример (English):**
```
A charming medieval cottage built primarily with oak planks and 
cobblestone, spanning 32x28x32 blocks. The structure features 4 
well-furnished rooms including a cozy living area with crafting 
table and chests. Decorative glass windows and wooden doors create 
a welcoming atmosphere, while torches provide warm interior lighting.
```

**Пример (Russian):**
```
Уютный средневековый домик из дубовых досок и булыжника размером 
32x28x32 блока. Постройка включает 4 обустроенные комнаты с мебелью, 
включая гостиную с верстаком и сундуками. Стеклянные окна и деревянные 
двери создают уютную атмосферу, а факелы обеспечивают освещение.
```

### Concise (Быстрая генерация)
**Длина:** 1-2 предложения  
**Содержит:** Тип, материал, размер

**Пример:**
```
Medieval stone castle 64x64x64 with 8 rooms and defensive towers.
```

### Creative (Атмосферный)
**Длина:** 3-4 предложения  
**Стиль:** Storytelling, фэнтези

**Пример:**
```
Rising from the earth like a sentinel of ages past, this magnificent 
fortress stands testament to architectural mastery. Within its weathered 
stone walls, torch-lit chambers offer warm refuge, each room telling 
stories of countless generations who sought shelter here.
```

## 📖 Примеры использования

### Пример 1: Генерация при первом обучении

```bash
# Включи генерацию описаний при обучении VQ-VAE
python mcbuilder/train_improved_vqvae.py \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru

# Диффузия будет использовать тот же кэш (описания уже есть)
python mcbuilder/train_diffusion.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints_diffusion \
    --epochs 100
```

**Время:** Обучение займёт дольше на ~10-20% из-за Gemini API запросов

### Пример 2: Batch генерация после обучения

```bash
# Сначала обычное обучение
python mcbuilder/train_improved_vqvae.py \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100

# Потом генерация описаний
python generate_dataset_descriptions.py \
    --cache_dir ./data/cache \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --language ru \
    --style detailed \
    --delay 1.0
```

**Время:** Зависит от количества построек (~1 сек на постройку)

### Пример 3: Тестирование на малом датасете

```bash
# Генерация для первых 10 построек
python generate_dataset_descriptions.py \
    --cache_dir ./data/cache \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --language en \
    --style detailed \
    --limit 10

# Просмотр результатов
python generate_dataset_descriptions.py \
    --cache_dir ./data/cache \
    --preview \
    --num_preview 5
```

### Пример 4: Разные стили для разных целей

```bash
# Detailed для обучения
python generate_dataset_descriptions.py \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --style detailed \
    --language en

# Можно потом перегенерировать в другой стиль
# (просто удали ./data/cache/descriptions/ и запусти снова)
```

## 📊 Что анализируется

BuildAnalyzer автоматически извлекает:

### Структурные метрики
- **Размер:** H x W x D блоков
- **Всего блоков:** количество использованных блоков
- **Плотность:** процент заполнения пространства
- **Высота:** реальная высота постройки
- **Footprint:** размер основания

### Материалы
- **Топ-10 блоков** с процентами
- **Основной материал** (самый используемый)
- **Категории:**
  - Структурные (stone, planks, bricks)
  - Декоративные (glass, wool, flowers)
  - Мебель (chest, bed, table)

### Функциональность
- **Количество комнат** (enclosed spaces)
- **Наличие мебели**
- **Метаданные** (имя, категория из BuildPaste)

## 🔍 Просмотр описаний

### Preview режим

```bash
python generate_dataset_descriptions.py \
    --cache_dir ./data/cache \
    --preview \
    --num_preview 10
```

**Выведет:**
```
============================================================
Sample Descriptions (10 shown):
============================================================

Build: Medieval Castle (abc123xyz)
Description: A grand medieval fortress constructed with cobblestone 
and stone bricks, measuring 48x64x48 blocks. The castle features 
12 furnished rooms across multiple floors, including throne rooms, 
armories, and living quarters...
------------------------------------------------------------

Build: Cozy Cottage (def456uvw)
Description: ...
```

## ⚡ Оптимизация и Rate Limits

### Gemini API Limits
- **Free tier:** 60 requests/minute
- **Рекомендуемый delay:** 1.0 секунда между запросами
- **Для большого датасета:** используй `--delay 1.5`

### Кэширование
- Описания сохраняются в `.txt` файлах
- **Если файл существует → пропускается**
- Можно прервать и продолжить без потерь

### Параллелизация
```bash
# НЕ рекомендуется - можно превысить rate limit
# Лучше один процесс с delay
```

## 🎓 Использование описаний

После генерации описаний:

### Вариант 1: Просмотр и анализ
```python
from pathlib import Path

descriptions = Path('./data/cache/descriptions')
for desc_file in descriptions.glob('*.txt'):
    with open(desc_file) as f:
        print(f.read())
```

### Вариант 2: Text-to-Build обучение (будущее)
```python
# Датасет готов для обучения text-conditioned модели
dataset = BuildPasteDataset(
    cache_dir='./data/cache',
    generate_descriptions=False  # описания уже есть!
)

for sample in dataset:
    blocks = sample['blocks']         # 3D постройка
    description = sample['description']  # AI описание
    # Обучай text-to-build модель
```

### Вариант 3: Экспорт датасета
```python
# Можно экспортировать в JSON для других целей
import json
from pathlib import Path

dataset = []
builds_cache = Path('./data/cache/builds')
desc_cache = Path('./data/cache/descriptions')

for build_file in builds_cache.glob('*.npz'):
    build_id = build_file.stem
    desc_file = desc_cache / f'{build_id}.txt'
    
    if desc_file.exists():
        with open(desc_file) as f:
            description = f.read()
        
        dataset.append({
            'build_id': build_id,
            'build_path': str(build_file),
            'description': description
        })

with open('text_build_dataset.json', 'w') as f:
    json.dump(dataset, f, indent=2, ensure_ascii=False)
```

## 💡 Best Practices

### Для обучения:
1. **Используй detailed style** - больше информации для модели
2. **Язык:** английский (больше данных в Gemini)
3. **Генерируй при первом обучении** - экономит время потом
4. **Сохраняй checkpoints часто** - если процесс прервётся

### Для эксперимента:
1. **Начни с --limit 10** - проверь качество
2. **Используй --preview** - посмотри результаты
3. **Попробуй разные стили** - выбери лучший

### Для production:
1. **Detailed + English** - универсально
2. **delay 1.0-1.5** - соблюдай rate limits
3. **Логируй процесс** - знай где остановился
4. **Backup описаний** - скопируй descriptions/ куда-то

## 🛠️ Troubleshooting

### Ошибка: "gemini_api_key required"
```bash
# Забыл передать API ключ
--gemini_api_key YOUR_KEY
```

### Слишком медленно
```bash
# Уменьши delay (но не ниже 0.5!)
--delay 0.5

# Или генерируй описания отдельно после обучения
```

### Rate limit exceeded
```bash
# Увеличь delay
--delay 2.0

# Или используй --limit для batch обработки
--limit 50  # по 50 построек
```

### Некачественные описания
```bash
# Попробуй другой стиль
--style creative  # вместо detailed

# Или другой язык
--language en  # вместо ru
```

### Хочу перегенерировать
```bash
# Удали существующие описания
rm -rf ./data/cache/descriptions/

# Запусти заново
python generate_dataset_descriptions.py ...
```

## 📈 Статистика

После генерации скрипт покажет:

```
============================================================
Summary:
  Generated: 245
  Skipped (already cached): 55
  Errors: 3
  Total descriptions: 300
============================================================

✓ Descriptions saved to: ./data/cache/descriptions
✓ Ready for text-to-build training!
```

## 🔮 Будущее использование

С готовым датасетом (постройки + описания) можно:

### 1. Text-to-Build модель
```python
# Генерация построек по описанию
prompt = "medieval castle with towers"
build = model.generate_from_text(prompt)
```

### 2. Build-to-Text модель
```python
# Описание существующих построек
description = model.describe_build(blocks)
```

### 3. Поиск по описаниям
```python
# Найти постройки по запросу
query = "castle with furniture"
results = search_builds(query, descriptions_dataset)
```

### 4. Style transfer
```python
# "Сделай этот дом в стиле замка"
new_build = transfer_style(source_build, target_description)
```

## 📝 Пример workflow

```bash
# Полный цикл с описаниями:

# 1. Обучение VQ-VAE с генерацией описаний
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru

# 2. Обучение Diffusion (описания уже в кэше)
python mcbuilder/train_diffusion.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --epochs 100

# 3. Просмотр описаний
python generate_dataset_descriptions.py --preview

# 4. Генерация построек
python generate_hq.py --size 64,64,64 --validate

# 5. (Будущее) Обучение text-to-build модели
# python train_text_to_build.py --use_descriptions
```

---

**Теперь датасет содержит не только постройки, но и их AI-описания! 🤖📝**

*Готово для обучения text-to-build моделей!*
