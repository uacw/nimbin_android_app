package tech.nimbus.nimbin.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel // Keep for ViewModels within this graph
import tech.nimbus.nimbin.ui.features.auth.login.LoginScreen
import tech.nimbus.nimbin.ui.features.auth.login.LoginViewModel
import tech.nimbus.nimbin.ui.features.auth.register.RegisterScreen
import tech.nimbus.nimbin.ui.features.auth.register.RegisterViewModel

// Routes for screens within the Auth graph
sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login_screen")
    object Register : AuthScreen("register_screen")
}

// Routes for graphs
object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val MAIN_APP = "main_app_graph" // Example for later
}

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthScreen.Login.route,
        route = Graph.AUTHENTICATION // This is the route for the entire auth graph
    ) {
        composable(route = AuthScreen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(route = AuthScreen.Register.route) {
            val viewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}