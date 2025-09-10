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
import tech.nimbus.nimbin.domain.usecase.paste.GetSyntaxLanguagesUseCase
import tech.nimbus.nimbin.domain.utils.StringProvider
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val createPasteUseCase: CreatePasteUseCase,
    private val authRepository: AuthRepository,
    private val stringProvider: StringProvider,
    private val getSyntaxLanguagesUseCase: GetSyntaxLanguagesUseCase
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set
    var visibility by mutableStateOf(PasteVisibility.PUBLIC)
        private set
    var syntaxLanguage by mutableStateOf("plaintext")
        private set

    var createState by mutableStateOf<PasteResult<PasteDto>?>(null)
        private set

    var languagesState by mutableStateOf<PasteResult<List<String>>?>(null)
        private set
    var syntaxLanguages by mutableStateOf(listOf<String>())
        private set

    private var authToken: String? = null

    init {
        viewModelScope.launch {
            // Берём текущий токен один раз (при необходимости можно подписаться постоянно)
            authToken = authRepository.getAuthToken().first()
        }
        loadSyntaxLanguages()
    }

    private fun loadSyntaxLanguages() {
        languagesState = PasteResult.Loading
        viewModelScope.launch {
            getSyntaxLanguagesUseCase().collectLatest { res ->
                languagesState = res
                if (res is PasteResult.Success) {
                    syntaxLanguages = res.data
                    if (!syntaxLanguages.contains(syntaxLanguage)) {
                        syntaxLanguage = syntaxLanguages.firstOrNull() ?: "plaintext"
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) { title = value }
    fun onContentChange(value: String) { content = value }
    fun onVisibilityChange(v: PasteVisibility) { visibility = v }
    fun onSyntaxLanguageChange(lang: String) { syntaxLanguage = lang }
    fun reloadLanguages() { loadSyntaxLanguages() }

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
            createPasteUseCase(title, content, visibility, syntaxLanguage).collectLatest { result ->
                createState = result
            }
        }
    }

    fun resetAfterSuccess() {
        title = ""
        content = ""
        visibility = PasteVisibility.PUBLIC
        syntaxLanguage = syntaxLanguages.firstOrNull() ?: "plaintext"
        createState = null
    }
}
