package tech.nimbus.nimbin.ui.features.home

import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.domain.usecase.auth.LogoutUseCase
import tech.nimbus.nimbin.domain.usecase.auth.ClearAuthTokenUseCase
import tech.nimbus.nimbin.domain.usecase.paste.GetPublicPastesUseCase
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto

data class PublicPastesUiState(
    val items: List<PasteDto> = emptyList(),
    val page: Int = 1,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val clearAuthTokenUseCase: ClearAuthTokenUseCase,
    private val getPublicPastesUseCase: GetPublicPastesUseCase
) : ViewModel() {

    // Изменено начальное состояние на null
    var logoutState by mutableStateOf<AuthResult<Unit>?>(null)
        private set

    var publicUiState by mutableStateOf(PublicPastesUiState())
        private set
    private var loadJob: Job? = null

    init { refresh() }

    fun logout() {
        // Устанавливаем Loading перед вызовом use case
        logoutState = AuthResult.Loading
        viewModelScope.launch {
            logoutUseCase().collectLatest { result ->
                logoutState = result
                if (result is AuthResult.Success) {
                    // Очистка токена локально
                    clearAuthTokenSafely()
                }
            }
        }
    }

    private fun clearAuthTokenSafely() {
        viewModelScope.launch {
            try { clearAuthTokenUseCase() } catch (_: Exception) { }
        }
    }

    fun resetLogoutState() { logoutState = null }

    fun refresh() {
        if (publicUiState.isRefreshing) return
        publicUiState = publicUiState.copy(isRefreshing = true, error = null, page = 1, endReached = false)
        loadPage(1, append = false)
    }

    fun loadNextPage() {
        if (publicUiState.isLoadingMore || publicUiState.endReached || publicUiState.isRefreshing) return
        val next = publicUiState.page + 1
        publicUiState = publicUiState.copy(isLoadingMore = true, error = null)
        loadPage(next, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getPublicPastesUseCase(page).collectLatest { result ->
                when (result) {
                    is PasteResult.Loading -> { /* отдельные флаги уже учтены */ }
                    is PasteResult.Error -> {
                        publicUiState = if (append) {
                            publicUiState.copy(isLoadingMore = false, error = result.message)
                        } else {
                            publicUiState.copy(isRefreshing = false, error = result.message, items = emptyList())
                        }
                    }
                    is PasteResult.Success -> {
                        val newList = if (append) publicUiState.items + result.data else result.data
                        val endReached = result.data.isEmpty()
                        publicUiState = publicUiState.copy(
                            items = newList,
                            page = page,
                            isRefreshing = false,
                            isLoadingMore = false,
                            error = null,
                            endReached = endReached
                        )
                    }
                }
            }
        }
    }

    fun loadPublicPastes(page: Int = 1) = refresh() // совместимость со старым вызовом
}
