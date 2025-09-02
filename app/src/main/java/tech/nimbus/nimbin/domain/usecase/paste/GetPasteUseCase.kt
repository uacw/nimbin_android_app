package tech.nimbus.nimbin.domain.usecase.paste

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject

class GetPasteUseCase @Inject constructor(
    private val repository: PasteRepository
) {
    operator fun invoke(id: String): Flow<PasteResult<PasteDto>> = repository.getPaste(id)
}

