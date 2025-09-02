package tech.nimbus.nimbin.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tech.nimbus.nimbin.data.preferences.UserPreferencesRepository
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.shared.dto.AuthResponseDto
import tech.nimbus.shared.dto.UserDto
import tech.nimbus.shared.dto.request.LoginRequestDto
import tech.nimbus.shared.dto.request.RegisterRequestDto
import tech.nimbus.shared.utils.ApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit тесты для AuthRepositoryImpl.
 *
 * Проверяют:
 * - Корректное взаимодействие с API клиентом
 * - Управление локальными токенами через UserPreferences
 * - Преобразование ApiResult в AuthResult
 * - Обработку различных сценариев ответов сервера
 */
class AuthRepositoryImplTest {

    private lateinit var apiClient: NimbinApiClient
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        apiClient = mock()
        preferencesRepository = mock()
        authRepository = AuthRepositoryImpl(preferencesRepository, apiClient)
    }

    @Test
    fun `login success should save token and return success flow`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val token = "jwt_token_123"
        val user = UserDto(
            id = "user123",
            username = "testuser",
            email = email,
            displayName = "Test User",
            createdAt = "2025-01-20T12:00:00Z"
        )
        val authResponse = AuthResponseDto(token, user)

        whenever(apiClient.login(LoginRequestDto(email, password)))
            .thenReturn(ApiResult.Success(authResponse))

        // When & Then
        authRepository.login(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            assertEquals(AuthResult.Success(token), awaitItem())
            awaitComplete()
        }

        verify(preferencesRepository).saveAuthToken(token)
    }

    @Test
    fun `login failure should return error flow without saving token`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Неверные учетные данные"

        whenever(apiClient.login(LoginRequestDto(email, password)))
            .thenReturn(ApiResult.Error(errorMessage, 401))

        // When & Then
        authRepository.login(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals(errorMessage, (errorResult as AuthResult.Error).message)
            awaitComplete()
        }

        // Токен не должен сохраняться при ошибке
        verify(preferencesRepository, org.mockito.kotlin.never()).saveAuthToken(org.mockito.kotlin.any())
    }

    @Test
    fun `register success should save token and return success flow`() = runTest {
        // Given
        val username = "newuser"
        val email = "newuser@example.com"
        val password = "password123"
        val token = "jwt_new_token"
        val user = UserDto(
            id = "newuser123",
            username = username,
            email = email,
            displayName = null,
            createdAt = "2025-01-20T12:00:00Z"
        )
        val authResponse = AuthResponseDto(token, user)

        whenever(apiClient.register(RegisterRequestDto(username, email, password)))
            .thenReturn(ApiResult.Success(authResponse))

        // When & Then
        authRepository.register(username, email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            assertEquals(AuthResult.Success(Unit), awaitItem())
            awaitComplete()
        }

        verify(preferencesRepository).saveAuthToken(token)
    }

    @Test
    fun `register with existing email should return error`() = runTest {
        // Given
        val username = "newuser"
        val email = "existing@example.com"
        val password = "password123"
        val errorMessage = "Пользователь с таким email уже существует"

        whenever(apiClient.register(RegisterRequestDto(username, email, password)))
            .thenReturn(ApiResult.Error(errorMessage, 409))

        // When & Then
        authRepository.register(username, email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals(errorMessage, (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `logout should clear token and return success`() = runTest {
        // Given - пользователь авторизован
        whenever(preferencesRepository.clearAuthToken()).thenReturn(Unit)

        // When & Then
        authRepository.logout().test {
            assertEquals(AuthResult.Loading, awaitItem())
            assertEquals(AuthResult.Success(Unit), awaitItem())
            awaitComplete()
        }

        verify(preferencesRepository).clearAuthToken()
    }

    @Test
    fun `getAuthToken should return token from preferences`() = runTest {
        // Given
        val expectedToken = "stored_jwt_token"
        whenever(preferencesRepository.authToken)
            .thenReturn(flowOf(expectedToken))

        // When & Then
        authRepository.getAuthToken().test {
            assertEquals(expectedToken, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAuthToken should return null when no token stored`() = runTest {
        // Given
        whenever(preferencesRepository.authToken)
            .thenReturn(flowOf(null))

        // When & Then
        authRepository.getAuthToken().test {
            assertEquals(null, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `saveAuthToken should delegate to preferences repository`() = runTest {
        // Given
        val token = "new_token_to_save"

        // When
        authRepository.saveAuthToken(token)

        // Then
        verify(preferencesRepository).saveAuthToken(token)
    }

    @Test
    fun `clearAuthToken should delegate to preferences repository`() = runTest {
        // When
        authRepository.clearAuthToken()

        // Then
        verify(preferencesRepository).clearAuthToken()
    }

    @Test
    fun `login with network error should return network error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val networkError = "Сетевая ошибка"

        whenever(apiClient.login(LoginRequestDto(email, password)))
            .thenReturn(ApiResult.Error(networkError, null))

        // When & Then
        authRepository.login(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals(networkError, (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `multiple concurrent logins should be handled correctly`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val token = "jwt_token_concurrent"
        val user = UserDto(
            id = "user123",
            username = "testuser",
            email = email,
            displayName = "Test User",
            createdAt = "2025-01-20T12:00:00Z"
        )
        val authResponse = AuthResponseDto(token, user)

        whenever(apiClient.login(LoginRequestDto(email, password)))
            .thenReturn(ApiResult.Success(authResponse))

        // When - запускаем несколько логинов одновременно
        val flow1 = authRepository.login(email, password)
        val flow2 = authRepository.login(email, password)

        // Then - оба должны работать корректно
        flow1.test {
            assertEquals(AuthResult.Loading, awaitItem())
            val successResult = awaitItem()
            assertTrue(successResult is AuthResult.Success)
            assertEquals(token, (successResult as AuthResult.Success).data)
            awaitComplete()
        }

        flow2.test {
            assertEquals(AuthResult.Loading, awaitItem())
            val successResult = awaitItem()
            assertTrue(successResult is AuthResult.Success)
            assertEquals(token, (successResult as AuthResult.Success).data)
            awaitComplete()
        }
    }
}
