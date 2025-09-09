package tech.nimbus.nimbin.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility

sealed class PasteResult<out T> {
    data class Success<out T>(val data: T): PasteResult<T>()
    data class Error(val message: String, val code: Int? = null): PasteResult<Nothing>()
    object Loading: PasteResult<Nothing>()
}

interface PasteRepository {
    fun getPublicPastes(page: Int = 1, limit: Int = 20): Flow<PasteResult<List<PasteDto>>>

    fun createPaste(
        title: String,
        content: String,
        visibility: PasteVisibility,
        syntaxLanguage: String = "plaintext",
        expiresAt: String? = null
    ): Flow<PasteResult<PasteDto>>

    fun getPaste(id: String): Flow<PasteResult<PasteDto>>

    fun getUserPastes(token: String, page: Int = 1, limit: Int = 20): Flow<PasteResult<List<PasteDto>>>

    fun updatePaste(
        id: String,
        title: String,
        content: String,
        visibility: PasteVisibility,
        syntaxLanguage: String = "plaintext",
        expiresAt: String? = null,
        etag: String
    ): Flow<PasteResult<PasteDto>>

    fun getSyntaxLanguages(): Flow<PasteResult<List<String>>>
}
