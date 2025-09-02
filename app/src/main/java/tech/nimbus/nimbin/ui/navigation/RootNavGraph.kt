package tech.nimbus.nimbin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = Graph.ROOT
    ) {
        authNavGraph(navController = navController)
        homeNavGraph(navController = navController) // <--- ДОБАВЛЕНО
    }
}
