package tech.nimbus.nimbin.domain.usecase.paste

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject

class GetPublicPastesUseCase @Inject constructor(
    private val repository: PasteRepository
) {
    operator fun invoke(page: Int = 1, limit: Int = 20): Flow<PasteResult<List<PasteDto>>> =
        repository.getPublicPastes(page, limit)
}
