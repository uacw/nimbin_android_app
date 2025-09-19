package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject


data class FavoritesUiState(
    val items: List<PasteDto> = emptyList(),
    val page: Int = 1,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val tokenMissing: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val pasteRepository: PasteRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    var uiState by mutableStateOf(FavoritesUiState())
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
                    items = emptyList(), tokenMissing = true,
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
            pasteRepository.getMyPastesFavoriteOnly(t, page).collectLatest { result ->
                when (result) {
                    is PasteResult.Loading -> {}
                    is PasteResult.Error -> {
                        uiState = if (append) {
                            uiState.copy(isLoadingMore = false, error = result.message)
                        } else {
                            uiState.copy(
                                tokenMissing = false,
                                isRefreshing = false,
                                error = result.message,
                                items = emptyList()
                            )
                        }
                    }
                    is PasteResult.Success -> {
                        val newItems = if (append) uiState.items + result.data else result.data
                        val endReached = result.data.isEmpty()
                        uiState = uiState.copy(
                            items = newItems,
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

    fun toggleFavorite(pasteId: String, current: Boolean?) {
        val target = !(current ?: false)
        val itemsUpd = uiState.items.map { if (it.id == pasteId) it.copy(isFavorite = target) else it }
        uiState = uiState.copy(items = itemsUpd)
        viewModelScope.launch {
            pasteRepository.toggleFavorite(pasteId, target).collectLatest { res ->
                if (res is PasteResult.Error) {
                    val rollback = !target
                    val itemsRb = uiState.items.map { if (it.id == pasteId) it.copy(isFavorite = rollback) else it }
                    uiState = uiState.copy(items = itemsRb, error = res.message)
                } else if (target == false) {
                    // Если сняли из избранного — удаляем из списка
                    uiState = uiState.copy(items = uiState.items.filter { it.id != pasteId })
                }
            }
        }
    }
}

