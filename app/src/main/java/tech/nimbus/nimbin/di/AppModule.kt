package tech.nimbus.nimbin.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tech.nimbus.shared.api.ApiConfig
import tech.nimbus.shared.api.NimbinApiClient
import javax.inject.Singleton
import tech.nimbus.nimbin.data.remote.NimbinApiClientImpl
import tech.nimbus.nimbin.data.remote.TokenProvider
import tech.nimbus.nimbin.data.preferences.UserPreferencesRepository
import tech.nimbus.nimbin.domain.utils.StringProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import tech.nimbus.nimbin.BuildConfig
import timber.log.Timber

class DataStoreTokenProvider(private val prefs: UserPreferencesRepository): TokenProvider {
    override fun getToken(): String? = runBlocking { prefs.authToken.firstOrNull() }
}

class AndroidStringProvider(
    @ApplicationContext private val context: Context
) : StringProvider {
    override fun getString(stringResId: Int): String = context.getString(stringResId)

    override fun getString(stringResId: Int, vararg formatArgs: Any): String =
        context.getString(stringResId, *formatArgs)
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiConfig(): ApiConfig = ApiConfig(
        baseUrl = BuildConfig.API_BASE_URL,
        enableLogging = BuildConfig.ENABLE_LOGGING
    )

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true // важно: сериализовать поля со значениями по умолчанию
            })
        }
        if (BuildConfig.ENABLE_LOGGING) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("Ktor").d(message)
                    }
                }
                level = LogLevel.BODY
            }
        }
    }

    @Provides
    @Singleton
    fun provideTokenProvider(prefs: UserPreferencesRepository): TokenProvider = DataStoreTokenProvider(prefs)

    @Provides
    @Singleton
    fun provideNimbinApiClient(client: HttpClient, config: ApiConfig, tokenProvider: TokenProvider): NimbinApiClient =
        NimbinApiClientImpl(client, config, tokenProvider)

    @Provides
    @Singleton
    fun provideStringProvider(@ApplicationContext context: Context): StringProvider =
        AndroidStringProvider(context)
}