package tech.nimbus.nimbin.test

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbus.nimbin.data.remote.TokenProvider
import tech.nimbus.shared.api.ApiConfig

/**
 * Helper класс для создания mock объектов в тестах.
 *
 * Упрощает настройку моков для различных компонентов системы,
 * предоставляет готовые конфигурации для типичных сценариев тестирования.
 */
object MockHelper {

    /**
     * Создает mock TokenProvider с заданным токеном.
     */
    fun createMockTokenProvider(token: String? = "test_jwt_token"): TokenProvider {
        return mock<TokenProvider>().also {
            whenever(it.getToken()).thenReturn(token)
        }
    }

    /**
     * Создает HttpClient с MockEngine для тестирования API запросов.
     *
     * @param responses Карта путей к JSON ответам для mock сервера
     * @param defaultStatus HTTP статус по умолчанию
     */
    fun createMockHttpClient(
        responses: Map<String, String> = emptyMap(),
        defaultStatus: HttpStatusCode = HttpStatusCode.OK
    ): HttpClient {
        val mockEngine = MockEngine { request ->
            val path = request.url.encodedPath
            val method = request.method.value
            val key = "$method:$path"

            val responseBody = responses[key]
                ?: responses[path]
                ?: """{"message": "Mock response for $path"}"""

            respond(
                content = responseBody,
                status = defaultStatus,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    /**
     * Создает mock HTTP ответы для успешных API операций.
     */
    fun createSuccessApiResponses(): Map<String, String> = mapOf(
        "POST:/api/auth/login" to """
            {
                "token": "jwt_success_token",
                "user": {
                    "id": "user123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "displayName": "Test User",
                    "createdAt": "2025-01-20T12:00:00Z",
                    "isActive": true
                }
            }
        """.trimIndent(),

        "POST:/api/auth/register" to """
            {
                "token": "jwt_register_token",
                "user": {
                    "id": "newuser123",
                    "username": "newuser",
                    "email": "newuser@example.com",
                    "displayName": null,
                    "createdAt": "2025-01-20T12:00:00Z",
                    "isActive": true
                }
            }
        """.trimIndent(),

        "POST:/api/pastes" to """
            {
                "id": "paste_created_123",
                "title": "Created Paste",
                "content": "Successfully created paste content",
                "visibility": "PUBLIC",
                "language": "text",
                "createdAt": "2025-01-20T12:00:00Z",
                "userId": "user123",
                "authorUsername": "testuser",
                "authorDisplayName": "Test User",
                "expiresAt": null,
                "viewCount": 0
            }
        """.trimIndent(),

        "GET:/api/pastes/test123" to """
            {
                "id": "test123",
                "title": "Test Paste",
                "content": "Test paste content",
                "visibility": "PUBLIC",
                "language": "text",
                "createdAt": "2025-01-20T12:00:00Z",
                "userId": "user123",
                "authorUsername": "testuser",
                "authorDisplayName": "Test User",
                "expiresAt": null,
                "viewCount": 5
            }
        """.trimIndent(),

        "GET:/api/pastes/public" to """
            [
                {
                    "id": "paste1",
                    "title": "Public Paste 1",
                    "content": "Public content 1",
                    "visibility": "PUBLIC",
                    "language": "text",
                    "createdAt": "2025-01-20T12:00:00Z",
                    "userId": "user1",
                    "authorUsername": "author1",
                    "authorDisplayName": "Author One",
                    "expiresAt": null,
                    "viewCount": 10
                },
                {
                    "id": "paste2",
                    "title": "Public Paste 2",
                    "content": "Public content 2",
                    "visibility": "PUBLIC",
                    "language": "kotlin",
                    "createdAt": "2025-01-20T11:00:00Z",
                    "userId": "user2",
                    "authorUsername": "author2",
                    "authorDisplayName": null,
                    "expiresAt": null,
                    "viewCount": 3
                }
            ]
        """.trimIndent()
    )

    /**
     * Создает mock HTTP ответы для ошибок API.
     */
    fun createErrorApiResponses(): Map<String, String> = mapOf(
        "POST:/api/auth/login" to """{"error": "Неверные учетные данные"}""",
        "POST:/api/auth/register" to """{"error": "Пользователь с таким email уже существует"}""",
        "POST:/api/pastes" to """{"error": "Заголовок не может быть пустым"}""",
        "GET:/api/pastes/nonexistent" to """{"error": "Заметка не найдена"}"""
    )

    /**
     * Создает HttpClient с ошибками для тестирования обработки ошибок.
     */
    fun createErrorHttpClient(
        errorStatus: HttpStatusCode = HttpStatusCode.BadRequest
    ): HttpClient {
        return createMockHttpClient(
            responses = createErrorApiResponses(),
            defaultStatus = errorStatus
        )
    }

    /**
     * Создает тестовую конфигурацию API.
     */
    fun createTestApiConfig(): ApiConfig = ApiConfig(
        baseUrl = "https://test-api.nimbin.com",
        timeoutMs = 5000,
        enableLogging = true
    )

    /**
     * Создает HttpClient для тестирования сетевых ошибок.
     */
    fun createNetworkErrorHttpClient(): HttpClient {
        val mockEngine = MockEngine { _ ->
            throw Exception("Network timeout")
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /**
     * Создает HttpClient для тестирования медленных запросов.
     */
    fun createSlowHttpClient(delayMs: Long = 1000): HttpClient {
        val mockEngine = MockEngine { _ ->
            Thread.sleep(delayMs)
            respond(
                content = """{"message": "Delayed response"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /**
     * Создает HttpClient для тестирования различных HTTP статусов.
     */
    fun createStatusCodeHttpClient(statusCode: HttpStatusCode): HttpClient {
        val mockEngine = MockEngine { _ ->
            val content = when (statusCode) {
                HttpStatusCode.Unauthorized -> """{"error": "Токен недействителен"}"""
                HttpStatusCode.Forbidden -> """{"error": "Доступ запрещен"}"""
                HttpStatusCode.NotFound -> """{"error": "Ресурс не найден"}"""
                HttpStatusCode.InternalServerError -> """{"error": "Внутренняя ошибка сервера"}"""
                HttpStatusCode.TooManyRequests -> """{"error": "Превышен лимит запросов"}"""
                else -> """{"message": "Status: ${statusCode.value}"}"""
            }

            respond(
                content = content,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
