# GoldBrain

Android-приложение «вторая память» для фотографий.

## MVP

- выбор нескольких фотографий через системный Android Photo Picker;
- оригиналы не копируются в приложение;
- локальное OCR через ML Kit;
- локальные визуальные метки через ML Kit;
- автоматическая базовая классификация: чек, книга, событие, контакт, парковка;
- Room-база для индекса;
- поиск по распознанному тексту и меткам;
- интерфейс на Jetpack Compose.

## APK через GitHub Actions

Открой:

**Actions → Build APK → Run workflow**

После успешной сборки внизу страницы запуска появится артефакт **GoldBrain-debug-apk**.
Внутри находится **GoldBrain-debug.apk**.

Workflow также запускается автоматически после изменений Android-проекта в ветке `main`.

## Техническая база

- Kotlin
- Jetpack Compose
- Room
- ML Kit Text Recognition
- ML Kit Image Labeling
- Coil
- Gradle 8.11.1
- Android Gradle Plugin 8.9.2
- compileSdk / targetSdk 35

Следующий этап: CameraX, MediaStore, WorkManager и фоновая индексация разрешённой галереи.
