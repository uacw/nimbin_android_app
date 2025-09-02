package tech.nimbus.nimbin.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors - современная синяя палитра для tech приложения
val Primary = Color(0xFF1976D2)      // Material Blue 700
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFE3F2FD) // Light blue 50
val OnPrimaryContainer = Color(0xFF0D47A1) // Blue 900

// Secondary Colors - дополнительная зеленая палитра
val Secondary = Color(0xFF388E3C)    // Green 600
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE8F5E8) // Light green 50
val OnSecondaryContainer = Color(0xFF1B5E20) // Green 800

// Tertiary Colors - акцентная оранжевая палитра
val Tertiary = Color(0xFFFF5722)     // Deep Orange 500
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFBE9E7) // Deep Orange 50
val OnTertiaryContainer = Color(0xFFBF360C) // Deep Orange 800

// Error Colors
val Error = Color(0xFFD32F2F)        // Red 700
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFDEDED) // Light red
val OnErrorContainer = Color(0xFF5F2120)

// Warning Colors (расширение для лучшего UX)
val Warning = Color(0xFFFF9800)      // Orange 500
val OnWarning = Color(0xFFFFFFFF)
val WarningContainer = Color(0xFFFFF3E0) // Orange 50
val OnWarningContainer = Color(0xFFE65100) // Orange 900

// Success Colors (расширение для лучшего UX)
val Success = Color(0xFF4CAF50)      // Green 500
val OnSuccess = Color(0xFFFFFFFF)
val SuccessContainer = Color(0xFFE8F5E8) // Light green
val OnSuccessContainer = Color(0xFF2E7D32) // Green 700

// Info Colors (расширение для лучшего UX)
val Info = Color(0xFF2196F3)         // Blue 500
val OnInfo = Color(0xFFFFFFFF)
val InfoContainer = Color(0xFFE3F2FD) // Blue 50
val OnInfoContainer = Color(0xFF1565C0) // Blue 800

// Surface Colors - современные нейтральные цвета
val Surface = Color(0xFFFEFBFF)      // Neutral 99
val OnSurface = Color(0xFF1C1B1F)    // Neutral 10
val SurfaceVariant = Color(0xFFE7E0EC) // Neutral variant 90
val OnSurfaceVariant = Color(0xFF49454F) // Neutral variant 30

// Surface Container Colors
val SurfaceContainer = Color(0xFFF3EDF7) // Neutral 94
val SurfaceContainerHigh = Color(0xFFECE6F0) // Neutral 92
val SurfaceContainerHighest = Color(0xFFE6E0E9) // Neutral 90
val SurfaceContainerLow = Color(0xFFF7F2FA)  // Neutral 96
val SurfaceContainerLowest = Color(0xFFFFFFFF) // Neutral 100

val SurfaceDim = Color(0xFFDDD8E1)      // Neutral 87
val SurfaceBright = Color(0xFFFEFBFF)   // Neutral 99

// Inverse Colors
val InverseSurface = Color(0xFF313033) // Neutral 20
val InverseOnSurface = Color(0xFFF4EFF4) // Neutral 95
val InversePrimary = Color(0xFFBBDDFF) // Primary 80

// Outline Colors
val Outline = Color(0xFF79747E)        // Neutral variant 50
val OutlineVariant = Color(0xFFCAC4D0) // Neutral variant 80

// Background Colors (для совместимости)
val Background = Surface
val OnBackground = OnSurface

// Dark Theme Colors
val DarkPrimary = Color(0xFF90CAF9)      // Blue 200
val DarkOnPrimary = Color(0xFF003C71)    // Blue 900
val DarkPrimaryContainer = Color(0xFF0D47A1) // Blue 900
val DarkOnPrimaryContainer = Color(0xFFE3F2FD) // Blue 50

val DarkSecondary = Color(0xFFA5D6A7)    // Green 200
val DarkOnSecondary = Color(0xFF1B5E20)  // Green 800
val DarkSecondaryContainer = Color(0xFF2E7D32) // Green 700
val DarkOnSecondaryContainer = Color(0xFFE8F5E8) // Green 50

val DarkTertiary = Color(0xFFFFAB91)     // Deep Orange 200
val DarkOnTertiary = Color(0xFFBF360C)   // Deep Orange 800
val DarkTertiaryContainer = Color(0xFFD84315) // Deep Orange 700
val DarkOnTertiaryContainer = Color(0xFFFBE9E7) // Deep Orange 50

val DarkError = Color(0xFFEF5350)        // Red 400
val DarkOnError = Color(0xFF5F2120)
val DarkErrorContainer = Color(0xFFC62828) // Red 600
val DarkOnErrorContainer = Color(0xFFFDEDED)

val DarkSurface = Color(0xFF111318)      // Neutral 4
val DarkOnSurface = Color(0xFFE6E0E9)    // Neutral 90
val DarkSurfaceVariant = Color(0xFF49454F) // Neutral variant 30
val DarkOnSurfaceVariant = Color(0xFFCAC4D0) // Neutral variant 80

val DarkSurfaceContainer = Color(0xFF1E192B) // Neutral 12
val DarkSurfaceContainerHigh = Color(0xFF292336) // Neutral 17
val DarkSurfaceContainerHighest = Color(0xFF342D40) // Neutral 22
val DarkSurfaceContainerLow = Color(0xFF191624)  // Neutral 10
val DarkSurfaceContainerLowest = Color(0xFF0F0D13) // Neutral 4

val DarkSurfaceDim = Color(0xFF111318)     // Neutral 6
val DarkSurfaceBright = Color(0xFF3B383E) // Neutral 24

val DarkInverseSurface = Color(0xFFE6E0E9) // Neutral 90
val DarkInverseOnSurface = Color(0xFF313033) // Neutral 20
val DarkInversePrimary = Color(0xFF1976D2) // Primary 40

val DarkOutline = Color(0xFF938F99)        // Neutral variant 60
val DarkOutlineVariant = Color(0xFF49454F) // Neutral variant 30

val DarkBackground = DarkSurface
val DarkOnBackground = DarkOnSurface

// Branded/Custom Colors для NimBin
val NimBinBlue = Color(0xFF1565C0)       // Основной цвет бренда
val NimBinBlueDark = Color(0xFF0D47A1)   // Темный вариант бренда
val NimBinBlueLight = Color(0xFF42A5F5)  // Светлый вариант бренда

// Gradient Colors для красивых переходов
val GradientStart = Color(0xFF1976D2)
val GradientEnd = Color(0xFF42A5F5)
val GradientStartDark = Color(0xFF0D47A1)
val GradientEndDark = Color(0xFF1976D2)

// Status Colors для различных состояний
val StatusOnline = Success
val StatusOffline = Color(0xFF9E9E9E)    // Grey 500
val StatusBusy = Warning
val StatusAway = Color(0xFF9C27B0)       // Purple 500

// Elevation Colors для карточек и компонентов
val ElevationLevel1 = Color(0x1F000000)  // 8% opacity
val ElevationLevel2 = Color(0x24000000)  // 12% opacity
val ElevationLevel3 = Color(0x1F000000)  // 16% opacity
val ElevationLevel4 = Color(0x24000000)  // 20% opacity
val ElevationLevel5 = Color(0x29000000)  // 24% opacity
