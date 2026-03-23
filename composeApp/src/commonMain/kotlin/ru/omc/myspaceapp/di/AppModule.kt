package ru.omc.myspaceapp.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.omc.myspaceapp.createDatabase
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.repository.FavoritesRepository
import ru.omc.myspaceapp.features.asteroids.*
import ru.omc.myspaceapp.features.astronauts.*
import ru.omc.myspaceapp.features.favorites.FavoritesViewModel


val appModule = module {
    println("🔧 [DI] AppModule: registering dependencies...")

    single {
        println("🗄️ [DI] Creating database...")
        createDatabase()
    }

    single {
        println("📦 [DI] Creating FavoritesRepository...")
        FavoritesRepository(get())
    }

    single {
        println("🌐 [DI] Creating SpaceApi...")
        SpaceApi()
    }

    // ViewModels
    viewModel { AsteroidsViewModel(get(), get()) }
    viewModel { AsteroidDetailsViewModel(get(), get()) }
    viewModel { AstronautsViewModel(get(), get()) }
    viewModel { AstronautDetailsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
}