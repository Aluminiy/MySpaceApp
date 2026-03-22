package ru.omc.myspaceapp.features.astronauts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AstronautDto
import ru.omc.myspaceapp.data.repository.FavoritesRepository
import kotlin.time.Clock as StdClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Instant as KtxInstant

data class AstronautDetailsState(
    val isLoading: Boolean = true,
    val astronaut: AstronautDto? = null,
    val allAstronauts: List<AstronautDto> = emptyList(),
    val error: String? = null,
    val isFavorite: Boolean = false
)

sealed interface AstronautDetailsIntent {
    data class Load(val astronautName: String) : AstronautDetailsIntent
    data class ToggleFavorite(val astronautName: String) : AstronautDetailsIntent
}

class AstronautDetailsViewModel(
    private val spaceApi: SpaceApi,
    private val favoritesRepo: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AstronautDetailsState())
    val state: StateFlow<AstronautDetailsState> = _state.asStateFlow()

    fun processIntent(intent: AstronautDetailsIntent) {
        when (intent) {
            is AstronautDetailsIntent.Load -> loadAstronaut(intent.astronautName)
            is AstronautDetailsIntent.ToggleFavorite -> toggleFavorite(intent.astronautName)
        }
    }

    private fun loadAstronaut(astronautName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = spaceApi.getAstronauts()
                val astronaut = response.people.find { it.name == astronautName }

                if (astronaut != null) {
                    val isFav = favoritesRepo.isFavorite(astronautName, "astronaut")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        astronaut = astronaut,
                        allAstronauts = response.people,
                        isFavorite = isFav
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Астронавт не найден"
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

    private fun toggleFavorite(astronautName: String) {
        viewModelScope.launch {
            val current = _state.value.astronaut ?: return@launch
            val currentlyFavorite = _state.value.isFavorite

            if (currentlyFavorite) {
                favoritesRepo.deleteFavorite(astronautName, "astronaut")
            } else {
                val now = StdClock.System.now()
                val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
                val currentTime = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                favoritesRepo.insertFavorite(
                    id = astronautName,
                    type = "astronaut",
                    name = current.name,
                    description = "Корабль: ${current.craft}",
                    addedDate = currentTime
                )
            }
            _state.value = _state.value.copy(isFavorite = !currentlyFavorite)
        }
    }
}