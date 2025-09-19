package tech.nimbus.nimbin.ui.features.auth.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.domain.usecase.auth.LoginUseCase
import tech.nimbus.nimbin.domain.usecase.auth.SaveAuthTokenUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import tech.nimbus.nimbin.domain.usecase.auth.ContinueAsGuestUseCase

/**
 * Unit тесты для LoginViewModel.
 */
@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var saveAuthTokenUseCase: SaveAuthTokenUseCase
    private lateinit var continueAsGuestUseCase: ContinueAsGuestUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mock()
        saveAuthTokenUseCase = mock()
        continueAsGuestUseCase = mock()
        viewModel = LoginViewModel(loginUseCase, saveAuthTokenUseCase, continueAsGuestUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() = runTest {
        // Then
        assertEquals("", viewModel.email)
        assertEquals("", viewModel.password)
        assertEquals(null, viewModel.loginState)
    }

    @Test
    fun `onEmailChange should update email field`() = runTest {
        // Given
        val newEmail = "test@example.com"

        // When
        viewModel.onEmailChange(newEmail)

        // Then
        assertEquals(newEmail, viewModel.email)
    }

    @Test
    fun `onPasswordChange should update password field`() = runTest {
        // Given
        val newPassword = "password123"

        // When
        viewModel.onPasswordChange(newPassword)

        // Then
        assertEquals(newPassword, viewModel.password)
    }

    @Test
    fun `successful login should update state correctly`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val token = "jwt_token"

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        whenever(loginUseCase(email, password))
            .thenReturn(flowOf(
                AuthResult.Loading,
                AuthResult.Success(token)
            ))

        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.loginState is AuthResult.Success)
        assertEquals("", viewModel.password) // Password should be cleared after success
    }

    @Test
    fun `login failure should update state with error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        whenever(loginUseCase(email, password))
            .thenReturn(flowOf(
                AuthResult.Loading,
                AuthResult.Error(errorMessage)
            ))

        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.loginState is AuthResult.Error)
        assertEquals(errorMessage, (viewModel.loginState as AuthResult.Error).message)
    }

    @Test
    fun `login with empty email should show validation error`() = runTest {
        // Given
        val password = "password123"
        viewModel.onPasswordChange(password)

        // When
        viewModel.login()

        // Then
        assertTrue(viewModel.loginState is AuthResult.Error)
        assertEquals("Email не может быть пустым", (viewModel.loginState as AuthResult.Error).message)
    }

    @Test
    fun `onEmailChange should clear previous error`() = runTest {
        // Given - Set an error state first
        viewModel.onPasswordChange("password123")
        viewModel.login() // This will set error for empty email

        // When
        viewModel.onEmailChange("test@example.com")

        // Then
        assertEquals(null, viewModel.loginState)
    }
}
