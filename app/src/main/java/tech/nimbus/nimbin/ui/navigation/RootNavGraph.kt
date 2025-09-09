package tech.nimbus.nimbin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tech.nimbus.nimbin.core.session.GlobalSessionViewModel
import tech.nimbus.nimbin.core.session.SessionEvent

@Composable
fun RootNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val sessionVm: GlobalSessionViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        sessionVm.events.collect { event ->
            if (event is SessionEvent.SessionExpired) {
                navController.navigate(Graph.AUTHENTICATION) {
                    popUpTo(Graph.ROOT) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = Graph.ROOT
    ) {
        authNavGraph(navController = navController)
        homeNavGraph(navController = navController)
    }
}
