package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import javax.inject.Inject

enum class MyPastesFilter { ALL, PUBLIC, UNLISTED, PRIVATE }

data class MyPastesUiState(
    val items: List<PasteDto> = emptyList(),
    val filtered: List<PasteDto> = emptyList(),
    val page: Int = 1,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val filter: MyPastesFilter = MyPastesFilter.ALL,
    val tokenMissing: Boolean = false
)

@HiltViewModel
class MyPastesViewModel @Inject constructor(
    private val pasteRepository: PasteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(MyPastesUiState())
        private set

    private var loadJob: Job? = null
    private var token: String? = null

    init { refresh() }

    fun refresh() {
        if (uiState.isRefreshing) return
        viewModelScope.launch {
            token = authRepository.getAuthToken().first()
            if (token.isNullOrBlank()) {
                uiState = uiState.copy(
                    items = emptyList(), filtered = emptyList(), tokenMissing = true,
                    error = null, isRefreshing = false, page = 1, endReached = true
                )
                return@launch
            }
            uiState = uiState.copy(isRefreshing = true, error = null, page = 1, endReached = false, tokenMissing = false)
            loadPage(1, append = false)
        }
    }

    fun loadNextPage() {
        if (uiState.isLoadingMore || uiState.endReached || uiState.isRefreshing || token.isNullOrBlank()) return
        val next = uiState.page + 1
        uiState = uiState.copy(isLoadingMore = true, error = null)
        loadPage(next, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        loadJob?.cancel()
        val t = token ?: return
        loadJob = viewModelScope.launch {
            pasteRepository.getUserPastes(t, page).collectLatest { result ->
                when (result) {
                    is PasteResult.Loading -> {}
                    is PasteResult.Error -> {
                        // Проверяем на истечение токена
                        if (isTokenExpiredError(result.message)) {
                            authRepository.clearAuthToken()
                            // Токен истек, показываем состояние "требуется вход"
                            uiState = uiState.copy(
                                tokenMissing = true,
                                isRefreshing = false,
                                isLoadingMore = false,
                                error = null,
                                items = emptyList(),
                                filtered = emptyList()
                            )
                        } else {
                            uiState = if (append) {
                                uiState.copy(isLoadingMore = false, error = result.message)
                            } else {
                                uiState.copy(isRefreshing = false, error = result.message, items = emptyList(), filtered = emptyList())
                            }
                        }
                    }
                    is PasteResult.Success -> {
                        val newItems = if (append) uiState.items + result.data else result.data
                        val filtered = applyFilter(newItems, uiState.filter)
                        val endReached = result.data.isEmpty()
                        uiState = uiState.copy(
                            items = newItems,
                            filtered = filtered,
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

    fun setFilter(filter: MyPastesFilter) {
        if (uiState.filter == filter) return
        val filtered = applyFilter(uiState.items, filter)
        uiState = uiState.copy(filter = filter, filtered = filtered)
    }

    private fun isTokenExpiredError(message: String): Boolean {
        return message.contains("Token is not valid", ignoreCase = true) ||
               message.contains("token expired", ignoreCase = true) ||
               message.contains("not authorized", ignoreCase = true)
    }

    private fun applyFilter(list: List<PasteDto>, filter: MyPastesFilter): List<PasteDto> = when (filter) {
        MyPastesFilter.ALL -> list
        MyPastesFilter.PUBLIC -> list.filter { it.visibility == PasteVisibility.PUBLIC }
        MyPastesFilter.UNLISTED -> list.filter { it.visibility == PasteVisibility.UNLISTED }
        MyPastesFilter.PRIVATE -> list.filter { it.visibility == PasteVisibility.PRIVATE }
    }
}
