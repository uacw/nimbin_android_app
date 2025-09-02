package tech.nimbus.nimbin.ui.features.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.domain.usecase.auth.RegisterUseCase
import javax.inject.Inject

/**
 * ViewModel for the registration screen, handling UI state and registration logic.
 * @param registerUseCase The use case for performing registration operations.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var username by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    // null = Idle (ничего не происходило)
    var registerState by mutableStateOf<AuthResult<Unit>?>(null)
        private set

    /**
     * Updates the username state.
     * @param newUsername The new username value.
     */
    fun onUsernameChange(newUsername: String) {
        username = newUsername
    }

    /**
     * Updates the email state.
     * @param newEmail The new email value.
     */
    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    /**
     * Updates the password state.
     * @param newPassword The new password value.
     */
    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    /**
     * Updates the confirm password state.
     * @param newConfirmPassword The new confirm password value.
     */
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
    }

    /**
     * Initiates the registration process.
     */
    fun register() {
        if (password != confirmPassword) {
            registerState = AuthResult.Error("Passwords do not match.")
            return
        }
        registerState = AuthResult.Loading
        viewModelScope.launch {
            registerUseCase(username, email, password).collectLatest { result ->
                registerState = result
            }
        }
    }
}