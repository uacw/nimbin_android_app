package tech.nimbus.nimbin.domain.usecase.paste

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject

class GetUserPastesUseCase @Inject constructor(
    private val repository: PasteRepository
) {
    operator fun invoke(
        token: String,
        page: Int = 1,
        limit: Int = 20
    ): Flow<PasteResult<List<PasteDto>>> = repository.getUserPastes(token, page, limit)
}
