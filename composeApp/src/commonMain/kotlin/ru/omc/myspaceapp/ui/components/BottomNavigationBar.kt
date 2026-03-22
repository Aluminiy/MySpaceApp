package ru.omc.myspaceapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.omc.myspaceapp.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: String
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Asteroids, "Астероиды", "☄️"),
        BottomNavItem(Screen.Astronauts, "Космонавты", "👨‍"),
        BottomNavItem(Screen.Favorites, "Избранное", "⭐")
    )

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = item.icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Asteroids.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}