package tech.nimbus.nimbin.domain.usecase.auth

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit тесты для LoginUseCase.
 */
class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        authRepository = mock()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `valid credentials should return success flow`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val token = "jwt_token"

        whenever(authRepository.login(email, password))
            .thenReturn(kotlinx.coroutines.flow.flowOf(
                AuthResult.Loading,
                AuthResult.Success(token)
            ))

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            assertEquals(AuthResult.Success(token), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invalid credentials should return error flow`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        whenever(authRepository.login(email, password))
            .thenReturn(kotlinx.coroutines.flow.flowOf(
                AuthResult.Loading,
                AuthResult.Error(errorMessage)
            ))

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals(errorMessage, (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `empty email should throw exception`() = runTest {
        // Given
        val email = ""
        val password = "password123"

        // When & Then
        try {
            loginUseCase(email, password).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `empty password should throw exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = ""

        // When & Then
        try {
            loginUseCase(email, password).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `blank email should throw exception`() = runTest {
        // Given
        val email = "   "
        val password = "password123"

        // When & Then
        try {
            loginUseCase(email, password).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `blank password should throw exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "   "

        // When & Then
        try {
            loginUseCase(email, password).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `network error should return error flow`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val networkError = "Network error"

        whenever(authRepository.login(email, password))
            .thenReturn(kotlinx.coroutines.flow.flowOf(
                AuthResult.Loading,
                AuthResult.Error(networkError)
            ))

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals(networkError, (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `server error should return error flow`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val serverError = "Server error"

        whenever(authRepository.login(email, password))
            .thenReturn(kotlinx.coroutines.flow.flowOf(
                AuthResult.Loading,
                AuthResult.Error(serverError)
            ))

        // When & Then
        loginUseCase(email, password).test {
            assertEquals(AuthResult.Loading, awaitItem())
            val errorResult = awaitItem()
            assertTrue(errorResult is AuthResult.Error)
            assertEquals(serverError, (errorResult as AuthResult.Error).message)
            awaitComplete()
        }
    }
}
