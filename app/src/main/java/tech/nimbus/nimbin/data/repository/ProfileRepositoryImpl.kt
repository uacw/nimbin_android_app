package tech.nimbus.nimbin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.nimbus.nimbin.domain.repository.ProfileRepository
import tech.nimbus.nimbin.domain.repository.ProfileResult
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.UserDto
import tech.nimbus.shared.dto.UserProfileDto
import tech.nimbus.shared.dto.request.UpdateProfileRequestDto
import tech.nimbus.shared.utils.ApiResult
import javax.inject.Inject
import javax.inject.Singleton
import tech.nimbus.nimbin.core.session.SessionManager
import tech.nimbus.nimbin.data.remote.NimbinApiClientImpl

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiClient: NimbinApiClient,
    private val sessionManager: SessionManager,
    private val rawApi: NimbinApiClientImpl
) : ProfileRepository {

    override fun getMyProfile(token: String): Flow<ProfileResult<UserProfileDto>> = flow {
        emit(ProfileResult.Loading)
        // Используем конкретную реализацию клиента, где есть getMyProfile
        when (val result = rawApi.getMyProfile(token)) {
            is ApiResult.Success -> emit(ProfileResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(ProfileResult.Error(result.message, result.code))
            }
        }
    }

    override fun getUserProfile(userId: String): Flow<ProfileResult<UserProfileDto>> = flow {
        emit(ProfileResult.Loading)
        when (val result = apiClient.getUserProfile(userId)) {
            is ApiResult.Success -> emit(ProfileResult.Success(result.data))
            is ApiResult.Error -> emit(ProfileResult.Error(result.message, result.code))
        }
    }

    override fun updateProfile(token: String, username: String?, displayName: String?): Flow<ProfileResult<UserDto>> = flow {
        emit(ProfileResult.Loading)
        val req = UpdateProfileRequestDto(username = username, displayName = displayName)
        when (val result = apiClient.updateProfile(token, req)) {
            is ApiResult.Success -> emit(ProfileResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(ProfileResult.Error(result.message, result.code))
            }
        }
    }

    override fun getUserPublicPastes(userId: String, page: Int, limit: Int): Flow<ProfileResult<List<PasteDto>>> = flow {
        emit(ProfileResult.Loading)
        when (val result = apiClient.getUserPublicPastes(userId, page, limit)) {
            is ApiResult.Success -> emit(ProfileResult.Success(result.data))
            is ApiResult.Error -> emit(ProfileResult.Error(result.message, result.code))
        }
    }
}
