package ru.omc.myspaceapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import ru.omc.myspaceapp.Favorite
import ru.omc.myspaceapp.db.AppDatabase

class FavoritesRepository(private val database: AppDatabase) {
    private val queries = database.appDatabaseQueries

    fun getAllFavorites(): Flow<List<Favorite>> {
        return queries.selectAllFavorites()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    fun insertFavorite(id: String, type: String, name: String, description: String?, addedDate: String) {
        queries.insertFavorite(id, type, name, description, addedDate)
    }

    fun deleteFavorite(id: String, type: String) {
        queries.deleteFavorite(id, type)
    }

    fun isFavorite(id: String, type: String): Boolean {
        return queries.isFavorite(id, type).executeAsOne() > 0
    }
}