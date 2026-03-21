package ru.omc.myspaceapp.navigation

sealed class Screen(val route: String) {
    object Asteroids : Screen("asteroids")
    object AsteroidDetails : Screen("asteroid_details/{id}") {
        fun createRoute(id: String) = "asteroid_details/$id"
    }

    object Astronauts : Screen("astronauts")
    object AstronautDetails : Screen("astronaut_details/{name}") {
        fun createRoute(name: String) = "astronaut_details/${name.replace(" ", "_")}"
    }

    object Favorites : Screen("favorites")
}