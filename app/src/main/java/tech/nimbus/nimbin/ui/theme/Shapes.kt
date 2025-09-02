package tech.nimbus.nimbin.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Современная система форм Material 3 для NimBin
 * Использует правильные радиусы скругления для различных компонентов
 */
val Shapes = Shapes(
    // Экстра маленькие формы - для чипов, маленьких кнопок
    extraSmall = RoundedCornerShape(4.dp),

    // Маленькие формы - для кнопок, полей ввода
    small = RoundedCornerShape(8.dp),

    // Средние формы - для карточек, диалогов
    medium = RoundedCornerShape(12.dp),

    // Большие формы - для модальных окон, bottom sheets
    large = RoundedCornerShape(16.dp),

    // Экстра большие формы - для полноэкранных элементов
    extraLarge = RoundedCornerShape(24.dp)
)

// Дополнительные формы для специальных случаев
object CustomShapes {
    val pill = RoundedCornerShape(50) // Полностью округлая форма
    val topRounded = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val bottomRounded = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    val leftRounded = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
    val rightRounded = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)

    // Специальные формы для Material 3
    val surfaceContainer = RoundedCornerShape(12.dp)
    val surfaceContainerHigh = RoundedCornerShape(16.dp)
    val surfaceContainerHighest = RoundedCornerShape(20.dp)

    // Формы для FAB и других элементов
    val fabSmall = RoundedCornerShape(12.dp)
    val fabRegular = RoundedCornerShape(16.dp)
    val fabLarge = RoundedCornerShape(28.dp)
    val fabExtended = RoundedCornerShape(16.dp)
}
