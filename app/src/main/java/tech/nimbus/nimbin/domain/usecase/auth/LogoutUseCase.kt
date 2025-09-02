package tech.nimbus.nimbin.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.AuthResult
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // Убран suspend, так как repository.logout() уже возвращает Flow
    operator fun invoke(): Flow<AuthResult<Unit>> {
        return repository.logout()
    }
}
