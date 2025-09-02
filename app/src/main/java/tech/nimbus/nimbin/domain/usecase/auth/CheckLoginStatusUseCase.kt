package tech.nimbus.nimbin.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for checking the current login status by observing the auth token.
 * @param repository The authentication repository.
 */
class CheckLoginStatusUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the use case.
     * @return A [Flow] emitting the auth token, or null if not logged in.
     */
    operator fun invoke(): Flow<String?> {
        return repository.getAuthToken()
    }
}
