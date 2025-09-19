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
import tech.nimbus.nimbin.ui.features.paste.EditPasteScreen // добавлен импорт EditPasteScreen
import tech.nimbus.nimbin.ui.features.profile.FavoritesScreen // новый импорт FavoritesScreen

// Sealed class для экранов внутри Home графа
sealed class MainAppScreen(val route: String) {
    object Home : MainAppScreen(route = "home_screen")
    object CreateNote : MainAppScreen(route = "create_note_screen")
    object Profile : MainAppScreen(route = "profile_screen")
    object PasteDetail : MainAppScreen(route = "paste_detail/{id}") {
        fun build(id: String) = "paste_detail/$id"
    }
    object MyPastes : MainAppScreen(route = "my_pastes_screen")
    object Favorites : MainAppScreen(route = "favorites_screen") // новый маршрут
    object UserProfile : MainAppScreen(route = "user_profile/{userId}") {
        fun build(userId: String) = "user_profile/$userId"
    }
    object EditPaste : MainAppScreen(route = "edit_paste/{id}") { // новый маршрут для редактирования
        fun build(id: String) = "edit_paste/$id"
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
        composable(route = MainAppScreen.Favorites.route) { FavoritesScreen(navController = navController) } // новый экран
        composable(route = MainAppScreen.UserProfile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(navController = navController, userId = userId) // Отображаем UserProfileScreen
        }
        composable(route = MainAppScreen.EditPaste.route) { backStackEntry -> // новый экран редактирования
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            EditPasteScreen(navController = navController, pasteId = id)
        }
    }
}
