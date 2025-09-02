package tech.nimbus.nimbin.domain.usecase.profile

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.ProfileRepository
import tech.nimbus.nimbin.domain.repository.ProfileResult
import tech.nimbus.shared.dto.UserProfileDto
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(token: String): Flow<ProfileResult<UserProfileDto>> = repo.getMyProfile(token)
}
