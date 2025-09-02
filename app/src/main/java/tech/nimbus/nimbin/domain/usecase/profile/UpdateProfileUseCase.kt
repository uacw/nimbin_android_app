package tech.nimbus.nimbin.domain.usecase.profile

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.ProfileRepository
import tech.nimbus.nimbin.domain.repository.ProfileResult
import tech.nimbus.shared.dto.UserDto
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(token: String, username: String?, displayName: String?): Flow<ProfileResult<UserDto>> =
        repo.updateProfile(token, username, displayName)
}
