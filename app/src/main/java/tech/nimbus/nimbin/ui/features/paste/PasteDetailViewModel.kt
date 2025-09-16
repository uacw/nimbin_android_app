package tech.nimbus.nimbin.ui.features.paste

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.nimbin.domain.usecase.paste.GetPasteUseCase
import tech.nimbus.nimbin.domain.usecase.profile.GetMyProfileUseCase
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject
import tech.nimbus.nimbin.domain.repository.PasteRepository

@HiltViewModel
class PasteDetailViewModel @Inject constructor(
    private val getPasteUseCase: GetPasteUseCase,
    private val authRepository: AuthRepository,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val pasteRepository: PasteRepository
) : ViewModel() {

    var state by mutableStateOf<PasteResult<PasteDto>?>(null)
        private set

    var currentUserId by mutableStateOf<String?>(null)
        private set

    init {
        // Загрузим текущего пользователя один раз
        viewModelScope.launch {
            val token = authRepository.getAuthToken().first()
            if (!token.isNullOrBlank()) {
                getMyProfileUseCase(token).collectLatest { res ->
                    if (res is tech.nimbus.nimbin.domain.repository.ProfileResult.Success) {
                        currentUserId = res.data.user.id
                    }
                }
            }
        }
    }

    fun load(id: String) {
        // всегда перезагружаем (исправляет неработающий Refresh и post-edit reload)
        state = PasteResult.Loading
        viewModelScope.launch {
            getPasteUseCase(id).collectLatest { result ->
                state = result
            }
        }
    }

    fun toggleFavorite() {
        val current = state
        if (current !is PasteResult.Success) return
        val paste = current.data
        val curFav = paste.isFavorite ?: return
        val target = !curFav
        // Оптимистичное обновление
        state = PasteResult.Success(paste.copy(isFavorite = target))
        viewModelScope.launch {
            pasteRepository.toggleFavorite(paste.id, target).collectLatest { res ->
                if (res is PasteResult.Error) {
                    // откат
                    state = PasteResult.Success(paste.copy(isFavorite = curFav))
                }
            }
        }
    }
}
