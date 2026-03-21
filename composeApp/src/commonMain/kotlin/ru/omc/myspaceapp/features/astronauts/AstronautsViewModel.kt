package ru.omc.myspaceapp.features.astronauts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AstronautDto
import ru.omc.myspaceapp.data.repository.FavoritesRepository

data class AstronautsState(
    val isLoading: Boolean = false,
    val astronauts: List<AstronautDto> = emptyList(),
    val error: String? = null
)

sealed interface AstronautsIntent {
    object Load : AstronautsIntent
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
        }
    }

    private fun loadAstronauts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = spaceApi.getAstronauts()
                _state.value = _state.value.copy(
                    isLoading = false,
                    astronauts = response.people
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}