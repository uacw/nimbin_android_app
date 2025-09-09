package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.domain.repository.ProfileResult
import tech.nimbus.nimbin.domain.usecase.auth.ClearAuthTokenUseCase
import tech.nimbus.nimbin.domain.usecase.auth.LogoutUseCase
import tech.nimbus.nimbin.domain.usecase.profile.GetMyProfileUseCase
import tech.nimbus.nimbin.domain.usecase.profile.UpdateProfileUseCase
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.utils.StringProvider
import tech.nimbus.shared.dto.UserProfileDto
import javax.inject.Inject

sealed class MyProfileUiState {
    object Idle: MyProfileUiState()
    object Loading: MyProfileUiState()
    data class Data(val profile: UserProfileDto): MyProfileUiState()
    data class Error(val message: String): MyProfileUiState()
    data class Updating(val profile: UserProfileDto): MyProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val clearAuthTokenUseCase: ClearAuthTokenUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val authRepository: AuthRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    var logoutState by mutableStateOf<AuthResult<Unit>?>(null)
        private set

    var profileState by mutableStateOf<MyProfileUiState>(MyProfileUiState.Idle)
        private set

    private var token: String? = null

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            profileState = MyProfileUiState.Loading
            token = authRepository.getAuthToken().first()
            val t = token
            if (t.isNullOrBlank()) {
                profileState = MyProfileUiState.Error(stringProvider.getString(R.string.error_not_authorized))
                return@launch
            }
            getMyProfileUseCase(t).collectLatest { res ->
                when (res) {
                    is ProfileResult.Loading -> profileState = MyProfileUiState.Loading
                    is ProfileResult.Error -> {
                        profileState = MyProfileUiState.Error(res.message)
                    }
                    is ProfileResult.Success -> profileState = MyProfileUiState.Data(res.data)
                }
            }
        }
    }

    fun logout() {
        if (logoutState is AuthResult.Loading) return
        logoutState = AuthResult.Loading
        viewModelScope.launch {
            logoutUseCase().collectLatest { result ->
                logoutState = result
                if (result is AuthResult.Success) {
                    try { clearAuthTokenUseCase() } catch (_: Exception) {}
                }
            }
        }
    }

    fun updateProfile(username: String?, displayName: String?) {
        val current = (profileState as? MyProfileUiState.Data)?.profile ?: return
        val t = token ?: return
        profileState = MyProfileUiState.Updating(current)
        viewModelScope.launch {
            updateProfileUseCase(t, username, displayName).collectLatest { res ->
                when (res) {
                    is ProfileResult.Loading -> {}
                    is ProfileResult.Error -> {
                        val msg = when (res.code) {
                            409 -> stringProvider.getString(R.string.error_username_taken)
                            400 -> stringProvider.getString(R.string.error_profile_validation)
                            else -> res.message
                        }
                        profileState = MyProfileUiState.Error(msg)
                    }
                    is ProfileResult.Success -> loadProfile()
                }
            }
        }
    }

    fun resetLogout() {
        logoutState = null
    }
}
