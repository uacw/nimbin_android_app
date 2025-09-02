package tech.nimbus.nimbin.test

import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.nimbin.data.remote.TokenProvider

/**
 * Helper class for creating mock objects and test data for instrumented tests
 */
object MockHelper {

    fun createTestApiConfig(): ApiConfig {
        return ApiConfig(
            baseUrl = "https://test-api.nimbin.app"
        )
    }

    fun createMockTokenProvider(): TokenProvider {
        return object : TokenProvider {
            private var token: String? = "test-token"

            override fun getToken(): String? = token
        }
    }
}
