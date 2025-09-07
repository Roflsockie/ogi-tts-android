# Технический план проекта OGI TTS Android

## Обзор
Проект представляет собой Android-приложение для преобразования текста в речь (TTS) с использованием нативных Android TTS API. Приложение заменяет Microsoft Edge TTS из ПК-версии на встроенные движки (Google TTS и др.), обеспечивая совместимость и независимость от внешних сервисов. Основной фокус на управлении файлами с учетом Scoped Storage, разрешениях и современном UI с темной темой.

## Технологический стек
- **Язык программирования**: Kotlin
- **Фреймворк UI**: Jetpack Compose (рекомендуется для современного, декларативного интерфейса) или стандартный Android View (если Compose не подходит для совместимости)
- **TTS**: Android TextToSpeech API (android.speech.tts.TextToSpeech)
- **Файловое управление**: Scoped Storage (MediaStore API для Android 10+), File API для старых версий
- **Обработка текста**: Apache POI для .docx (внешняя библиотека), стандартные средства для .txt
- **Определение языка**: Android Locale API или библиотека like 'language-detector' (если нужно)
- **Архитектура**: MVVM (Model-View-ViewModel) для разделения логики
- **Зависимости**:
  - androidx.core:core-ktx (для разрешений)
  - androidx.activity:activity-compose (если Compose)
  - org.apache.poi:poi (для .docx)
  - androidx.lifecycle:lifecycle-viewmodel-ktx (для ViewModel)
- **Минимум SDK**: API 26 (Android 8.0) для поддержки TTS и Scoped Storage

## Архитектура приложения
- **MVVM**:
  - **Model**: Классы для обработки файлов, TTS и данных (TextFile, AudioFile, TtsEngine)
  - **View**: Compose компоненты или XML layouts (MainActivity, ImportScreen, SynthesisScreen, AudioListScreen)
  - **ViewModel**: Логика импорта, синтеза, управления состоянием (ImportViewModel, TtsViewModel)
- **Компоненты**:
  - MainActivity: Навигация между экранами
  - PermissionManager: Обработка запросов разрешений (READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
  - FileManager: Импорт файлов, чтение/запись в Scoped Storage
  - TtsManager: Инициализация TTS, выбор голоса, синтез
  - AudioPlayer: Воспроизведение аудио
- **Хранение данных**:
  - Аудиофайлы: В папке приложения (getExternalFilesDir) или MediaStore
  - Настройки: SharedPreferences для выбранного голоса/языка

## Этапы реализации
1. **Настройка проекта**: Создание Android проекта в Android Studio, добавление зависимостей.
2. **UI и навигация**: Реализация экранов с Compose или View, темная тема (Theme.Material3.Dark).
3. **Разрешения и файлы**: Запрос разрешений при запуске, реализация Scoped Storage.
4. **Импорт текста**: Чтение .txt и .docx, редактирование в TextField.
5. **TTS интеграция**: Инициализация TextToSpeech, список доступных голосов, синтез с прогрессом.
6. **Сохранение аудио**: Запись в файл, отображение списка аудио с плеером.
7. **Тестирование**: Unit-тесты для ViewModel, интеграционные тесты на эмуляторе.

## Риски и решения
- **Scoped Storage**: Для Android 10+ использовать MediaStore; для старых - File API с разрешениями.
- **TTS совместимость**: Проверить на разных устройствах; fallback на системный TTS.
- **Внешние библиотеки**: Минимизировать; использовать только POI для .docx.
- **Производительность**: Синтез в фоне с ProgressBar; избегать блокировок UI.

## Оценка ресурсов
- **Время**: 4-6 недель для MVP (импорт, синтез, сохранение).
- **Команда**: 1-2 разработчика (Android).
- **Тестирование**: Эмуляторы Android 8.0+, реальные устройства.

## Следующие шаги
- Перейти к /tasks для разбивки на конкретные задачи.
- Пример: "/tasks Разбить план на задачи по экранам и компонентам."</content>
<parameter name="filePath">M:\AI\ogi-tts-android\PLAN.md
