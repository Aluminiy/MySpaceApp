package ru.omc.myspaceapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import ru.omc.myspaceapp.ui.navigation.NavGraph

@Composable
fun MySpaceApp() {
    val navController = rememberNavController()
    MaterialTheme {
        Surface {
            NavGraph(navController = navController)
        }
    }
}