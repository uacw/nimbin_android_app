package tech.nimbus.nimbin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tech.nimbus.nimbin.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthStatusViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val isGuest: StateFlow<Boolean> = authRepository.isGuest()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

