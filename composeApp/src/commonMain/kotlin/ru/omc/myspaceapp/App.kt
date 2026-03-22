package ru.omc.myspaceapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import ru.omc.myspaceapp.di.appModule
import ru.omc.myspaceapp.navigation.NavGraph

@Composable
fun App(koinAppDeclaration: KoinAppDeclaration? = null) {
    KoinApplication(application = {
        koinAppDeclaration?.invoke(this)
        modules(appModule)
    }) {
        val navController = rememberNavController()
        MaterialTheme {
            Surface {
                NavGraph(navController = navController)
            }
        }
    }
}
