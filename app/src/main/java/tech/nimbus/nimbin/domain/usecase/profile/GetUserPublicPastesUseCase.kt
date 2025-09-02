package tech.nimbus.nimbin.domain.usecase.profile

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.ProfileRepository
import tech.nimbus.nimbin.domain.repository.ProfileResult
import tech.nimbus.shared.dto.PasteDto
import javax.inject.Inject

class GetUserPublicPastesUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(userId: String, page: Int = 1, limit: Int = 20): Flow<ProfileResult<List<PasteDto>>> =
        repo.getUserPublicPastes(userId, page, limit)
}
