package tech.nimbus.nimbin.ui.features.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.nimbin.domain.usecase.paste.CreatePasteUseCase
import tech.nimbus.nimbin.domain.utils.StringProvider
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val createPasteUseCase: CreatePasteUseCase,
    private val authRepository: AuthRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set
    var visibility by mutableStateOf(PasteVisibility.PUBLIC)
        private set
    var createState by mutableStateOf<PasteResult<PasteDto>?>(null)
        private set

    private var authToken: String? = null

    init {
        viewModelScope.launch {
            // Берём текущий токен один раз (при необходимости можно подписаться постоянно)
            authToken = authRepository.getAuthToken().first()
        }
    }

    fun onTitleChange(value: String) { title = value }
    fun onContentChange(value: String) { content = value }
    fun onVisibilityChange(v: PasteVisibility) { visibility = v }

    fun create() {
        if (title.isBlank() || content.isBlank()) {
            createState = PasteResult.Error(stringProvider.getString(R.string.create_note_fields_required))
            return
        }

        val token = authToken
        if (token.isNullOrBlank()) {
            createState = PasteResult.Error(stringProvider.getString(R.string.error_authentication_required))
            return
        }

        createState = PasteResult.Loading
        viewModelScope.launch {
            createPasteUseCase(title, content, visibility).collectLatest { result ->
                if (result is PasteResult.Error) {
                    // Проверяем на истечение токена
                    if (isTokenExpiredError(result.message)) {
                        authRepository.clearAuthToken()
                        createState = PasteResult.Error("Session expired. Please log in again.")
                        return@collectLatest
                    }
                }
                createState = result
            }
        }
    }

    private fun isTokenExpiredError(message: String): Boolean {
        return message.contains("Token is not valid", ignoreCase = true) ||
               message.contains("token expired", ignoreCase = true) ||
               message.contains("not authorized", ignoreCase = true)
    }

    fun resetAfterSuccess() {
        title = ""
        content = ""
        visibility = PasteVisibility.PUBLIC
        createState = null
    }
}
