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

// === State ===
data class AsteroidsState(
    val isLoading: Boolean = false,
    val asteroids: List<AsteroidDto> = emptyList(),
    val error: String? = null
)

// === Intent ===
sealed interface AsteroidsIntent {
    object Load : AsteroidsIntent
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
        }
    }

    private fun loadAsteroids() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // ✅ Даты через kotlinx-datetime
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                val startDate = today.minus(7, DateTimeUnit.DAY)

                println("🚀 NEO Request: start=$startDate, end=$today")

                val response = spaceApi.getNearEarthObjects(
                    startDate = startDate.toString(),
                    endDate = today.toString()
                )

                val allAsteroids = response.nearEarthObjects.values.flatten()
                println("✅ Found ${allAsteroids.size} asteroids")

                _state.value = _state.value.copy(
                    isLoading = false,
                    asteroids = allAsteroids
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
}