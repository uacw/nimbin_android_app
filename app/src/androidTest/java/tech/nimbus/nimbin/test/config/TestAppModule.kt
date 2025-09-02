package tech.nimbus.nimbin.test.config

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.ktor.client.HttpClient
import org.mockito.kotlin.mock
import tech.nimbus.nimbin.di.AppModule
import tech.nimbus.nimbin.test.MockHelper
import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.shared.api.NimbinApiClient
import tech.nimbus.nimbin.data.remote.TokenProvider
import tech.nimbus.nimbin.domain.utils.StringProvider
import javax.inject.Singleton

/**
 * Тестовый модуль Hilt для замены production зависимостей mock объектами.
 *
 * Заменяет AppModule в тестах, предоставляя mock реализации для:
 * - Тестовую конфигурацию API
 * - Mock провайдеры токенов
 * - Mock NimbinApiClient
 * - Mock HttpClient
 * - Mock StringProvider
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideTestApiConfig(): ApiConfig = MockHelper.createTestApiConfig()

    @Provides
    @Singleton
    fun provideTestTokenProvider(): TokenProvider = MockHelper.createMockTokenProvider()

    @Provides
    @Singleton
    fun provideTestHttpClient(): HttpClient {
        // Простой mock HttpClient для тестов
        return mock<HttpClient>()
    }

    @Provides
    @Singleton
    fun provideTestNimbinApiClient(): NimbinApiClient {
        // Mock NimbinApiClient для UI тестов
        return mock<NimbinApiClient>()
    }

    @Provides
    @Singleton
    fun provideTestStringProvider(): StringProvider {
        // Mock StringProvider для тестов ViewModels
        return object : StringProvider {
            override fun getString(stringResId: Int): String = "Test String $stringResId"
            override fun getString(stringResId: Int, vararg formatArgs: Any): String =
                "Test String $stringResId with args: ${formatArgs.joinToString()}"
        }
    }
}
