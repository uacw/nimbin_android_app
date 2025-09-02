package tech.nimbus.nimbin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.usecase.auth.CheckLoginStatusUseCase
import tech.nimbus.nimbin.ui.navigation.Graph
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val startDestination: String = Graph.AUTHENTICATION
)

@HiltViewModel
class MainViewModel @Inject constructor(
    checkLoginStatusUseCase: CheckLoginStatusUseCase
) : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set

    init {
        viewModelScope.launch {
            val authToken = checkLoginStatusUseCase().first()
            uiState = if (authToken != null) {
                MainUiState(isLoading = false, startDestination = Graph.MAIN_APP)
            } else {
                MainUiState(isLoading = false, startDestination = Graph.AUTHENTICATION)
            }
        }
    }
}
