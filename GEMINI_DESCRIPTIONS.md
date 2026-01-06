# 🤖 AI-Generated Descriptions with Gemini

Автоматическая генерация AI-описаний для построек из датасета BuildPaste.

## 🎯 Основное использование: Dataset Descriptions

**Генерация описаний для построек из BuildPaste во время обучения.**

### Как включить:

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
1. Скачивается постройка из BuildPaste
2. Анализируется структура (блоки, материалы, комнаты)
3. Gemini генерирует детальное описание
4. Описание сохраняется в `./data/cache/descriptions/`
5. Постройка используется для обучения

**Результат:**
```
./data/cache/
  ├── builds/
  │   └── abc123.npz          ← Постройка
  └── descriptions/
      └── abc123.txt          ← "Средневековый замок из камня..."
```

### Полное руководство:

**См. [DATASET_DESCRIPTIONS.md](DATASET_DESCRIPTIONS.md)** для детальной документации.

---

## 📊 Параметры

### Training скрипты

```bash
--generate_descriptions          # Включить генерацию описаний
--gemini_api_key KEY             # API ключ Gemini (обязательно)
--description_language LANG      # en или ru (default: en)
```

### Пример:

```bash
# Русские описания
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru

# Английские описания
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language en
```

---

## 🎨 Стиль описаний

Используется **detailed** стиль (3-5 предложений):

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

---

## ⚡ Производительность

### Кэширование
- Описания создаются **один раз**
- Сохраняются в `.txt` файлах
- При повторном запуске: **используются из кэша**
- Никаких лишних API запросов

### Влияние на время обучения
- **Первый запуск:** +10-20% времени (генерация описаний)
- **Повторные запуски:** 0% (описания из кэша)

### Rate Limits
- Gemini API: 60 запросов/минуту
- Автоматический delay между запросами
- Безопасно для непрерывного обучения

---

## 💡 Best Practices

### Рекомендую:
1. ✅ **Генерируй при первом обучении** - один раз и навсегда
2. ✅ **Используй английский** - `--description_language en`
3. ✅ **Сохраняй checkpoints часто** - `--save_every 5`
4. ✅ **Backup кэша** - копируй `./data/cache/` периодически

### Если обучение прервалось:
- Описания сохранены в кэше
- Просто продолжи обучение
- Новые описания только для новых построек

---

## 🔮 Использование описаний

После обучения датасет готов для text-to-build:

```python
from mcbuilder import BuildPasteDataset

dataset = BuildPasteDataset(
    cache_dir='./data/cache',
    generate_descriptions=False  # описания уже есть!
)

for sample in dataset:
    blocks = sample['blocks']         # Постройка [32,32,32]
    description = sample['description']  # AI описание
    build_name = sample['build_name']   # Имя постройки
    
    # Готово для text-to-build обучения
```

---

**Полная документация:** [DATASET_DESCRIPTIONS.md](DATASET_DESCRIPTIONS.md)

**Автоматические AI-описания для датасета BuildPaste! 🤖📝**
