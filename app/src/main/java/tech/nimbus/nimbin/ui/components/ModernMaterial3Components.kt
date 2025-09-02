package tech.nimbus.nimbin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tech.nimbus.nimbin.ui.theme.*

/**
 * Современные кнопки Material 3
 */
@Composable
fun NimBinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    leadingIcon: ImageVector? = null,
    style: ButtonStyle = ButtonStyle.PRIMARY
) {
    when (style) {
        ButtonStyle.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            ButtonContent(text, leadingIcon)
        }

        ButtonStyle.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            ButtonContent(text, leadingIcon)
        }

        ButtonStyle.TERTIARY -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text, leadingIcon)
        }

        ButtonStyle.TONAL -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            ButtonContent(text, leadingIcon)
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        leadingIcon?.let { icon ->
            Icon(
                imageVector = icon,
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

enum class ButtonStyle {
    PRIMARY, SECONDARY, TERTIARY, TONAL
}

/**
 * Современная карточка Material 3 с различными стилями elevation
 */
@Composable
fun NimBinCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            elevation = elevation,
            colors = colors,
            border = border,
            shape = MaterialTheme.shapes.medium,
            content = {
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        )
    } else {
        Card(
            modifier = modifier,
            elevation = elevation,
            colors = colors,
            border = border,
            shape = MaterialTheme.shapes.medium,
            content = {
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        )
    }
}

/**
 * Современный чип Material 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NimBinChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    if (onClick != null) {
        FilterChip(
            onClick = onClick,
            label = { Text(label) },
            selected = selected,
            modifier = modifier,
            enabled = enabled,
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null) } }
        )
    } else {
        AssistChip(
            onClick = { },
            label = { Text(label) },
            modifier = modifier,
            enabled = enabled,
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null) } }
        )
    }
}

/**
 * Статусные карточки для различных состояний
 */
@Composable
fun StatusCard(
    message: String,
    type: StatusType,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    val (containerColor, contentColor, icon) = when (type) {
        StatusType.SUCCESS -> Triple(SuccessContainer, OnSuccessContainer, Icons.Default.Check)
        StatusType.WARNING -> Triple(WarningContainer, OnWarningContainer, Icons.Default.Warning)
        StatusType.ERROR -> Triple(ErrorContainer, OnErrorContainer, Icons.Default.Error)
        StatusType.INFO -> Triple(InfoContainer, OnInfoContainer, Icons.Default.Info)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                title?.let { titleText ->
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleSmall,
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }

            action?.let { actionContent ->
                Spacer(modifier = Modifier.width(8.dp))
                actionContent()
            }
        }
    }
}

enum class StatusType {
    SUCCESS, WARNING, ERROR, INFO
}

/**
 * Современный загрузочный индикатор
 */
@Composable
fun NimBinLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Загрузка..."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Пустое состояние с иконкой и сообщением
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        action?.let { actionContent ->
            Spacer(modifier = Modifier.height(24.dp))
            actionContent()
        }
    }
}

/**
 * Анимированный индикатор состояния загрузки
 */
@Composable
fun AnimatedLoadingState(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }
    }
}
