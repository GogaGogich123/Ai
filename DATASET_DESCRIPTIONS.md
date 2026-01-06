# 📝 Dataset Description Generation

Автоматическая генерация AI-описаний для построек из датасета BuildPaste **во время обучения**.

## 🎯 Зачем это нужно?

При обучении модели скачиваются постройки из BuildPaste API. Для каждой постройки автоматически генерируется детальное описание через Gemini API и сохраняется в кэш.

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

## 🚀 Использование

### Stage 1: VQ-VAE с генерацией описаний

```bash
python mcbuilder/train_improved_vqvae.py \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints_improved \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru
```

**Что происходит:**
1. ⬇️ Скачивается постройка из BuildPaste
2. 📊 Анализируется структура (блоки, материалы, комнаты)
3. 🤖 Gemini генерирует детальное описание
4. 💾 Описание сохраняется в `./data/cache/descriptions/{build_id}.txt`
5. ✅ Постройка используется для обучения VQ-VAE

**Важно:**
- Описания генерируются **один раз** и кэшируются
- При повторном запуске используются закэшированные описания
- Обучение займёт дольше на ~10-20% из-за Gemini API запросов

### Stage 2: Diffusion (описания уже в кэше)

```bash
python mcbuilder/train_diffusion.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --cache_dir ./data/cache \
    --checkpoint_dir ./checkpoints_diffusion \
    --epochs 100
```

**Что происходит:**
- Использует тот же кэш построек
- Описания уже сгенерированы на Stage 1
- Никаких дополнительных Gemini запросов
- Обучение идёт с нормальной скоростью

## 📊 Параметры

### Обязательные (для генерации описаний)

```bash
--generate_descriptions          # Включить генерацию
--gemini_api_key YOUR_KEY        # API ключ Gemini
```

### Опциональные

```bash
--description_language LANG      # en или ru (default: en)
```

## 📖 Что анализируется

BuildAnalyzer автоматически извлекает для каждой постройки:

### Структурные метрики
- **Размер:** H x W x D блоков
- **Всего блоков:** количество использованных
- **Плотность:** процент заполнения
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
- **Оригинальное имя** из BuildPaste
- **Категория** постройки

## 🎨 Стиль описаний

Используется **detailed** стиль (3-5 предложений):

**Содержит:**
- Тип постройки и архитектурный стиль
- Основные материалы
- Ключевые особенности (размер, комнаты)
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

## ⚡ Производительность

### Скорость генерации
- **~1 секунда** на описание (Gemini API)
- **Rate limit:** 60 запросов/минуту
- **Automatic delay** между запросами

### Влияние на обучение
- **Без описаний:** ~8-12 часов (VQ-VAE)
- **С описаниями:** ~9-14 часов (VQ-VAE)
- **Разница:** +10-20% времени

### Кэширование
- Описания сохраняются **навсегда**
- При повторном запуске: **0 секунд** (используются из кэша)
- Можно прервать и продолжить без потерь

## 🎓 Workflow

```bash
# 1. Первый запуск - с генерацией описаний
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru
# Время: ~9-14 часов (скачивание + описания + обучение)

# 2. Diffusion - описания уже есть
python mcbuilder/train_diffusion.py \
    --vqvae_checkpoint ./checkpoints_improved/improved_vqvae_final.pt \
    --epochs 100
# Время: ~12-16 часов (только обучение)

# 3. Повторное обучение - описания из кэша
python mcbuilder/train_improved_vqvae.py \
    --epochs 150 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw
# Время: ~8-12 часов (описания берутся из кэша, 0 новых запросов!)
```

## 💡 Рекомендации

### Для максимальной эффективности:
1. ✅ **Генерируй описания с первого раза** - при первом обучении
2. ✅ **Используй английский** - `--description_language en`
3. ✅ **Сохраняй checkpoints часто** - `--save_every 5`
4. ✅ **Backup кэша** - периодически копируй `./data/cache/`

### Если обучение прервалось:
- Описания **сохранены** в кэше
- Постройки **сохранены** в кэше
- Просто запусти обучение снова
- Новые Gemini запросы **не нужны**

## 🛠️ Troubleshooting

### "gemini_api_key required"
```bash
# Забыл флаг --gemini_api_key
# Добавь:
--generate_descriptions \
--gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw
```

### Rate limit exceeded
```bash
# Gemini API лимит: 60 req/min
# Система автоматически делает delay
# Просто жди, всё продолжится автоматически
```

### Хочу перегенерировать описания
```bash
# Удали существующие
rm -rf ./data/cache/descriptions/

# Запусти обучение снова с --generate_descriptions
```

### Проверить сколько описаний сгенерировано
```bash
ls -1 ./data/cache/descriptions/ | wc -l
```

### Посмотреть примеры описаний
```bash
head -3 ./data/cache/descriptions/*.txt
```

## 🎯 Использование описаний

После обучения датасет содержит описания:

```python
from mcbuilder import BuildPasteDataset

# Датасет автоматически загружает описания
dataset = BuildPasteDataset(
    cache_dir='./data/cache',
    generate_descriptions=False  # описания уже в кэше
)

for sample in dataset:
    blocks = sample['blocks']           # 3D постройка [32,32,32]
    description = sample['description']  # "Средневековый замок..."
    build_name = sample['build_name']   # "Medieval Castle"
    
    # Готово для text-to-build обучения!
```

## 🔮 Будущее: Text-to-Build модель

С готовым датасетом (постройки + описания) можно обучить:

```python
# Генерация построек по описанию (будущая фича)
prompt = "средневековый замок с башнями"
build = model.generate_from_text(prompt)
```

---

## 📝 Краткое резюме

### Что делать:

```bash
# Просто добавь эти флаги при обучении:
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru
```

### Что получишь:

- ✅ Обученный VQ-VAE
- ✅ Кэш построек из BuildPaste
- ✅ AI-описания для каждой постройки
- ✅ Готовый датасет для text-to-build

### Время:

- **Первый раз:** ~9-14 часов (с описаниями)
- **Повторно:** ~8-12 часов (описания из кэша)

---

**Теперь датасет автоматически пополняется описаниями во время обучения! 🤖📝**
