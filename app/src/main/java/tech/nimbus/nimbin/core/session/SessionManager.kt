package tech.nimbus.nimbin.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

sealed class SessionEvent {
    object SessionExpired : SessionEvent()
}

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SessionEvent> = _events

    /**
     * Возвращает true, если ошибка аутентификации обработана (токен очищен, событие отправлено).
     */
    fun handleAuthError(message: String?, code: Int?): Boolean {
        val isAuthError = code == 401 || code == 403 || (message?.contains("Token is not valid", true) == true) || (message?.contains("token expired", true) == true)
        if (!isAuthError) return false
        scope.launch {
            try { authRepository.clearAuthToken() } catch (_: Exception) {}
            _events.emit(SessionEvent.SessionExpired)
        }
        return true
    }
}

