package ru.omc.myspaceapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import ru.omc.myspaceapp.navigation.NavGraph

@Composable
fun App() {
    val navController = rememberNavController()
    MaterialTheme {
        Surface {
            NavGraph(navController = navController)
        }
    }
}