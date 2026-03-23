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
import ru.omc.myspaceapp.data.repository.FavoritesRepository
import kotlin.time.Clock as StdClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant as KtxInstant

// === State ===
data class AsteroidsState(
    val isLoading: Boolean = false,
    val asteroids: List<AsteroidDto> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val error: String? = null
)

// === Intent ===
sealed interface AsteroidsIntent {
    object Load : AsteroidsIntent
    data class ToggleFavorite(val asteroidId: String) : AsteroidsIntent
}

// === ViewModel ===
class AsteroidsViewModel(
    private val spaceApi: SpaceApi,
    private val favoritesRepo: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AsteroidsState())
    val state: StateFlow<AsteroidsState> = _state.asStateFlow()

    fun processIntent(intent: AsteroidsIntent) {
        when (intent) {
            is AsteroidsIntent.Load -> loadAsteroids()
            is AsteroidsIntent.ToggleFavorite -> toggleFavorite(intent.asteroidId)
        }
    }

    private fun loadAsteroids() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // ✅ Даты через kotlinx-datetime
                val now = StdClock.System.now()
                val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
                val today: LocalDate = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startDate = today.minus(7, DateTimeUnit.DAY)

                println("🚀 NEO Request: start=$startDate, end=$today")

                val response = spaceApi.getNearEarthObjects(
                    startDate = startDate.toString(),
                    endDate = today.toString()
                )

                val allAsteroids = response.nearEarthObjects.values.flatten()
                println("✅ Found ${allAsteroids.size} asteroids")

                // ✅ Загружаем статус избранного для всех астероидов
                val favoriteIds = allAsteroids
                    .filter { favoritesRepo.isFavorite(it.id, "asteroid") }
                    .map { it.id }
                    .toSet()

                _state.value = _state.value.copy(
                    isLoading = false,
                    asteroids = allAsteroids,
                    favoriteIds = favoriteIds
                )
            } catch (e: Exception) {
                println("❌ ERROR: ${e::class.simpleName} - ${e.message}")
                e.printStackTrace()

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
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