package tech.nimbus.nimbin.domain.usecase.auth

import tech.nimbus.nimbin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for clearing the authentication token upon logout.
 * @param repository The authentication repository.
 */
class ClearAuthTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the use case.
     */
    suspend operator fun invoke() {
        repository.clearAuthToken()
    }
}
