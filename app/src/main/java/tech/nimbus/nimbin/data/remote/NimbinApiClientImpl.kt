package tech.nimbus.nimbin.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import kotlinx.serialization.decodeFromString // added
import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.shared.api.ApiEndpoints
import tech.nimbus.shared.api.ApiHeaders
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.shared.dto.*
import tech.nimbus.shared.dto.request.*
import tech.nimbus.shared.utils.ApiResult
import io.ktor.client.statement.bodyAsText
import timber.log.Timber

/**
 * Интерфейс для предоставления JWT токена авторизации.
 *
 * Используется для централизованного управления токенами авторизации
 * в HTTP клиенте без жесткой привязки к конкретной реализации хранения.
 *
 * @see NimbinApiClientImpl
 * @author NimBin Team
 * @since 1.0
 */
interface TokenProvider {
    /**
     * Возвращает текущий JWT токен для авторизации API запросов.
     *
     * @return JWT токен или null если пользователь не авторизован
     */
    fun getToken(): String?
}

/**
 * Реализация API клиента для взаимодействия с backend сервисом NimBin.
 *
 * Обеспечивает типобезопасное взаимодействие с REST API через Ktor Client,
 * включая автоматическую обработку ошибок, сериализацию/десериализацию данных,
 * и универсальные стратегии парсинга ответов сервера.
 *
 * ## Основные возможности:
 * - Автоматическая вставка JWT токенов в заголовки запросов
 * - Универсальный парсинг ответов с поддержкой разных форматов (прямой объект, вложенный в envelope)
 * - Централизованная обработка всех типов ошибок (4xx, 5xx, сеть, сериализация)
 * - Детальное логирование всех запросов и ответов через Timber
 * - Поддержка как авторизованных, так и анонимных запросов
 *
 * ## Обработка ошибок:
 * - **ClientRequestException (4xx)** - ошибки валидации, авторизации
 * - **ServerResponseException (5xx)** - серверные ошибки
 * - **SerializationException** - проблемы парсинга JSON
 * - **Exception** - сетевые и прочие ошибки
 *
 * ## Стратегии парсинга:
 * Поддерживает различные форматы ответов сервера:
 * - Прямой объект: `PasteDto { id: "...", title: "..." }`
 * - Envelope: `{ "data": PasteDto, "result": PasteDto, "payload": PasteDto }`
 * - Структурированные ошибки: `{ "error": "message", "code": "ERR_001" }`
 *
 * @param client Сконфигурированный Ktor HttpClient с поддержкой JSON сериализации
 * @param config Конфигурация API (базовый URL, таймауты, отладка)
 * @param tokenProvider Провайдер JWT токенов для авторизации запросов
 *
 * @see NimbinApiClient
 * @see ApiConfig
 * @see TokenProvider
 * @see ApiResult
 *
 * @author NimBin Team
 * @since 1.0
 */
class NimbinApiClientImpl(
    private val client: HttpClient,
    private val config: ApiConfig,
    private val tokenProvider: TokenProvider
) : NimbinApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    private fun full(path: String): String = if (path.startsWith("http")) path else config.baseUrl.trimEnd('/') + path

    /**
     * Извлекает структурированную ошибку API из тела ответа.
     *
     * @param body JSON строка с телом ответа сервера
     * @return ApiErrorDto с деталями ошибки или null если не удалось распарсить
     */
    private fun extractApiError(body: String?): ApiErrorDto? =
        if (body.isNullOrBlank()) null else runCatching { json.decodeFromString(ApiErrorDto.serializer(), body) }.getOrNull()

    private fun extractErrorMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null

        // Попробуем извлечь ошибку через ApiErrorDto
        extractApiError(body)?.let { return it.error }

        // Попробуем извлечь простое сообщение об ошибке из JSON
        try {
            val jsonElement = json.parseToJsonElement(body)
            val jsonObject = jsonElement.jsonObject
            jsonObject["error"]?.let { errorElement ->
                // Убираем кавычки из строки JSON
                return errorElement.toString().removeSurrounding("\"")
            }
            jsonObject["message"]?.let { messageElement ->
                return messageElement.toString().removeSurrounding("\"")
            }
        } catch (e: Exception) {
            // Если не удалось парсить JSON, возвращаем null
        }

        return null
    }

    /**
     * Универсальная обертка для безопасного выполнения HTTP запросов.
     *
     * Обеспечивает единообразную обработку всех типов ошибок с детальным логированием
     * и преобразованием исключений в типобезопасные результаты ApiResult.
     *
     * @param block Suspend функция с HTTP запросом
     * @return ApiResult.Success с данными или ApiResult.Error с описанием ошибки
     */
    private suspend inline fun <reified T> safe(block: suspend () -> T): ApiResult<T> = try {
        ApiResult.Success(block())
    } catch (e: ClientRequestException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val msg = extractErrorMessage(body) ?: body ?: "HTTP ${e.response.status.value}"
        Timber.w(e, "Client error ${e.response.status.value}: $body")

        // Проверяем на ошибки авторизации (истечение токена)
        if (e.response.status.value == 401 || e.response.status.value == 403 ||
            msg.contains("Token is not valid", ignoreCase = true) ||
            msg.contains("token expired", ignoreCase = true)) {
            // Возвращаем специальную ошибку авторизации
            ApiResult.Error("Token is not valid or has expired", e.response.status.value)
        } else {
            ApiResult.Error(msg, e.response.status.value)
        }
    } catch (e: ServerResponseException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val msg = extractErrorMessage(body) ?: body ?: "HTTP ${e.response.status.value}"
        Timber.e(e, "Server error ${e.response.status.value}: $body")
        ApiResult.Error(msg, e.response.status.value)
    } catch (e: SerializationException) {
        Timber.e(e, "Serialization error: ${e.message}")

        // Проверяем, не связана ли SerializationException с истечением токена
        if (e.message?.contains("Token is not valid", ignoreCase = true) == true ||
            e.message?.contains("token expired", ignoreCase = true) == true) {
            ApiResult.Error("Token is not valid or has expired", 401)
        } else {
            ApiResult.Error("Serialization error", null)
        }
    } catch (e: Exception) {
        Timber.e(e, "Network/general error")
        ApiResult.Error(e.message ?: "Сетевая ошибка", null)
    }

    /**
     * Универсальная обертка для безопасного выполнения HTTP запросов с сохранением кодов статуса.
     *
     * @param block Suspend функция с HTTP запросом
     * @return ApiResult.Success с данными или ApiResult.Error с описанием ошибки и кодом статуса
     */
    private suspend inline fun <reified T> safeWithStatusCode(crossinline block: suspend () -> T): ApiResult<T> = try {
        ApiResult.Success(block())
    } catch (e: ClientRequestException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val msg = extractErrorMessage(body) ?: body ?: "HTTP ${e.response.status.value}"
        Timber.w(e, "Client error ${e.response.status.value}: $body")
        ApiResult.Error(msg, e.response.status.value)
    } catch (e: ServerResponseException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val msg = extractErrorMessage(body) ?: body ?: "HTTP ${e.response.status.value}"
        Timber.e(e, "Server error ${e.response.status.value}: $body")
        ApiResult.Error(msg, e.response.status.value)
    } catch (e: SerializationException) {
        Timber.e(e, "Serialization error")
        // Для SerializationException сохраняем null код, поскольку это не HTTP ошибка
        ApiResult.Error("Ошибка обработки данных", null)
    } catch (e: Exception) {
        Timber.e(e, "Network/general error")
        ApiResult.Error(e.message ?: "Сетевая ошибка", null)
    }

    /**
     * Специальная версия safe для HTTP запросов, которые могут возвращать ошибки с кодами статуса.
     */
    private suspend inline fun <reified T> safeHttpWithDecode(
        crossinline httpCall: suspend () -> io.ktor.client.statement.HttpResponse,
        crossinline decoder: (String) -> T
    ): ApiResult<T> = try {
        val response = httpCall()
        val body = response.bodyAsText()
        val data = decoder(body)
        ApiResult.Success(data)
    } catch (e: ClientRequestException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val msg = extractErrorMessage(body) ?: "Ошибка обработки данных"
        Timber.w(e, "Client error ${e.response.status.value}: $body")
        ApiResult.Error(msg, e.response.status.value)
    } catch (e: ServerResponseException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val msg = extractErrorMessage(body) ?: "Ошибка обработки данных"
        Timber.e(e, "Server error ${e.response.status.value}: $body")
        ApiResult.Error(msg, e.response.status.value)
    } catch (e: SerializationException) {
        Timber.e(e, "Serialization error")
        ApiResult.Error("Ошибка обработки данных", null)
    } catch (e: Exception) {
        Timber.e(e, "Network/general error")
        ApiResult.Error(e.message ?: "Сетевая ошибка", null)
    }

    private inline fun <reified T> tryDecode(raw: String): T? =
        runCatching { json.decodeFromString(serializer<T>(), raw) }.getOrNull()

    /**
     * Универсальная стратегия многов��риантного декодирования сущностей.
     *
     * Поддерживает различные форматы ответов backend сервиса:
     * 1. Прямой объект (наиболее частый случай)
     * 2. Объект вложенный в envelope по ключам: data, result, payload, item, paste, user
     * 3. Структурированная ошибка API
     *
     * @param raw JSON строка с ответом сервера
     * @param nestedKeys Функция, возвращающая список ключей для поиска вложенного объекта
     * @return Декодированный объект типа T
     * @throws SerializationException если не удалось декодировать в любом из форматов
     */
    private inline fun <reified T> decodeVariants(
        raw: String,
        crossinline nestedKeys: () -> List<String> = { listOf("data", "result", "payload", "item", "paste", "user") }
    ): T {
        tryDecode<T>(raw)?.let { return it }
        val rootObj = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull()
        if (rootObj != null) {
            for (k in nestedKeys()) {
                val node = rootObj[k] ?: continue
                runCatching { json.decodeFromJsonElement(serializer<T>(), node) }.getOrNull()?.let { return it }
            }
        }
        extractApiError(raw)?.let { throw SerializationException(it.error) }
        throw SerializationException("Не удалось распарсить ответ: $raw")
    }

    /**
     * Добавляет JWT токен авторизации к HTTP запросу при наличии.
     *
     * @param builder Строитель HTTP запроса
     * @param explicitToken Явно переданный токен (приоритет над tokenProvider)
     */
    private fun addAuthIfPresent(builder: HttpRequestBuilder, explicitToken: String? = null) {
        val token = explicitToken ?: tokenProvider.getToken()
        if (!token.isNullOrBlank()) builder.headers.append(ApiHeaders.AUTHORIZATION, ApiHeaders.bearerToken(token))
    }

    // === PASTES ===

    override suspend fun createPaste(request: CreatePasteRequestDto): ApiResult<PasteDto> = safe {
        client.post {
            url(full(ApiEndpoints.PASTES))
            contentType(ContentType.Application.Json)
            addAuthIfPresent(this)
            setBody(request)
        }.body()
    }

    override suspend fun getPaste(id: String): ApiResult<PasteDto> = safe {
        val raw = client.get {
            url(full(ApiEndpoints.pasteById(id)))
            addAuthIfPresent(this)
        }.bodyAsText()
        decodeVariants<PasteDto>(raw) { listOf("paste", "data", "result", "payload") }
    }

    override suspend fun getPublicPastes(page: Int, limit: Int): ApiResult<List<PasteDto>> = safe {
        client.get { url(full(ApiEndpoints.publicPastes(page, limit))) }.body()
    }

    override suspend fun getUserPastes(token: String, page: Int, limit: Int): ApiResult<List<PasteDto>> = safe {
        client.get {
            url(full(ApiEndpoints.userPastes(page, limit)))
            addAuthIfPresent(this, token)
        }.body()
    }

    override suspend fun deletePaste(token: String, id: String): ApiResult<DeleteResponseDto> = safe {
        client.delete {
            url(full(ApiEndpoints.pasteById(id)))
            addAuthIfPresent(this, token)
        }.body()
    }

    // === AUTH ===
    override suspend fun register(request: RegisterRequestDto): ApiResult<AuthResponseDto> = safe {
        client.post {
            url(full(ApiEndpoints.REGISTER))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun login(request: LoginRequestDto): ApiResult<AuthResponseDto> = safe {
        client.post {
            url(full(ApiEndpoints.LOGIN))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getCurrentUser(token: String): ApiResult<UserDto> = safe {
        val raw = client.get {
            url(full(ApiEndpoints.USER_PROFILE))
            addAuthIfPresent(this, token)
        }.bodyAsText()
        tryDecode<UserDto>(raw)?.let { return@safe it }
        tryDecode<AuthResponseDto>(raw)?.let { return@safe it.user }
        tryDecode<UserProfileDto>(raw)?.let { return@safe it.user }
        val root = runCatching { json.parseToJsonElement(raw) }.getOrNull()
        val userNode = root?.jsonObject?.get("user")
        if (userNode != null) {
            runCatching { json.decodeFromJsonElement(UserDto.serializer(), userNode) }.getOrNull()?.let { return@safe it }
        }
        extractApiError(raw)?.let { throw SerializationException(it.error) }
        throw SerializationException("Не удалось распарсить пользовате��я: $raw")
    }

    // === PROFILE ===
    override suspend fun getMyProfile(token: String): ApiResult<UserProfileDto> = safe {
        client.get {
            url(full(ApiEndpoints.USER_PROFILE))
            addAuthIfPresent(this, token)
        }.body()
    }

    override suspend fun getUserProfile(userId: String): ApiResult<UserProfileDto> = safe {
        client.get { url(full(ApiEndpoints.userProfileById(userId))) }.body()
    }

    override suspend fun updateProfile(token: String, request: UpdateProfileRequestDto): ApiResult<UserDto> = safe {
        client.put {
            url(full(ApiEndpoints.USER_PROFILE))
            contentType(ContentType.Application.Json)
            addAuthIfPresent(this, token)
            setBody(request)
        }.body()
    }

    override suspend fun getUserPublicPastes(userId: String, page: Int, limit: Int): ApiResult<List<PasteDto>> = safe {
        client.get { url(full(ApiEndpoints.userPublicPastes(userId, page, limit))) }.body()
    }
}
