package tech.nimbus.nimbin.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.ui.auth.AuthStatusViewModel
import tech.nimbus.nimbin.ui.navigation.MainAppScreen
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Современная нижняя навигационная пане��ь Material 3 с анимациями и улучшенным дизайном
 */
@Composable
fun MainBottomNavBar(navController: NavController) {
    val authVM: AuthStatusViewModel = hiltViewModel()
    val isGuest by authVM.isGuest.collectAsState()

    val items = listOf(
        ModernBottomNavItem(
            route = MainAppScreen.Home.route,
            titleRes = R.string.bottom_nav_home,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            contentDescriptionRes = R.string.bottom_nav_home_cd
        ),
        ModernBottomNavItem(
            route = MainAppScreen.CreateNote.route,
            titleRes = R.string.bottom_nav_create,
            selectedIcon = Icons.Filled.Add,
            unselectedIcon = Icons.Outlined.Add,
            contentDescriptionRes = R.string.bottom_nav_create_cd
        ),
        ModernBottomNavItem(
            route = MainAppScreen.Profile.route,
            titleRes = R.string.bottom_nav_profile,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            contentDescriptionRes = R.string.bottom_nav_profile_cd
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            // Анимации для плавных переходов
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconScale"
            )

            val iconColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 200),
                label = "iconColor"
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        // Ранее тут был редирект гостей на экран аутентификации. Теперь всегда ведем на целевой экран.
                        val targetRoute = item.route
                        navController.navigate(targetRoute) {
                            popUpTo(MainAppScreen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(id = item.contentDescriptionRes),
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale),
                        tint = iconColor
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.titleRes),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Улучшенная структура данных для элементов навигации с поддержкой выбранных/невыбранных иконок
 */
private data class ModernBottomNavItem(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescriptionRes: Int
)
