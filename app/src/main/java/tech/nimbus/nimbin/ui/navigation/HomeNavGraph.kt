package tech.nimbus.nimbin.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import tech.nimbus.nimbin.ui.features.home.HomeScreen // Импорт для HomeScreen
import tech.nimbus.nimbin.ui.features.note.CreateNoteScreen // Импорт для CreateNoteScreen
import tech.nimbus.nimbin.ui.features.profile.ProfileScreen // Импорт для ProfileScreen
import tech.nimbus.nimbin.ui.features.paste.PasteDetailScreen // Импорт для PasteDetailScreen
import tech.nimbus.nimbin.ui.features.profile.MyPastesScreen
import tech.nimbus.nimbin.ui.features.profile.UserProfileScreen // Импорт для UserProfileScreen

// Sealed class для экранов внутри Home графа
sealed class MainAppScreen(val route: String) {
    object Home : MainAppScreen(route = "home_screen")
    object CreateNote : MainAppScreen(route = "create_note_screen")
    object Profile : MainAppScreen(route = "profile_screen")
    object PasteDetail : MainAppScreen(route = "paste_detail/{id}") {
        fun build(id: String) = "paste_detail/$id"
    }
    object MyPastes : MainAppScreen(route = "my_pastes_screen")
    object UserProfile : MainAppScreen(route = "user_profile/{userId}") {
        fun build(userId: String) = "user_profile/$userId"
    }
}

fun NavGraphBuilder.homeNavGraph(navController: NavController) {
    navigation(
        startDestination = MainAppScreen.Home.route, // Начальный экран в этом графе
        route = Graph.MAIN_APP // Маршрут для всего этого вложенного графа
    ) {
        composable(route = MainAppScreen.Home.route) {
            HomeScreen(navController = navController) // Отображаем HomeScreen
        }
        composable(route = MainAppScreen.CreateNote.route) {
            CreateNoteScreen(navController = navController) // Отображаем CreateNoteScreen
        }
        composable(route = MainAppScreen.Profile.route) {
            ProfileScreen(navController = navController) // Отображаем ProfileScreen
        }
        composable(route = MainAppScreen.PasteDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            PasteDetailScreen(navController = navController, pasteId = id) // Отображаем PasteDetailScreen
        }
        composable(route = MainAppScreen.MyPastes.route) { MyPastesScreen(navController = navController) }
        composable(route = MainAppScreen.UserProfile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(navController = navController, userId = userId) // Отображаем UserProfileScreen
        }
    }
}
