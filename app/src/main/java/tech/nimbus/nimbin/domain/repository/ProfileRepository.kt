package tech.nimbus.nimbin.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.nimbus.shared.dto.UserDto
import tech.nimbus.shared.dto.UserProfileDto
import tech.nimbus.shared.dto.PasteDto

sealed class ProfileResult<out T> {
    data class Success<out T>(val data: T): ProfileResult<T>()
    data class Error(val message: String, val code: Int? = null): ProfileResult<Nothing>()
    object Loading: ProfileResult<Nothing>()
}

interface ProfileRepository {
    fun getMyProfile(token: String): Flow<ProfileResult<UserProfileDto>>
    fun getUserProfile(userId: String): Flow<ProfileResult<UserProfileDto>>
    fun updateProfile(token: String, username: String?, displayName: String?): Flow<ProfileResult<UserDto>>
    fun getUserPublicPastes(userId: String, page: Int = 1, limit: Int = 20): Flow<ProfileResult<List<PasteDto>>>
}
