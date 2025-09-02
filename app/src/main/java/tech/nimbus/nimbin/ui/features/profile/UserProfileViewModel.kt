package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.ProfileRepository
import tech.nimbus.nimbin.domain.repository.ProfileResult
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.UserProfileDto
import javax.inject.Inject

data class UserProfileUiState(
    val profile: UserProfileDto? = null,
    val headerTitle: String? = null,
    val pastes: List<PasteDto> = emptyList(),
    val page: Int = 1,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val userId: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(UserProfileUiState())
        private set

    private var loadJob: Job? = null

    fun init(userId: String) {
        if (uiState.userId == userId && uiState.profile != null) return
        uiState = UserProfileUiState(userId = userId, loading = true)
        loadProfile(userId)
    }

    fun refresh() {
        val userId = uiState.userId ?: return
        uiState = uiState.copy(loading = true, error = null, page = 1, endReached = false, pastes = emptyList())
        loadProfile(userId)
    }

    private fun loadProfile(userId: String) {
        viewModelScope.launch {
            profileRepository.getUserProfile(userId).collectLatest { res ->
                when (res) {
                    is ProfileResult.Loading -> uiState = uiState.copy(loading = true)
                    is ProfileResult.Error -> uiState = uiState.copy(loading = false, error = res.message)
                    is ProfileResult.Success -> {
                        val p = res.data
                        uiState = uiState.copy(
                            profile = p,
                            headerTitle = p.user.displayName ?: p.user.username,
                            loading = false,
                            error = null
                        )
                        // затем загрузим первую страницу
                        loadPage(1, append = false)
                    }
                }
            }
        }
    }

    fun loadNextPage(force: Boolean = false) {
        if (uiState.loadingMore || uiState.endReached || uiState.loading) return
        loadPage(uiState.page + 1, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        val userId = uiState.userId ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (append) uiState = uiState.copy(loadingMore = true, error = null)
            profileRepository.getUserPublicPastes(userId, page).collectLatest { res ->
                when (res) {
                    is ProfileResult.Loading -> {}
                    is ProfileResult.Error -> uiState = if (append) {
                        uiState.copy(loadingMore = false, error = res.message)
                    } else {
                        uiState.copy(loading = false, error = res.message, pastes = emptyList())
                    }
                    is ProfileResult.Success -> {
                        val newList = if (append) uiState.pastes + res.data else res.data
                        uiState = uiState.copy(
                            pastes = newList,
                            page = page,
                            loading = false,
                            loadingMore = false,
                            endReached = res.data.isEmpty(),
                            error = null
                        )
                    }
                }
            }
        }
    }
}

