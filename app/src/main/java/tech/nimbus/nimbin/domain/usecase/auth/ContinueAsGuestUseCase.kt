package tech.nimbus.nimbin.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.AuthResult
import javax.inject.Inject

class ContinueAsGuestUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthResult<String>> = authRepository.continueAsGuest()
}

