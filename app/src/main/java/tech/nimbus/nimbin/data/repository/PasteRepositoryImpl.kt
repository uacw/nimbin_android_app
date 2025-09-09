package tech.nimbus.nimbin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import tech.nimbus.shared.dto.request.CreatePasteRequestDto
import tech.nimbus.shared.dto.request.UpdatePasteRequestDto
import tech.nimbus.shared.utils.ApiResult
import javax.inject.Inject
import javax.inject.Singleton
import tech.nimbus.nimbin.core.session.SessionManager

@Singleton
class PasteRepositoryImpl @Inject constructor(
    private val apiClient: NimbinApiClient,
    private val sessionManager: SessionManager
) : PasteRepository {

    override fun getPublicPastes(page: Int, limit: Int): Flow<PasteResult<List<PasteDto>>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getPublicPastes(page, limit)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(PasteResult.Error(result.message, result.code))
            }
        }
    }

    override fun createPaste(
        title: String,
        content: String,
        visibility: PasteVisibility,
        syntaxLanguage: String,
        expiresAt: String?
    ): Flow<PasteResult<PasteDto>> = flow {
        emit(PasteResult.Loading)
        val request = CreatePasteRequestDto(
            title = title,
            content = content,
            visibility = visibility,
            expiresAt = expiresAt,
            syntaxLanguage = syntaxLanguage
        )
        when (val result = apiClient.createPaste(request)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(PasteResult.Error(result.message, result.code))
            }
        }
    }

    override fun getPaste(id: String): Flow<PasteResult<PasteDto>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getPaste(id)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(PasteResult.Error(result.message, result.code))
            }
        }
    }

    override fun getUserPastes(token: String, page: Int, limit: Int): Flow<PasteResult<List<PasteDto>>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getUserPastes(token, page, limit)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(PasteResult.Error(result.message, result.code))
            }
        }
    }

    override fun updatePaste(
        id: String,
        title: String,
        content: String,
        visibility: PasteVisibility,
        syntaxLanguage: String,
        expiresAt: String?,
        etag: String
    ): Flow<PasteResult<PasteDto>> = flow {
        emit(PasteResult.Loading)
        val request = UpdatePasteRequestDto(
            title = title,
            content = content,
            visibility = visibility,
            expiresAt = expiresAt,
            syntaxLanguage = syntaxLanguage
        )
        when (val result = apiClient.updatePaste(id, request, etag)) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(PasteResult.Error(result.message, result.code))
            }
        }
    }

    override fun getSyntaxLanguages(): Flow<PasteResult<List<String>>> = flow {
        emit(PasteResult.Loading)
        when (val result = apiClient.getSyntaxLanguages()) {
            is ApiResult.Success -> emit(PasteResult.Success(result.data))
            is ApiResult.Error -> {
                sessionManager.handleAuthError(result.message, result.code)
                emit(PasteResult.Error(result.message, result.code))
            }
        }
    }
}
