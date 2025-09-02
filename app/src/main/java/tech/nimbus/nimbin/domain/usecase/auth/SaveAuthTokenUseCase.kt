package tech.nimbus.nimbin.domain.usecase.auth

import tech.nimbus.nimbin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for saving the authentication token after a successful login.
 * @param repository The authentication repository.
 */
class SaveAuthTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the use case.
     * @param token The authentication token to save.
     */
    suspend operator fun invoke(token: String) {
        repository.saveAuthToken(token)
    }
}
