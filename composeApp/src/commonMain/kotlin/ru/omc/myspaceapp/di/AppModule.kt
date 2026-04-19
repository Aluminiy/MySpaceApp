package ru.omc.myspaceapp.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.omc.myspaceapp.createDatabase
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.repository.AsteroidsRepository
import ru.omc.myspaceapp.data.repository.AstronautsRepository
import ru.omc.myspaceapp.data.repository.FavoritesRepository
import ru.omc.myspaceapp.features.asteroids.*
import ru.omc.myspaceapp.features.astronauts.*
import ru.omc.myspaceapp.features.favorites.FavoritesViewModel


val appModule = module {

    single { createDatabase() }
    single { SpaceApi() }
    single { FavoritesRepository(get()) }
    single { AsteroidsRepository(get(), get()) }
    single { AstronautsRepository(get(), get()) }

    // ViewModels
    viewModel { AsteroidsViewModel(get(), get(), get()) }
    viewModel { AsteroidDetailsViewModel(get(), get()) }
    viewModel { AstronautsViewModel(get(), get(), get()) }
    viewModel { AstronautDetailsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
}