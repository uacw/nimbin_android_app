package tech.nimbus.nimbin.domain.usecase.paste

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import javax.inject.Inject

class UpdatePasteUseCase @Inject constructor(
    private val repository: PasteRepository
) {
    operator fun invoke(
        id: String,
        title: String,
        content: String,
        visibility: PasteVisibility,
        syntaxLanguage: String = "plaintext",
        expiresAt: String? = null,
        etag: String
    ): Flow<PasteResult<PasteDto>> =
        repository.updatePaste(id, title, content, visibility, syntaxLanguage, expiresAt, etag)
}

