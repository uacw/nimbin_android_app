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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.shared.dto.UserProfileDto
import tech.nimbus.shared.utils.ApiResult

class NimbinApiClientImplProfileTest {

    private lateinit var tokenProvider: TokenProvider
    private lateinit var apiConfig: ApiConfig

    @Before
    fun setup() {
        tokenProvider = mock()
        apiConfig = ApiConfig(baseUrl = "https://test-api.nimbin.com")
    }

    private fun createHttpClient(responseJson: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val mockEngine = MockEngine {
            respond(
                content = responseJson,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `getMyProfile decodes direct object`() = runTest {
        val json = """
            {
              "user": {
                "id": "u1",
                "username": "john",
                "displayName": "John",
                "email": "john@example.com",
                "createdAt": "2025-01-01T00:00:00Z"
              },
              "publicPastesCount": 5,
              "totalPastesCount": 12
            }
        """.trimIndent()
        val http = createHttpClient(json)
        val api = NimbinApiClientImpl(http, apiConfig, tokenProvider)

        val result = api.getMyProfile("tkn")
        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success<UserProfileDto>).data
        assertEquals("u1", data.user.id)
        assertEquals(5, data.publicPastesCount)
        assertEquals(12, data.totalPastesCount)
    }

    @Test
    fun `getUserProfile decodes from data envelope`() = runTest {
        val json = """
            { "data": {
              "user": {
                "id": "u2",
                "username": "mary",
                "displayName": "Mary",
                "email": "mary@example.com",
                "createdAt": "2025-01-01T00:00:00Z"
              },
              "publicPastesCount": 3
            }}
        """.trimIndent()
        val http = createHttpClient(json)
        val api = NimbinApiClientImpl(http, apiConfig, tokenProvider)

        val result = api.getUserProfile("u2")
        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success<UserProfileDto>).data
        assertEquals("u2", data.user.id)
        assertEquals(3, data.publicPastesCount)
        assertEquals(null, data.totalPastesCount)
    }

    @Test
    fun `getMyProfile with 401 maps to auth error`() = runTest {
        val json = """{"error":"Token is not valid or has expired"}"""
        val http = createHttpClient(json, HttpStatusCode.Unauthorized)
        val api = NimbinApiClientImpl(http, apiConfig, tokenProvider)

        val result = api.getMyProfile("bad")
        assertTrue(result is ApiResult.Error)
        val err = result as ApiResult.Error
        assertEquals(401, err.code)
    }
}
