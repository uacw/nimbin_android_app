package tech.nimbus.nimbin.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.nimbus.nimbin.ui.theme.*

/**
 * Современный Floating Action Button с анимациями и градиентом
 */
@Composable
fun ModernFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Edit,
    contentDescription: String? = null,
    expanded: Boolean = false,
    text: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "fabScale"
    )

    if (expanded && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            shape = CustomShapes.fabExtended,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            shape = CustomShapes.fabRegular,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Современный Top App Bar с градиентным фоном и анимациями
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    withGradient: Boolean = false
) {
    val containerColor = if (withGradient) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: { },
        actions = actions ?: { },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Современный Toggle Button с анимациями
 */
@Composable
fun ModernToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "contentColor"
    )

    Button(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.small,
        elevation = if (checked) {
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        } else {
            ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let { iconVector ->
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Современная секция с заголовком и контентом
 */
@Composable
fun ContentSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                subtitle?.let { subtitleText ->
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            actions?.let { actionsContent ->
                Row {
                    actionsContent()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        content()
    }
}

/**
 * Современный Badge с анимацией
 */
@Composable
fun ModernBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: BadgeType = BadgeType.DEFAULT
) {
    val (backgroundColor, textColor) = when (type) {
        BadgeType.DEFAULT -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BadgeType.SUCCESS -> SuccessContainer to OnSuccessContainer
        BadgeType.WARNING -> WarningContainer to OnWarningContainer
        BadgeType.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BadgeType.INFO -> InfoContainer to OnInfoContainer
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = CustomShapes.pill,
        tonalElevation = 1.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class BadgeType {
    DEFAULT, SUCCESS, WARNING, ERROR, INFO
}

/**
 * Градиентный заголовок для героических секций
 */
@Composable
fun GradientHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            subtitle?.let { subtitleText ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.87f)
                )
            }
        }
    }
}

/**
 * Современный разделитель с текстом
 */
@Composable
fun TextDivider(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
