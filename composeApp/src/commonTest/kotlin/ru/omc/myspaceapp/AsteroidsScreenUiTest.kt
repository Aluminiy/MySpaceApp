package ru.omc.myspaceapp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AsteroidDto
import ru.omc.myspaceapp.data.model.AstronautResponseDto
import ru.omc.myspaceapp.data.model.DiameterRange
import ru.omc.myspaceapp.data.model.EstimatedDiameter
import ru.omc.myspaceapp.data.model.NearEarthObjectsResponse
import ru.omc.myspaceapp.data.repository.FakeAsteroidsRepository
import ru.omc.myspaceapp.data.repository.FakeFavoritesRepository
import ru.omc.myspaceapp.features.asteroids.AsteroidsIntent
import ru.omc.myspaceapp.features.asteroids.AsteroidsScreen
import ru.omc.myspaceapp.features.asteroids.AsteroidsState
import ru.omc.myspaceapp.features.asteroids.AsteroidsViewModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FakeAsteroidsViewModelForUiTest : AsteroidsViewModel(
    spaceApi = object : SpaceApi() {
        override suspend fun getNearEarthObjects(startDate: String, endDate: String): NearEarthObjectsResponse {
            return NearEarthObjectsResponse(emptyMap())
        }
        override suspend fun getAstronauts(): AstronautResponseDto {
            return AstronautResponseDto("success", 0, emptyList())
        }
    },
    favoritesRepo = FakeFavoritesRepository(),
    asteroidsRepo = FakeAsteroidsRepository()
) {
    fun updateState(newState: AsteroidsState) {
        _state.value = newState
    }

    var lastIntent: AsteroidsIntent? = null
        private set

    override fun processIntent(intent: AsteroidsIntent) {
        lastIntent = intent
        super.processIntent(intent)
    }
}

@OptIn(ExperimentalTestApi::class)
class AsteroidsScreenUiTest {

    private lateinit var viewModel: FakeAsteroidsViewModelForUiTest

    private val testAsteroid = AsteroidDto(
        id = "123",
        name = "Test Asteroid",
        isPotentiallyHazardous = false,
        closeApproachData = emptyList(),
        estimatedDiameter = EstimatedDiameter(
            kilometers = DiameterRange(min = 0.01, max = 0.05)
        )
    )

    @BeforeTest
    fun setup() {
        viewModel = FakeAsteroidsViewModelForUiTest()
    }

    // ═══════════════════════════════════════════════════════
    // UI ТЕСТ 1: Отображение списка астероидов
    // ═══════════════════════════════════════════════════════

    @Test
    fun `asteroids screen - displays asteroid name when data is loaded`() = runComposeUiTest {
        // Given
        val state = AsteroidsState(
            isLoading = false,
            isRefreshing = false,
            asteroids = listOf(testAsteroid),
            favoriteIds = emptySet(),
            error = null,
            isOffline = false
        )
        viewModel.updateState(state)

        // When
        setContent {
            AsteroidsScreen(
                onNavigateToDetails = {},
                viewModel = viewModel
            )
        }

        // Then - 1 асерт
        onNodeWithText("Test Asteroid").assertExists()
    }

    // ═══════════════════════════════════════════════════════
    // UI ТЕСТ 2: Отображение индикатора загрузки
    // ═══════════════════════════════════════════════════════

    @Test
    fun `asteroids screen - shows loading indicator when isLoading is true`() = runComposeUiTest {
        // Given
        val state = AsteroidsState(
            isLoading = true,
            isRefreshing = false,
            asteroids = emptyList(),
            favoriteIds = emptySet(),
            error = null,
            isOffline = false
        )
        viewModel.updateState(state)

        // When
        setContent {
            AsteroidsScreen(
                onNavigateToDetails = {},
                viewModel = viewModel
            )
        }

        // Then - 1 асерт
        onNodeWithTag("loading_indicator").assertExists()
    }

    // ═══════════════════════════════════════════════════════
    // UI ТЕСТ 3: Отображение ошибки
    // ═══════════════════════════════════════════════════════

    @Test
    fun `asteroids screen - displays error message when error is not null`() = runComposeUiTest {
        // Given
        val errorMessage = "Network error"
        val state = AsteroidsState(
            isLoading = false,
            isRefreshing = false,
            asteroids = emptyList(),
            favoriteIds = emptySet(),
            error = errorMessage,
            isOffline = false
        )
        viewModel.updateState(state)

        // When
        setContent {
            AsteroidsScreen(
                onNavigateToDetails = {},
                viewModel = viewModel
            )
        }

        // Then - 1 асерт
        onNodeWithText("Ошибка: $errorMessage").assertExists()
    }

    // ═══════════════════════════════════════════════════════
    // UI ТЕСТ 4: Кнопка избранного
    // ═══════════════════════════════════════════════════════

    @Test
    fun `asteroids screen - favorite button sends ToggleFavorite intent on click`() = runComposeUiTest {
        // Given
        val state = AsteroidsState(
            isLoading = false,
            isRefreshing = false,
            asteroids = listOf(testAsteroid),
            favoriteIds = emptySet(),
            error = null,
            isOffline = false
        )
        viewModel.updateState(state)

        // When
        setContent {
            AsteroidsScreen(
                onNavigateToDetails = {},
                viewModel = viewModel
            )
        }

        // Находим кнопку избранного и кликаем
        onNodeWithContentDescription("В избранное").performClick()

        // Then - 1 асерт
        assertTrue(viewModel.lastIntent is AsteroidsIntent.ToggleFavorite)
    }
}