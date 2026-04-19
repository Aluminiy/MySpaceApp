package ru.omc.myspaceapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.omc.myspaceapp.Favorite
import kotlin.collections.emptyList


class FakeFavoritesRepository : FavoritesRepository {
    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    private val favoritesMap = mutableMapOf<Pair<String, String>, Favorite>()

    override fun isFavorite(id: String, type: String): Boolean {
        return favoritesMap.containsKey(Pair(id, type))
    }

    override suspend fun insertFavorite(
        id: String,
        type: String,
        name: String,
        description: String,
        addedDate: String
    ) {
        val favorite = Favorite(
            id = id,
            type = type,
            name = name,
            description = description,
            added_date = addedDate
        )
        favoritesMap[Pair(id, type)] = favorite
        _favorites.value = favoritesMap.values.toList()
    }

    override suspend fun deleteFavorite(id: String, type: String) {
        favoritesMap.remove(Pair(id, type))
        _favorites.value = favoritesMap.values.toList()
    }

    override suspend fun getAllFavorites(): Flow<List<Favorite>> {
        return favorites
    }

    // Helper methods for tests
    fun clear() {
        favoritesMap.clear()
        _favorites.value = emptyList()
    }

    suspend fun addTestFavorite(id: String, type: String, name: String) {
        insertFavorite(id, type, name, "Test desc", "2024-01-01")
    }
}