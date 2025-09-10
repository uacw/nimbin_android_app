package tech.nimbus.nimbin.ui.features.paste

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.nimbin.domain.usecase.paste.GetPasteUseCase
import tech.nimbus.nimbin.domain.usecase.paste.UpdatePasteUseCase
import tech.nimbus.nimbin.domain.usecase.paste.GetSyntaxLanguagesUseCase
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import javax.inject.Inject

@HiltViewModel
class EditPasteViewModel @Inject constructor(
    private val getPasteUseCase: GetPasteUseCase,
    private val updatePasteUseCase: UpdatePasteUseCase,
    private val getSyntaxLanguagesUseCase: GetSyntaxLanguagesUseCase
) : ViewModel() {

    // Editable fields
    var title by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set
    var visibility by mutableStateOf(PasteVisibility.PUBLIC)
        private set
    var syntaxLanguage by mutableStateOf("plaintext")
        private set
    var expiresAt: String? by mutableStateOf(null)
        private set

    // Syntax languages
    var languagesState by mutableStateOf<PasteResult<List<String>>?>(null)
        private set
    var syntaxLanguages by mutableStateOf(listOf<String>())
        private set

    // Backend data
    private var pasteId: String? = null
    private var etag: String? = null

    // UI state
    var loadState by mutableStateOf<PasteResult<PasteDto>?>(null)
        private set
    var saveState by mutableStateOf<PasteResult<PasteDto>?>(null)
        private set
    var conflict by mutableStateOf(false)
        private set

    init {
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

    fun reloadLanguages() { loadSyntaxLanguages() }

    fun load(id: String) {
        if (pasteId == id && loadState is PasteResult.Success) return
        pasteId = id
        loadState = PasteResult.Loading
        viewModelScope.launch {
            getPasteUseCase(id).collectLatest { result ->
                loadState = result
                if (result is PasteResult.Success) {
                    val dto = result.data
                    title = dto.title
                    content = dto.content
                    visibility = dto.visibility
                    syntaxLanguage = dto.syntaxLanguage
                    expiresAt = dto.expiresAt
                    etag = dto.etag
                }
            }
        }
    }

    fun onTitleChange(value: String) { title = value }
    fun onContentChange(value: String) { content = value }
    fun onVisibilityChange(v: PasteVisibility) { visibility = v }
    fun onSyntaxLanguageChange(lang: String) { syntaxLanguage = lang }
    fun onExpiresAtChange(value: String?) { expiresAt = value }

    fun save() {
        val id = pasteId ?: return
        val currentEtag = etag ?: return
        saveState = PasteResult.Loading
        viewModelScope.launch {
            updatePasteUseCase(
                id = id,
                title = title,
                content = content,
                visibility = visibility,
                syntaxLanguage = syntaxLanguage,
                expiresAt = expiresAt,
                etag = currentEtag
            ).collectLatest { result ->
                saveState = result
                if (result is PasteResult.Success) {
                    val dto = result.data
                    // Update local state from server response
                    title = dto.title
                    content = dto.content
                    visibility = dto.visibility
                    syntaxLanguage = dto.syntaxLanguage
                    expiresAt = dto.expiresAt
                    etag = dto.etag
                    conflict = false
                } else if (result is PasteResult.Error && result.code == 412) {
                    // Optimistic locking conflict
                    conflict = true
                }
            }
        }
    }

    fun reloadAfterConflict() {
        val id = pasteId ?: return
        viewModelScope.launch {
            getPasteUseCase(id).collectLatest { result ->
                if (result is PasteResult.Success) {
                    val dto = result.data
                    title = dto.title
                    content = dto.content
                    visibility = dto.visibility
                    syntaxLanguage = dto.syntaxLanguage
                    expiresAt = dto.expiresAt
                    etag = dto.etag
                }
                conflict = false
                loadState = result
            }
        }
    }

    fun dismissConflict() { conflict = false }
}
