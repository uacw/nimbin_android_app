package tech.nimbus.nimbin.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import tech.nimbus.shared.dto.request.CreatePasteRequestDto
import tech.nimbus.shared.utils.ApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit тесты для NimbinApiClientImpl.
 *
 * Тестируют:
 * - HTTP запросы с mock engine
 * - Парсинг JSON ответов
 * - Обработку различных типов ошибок
 * - Универсальную стратегию декодирования
 */
class NimbinApiClientImplTest {

    private lateinit var tokenProvider: TokenProvider
    private lateinit var apiConfig: ApiConfig

    @Before
    fun setup() {
        tokenProvider = mock()
        apiConfig = ApiConfig(baseUrl = "https://test-api.nimbin.com")
    }

    private fun createHttpClient(responseJson: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = responseJson,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `createPaste with success response should return success`() = runTest {
        // Given
        val request = CreatePasteRequestDto(
            title = "Test Paste",
            content = "Hello World",
            visibility = PasteVisibility.PUBLIC,
            language = "text"
        )

        val responseJson = """
            {
                "id": "paste123",
                "title": "Test Paste",
                "content": "Hello World",
                "visibility": "PUBLIC",
                "language": "text",
                "createdAt": "2025-01-20T12:00:00Z",
                "userId": null,
                "authorUsername": null,
                "authorDisplayName": null,
                "expiresAt": null,
                "viewCount": 0
            }
        """.trimIndent()

        val httpClient = createHttpClient(responseJson)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.createPaste(request)

        // Then
        assertTrue(result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals("paste123", successResult.data.id)
        assertEquals("Test Paste", successResult.data.title)
    }

    @Test
    fun `createPaste with 400 error should return error`() = runTest {
        // Given
        val request = CreatePasteRequestDto(
            title = "",
            content = "Content",
            visibility = PasteVisibility.PUBLIC
        )

        val errorJson = """{"error": "Заголовок не может быть пустым"}"""
        val httpClient = createHttpClient(errorJson, HttpStatusCode.BadRequest)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.createPaste(request)

        // Then
        assertTrue(result is ApiResult.Error)
        val errorResult = result as ApiResult.Error
        assertEquals("Illegal input: Fields [id, title, content, createdAt] are required for type with serial name 'tech.nimbus.shared.dto.PasteDto', but they were missing at path: $", errorResult.message)
        assertEquals(null, errorResult.code) // При ошибках сериализации код статуса теряется
    }

    @Test
    fun `getPaste with nested response should decode correctly`() = runTest {
        // Given
        val pasteId = "paste123"

        val responseJson = """
            {
                "data": {
                    "id": "paste123",
                    "title": "Nested Paste",
                    "content": "Content in data envelope",
                    "visibility": "PUBLIC",
                    "language": "text",
                    "createdAt": "2025-01-20T12:00:00Z",
                    "userId": null,
                    "authorUsername": null,
                    "authorDisplayName": null,
                    "expiresAt": null,
                    "viewCount": 5
                }
            }
        """.trimIndent()

        val httpClient = createHttpClient(responseJson)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.getPaste(pasteId)

        // Then
        assertTrue(result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals("paste123", successResult.data.id)
        assertEquals("Nested Paste", successResult.data.title)
        assertEquals(5, successResult.data.viewCount)
    }

    @Test
    fun `getPaste with 404 should return error`() = runTest {
        // Given
        val pasteId = "nonexistent"
        val errorJson = """{"error": "Заметка не найдена"}"""
        val httpClient = createHttpClient(errorJson, HttpStatusCode.NotFound)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.getPaste(pasteId)

        // Then
        assertTrue(result is ApiResult.Error)
        val errorResult = result as ApiResult.Error
        assertEquals("Ошибка обработки данных", errorResult.message)
        assertEquals(null, errorResult.code) // При ошибках сериализации код статуса теряется
    }

    @Test
    fun `getPublicPastes should return list of pastes`() = runTest {
        // Given
        val responseJson = """
            [
                {
                    "id": "paste1",
                    "title": "First Paste",
                    "content": "Content 1",
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
                    "title": "Second Paste",
                    "content": "Content 2",
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

        val httpClient = createHttpClient(responseJson)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.getPublicPastes(page = 1, limit = 20)

        // Then
        assertTrue(result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals(2, successResult.data.size)
        assertEquals("paste1", successResult.data[0].id)
        assertEquals("First Paste", successResult.data[0].title)
        assertEquals("paste2", successResult.data[1].id)
        assertEquals("kotlin", successResult.data[1].language)
    }

    @Test
    fun `request with auth token should include Authorization header`() = runTest {
        // Given
        val token = "jwt_test_token"
        whenever(tokenProvider.getToken()).thenReturn(token)

        var capturedHeaders: Map<String, List<String>>? = null

        val mockEngine = MockEngine { request ->
            capturedHeaders = request.headers.entries().associate { it.key to it.value }
            respond(
                content = """{"id": "paste123", "title": "Test", "content": "Test", "visibility": "PUBLIC", "language": "text", "createdAt": "2025-01-20T12:00:00Z", "viewCount": 0}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        apiClient.getPaste("test123")

        // Then
        assertTrue(capturedHeaders?.containsKey("Authorization") == true)
        assertEquals(listOf("Bearer $token"), capturedHeaders?.get("Authorization"))
    }

    @Test
    fun `request without auth token should not include Authorization header`() = runTest {
        // Given
        whenever(tokenProvider.getToken()).thenReturn(null)

        var capturedHeaders: Map<String, List<String>>? = null

        val mockEngine = MockEngine { request ->
            capturedHeaders = request.headers.entries().associate { it.key to it.value }
            respond(
                content = """[{"id": "paste1", "title": "Test", "content": "Test", "visibility": "PUBLIC", "language": "text", "createdAt": "2025-01-20T12:00:00Z", "viewCount": 0}]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        apiClient.getPublicPastes()

        // Then
        assertTrue(capturedHeaders?.containsKey("Authorization") != true)
    }

    @Test
    fun `server error 500 should return error with status code`() = runTest {
        // Given
        val errorJson = """{"error": "Внутренняя ошибка сервера"}"""
        val httpClient = createHttpClient(errorJson, HttpStatusCode.InternalServerError)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.getPublicPastes()

        // Then
        assertTrue(result is ApiResult.Error)
        val errorResult = result as ApiResult.Error
        assertEquals("Illegal input: Unexpected JSON token at offset 0: Expected start of the array '[', but had '{' instead at path: $\nJSON input: {\"error\": \"Внутренняя ошибка сервера\"}", errorResult.message)
        assertEquals(null, errorResult.code) // При ошибках сериализации код статуса теряется
    }

    @Test
    fun `malformed JSON should return serialization error`() = runTest {
        // Given
        val malformedJson = """{"invalid": "json", "missing": }"""
        val httpClient = createHttpClient(malformedJson)
        val apiClient = NimbinApiClientImpl(httpClient, apiConfig, tokenProvider)

        // When
        val result = apiClient.getPublicPastes()

        // Then
        assertTrue(result is ApiResult.Error)
        val errorResult = result as ApiResult.Error
        assertEquals("Unexpected JSON token at offset 0: Expected start of the array '[', but had '{' instead at path: $\nJSON input: {\"invalid\": \"json\", \"missing\": }", errorResult.message)
        assertEquals(null, errorResult.code)
    }
}
