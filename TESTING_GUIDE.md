# 🧪 Тестирование приложения NimBin

## 📋 Обзор тестового покрытия

Проект NimBin включает комплексное тестовое покрытие всех слоев архитектуры:

### 🔧 Типы тестов

1. **Unit Tests** - изолированное тестирование отдельных компонентов
2. **Integration Tests** - тестирование взаимодействия между слоями
3. **UI Tests** - тестирование пользовательского интерфейса Compose
4. **End-to-End Tests** - полные пользовательские сценарии

---

## 🏗️ Структура тестов

```
app/src/
├── test/java/                          # Unit тесты
│   └── tech/nimbus/nimbin/
│       ├── domain/usecase/            # Тесты Use Cases
│       │   ├── auth/LoginUseCaseTest.kt
│       │   └── paste/CreatePasteUseCaseTest.kt
│       ├── data/
│       │   ├── remote/NimbinApiClientImplTest.kt
│       │   └── repository/AuthRepositoryImplTest.kt
│       ├── ui/features/auth/login/LoginViewModelTest.kt
│       ├── integration/LayerIntegrationTest.kt
│       └── test/                      # Тестовые утилиты
│           ├── CoroutineTestRule.kt
│           ├── TestDataFactory.kt
│           └── MockHelper.kt
└── androidTest/java/                   # Instrumented тесты
    └── tech/nimbus/nimbin/
        ├── ui/features/auth/login/LoginScreenTest.kt
        ├── e2e/EndToEndTest.kt
        └── test/config/TestAppModule.kt

shared/src/test/kotlin/                 # Тесты shared модуля
└── tech/nimbus/shared/utils/ApiResultTest.kt
```

---

## ⚡ Быстрый старт

### Запуск всех тестов
```bash
./gradlew test                    # Unit тесты
./gradlew connectedAndroidTest    # UI и E2E тесты
./gradlew testDebugUnitTest       # Только unit тесты debug
```

### Запуск конкретных тестов
```bash
# Тесты конкретного класса
./gradlew testDebugUnitTest --tests="*LoginUseCaseTest"

# Тесты конкретного метода
./gradlew testDebugUnitTest --tests="*LoginUseCaseTest.login*"

# UI тесты конкретного экрана
./gradlew connectedAndroidTest --tests="*LoginScreenTest"
```

### Отчеты о покрытии
```bash
./gradlew testDebugUnitTestCoverage
# Отчеты: app/build/reports/coverage/test/debug/
```

---

## 📊 Покрытие компонентов

### ✅ Domain Layer (Use Cases)
- **`LoginUseCase`**: валидация, успех/ошибка flows, edge cases
- **`CreatePasteUseCase`**: валидация контента, лимиты, различные языки программирования

### ✅ Data Layer 
- **`NimbinApiClientImpl`**: HTTP запросы, парсинг JSON, обработка ошибок (4xx/5xx), mock engine
- **`AuthRepositoryImpl`**: взаимодействие с API и preferences, преобразование результатов

### ✅ Presentation Layer
- **`LoginViewModel`**: управление состоянием формы, валидация, взаимодействие с use cases
- **`LoginScreen`**: UI компоненты, пользовательский ввод, состояния загрузки

### ✅ Shared Module
- **`ApiResult`**: функциональные методы, chaining, обработка состояний

### ✅ Integration Tests
- **Layer Integration**: полный поток Use Case → Repository → API Client
- **Authentication Flow**: логин с сохранением токена
- **Error Propagation**: распространение ошибок через слои

### ✅ End-to-End Tests
- **User Journey**: регистрация → создание заметки → просмотр
- **Navigation**: навигация по всему приложению
- **Error Handling**: обработка ошибок сети и валидация
- **Anonymous Usage**: работа без авторизации

---

## 🛠️ Тестовые зависимости

```kotlin
// Unit тесты
testImplementation("org.mockito:mockito-core:5.5.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("app.cash.turbine:turbine:1.0.0")           // Flow testing
testImplementation("io.ktor:ktor-client-mock:2.3.7")           // HTTP mocking

// Android Instrumented тесты
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test:runner:1.5.2")
androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
androidTestImplementation("org.mockito:mockito-android:5.5.0")
```

---

## 🧪 Примеры тестов

### Unit Test (Use Case)
```kotlin
@Test
fun `login with valid credentials should return success flow`() = runTest {
    // Given
    val email = "test@example.com"
    val password = "password123" 
    val expectedToken = "jwt_token_123"
    
    whenever(authRepository.login(email, password))
        .thenReturn(flowOf(AuthResult.Success(expectedToken)))

    // When & Then
    loginUseCase(email, password).test {
        assertEquals(AuthResult.Success(expectedToken), awaitItem())
        awaitComplete()
    }
}
```

### UI Test (Compose)
```kotlin
@Test
fun loginScreen_successState_triggersNavigation() {
    // Given
    var navigationTriggered = false
    
    composeTestRule.setContent {
        LoginScreen(
            viewModel = viewModel,
            onNavigateToHome = { navigationTriggered = true }
        )
    }

    // When
    composeTestRule.onNodeWithTag("login_button").performClick()

    // Then
    assert(navigationTriggered)
}
```

### Integration Test
```kotlin
@Test
fun `full login flow should work end-to-end`() = runTest {
    // Given - настройка mock HTTP ответа
    mockEngine.config {
        addHandler { 
            respond("""{"token": "jwt_token", "user": {...}}""")
        }
    }

    // When - выполняем полный поток
    loginUseCase(email, password).test {
        assertEquals(AuthResult.Loading, awaitItem())
        assertEquals(AuthResult.Success("jwt_token"), awaitItem())
        awaitComplete()
    }
}
```

---

## 🔧 Тестовые утилиты

### `TestDataFactory`
Создание консистентных тестовых данных:
```kotlin
val testUser = TestDataFactory.createTestUser(
    username = "testuser",
    email = "test@example.com"
)

val testPaste = TestDataFactory.createKotlinPaste(
    title = "Kotlin Example",
    content = "fun main() { ... }"
)
```

### `MockHelper`
Упрощение создания mock объектов:
```kotlin
val mockTokenProvider = MockHelper.createMockTokenProvider("test_token")
val mockHttpClient = MockHelper.createMockHttpClient(
    responses = MockHelper.createSuccessApiResponses()
)
```

### `CoroutineTestRule`
Автоматическая настройка тестовых корутин:
```kotlin
class MyViewModelTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()
    
    @Test
    fun myTest() = runTest {
        // Тест с корутинами
    }
}
```

---

## 📈 Метрики качества

### Покрытие кода
- **Domain Layer**: >90%
- **Data Layer**: >85% 
- **Presentation Layer**: >80%
- **Shared Module**: >95%

### Типы проверок
- ✅ Успешные сценарии
- ✅ Обработка ошибок
- ✅ Edge cases
- ✅ Валидация данных
- ✅ Network errors
- ✅ UI состояния
- ✅ Navigation flows

---

## 🚀 CI/CD Integration

### GitHub Actions / Jenkins
```yaml
- name: Run Unit Tests
  run: ./gradlew testDebugUnitTest

- name: Run UI Tests  
  run: ./gradlew connectedAndroidTest

- name: Generate Test Report
  run: ./gradlew testDebugUnitTestCoverage

- name: Upload Coverage
  uses: codecov/codecov-action@v3
```

### Pre-commit hooks
```bash
#!/bin/sh
echo "Running tests before commit..."
./gradlew testDebugUnitTest
```

---

## 🔍 Debugging тестов

### Логирование в тестах
```kotlin
@Test
fun debugTest() = runTest {
    Timber.plant(Timber.DebugTree())
    // Тест с логированием
}
```

### Debugging UI тестов
```kotlin
composeTestRule.onRoot().printToLog("UI_TEST")  // Печать UI дерева
composeTestRule.waitForIdle()                   // Ожидание завершения анимаций
```

---

## 📝 Best Practices

### ✅ Do
- Используйте `TestDataFactory` для создания тестовых данных
- Мокайте внешние зависимости с помощью `MockHelper`
- Тестируйте как успешные, так и failure сценарии
- Используйте `turbine` для тестирования Flow
- Группируйте связанные тесты в классы
- Пишите описательные названия тестов

### ❌ Don't  
- Не тестируйте implementation детали
- Не создавайте хрупкие тесты, завязанные на UI
- Не игнорируйте edge cases
- Не используйте `Thread.sleep()` в тестах
- Не мокайте то, чем вы не владеете

---

## 🐛 Troubleshooting

### Частые проблемы

1. **Coroutine timeout**: Используйте `runTest` и `CoroutineTestRule`
2. **Hilt injection**: Добавьте `@HiltAndroidTest` и `HiltAndroidRule`
3. **Mock not working**: Проверьте порядок `whenever().thenReturn()`
4. **UI test flaky**: Добавьте `waitForIdle()` и используйте semantic matchers

### Debug команды
```bash
# Подробный вывод тестов
./gradlew test --info

# Только failed тесты  
./gradlew test --continue

# Профилирование тестов
./gradlew test --profile
```

---

*Документация поддерживается командой разработки NimBin*
