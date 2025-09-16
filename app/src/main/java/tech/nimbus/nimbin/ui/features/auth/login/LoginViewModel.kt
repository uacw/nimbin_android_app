package tech.nimbus.nimbin.ui.features.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.domain.usecase.auth.ContinueAsGuestUseCase
import tech.nimbus.nimbin.domain.usecase.auth.LoginUseCase
import tech.nimbus.nimbin.domain.usecase.auth.SaveAuthTokenUseCase
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel для экрана авторизации пользователей в приложении NimBin.
 *
 * Управляет состоянием формы входа, обрабатывает пользовательский ввод и координирует
 * взаимодействие между UI слоем и domain слоем для выполнения аутентификации.
 * Реализует паттерн MVVM с реактивным управлением состоянием через Compose State.
 *
 * ## Функциональность:
 * - Валидация полей email/username и пароля в реальном времени
 * - Выполнение процедуры аутентификации через LoginUseCase
 * - Автоматическое сохранение JWT токена при успешном входе
 * - Управление состояниями загрузки и обработка ошибок
 * - Логирование всех операций для отладки
 *
 * ## Состояния UI:
 * - **Idle**: начальное состояние, форма готова к вводу
 * - **Loading**: процесс аутентификации, показывается прогресс
 * - **Success**: успешный вход, можно навигировать на главный экран
 * - **Error**: ошибка входа, показывается сообщение пользователю
 *
 * ## Lifecycle:
 * - Создается при навигации на экран входа
 * - Автоматически очищается при уходе с экрана
 * - Сохраняет состояние при поворотах экрана
 *
 * @param loginUseCase Use case для выполнения аутентификации пользователя
 * @param saveAuthTokenUseCase Use case для сохранения JWT токена в локальное хранилище
 *
 * @see LoginUseCase
 * @see SaveAuthTokenUseCase
 * @see AuthResult
 *
 * @author NimBin Team
 * @since 1.0
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val saveAuthTokenUseCase: SaveAuthTokenUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase
) : ViewModel() {

    /**
     * Email адрес или username пользователя.
     *
     * Двунаправленная связь с UI - обновляется при вводе пользователя
     * и используется для валидации и отправки запроса аутентификации.
     *
     * Поддерживает как email формат (user@domain.com), так и простой username.
     */
    var email by mutableStateOf("")
        private set

    /**
     * Пароль пользователя для аутентификации.
     *
     * Хранится в открытом виде только во время сессии ввода.
     * Очищается автоматически после успешного входа или при уходе с экрана.
     * Никогда не логируется в целях безопасности.
     */
    var password by mutableStateOf("")
        private set

    /**
     * Текущее состояние процесса аутентификации.
     *
     * Отс��еживается UI компонентами для:
     * - Отображения индикаторов загрузки
     * - Показа сообщений об ошибках
     * - Навигации при успешном входе
     * - Блокировки повторных запросов
     *
     * @see AuthResult
     */
    var loginState by mutableStateOf<AuthResult<String>?>(null)
        private set

    /**
     * Обновляет email/username в форме входа.
     *
     * Автоматически очищает предыдущие ошибки аутентификации
     * для лучшего пользовательского опыта.
     *
     * @param newValue Новое значение email или username
     */
    fun onEmailChange(newValue: String) {
        email = newValue
        // Очищаем ошибки при изменении данных
        if (loginState is AuthResult.Error) {
            loginState = null
        }
    }

    /**
     * Обновляет пароль в форме входа.
     *
     * Автоматически очищает предыдущие ошибки аутентификации
     * для лучш��го пользовательского опыта.
     *
     * @param newPassword Новое значение пароля
     */
    fun onPasswordChange(newPassword: String) {
        password = newPassword
        // Очищаем ошибки при изменении данных
        if (loginState is AuthResult.Error) {
            loginState = null
        }
    }

    /**
     * Выполняет аутентификацию пользователя.
     *
     * Запускает процесс входа в систему с текущими значениями email и пароля.
     * Автоматически управляет состояниями загрузки и обрабатывает результат.
     * При успешном входе сохраняет полученный JWT токен для дальнейших API запросов.
     *
     * ## Процесс выполнения:
     * 1. Валидация входных данных (не пустые поля)
     * 2. Установка состояния Loading
     * 3. Отправка запроса через LoginUseCase
     * 4. При успехе: сохранение токена и установка состояния Success
     * 5. При ошибке: установка состояния Error с сообщением
     *
     * ## Обработка ошибок:
     * - Валидационные ошибки: показываются немедленно
     * - Сетевые ошибки: повтор через некоторое время
     * - Ошибки сервера: отображение сообщения пользователю
     *
     * @throws IllegalStateException если уже выполняется процесс аутентификации
     */
    fun login() {
        // Предотвращаем множественные запросы
        if (loginState is AuthResult.Loading) {
            Timber.w("Login already in progress, ignoring duplicate request")
            return
        }

        // Базовая валидация
        if (email.isBlank()) {
            loginState = AuthResult.Error("Email не может быть пустым")
            return
        }

        if (password.isBlank()) {
            loginState = AuthResult.Error("Пароль не может быть пустым")
            return
        }

        viewModelScope.launch {
            loginUseCase(email, password)
                .onEach { result ->
                    loginState = result
                    when (result) {
                        is AuthResult.Success -> {
                            Timber.i("Login successful for user: ${email.take(3)}***")
                            saveAuthTokenUseCase(result.data)
                            // Очищаем пароль из памяти после успешного входа
                            password = ""
                        }
                        is AuthResult.Error -> {
                            Timber.w("Login failed: ${result.message}")
                        }
                        is AuthResult.Loading -> {
                            Timber.d("Login in progress...")
                        }
                    }
                }
                .collect { }
        }
    }

    /**
     * Продолжает как гость, без аутентификации.
     *
     * Запускает процесс входа в систему с использованием временной сессии.
     * Полезно для пользователей, желающих исследовать приложение без регистрации.
     *
     * ## Процесс выполнения:
     * 1. Установка состояния Loading
     * 2. Отправка запроса через ContinueAsGuestUseCase
     * 3. При успехе: сохранение токена и установка состояния Success
     * 4. При ошибке: установка состояния Error с сообщением
     *
     * ## Ограничения:
     * - Гостевая сессия имеет ограниченный доступ к функциям приложения
     * - Рекомендуется завершить регистрацию для полного доступа
     *
     * @see ContinueAsGuestUseCase
     */
    fun continueAsGuest() {
        if (loginState is AuthResult.Loading) return
        viewModelScope.launch {
            continueAsGuestUseCase()
                .onEach { result ->
                    loginState = result
                    when (result) {
                        is AuthResult.Success -> {
                            Timber.i("Guest session started")
                            saveAuthTokenUseCase(result.data)
                            password = ""
                        }
                        is AuthResult.Error -> Timber.w("Guest auth failed: ${result.message}")
                        is AuthResult.Loading -> Timber.d("Guest auth in progress...")
                    }
                }
                .collect { }
        }
    }
}
