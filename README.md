# NimBin - Modern Android Pastebin

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=flat&logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2024+-3DDC84?style=flat&logo=android)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose)
![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?style=flat&logo=materialdesign)

*Современное Android приложение для создания и обмена текстовыми заметками*

</div>

---

## 📱 О проекте

**NimBin** — это feature-complete клон Pastebin/Ghostbin, построенный с использованием современного Android technology stack. Приложение предоставляет полноценную экосистему для создания, хранения и обмена текстовыми заметками с поддержкой различных уровней видимости, пользовательских профилей и продвинутой системы аутентификации.

### ✨ Ключевые особенности

- 🔐 **Полная система аутентификации** с JWT токенами
- 📝 **Три уровня видимости заметок**: Public, Unlisted, Private
- 👥 **Система пользовательских профилей** с настройкой отображения
- 🎨 **Modern Material Design 3** с поддержкой тем
- 📱 **Responsive UI** с адаптацией под разные размеры экрана
- 🔄 **Real-time синхронизация** с backend API
- 🌐 **Cross-platform shared модуль** для переиспользования кода

---

## 🛠 Технологический стек

### Core Platform
- **Kotlin** — 100% Kotlin codebase
- **Android SDK** — минимальная поддержка API 24+
- **Jetpack Compose** — декларативный UI framework
- **Material Design 3** — современная дизайн-система

### Architecture
- **Clean Architecture** — четкое разделение слоев
- **MVVM Pattern** — архитектурный паттерн для UI
- **Repository Pattern** — абстракция data layer
- **Use Cases** — инкапсуляция бизнес-логики

### Networking & Data
- **Ktor Client** — type-safe HTTP клиент
- **Kotlinx Serialization** — эффективная JSON обработка
- **JWT Authentication** — безопасная аутентификация
- **Shared Module** — общие DTO между платформами

### Development Tools
- **Hilt** — dependency injection
- **Navigation Component** — типобезопасная навигация
- **Coroutines & Flow** — асинхронность и реактивность
- **DataStore** — современное безопасное локальное хранение

---

## 🏗 Архитектура

Приложение построено по принципам Clean Architecture с четким разделением ответственности:

```
📱 Presentation (UI)
   ├── Jetpack Compose UI
   ├── ViewModels + State Management
   └── Navigation Component

🎯 Domain (Business Logic)
   ├── Use Cases
   ├── Domain Models
   └── Repository Contracts

💾 Data (External Sources)
   ├── Remote API Client
   ├── Local Storage
   └── Repository Implementations
```

---

## 🚀 Функциональность

### 🔐 Аутентификация
- Регистрация и авторизация пользователей
- JWT токен management с автообновлением
- Безопасное хранение учетных данных
- Автоматический logout при истечении сессии

### 📝 Управление заметками
- **Создание заметок** с гибкими настройками видимости
- **Просмотр и редактирование** с поддержкой syntax highlighting
- **Пагинация и поиск** по большим объемам данных
- **Статистика просмотров** и аналитика использования

### 👤 Пользовательские профили
- Настройка публичного профиля (username, display name)
- Просмотр статистики активности пользователей
- Управление личными заметками с фильтрацией
- Публичные страницы профилей других пользователей

### 🎨 Пользовательский опыт
- Современный Material Design 3 интерфейс
- Адаптивная верстка для планшетов
- Поддержка темной и светлой тем
- Плавные анимации и переходы
- Интуитивная навигация с deep linking

---

## 🧪 Качество кода

- **Comprehensive Testing** — unit, integration, UI тесты
- **Static Analysis** — Detekt для контроля качества кода
- **Code Style** — единообразное форматирование с Ktlint
- **Documentation** — подробная техническая документация
- **Type Safety** — максимальное использование системы типов Kotlin

---

### 🔄 AI-Enhanced Workflow
- **Automated Testing** — AI-генерируемые test cases с высоким покрытием
- **Code Refactoring** — интеллектуальное улучшение существующего кода
- **Documentation Generation** — автоматическое создание README и технической документации

---

## 📦 Структура проекта

```
NimBin/
├── app/                    # Android приложение
│   ├── src/main/kotlin/   # Основной код
│   └── src/test/kotlin/   # Unit тесты
├── shared/                # Shared Kotlin module
│   └── src/commonMain/    # Общие DTO и утилиты
├── config/                # Конфигурация инструментов
└── gradle/                # Gradle конфигурация
```
