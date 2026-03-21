package ru.omc.myspaceapp.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.omc.myspaceapp.createDatabase
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.repository.FavoritesRepository

val appModule = module {
    // Database
    single { createDatabase() }
    single { FavoritesRepository(get()) }

    // API
    single { SpaceApi() }

    // ViewModels (добавим позже для каждой фичи)
    // viewModel { AsteroidsViewModel(get(), get()) }
    // viewModel { AstronautsViewModel(get(), get()) }
    // viewModel { FavoritesViewModel(get()) }
}