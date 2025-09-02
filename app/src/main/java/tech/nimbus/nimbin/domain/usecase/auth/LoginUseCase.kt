package tech.nimbus.nimbin.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.AuthResult
import javax.inject.Inject

/**
 * Use case для аутентификации пользователя в системе NimBin.
 *
 * Выполняет процедуру входа пользователя с валидацией данных и обработкой различных
 * сценариев ошибок. Возвращает поток состояний для реактивного обновления UI.
 *
 * ## Бизнес-логика:
 * - Валидация входных данных (email/username и пароль)
 * - Отправка запроса на сервер для проверки учетных данных
 * - Получение JWT токена при успешной аутентификации
 * - Обработка ошибок: неверные данные, сетевые проблемы, блокировка аккаунта
 *
 * ## Возможные состояния результата:
 * - `Loading` - процесс аутентификации
 * - `Success(token)` - успешная аутентификация с JWT токеном
 * - `Error(message)` - ошибка с описанием для пользователя
 *
 * @see AuthRepository
 * @see AuthResult
 *
 * @author NimBin Team
 * @since 1.0
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Выполняет аутентификацию пользователя.
     *
     * @param email Email адрес или username пользователя.
     *              Не должен быть пустым. Поддерживается как email формат,
     *              так и обычный username (3-30 символов).
     * @param password Пароль пользователя. Минимум 6 символов.
     *
     * @return Flow<AuthResult<String>> Поток состояний аутентификации:
     *         - Loading: в процессе выполнения запроса
     *         - Success: содержит JWT токен для дальнейших API вызовов
     *         - Error: описание ошибки для отображения пользователю
     *
     * @throws IllegalArgumentException если переданы пустые параметры
     *
     * ## Примеры использования:
     * ```kotlin
     * // В ViewModel
     * loginUseCase("user@example.com", "password123")
     *     .collect { result ->
     *         when (result) {
     *             is AuthResult.Loading -> showLoadingSpinner()
     *             is AuthResult.Success -> navigateToMainScreen(result.data)
     *             is AuthResult.Error -> showErrorMessage(result.message)
     *         }
     *     }
     * ```
     */
    operator fun invoke(email: String, password: String): Flow<AuthResult<String>> {
        require(email.isNotBlank()) { "Email не может быть пустым" }
        require(password.isNotBlank()) { "Пароль не может быть пустым" }

        return repository.login(email, password)
    }
}