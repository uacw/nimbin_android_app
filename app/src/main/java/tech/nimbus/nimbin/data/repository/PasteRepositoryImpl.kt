package tech.nimbus.nimbin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import tech.nimbus.shared.dto.request.CreatePasteRequestDto
import tech.nimbus.shared.utils.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasteRepositoryImpl @Inject constructor(
    private val apiClient: NimbinApiClient
) : PasteRepository {

    override fun getPublicPastes(page: Int, limit: Int): Flow<PasteResult<List<PasteDto>>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getPublicPastes(page, limit)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> emit(PasteResult.Error(result.message))
        }
    }

    override fun createPaste(
        title: String,
        content: String,
        visibility: PasteVisibility,
        language: String,
        expiresAt: String?
    ): Flow<PasteResult<PasteDto>> = flow {
        emit(PasteResult.Loading)
        val request = CreatePasteRequestDto(
            title = title,
            content = content,
            visibility = visibility,
            expiresAt = expiresAt,
            language = language
        )
        when (val result = apiClient.createPaste(request)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> emit(PasteResult.Error(result.message))
        }
    }

    override fun getPaste(id: String): Flow<PasteResult<PasteDto>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getPaste(id)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> emit(PasteResult.Error(result.message))
        }
    }

    override fun getUserPastes(token: String, page: Int, limit: Int): Flow<PasteResult<List<PasteDto>>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getUserPastes(token, page, limit)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> emit(PasteResult.Error(result.message))
        }
    }
}
