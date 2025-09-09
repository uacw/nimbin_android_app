package tech.nimbus.nimbin.ui.features.paste

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.nimbin.domain.usecase.paste.GetPasteUseCase
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject

@HiltViewModel
class PasteDetailViewModel @Inject constructor(
    private val getPasteUseCase: GetPasteUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf<PasteResult<PasteDto>?>(null)
        private set

    fun load(id: String) {
        val current = state
        if (current is PasteResult.Success && current.data.id == id) return
        state = PasteResult.Loading
        viewModelScope.launch {
            getPasteUseCase(id).collectLatest { result ->
                state = result
            }
        }
    }
}
