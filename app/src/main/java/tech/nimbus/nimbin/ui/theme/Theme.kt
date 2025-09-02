package tech.nimbus.nimbin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Светлая цветовая схема Material 3 для NimBin
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Secondary colors
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Tertiary colors
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Error colors
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Surface colors
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,

    // Inverse colors
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,

    // Outline colors
    outline = Outline,
    outlineVariant = OutlineVariant,

    // Background colors (deprecated but still needed for compatibility)
    background = Background,
    onBackground = OnBackground,
)

/**
 * Темная цветовая схема Material 3 для NimBin
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    // Secondary colors
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    // Tertiary colors
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    // Error colors
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    // Surface colors
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,

    // Inverse colors
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,

    // Outline colors
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    // Background colors (deprecated but still needed for compatibility)
    background = DarkBackground,
    onBackground = DarkOnBackground,
)

/**
 * Основная тема приложения NimBin с поддержкой Material 3
 *
 * @param darkTheme Использовать ли темную тему (по умолчанию - системная настройка)
 * @param dynamicColor Использовать ли динамические цвета Android 12+ (по умолчанию включено)
 * @param content Контент приложения
 */
@Composable
fun NimBinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Определяем цветовую схему
    val colorScheme = when {
        // Динамические цвета доступны на Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // Fallback на наши кастомные цвета
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Настройка системных UI элементов (status bar, navigation bar)
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()

    if (!view.isInEditMode) {
        SideEffect {
            // Настройка цветов системных элементов
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            // Настройка светлых/темных иконок в зависимости от темы
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme

            // Используем SystemUiController для более гибкой настройки
            systemUiController.setSystemBarsColor(
                color = colorScheme.surface,
                darkIcons = !darkTheme,
                isNavigationBarContrastEnforced = false
            )
        }
    }

    // Применяем MaterialTheme с нашими настройками
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}