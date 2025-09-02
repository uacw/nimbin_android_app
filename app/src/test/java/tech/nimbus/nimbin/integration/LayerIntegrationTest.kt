package tech.nimbus.nimbin.integration

import app.cash.turbine.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbus.nimbin.data.preferences.UserPreferencesRepository
import tech.nimbus.nimbin.data.remote.NimbinApiClientImpl
import tech.nimbus.nimbin.data.remote.TokenProvider
import tech.nimbus.nimbin.data.repository.AuthRepositoryImpl
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.domain.usecase.auth.LoginUseCase
import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.shared.dto.AuthResponseDto
import tech.nimbus.shared.dto.UserDto
import tech.nimbus.shared.dto.request.LoginRequestDto
import tech.nimbus.shared.utils.ApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Интеграционные тесты между слоями приложения.
 *
 * Тестируют взаимодействие:
 * - API Client -> Repository -> UseCase
 * - Реальные HTTP запросы через MockEngine
 * - End-to-end сценарии без UI
 */
class LayerIntegrationTest {

    private lateinit var tokenProvider: TokenProvider
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var apiConfig: ApiConfig

    @Before
    fun setup() {
        tokenProvider = mock()
        preferencesRepository = mock()
        apiConfig = ApiConfig(baseUrl = "https://test-api.nimbin.com")
    }

    private fun createIntegratedLoginUseCase(responseJson: String, status: HttpStatusCode = HttpStatusCode.OK): LoginUseCase {
        val mockEngine = MockEngine { request ->
            respond(
                content = responseJson,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)
        val authRepository = AuthRepositoryImpl(preferencesRepository, apiClient)
        return LoginUseCase(authRepository)
    }

    @Test
    fun `successful login flow should work end-to-end`() = runTest {
        // Given
        val email = "integration@test.com"
        val password = "password123"
        val token = "integration_jwt_token"

        val responseJson = """
            {
                "token": "$token",
                "user": {
                    "id": "user_integration_123",
                    "username": "integrationuser",
                    "email": "$email",
                    "displayName": "Integration User",
                    "createdAt": "2025-01-20T12:00:00Z"
                }
            }
        """.trimIndent()

        val loginUseCase = createIntegratedLoginUseCase(responseJson)

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val successResult = awaitItem()
            assertTrue(successResult is AuthResult.Success)
            assertEquals(token, (successResult as AuthResult.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `failed login should propagate error through all layers`() = runTest {
        // Given
        val email = "wrong@test.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        val errorJson = """{"error": "$errorMessage"}"""
        val loginUseCase = createIntegratedLoginUseCase(errorJson, HttpStatusCode.Unauthorized)

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals("Illegal input: Fields [token, user] are required for type with serial name 'tech.nimbus.shared.dto.AuthResponseDto', but they were missing at path: $", (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `network error should be handled across layers`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val networkError = "Network timeout"

        val errorJson = """{"error": "$networkError"}"""
        val loginUseCase = createIntegratedLoginUseCase(errorJson, HttpStatusCode.RequestTimeout)

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals("Illegal input: Fields [token, user] are required for type with serial name 'tech.nimbus.shared.dto.AuthResponseDto', but they were missing at path: $", (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `server error should be handled properly`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val serverError = "Internal server error"

        val errorJson = """{"error": "$serverError"}"""
        val loginUseCase = createIntegratedLoginUseCase(errorJson, HttpStatusCode.InternalServerError)

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals("Illegal input: Fields [token, user] are required for type with serial name 'tech.nimbus.shared.dto.AuthResponseDto', but they were missing at path: $", (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `malformed response should be handled gracefully`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"

        val malformedJson = """{"invalid": "json", "missing": }"""
        val loginUseCase = createIntegratedLoginUseCase(malformedJson)

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals("Illegal input: Unexpected JSON token at offset 31: Expected beginning of the string, but got } at path: $\nJSON input: {\"invalid\": \"json\", \"missing\": }", (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `authentication token should be saved after successful login`() = runTest {
        // Given
        val email = "token@test.com"
        val password = "password123"
        val token = "token_to_save"

        val responseJson = """
            {
                "token": "$token",
                "user": {
                    "id": "user_token_123",
                    "username": "tokenuser",
                    "email": "$email",
                    "displayName": "Token User",
                    "createdAt": "2025-01-20T12:00:00Z"
                }
            }
        """.trimIndent()

        val loginUseCase = createIntegratedLoginUseCase(responseJson)

        // Mock preferences to verify token saving
        whenever(preferencesRepository.saveAuthToken(token)).thenReturn(Unit)

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val successResult = awaitItem()
            assertTrue(successResult is AuthResult.Success)
            assertEquals(token, (successResult as AuthResult.Success).data)
            awaitComplete()
        }

        // Verify token was saved
        org.mockito.kotlin.verify(preferencesRepository).saveAuthToken(token)
    }
}
