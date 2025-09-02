package tech.nimbus.nimbin.core.config

import tech.nimbus.nimbin.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Централизованная система управления конфигурациями приложения.
 *
 * Обеспечивает:
 * - Различные настройки для debug/staging/release
 * - Feature flags для A/B тестирования
 * - Гибкое управление API endpoints
 * - Настройки производительности
 */
@Singleton
class AppConfigManager @Inject constructor() {

    // === API CONFIGURATION ===

    val apiBaseUrl: String = BuildConfig.API_BASE_URL
    val enableApiLogging: Boolean = BuildConfig.ENABLE_LOGGING
    val apiTimeoutMs: Long = if (BuildConfig.DEBUG) 30_000L else 15_000L
    val apiRetryAttempts: Int = if (BuildConfig.DEBUG) 1 else 3

    // === FEATURE FLAGS ===

    val isDeepLinksEnabled: Boolean = true
    val isOfflineModeEnabled: Boolean = false // Для будущих версий
    val isPushNotificationsEnabled: Boolean = !BuildConfig.DEBUG
    val isBiometricAuthEnabled: Boolean = true
    val isAnalyticsEnabled: Boolean = !BuildConfig.DEBUG
    val isCrashReportingEnabled: Boolean = !BuildConfig.DEBUG

    // === UI CONFIGURATION ===

    val maxPasteContentLength: Int = 1_000_000
    val maxPasteTitleLength: Int = 255
    val pastesPerPage: Int = 20
    val maxRecentPastes: Int = 50
    val enableAnimations: Boolean = true
    val enableHapticFeedback: Boolean = true

    // === PERFORMANCE SETTINGS ===

    val imageCacheSize: Long = if (BuildConfig.DEBUG) 50 * 1024 * 1024L else 100 * 1024 * 1024L // 50MB debug, 100MB release
    val httpCacheSize: Long = 10 * 1024 * 1024L // 10MB
    val maxConcurrentRequests: Int = 5
    val enableRequestDeduplication: Boolean = true

    // === SECURITY SETTINGS ===

    val sessionTimeoutMinutes: Int = if (BuildConfig.DEBUG) 60 else 30
    val enableCertificatePinning: Boolean = !BuildConfig.DEBUG
    val enableRootDetection: Boolean = !BuildConfig.DEBUG
    val enableScreenshotBlocking: Boolean = false // Для финансовых приложений

    // === DEVELOPMENT TOOLS ===

    val enableFlipperIntegration: Boolean = BuildConfig.DEBUG
    val enableLeakCanary: Boolean = BuildConfig.DEBUG
    val enableStrictMode: Boolean = BuildConfig.DEBUG
    val showDebugInfo: Boolean = BuildConfig.DEBUG

    // === EXPERIMENTAL FEATURES ===

    val enableDarkTheme: Boolean = true
    val enableMaterialYou: Boolean = true
    val enableCodeHighlighting: Boolean = true
    val enableShareSheet: Boolean = true
    val enableExportFeature: Boolean = false // Будущая функциональность

    // === BUSINESS LOGIC ===

    val enableAnonymousPastes: Boolean = true
    val maxAnonymousPastesPerDay: Int = 10
    val enablePasteExpiration: Boolean = true
    val defaultPasteExpiration: String? = null // null = без срока
    val supportedLanguages: List<String> = listOf(
        "text", "kotlin", "java", "javascript", "typescript",
        "python", "cpp", "c", "html", "css", "json", "xml", "sql", "markdown"
    )

    // === RUNTIME CONFIGURATION ===

    fun getApiTimeout(endpoint: String): Long {
        return when (endpoint) {
            "/api/pastes" -> apiTimeoutMs * 2 // Создание может занимать больше времени
            "/api/auth/login", "/api/auth/register" -> apiTimeoutMs / 2 // Быстрые операции
            else -> apiTimeoutMs
        }
    }

    fun shouldEnableFeature(featureFlag: String): Boolean {
        return when (featureFlag) {
            "search" -> false // Пока не реализовано
            "favorites" -> false // Будущая функция
            "themes" -> true
            "export" -> enableExportFeature
            else -> false
        }
    }

    fun getEnvironmentInfo(): EnvironmentInfo {
        return EnvironmentInfo(
            buildType = BuildConfig.BUILD_TYPE,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            applicationId = BuildConfig.APPLICATION_ID,
            isDebug = BuildConfig.DEBUG,
            apiUrl = apiBaseUrl
        )
    }
}

/**
 * Информация об окружении приложения
 */
data class EnvironmentInfo(
    val buildType: String,
    val versionName: String,
    val versionCode: Int,
    val applicationId: String,
    val isDebug: Boolean,
    val apiUrl: String
)

/**
 * Feature flags для A/B тестирования
 */
object FeatureFlags {
    const val NEW_UI_DESIGN = "new_ui_design"
    const val ENHANCED_SEARCH = "enhanced_search"
    const val COLLABORATIVE_EDITING = "collaborative_editing"
    const val PREMIUM_FEATURES = "premium_features"
    const val SOCIAL_SHARING = "social_sharing"
}
