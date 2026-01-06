# 🤖 AI-Generated Descriptions with Gemini

Два варианта использования Gemini API для описаний:

## 🎯 Основной вариант: Описания для датасета BuildPaste

**Зачем:** Создать text-to-build датасет для обучения моделей генерации по описанию.

**См. полное руководство:** [DATASET_DESCRIPTIONS.md](DATASET_DESCRIPTIONS.md)

### Быстрый старт:

**Способ 1: Во время обучения**
```bash
python mcbuilder/train_improved_vqvae.py \
    --epochs 100 \
    --generate_descriptions \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_language ru
```

**Способ 2: Batch генерация**
```bash
python generate_dataset_descriptions.py \
    --cache_dir ./data/cache \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --language ru \
    --style detailed
```

**Результат:**
```
./data/cache/
  ├── builds/
  │   ├── abc123.npz          ← постройка
  │   └── def456.npz
  └── descriptions/
      ├── abc123.txt          ← AI описание
      └── def456.txt
```

---

## 📦 Дополнительно: Описания для сгенерированных построек

Также можно генерировать описания для AI-построек (для sharing/export):

```bash
python generate_hq.py \
    --size 32,32,32 \
    --validate \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style detailed \
    --description_language en \
    --output castle.litematic
```

**Результат:** .litematic файл с профессиональным AI-описанием внутри.

---

## 📊 Сравнение вариантов

| | Описания датасета | Описания генерации |
|---|---|---|
| **Цель** | Text-to-build обучение | Export/sharing |
| **Когда** | При обучении / batch | При генерации |
| **Файл** | `.txt` в кэше | Внутри `.litematic` |
| **Важность** | ⭐⭐⭐ Основное | ⭐ Опционально |
| **Руководство** | [DATASET_DESCRIPTIONS.md](DATASET_DESCRIPTIONS.md) | Ниже |

---

## 🎨 Описания для сгенерированных построек

### Параметры

```bash
--gemini_api_key KEY             # Gemini API ключ
--description_style STYLE        # detailed/concise/creative
--description_language LANG      # en/ru
--describe_variants              # Описать и варианты тоже
```

### Примеры

**Detailed (English):**
```bash
python generate_hq.py \
    --size 32,32,32 \
    --validate \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style detailed \
    --description_language en \
    --output house.litematic
```

**Concise (Russian):**
```bash
python generate_hq.py \
    --size 64,64,64 \
    --validate \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --description_style concise \
    --description_language ru \
    --output замок.litematic
```

**With Variants:**
```bash
python generate_hq.py \
    --size 32,32,32 \
    --validate \
    --generate_multiple 3 \
    --describe_variants \
    --gemini_api_key AIzaSyCDosYILwiaFepVMThinSM7IjJzrNFbYEw \
    --output builds.litematic
```

**Interactive:**
```bash
python generate_with_description.py
```

---

## 🎓 Рекомендации

### Для создания text-to-build датасета:
✅ Используй описания датасета (DATASET_DESCRIPTIONS.md)  
✅ Генерируй при обучении или batch режим  
✅ Detailed style + English  
✅ Сохраняй в кэш

### Для sharing построек:
✅ Описания для генерации (этот файл)  
✅ Генерируй при создании .litematic  
✅ Любой стиль на выбор  
✅ Сохраняется внутри .litematic

---

**Основное использование:** [DATASET_DESCRIPTIONS.md](DATASET_DESCRIPTIONS.md) - создание text-to-build датасета! 🎯
