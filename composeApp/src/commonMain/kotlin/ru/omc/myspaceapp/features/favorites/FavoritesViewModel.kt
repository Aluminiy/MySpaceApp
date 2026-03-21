package ru.omc.myspaceapp.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.omc.myspaceapp.Favorite
import ru.omc.myspaceapp.data.repository.FavoritesRepository

data class FavoritesState(
    val isLoading: Boolean = false,
    val favorites: List<Favorite> = emptyList(),
    val error: String? = null
)

sealed interface FavoritesIntent {
    object Load : FavoritesIntent
}

class FavoritesViewModel(
    private val favoritesRepo: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    fun processIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.Load -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                favoritesRepo.getAllFavorites().collect { list ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        favorites = list
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
}