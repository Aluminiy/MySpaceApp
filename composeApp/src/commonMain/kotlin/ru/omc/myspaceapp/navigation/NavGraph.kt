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
import ru.omc.myspaceapp.features.asteroids.AsteroidsScreen
import ru.omc.myspaceapp.features.asteroids.AsteroidDetailsScreen
import ru.omc.myspaceapp.features.astronauts.AstronautsScreen
import ru.omc.myspaceapp.features.astronauts.AstronautDetailsScreen
import ru.omc.myspaceapp.features.favorites.FavoritesScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

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
            composable(Screen.Asteroids.route) {
                AsteroidsScreen(
                    onNavigateToDetails = { id ->
                        navController.navigate(Screen.AsteroidDetails.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.AsteroidDetails.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                if (id != null) {
                    AsteroidDetailsScreen(
                        asteroidId = id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Astronauts.route) {
                AstronautsScreen(
                    onNavigateToDetails = { name ->
                        navController.navigate(Screen.AstronautDetails.createRoute(name))
                    }
                )
            }

            composable(
                route = Screen.AstronautDetails.route,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name")?.replace("_", " ")
                if (name != null) {
                    AstronautDetailsScreen(
                        astronautName = name,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen()
            }
        }
    }
}