package tech.nimbus.nimbin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.nimbus.nimbin.data.preferences.UserPreferencesRepository
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.shared.dto.request.LoginRequestDto
import tech.nimbus.shared.dto.request.RegisterRequestDto
import tech.nimbus.shared.utils.ApiResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import tech.nimbus.nimbin.data.remote.NimbinApiClientImpl

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiClient: NimbinApiClient,
    private val rawApi: NimbinApiClientImpl
) : AuthRepository {

    override fun login(email: String, password: String): Flow<AuthResult<String>> = flow {
        emit(AuthResult.Loading)
        // Backend ожидает поле email
        val request = LoginRequestDto(email = email, password = password)
        when (val result = apiClient.login(request)) {
            is ApiResult.Success -> {
                val token = result.data.token
                userPreferencesRepository.saveAuthToken(token)
                userPreferencesRepository.saveIsGuest(false)
                emit(AuthResult.Success(token))
            }
            is ApiResult.Error -> {
                Timber.e("Login error: %s code=%s", result.message, result.code)
                emit(AuthResult.Error(result.message))
            }
        }
    }

    override fun register(username: String, email: String, password: String): Flow<AuthResult<Unit>> = flow {
        emit(AuthResult.Loading)
        val request = RegisterRequestDto(username = username, email = email, password = password)
        when (val result = apiClient.register(request)) {
            is ApiResult.Success -> {
                val token = result.data.token
                userPreferencesRepository.saveAuthToken(token)
                userPreferencesRepository.saveIsGuest(false)
                emit(AuthResult.Success(Unit))
            }
            is ApiResult.Error -> {
                Timber.e("Register error: %s code=%s", result.message, result.code)
                emit(AuthResult.Error(result.message))
            }
        }
    }

    override fun logout(): Flow<AuthResult<Unit>> = flow {
        emit(AuthResult.Loading)
        userPreferencesRepository.clearAuthToken()
        userPreferencesRepository.clearIsGuest()
        emit(AuthResult.Success(Unit))
    }

    override fun getAuthToken(): Flow<String?> = userPreferencesRepository.authToken
    override suspend fun saveAuthToken(token: String) = userPreferencesRepository.saveAuthToken(token)
    override suspend fun clearAuthToken() = userPreferencesRepository.clearAuthToken()

    override fun continueAsGuest(): Flow<AuthResult<String>> = flow {
        emit(AuthResult.Loading)
        when (val result = rawApi.guestAuth()) {
            is ApiResult.Success -> {
                val token = result.data.token
                userPreferencesRepository.saveAuthToken(token)
                userPreferencesRepository.saveIsGuest(true)
                emit(AuthResult.Success(token))
            }
            is ApiResult.Error -> {
                Timber.e("Guest auth error: %s code=%s", result.message, result.code)
                emit(AuthResult.Error(result.message))
            }
        }
    }

    override fun isGuest(): Flow<Boolean> = userPreferencesRepository.isGuest
}
