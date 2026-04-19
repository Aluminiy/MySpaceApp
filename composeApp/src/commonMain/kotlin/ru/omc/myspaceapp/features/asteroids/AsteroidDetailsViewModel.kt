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
import ru.omc.myspaceapp.data.repository.FavoritesRepositoryImpl
import kotlin.time.Clock as StdClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant as KtxInstant

data class AsteroidDetailsState(
    val isLoading: Boolean = true,
    val asteroid: AsteroidDto? = null,
    val error: String? = null,
    val isFavorite: Boolean = false
)

sealed interface AsteroidDetailsIntent {
    data class Load(val asteroidId: String) : AsteroidDetailsIntent
    data class ToggleFavorite(val asteroidId: String) : AsteroidDetailsIntent
}

class AsteroidDetailsViewModel(
    private val spaceApi: SpaceApi,
    private val favoritesRepo: FavoritesRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(AsteroidDetailsState())
    val state: StateFlow<AsteroidDetailsState> = _state.asStateFlow()

    fun processIntent(intent: AsteroidDetailsIntent) {
        when (intent) {
            is AsteroidDetailsIntent.Load -> loadAsteroid(intent.asteroidId)
            is AsteroidDetailsIntent.ToggleFavorite -> toggleFavorite(intent.asteroidId)
        }
    }

    private fun loadAsteroid(asteroidId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val now = StdClock.System.now()
                val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
                val today: LocalDate = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startDate = today.minus(7, DateTimeUnit.DAY)

                val response = spaceApi.getNearEarthObjects(
                    startDate = startDate.toString(),
                    endDate = today.toString()
                )

                val asteroid = response.nearEarthObjects.values
                    .flatten()
                    .find { it.id == asteroidId }

                if (asteroid != null) {
                    val isFav = favoritesRepo.isFavorite(asteroidId, "asteroid")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        asteroid = asteroid,
                        isFavorite = isFav
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Астероид не найден"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun toggleFavorite(asteroidId: String) {
        viewModelScope.launch {
            val current = _state.value.asteroid ?: return@launch
            val currentlyFavorite = _state.value.isFavorite

            if (currentlyFavorite) {
                favoritesRepo.deleteFavorite(asteroidId, "asteroid")
            } else {
                val now = StdClock.System.now()
                val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
                val currentTime = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                favoritesRepo.insertFavorite(
                    id = asteroidId,
                    type = "asteroid",
                    name = current.name,
                    description = "Размер: ${current.estimatedDiameter.kilometers.min.toInt()} - ${current.estimatedDiameter.kilometers.max.toInt()} м",
                    addedDate = currentTime
                )
            }
            _state.value = _state.value.copy(isFavorite = !currentlyFavorite)
        }
    }
}