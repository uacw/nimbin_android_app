package tech.nimbus.nimbin.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbus.nimbin.MainActivity

/**
 * End-to-End тесты для полных пользовательских сценариев.
 *
 * Тестируют реальные flow пользователей от UI до backend:
 * - Регистрация → Создание заметки → Просмотр
 * - Логин → Просмотр своих заметок → Выход
 * - Анонимный просмотр публичных заметок
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun fullUserJourney_registerCreatePasteViewPaste() {
        // === Сценарий: Новый пользователь регистрируется и создает заметку ===

        // 1. Пользователь видит экран входа и переходит к регистрации
        composeTestRule.onNodeWithText("Нет аккаунта? Регистрация").performClick()

        // 2. Заполняет форму регистрации
        composeTestRule.onNodeWithTag("username_field").performTextInput("e2euser")
        composeTestRule.onNodeWithTag("email_field").performTextInput("e2e@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("register_button").performClick()

        // 3. После успешной регистрации попадает на главный экран
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Создать заметку").assertIsDisplayed()

        // 4. Создает новую заметку
        composeTestRule.onNodeWithText("Создать заметку").performClick()
        composeTestRule.onNodeWithTag("title_field").performTextInput("Моя первая заметка")
        composeTestRule.onNodeWithTag("content_field").performTextInput("Содержимое первой заметки")
        composeTestRule.onNodeWithTag("create_button").performClick()

        // 5. Заметка создается и отображается
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Моя первая заметка").assertIsDisplayed()
        composeTestRule.onNodeWithText("Содержимое первой заметки").assertIsDisplayed()
    }

    @Test
    fun existingUserJourney_loginViewPastesLogout() {
        // === Сценарий: Существующий пользователь входит, просматривает заметки и выходит ===

        // 1. Вход в систему
        composeTestRule.onNodeWithTag("email_field").performTextInput("existing@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("login_button").performClick()

        // 2. Переход в раздел "Мои заметки"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Мои заметки").performClick()

        // 3. Просматривает список своих заметок
        composeTestRule.onNodeWithText("Мои заметки").assertIsDisplayed()
        composeTestRule.onNodeWithText("У вас пока нет заметок").assertIsDisplayed() // Если заметок нет

        // 4. Переходит в профиль
        composeTestRule.onNodeWithText("Профиль").performClick()
        composeTestRule.onNodeWithText("existing").assertIsDisplayed() // Username

        // 5. Выходит из системы
        composeTestRule.onNodeWithText("Выйти").performClick()
        composeTestRule.onNodeWithText("Вход в систему").assertIsDisplayed()
    }

    @Test
    fun anonymousUserJourney_browsePastesCreateAnonymousPaste() {
        // === Сценарий: Анонимный пользователь просматривает заметки и создает анонимную ===

        // 1. Без входа в систему переходит к просмотру публичных заметок
        composeTestRule.onNodeWithText("Пропустить").performClick() // Если есть такая опция

        // 2. Просматривает публичные заметки
        composeTestRule.onNodeWithText("Публичные заметки").assertIsDisplayed()

        // 3. Создает анонимную заметку
        composeTestRule.onNodeWithText("Создать заметку").performClick()
        composeTestRule.onNodeWithTag("title_field").performTextInput("Анонимная заметка")
        composeTestRule.onNodeWithTag("content_field").performTextInput("Содержимое анонимной заметки")

        // Анонимные заметки всегда публичные
        composeTestRule.onNodeWithTag("create_button").performClick()

        // 4. Заметка создается и становится публично доступной
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Анонимная заметка").assertIsDisplayed()
    }

    @Test
    fun errorHandlingJourney_networkErrorsAndRecovery() {
        // === Сценарий: Обработка ошибок сети и восстановление ===

        // 1. Попытка входа с неверными данными
        composeTestRule.onNodeWithTag("email_field").performTextInput("wrong@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("wrongpassword")
        composeTestRule.onNodeWithTag("login_button").performClick()

        // 2. Отображение ошибки
        composeTestRule.onNodeWithText("Неверные учетные данные").assertIsDisplayed()

        // 3. Исправление данных и повторная попытка
        composeTestRule.onNodeWithTag("email_field").performTextInput("correct@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("correctpassword")
        composeTestRule.onNodeWithTag("login_button").performClick()

        // 4. Успешный вход
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Создать заметку").assertIsDisplayed()
    }

    @Test
    fun dataValidationJourney_formValidationAndFeedback() {
        // === Сценарий: Валидация форм и обратная связь ===

        // 1. Переход к регистрации
        composeTestRule.onNodeWithText("Нет аккаунта? Регистрация").performClick()

        // 2. Попытка регистрации с пустыми полями
        composeTestRule.onNodeWithTag("register_button").performClick()
        composeTestRule.onNodeWithText("Имя пользователя обязательно").assertIsDisplayed()

        // 3. Заполнение только username
        composeTestRule.onNodeWithTag("username_field").performTextInput("u")
        composeTestRule.onNodeWithTag("register_button").performClick()
        composeTestRule.onNodeWithText("Имя пользователя должно содержать минимум 3 символа").assertIsDisplayed()

        // 4. Некорректный email
        composeTestRule.onNodeWithTag("username_field").performTextInput("validuser")
        composeTestRule.onNodeWithTag("email_field").performTextInput("invalid-email")
        composeTestRule.onNodeWithTag("register_button").performClick()
        composeTestRule.onNodeWithText("Некорректный формат email").assertIsDisplayed()

        // 5. Слабый пароль
        composeTestRule.onNodeWithTag("email_field").performTextInput("valid@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123")
        composeTestRule.onNodeWithTag("register_button").performClick()
        composeTestRule.onNodeWithText("Пароль должен содержать минимум 6 символов").assertIsDisplayed()

        // 6. Корректные данные
        composeTestRule.onNodeWithTag("password_field").performTextInput("validpassword123")
        composeTestRule.onNodeWithTag("register_button").performClick()

        // 7. Успешная регистрация
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Создать заметку").assertIsDisplayed()
    }

    @Test
    fun navigationJourney_fullAppNavigation() {
        // === Сценарий: Навигация по всему приложению ===

        // 1. Вход в систему
        composeTestRule.onNodeWithTag("email_field").performTextInput("navuser@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.waitForIdle()

        // 2. Навигация по всем разделам
        // Главная
        composeTestRule.onNodeWithText("Главная").performClick()
        composeTestRule.onNodeWithText("Публичные заметки").assertIsDisplayed()

        // Создание заметки
        composeTestRule.onNodeWithText("Создать").performClick()
        composeTestRule.onNodeWithText("Создать заметку").assertIsDisplayed()
        composeTestRule.onNodeWithText("Назад").performClick() // Возврат

        // Мои заметки
        composeTestRule.onNodeWithText("Мои заметки").performClick()
        composeTestRule.onNodeWithText("Ваши заметки").assertIsDisplayed()

        // Профиль
        composeTestRule.onNodeWithText("Профиль").performClick()
        composeTestRule.onNodeWithText("navuser").assertIsDisplayed()

        // 3. Глубокая навигация: открытие заметки
        composeTestRule.onNodeWithText("Главная").performClick()
        // Клик на первую заметку в списке (если есть)
        composeTestRule.onNodeWithTag("paste_item_0").performClick()
        composeTestRule.onNodeWithText("Назад").assertIsDisplayed()
        composeTestRule.onNodeWithText("Назад").performClick()

        // Возврат на главную
        composeTestRule.onNodeWithText("Публичные заметки").assertIsDisplayed()
    }
}
