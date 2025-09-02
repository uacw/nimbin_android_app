package tech.nimbus.nimbin.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.AuthResult
import javax.inject.Inject

/**
 * Use case responsible for handling the user registration operation.
 * It interacts with the [AuthRepository] to perform the actual registration.
 * @param repository The authentication repository to use for registration.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the registration operation.
     * @param email The user's email.
     * @param password The user's password.
     * @return A [Flow] emitting [AuthResult] to represent the state of the registration operation.
     */
    // suspend был удален отсюда
    operator fun invoke(username: String, email: String, password: String): Flow<AuthResult<Unit>> {
        return repository.register(username, email, password)
    }
}