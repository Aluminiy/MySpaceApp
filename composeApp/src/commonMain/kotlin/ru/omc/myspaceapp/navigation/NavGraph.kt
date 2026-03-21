package ru.omc.myspaceapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import ru.omc.myspaceapp.ui.components.BottomNavigationBar
// import ru.omc.myspaceapp.features.asteroids.AsteroidsScreen
// import ru.omc.myspaceapp.features.astronauts.AstronautsScreen
// import ru.omc.myspaceapp.features.favorites.FavoritesScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Asteroids.route,
        Screen.Astronauts.route,
        Screen.Favorites.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Asteroids.route,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            // Заглушки для экранов (заменим на реальные)
            composable(Screen.Asteroids.route) {
                androidx.compose.material3.Text("🚧 Экран астероидов (в разработке)")
            }

            composable(Screen.Astronauts.route) {
                androidx.compose.material3.Text("🚧 Экран космонавтов (в разработке)")
            }

            composable(Screen.Favorites.route) {
                androidx.compose.material3.Text("🚧 Экран избранного (в разработке)")
            }
        }
    }
}