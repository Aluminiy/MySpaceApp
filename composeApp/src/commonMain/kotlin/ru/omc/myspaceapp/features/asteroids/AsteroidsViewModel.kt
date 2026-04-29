package ru.omc.myspaceapp.features.asteroids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AsteroidDto
import kotlin.time.Clock as StdClock
import kotlinx.datetime.TimeZone
import ru.omc.myspaceapp.data.repository.*
import kotlinx.datetime.Instant as KtxInstant

// === State ===
data class AsteroidsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val asteroids: List<AsteroidDto> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val error: String? = null,
    val isOffline: Boolean = false
)

// === Intent ===
sealed interface AsteroidsIntent {
    object Load : AsteroidsIntent
    object Refresh : AsteroidsIntent
    data class ToggleFavorite(val asteroidId: String) : AsteroidsIntent
}

// === ViewModel ===
open class AsteroidsViewModel(
    private val spaceApi: SpaceApi,
    private val favoritesRepo: FavoritesRepository,
    private val asteroidsRepo: AsteroidsRepository
) : ViewModel() {

    val _state = MutableStateFlow(AsteroidsState())
    val state: StateFlow<AsteroidsState> = _state.asStateFlow()

    open fun processIntent(intent: AsteroidsIntent) {
        when (intent) {
            is AsteroidsIntent.Load -> loadAsteroids(forceRefresh = false)
            is AsteroidsIntent.Refresh -> loadAsteroids(forceRefresh = true)
            is AsteroidsIntent.ToggleFavorite -> toggleFavorite(intent.asteroidId)
        }
    }

    private fun loadAsteroids(forceRefresh: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = !forceRefresh,
                isRefreshing = forceRefresh,
                error = null
            )

            val result = asteroidsRepo.getAsteroids(forceRefresh = forceRefresh)

            result.fold(
                onSuccess = { asteroids ->
                    // ✅ Загружаем статус избранного
                    val favoriteIds = asteroids
                        .filter { favoritesRepo.isFavorite(it.id, "asteroid") }
                        .map { it.id }
                        .toSet()

                    // ✅ Проверяем, есть ли интернет (если кэш вернули при ошибке)
                    val isOffline = result.isFailure || asteroids.isEmpty()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        asteroids = asteroids,
                        favoriteIds = favoriteIds,
                        isOffline = isOffline && asteroids.isNotEmpty()
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "Unknown error",
                        isOffline = false
                    )
                }
            )
        }
    }

    private fun toggleFavorite(asteroidId: String) {
        viewModelScope.launch {
            val asteroid = _state.value.asteroids.find { it.id == asteroidId } ?: return@launch
            val isCurrentlyFavorite = _state.value.favoriteIds.contains(asteroidId)

            if (isCurrentlyFavorite) {
                favoritesRepo.deleteFavorite(asteroidId, "asteroid")
                println("❌ Removed from favorites: $asteroidId")

                _state.value = _state.value.copy(
                    favoriteIds = _state.value.favoriteIds - asteroidId
                )
            } else {
                val now = StdClock.System.now()
                val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
                val currentTime = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                favoritesRepo.insertFavorite(
                    id = asteroidId,
                    type = "asteroid",
                    name = asteroid.name,
                    description = "Размер: ${asteroid.estimatedDiameter.kilometers.min.toInt()} - ${asteroid.estimatedDiameter.kilometers.max.toInt()} м",
                    addedDate = currentTime
                )
                println("⭐ Added to favorites: $asteroidId")

                _state.value = _state.value.copy(
                    favoriteIds = _state.value.favoriteIds + asteroidId
                )
            }
        }
    }
}