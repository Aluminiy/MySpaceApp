package ru.omc.myspaceapp.features.astronauts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AstronautDto
import ru.omc.myspaceapp.data.repository.FavoritesRepository
import kotlin.time.Clock as StdClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant as KtxInstant

data class AstronautsState(
    val isLoading: Boolean = false,
    val astronauts: List<AstronautDto> = emptyList(),
    val favoriteNames: Set<String> = emptySet(),
    val error: String? = null
)

sealed interface AstronautsIntent {
    object Load : AstronautsIntent
    data class ToggleFavorite(val astronautName: String) : AstronautsIntent
}

class AstronautsViewModel(
    private val spaceApi: SpaceApi,
    private val favoritesRepo: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AstronautsState())
    val state: StateFlow<AstronautsState> = _state.asStateFlow()

    fun processIntent(intent: AstronautsIntent) {
        when (intent) {
            is AstronautsIntent.Load -> loadAstronauts()
            is AstronautsIntent.ToggleFavorite -> toggleFavorite(intent.astronautName)
        }
    }

    private fun loadAstronauts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = spaceApi.getAstronauts()
                val allAstronauts = response.people
                val favoriteNames = allAstronauts
                    .filter { favoritesRepo.isFavorite(it.name, "astronaut") }
                    .map { it.name }
                    .toSet()

                _state.value = _state.value.copy(
                    isLoading = false,
                    astronauts = allAstronauts,
                    favoriteNames = favoriteNames
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun toggleFavorite(astronautName: String) {
        viewModelScope.launch {
            val astronaut = _state.value.astronauts.find { it.name == astronautName } ?: return@launch
            val isCurrentlyFavorite = _state.value.favoriteNames.contains(astronautName)

            if (isCurrentlyFavorite) {
                favoritesRepo.deleteFavorite(astronautName, "astronaut")
                println("❌ Removed from favorites: $astronautName")

                _state.value = _state.value.copy(
                    favoriteNames = _state.value.favoriteNames - astronautName
                )
            } else {
                val now = StdClock.System.now()
                val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
                val currentTime = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                favoritesRepo.insertFavorite(
                    id = astronautName,
                    type = "astronaut",
                    name = astronaut.name,
                    description = "Корабль: ${astronaut.craft}",
                    addedDate = currentTime
                )
                println("⭐ Added to favorites: $astronautName")

                _state.value = _state.value.copy(
                    favoriteNames = _state.value.favoriteNames + astronautName
                )
            }
        }
    }
}