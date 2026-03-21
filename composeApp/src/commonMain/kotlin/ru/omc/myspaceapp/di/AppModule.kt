package ru.omc.myspaceapp.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.omc.myspaceapp.createDatabase
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.repository.FavoritesRepository
import ru.omc.myspaceapp.features.asteroids.*
import ru.omc.myspaceapp.features.astronauts.*
import ru.omc.myspaceapp.features.favorites.FavoritesViewModel

val appModule = module {
    // Database
    single { createDatabase() }
    single { FavoritesRepository(get()) }

    // API
    single { SpaceApi() }

    // ViewModels
    viewModel { AsteroidsViewModel(get(), get()) }
    viewModel { AsteroidDetailsViewModel(get(), get()) }
    viewModel { AstronautsViewModel(get(), get()) }
    viewModel { AstronautDetailsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
}