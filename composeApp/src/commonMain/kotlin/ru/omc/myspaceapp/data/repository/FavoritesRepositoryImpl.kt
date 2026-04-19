package ru.omc.myspaceapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.omc.myspaceapp.Favorite
import ru.omc.myspaceapp.db.AppDatabase

interface FavoritesRepository {
    fun isFavorite(id: String, type: String): Boolean
    suspend fun insertFavorite(id: String, type: String, name: String, description: String, addedDate: String)
    suspend fun deleteFavorite(id: String, type: String)
    suspend fun getAllFavorites(): Flow<List<Favorite>>
}

class FavoritesRepositoryImpl(
    private val database: AppDatabase
) : FavoritesRepository {

    private val queries = database.appDatabaseQueries
    private val favoritesFlow = MutableStateFlow<List<Favorite>>(emptyList())

    override fun isFavorite(id: String, type: String): Boolean {
        val count = queries.isFavorite(id, type).executeAsOneOrNull() ?: return false
        return count > 0
    }

    override suspend fun insertFavorite(
        id: String,
        type: String,
        name: String,
        description: String,
        addedDate: String
    ) {
        queries.transaction {
            queries.insertFavorite(id, type, name, description, addedDate)
        }
        refreshFavorites()
    }

    override suspend fun deleteFavorite(id: String, type: String) {
        queries.transaction {
            queries.deleteFavorite(id, type)
        }
        refreshFavorites()
    }

    override suspend fun getAllFavorites(): Flow<List<Favorite>> {
        return favoritesFlow
    }

    private fun refreshFavorites() {
        val favorites = queries.selectAllFavorites().executeAsList().map { row ->
            Favorite(
                id = row.id,
                type = row.type,
                name = row.name,
                description = row.description,
                added_date = row.added_date
            )
        }
        favoritesFlow.value = favorites
    }
}