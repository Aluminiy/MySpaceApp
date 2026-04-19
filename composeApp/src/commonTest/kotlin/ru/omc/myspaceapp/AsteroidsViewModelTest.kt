package ru.omc.myspaceapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.*
import ru.omc.myspaceapp.data.repository.FakeAsteroidsRepository
import ru.omc.myspaceapp.data.repository.FakeFavoritesRepository
import ru.omc.myspaceapp.features.asteroids.AsteroidsIntent
import ru.omc.myspaceapp.features.asteroids.AsteroidsViewModel
import kotlin.collections.emptyList
import kotlin.collections.emptyMap
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AsteroidsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeSpaceApi = object : SpaceApi() {
        override suspend fun getNearEarthObjects(
            startDate: String,
            endDate: String
        ): NearEarthObjectsResponse {
            return NearEarthObjectsResponse(emptyMap())
        }

        override suspend fun getAstronauts(): AstronautResponseDto {
            return AstronautResponseDto(
                message = "success",
                number = 0,
                people = emptyList()
            )
        }
    }

    private val fakeAsteroidsRepo = FakeAsteroidsRepository()
    private val fakeFavoritesRepo = FakeFavoritesRepository()

    private lateinit var viewModel: AsteroidsViewModel

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
        Dispatchers.setMain(testDispatcher)
        fakeAsteroidsRepo.asteroidsToReturn = emptyList()
        fakeAsteroidsRepo.shouldFail = false
        fakeFavoritesRepo.clear()

        viewModel = AsteroidsViewModel(
            spaceApi = fakeSpaceApi,
            favoritesRepo = fakeFavoritesRepo,
            asteroidsRepo = fakeAsteroidsRepo
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Load Intent
    // ═══════════════════════════════════════════════════════

    @Test
    fun `load asteroids - success - should update state with data`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Kotlin Test assertions
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(testAsteroid), state.asteroids)
        assertNull(state.error)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `load asteroids - error - should update state with error`() = runTest {
        // Given
        fakeAsteroidsRepo.shouldFail = true
        fakeAsteroidsRepo.errorMessage = "Network error"

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains("Network error") == true)
        assertTrue(state.asteroids.isEmpty())
    }

    @Test
    fun `load asteroids - offline mode - should return cached data`() = runTest {
        // Given
        val cachedAsteroids = listOf(testAsteroid.copy(name = "Cached"))
        fakeAsteroidsRepo.asteroidsToReturn = cachedAsteroids

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(cachedAsteroids, state.asteroids)
        assertFalse(state.isOffline) // Успешный результат = не оффлайн
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Refresh Intent
    // ═══════════════════════════════════════════════════════

    @Test
    fun `refresh asteroids - should call with forceRefresh true`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(fakeAsteroidsRepo.lastForceRefreshValue)
        assertEquals(1, fakeAsteroidsRepo.getAsteroidsCallCount)
    }

    @Test
    fun `refresh asteroids - error - should reset isRefreshing`() = runTest {
        // Given
        fakeAsteroidsRepo.shouldFail = true
        fakeAsteroidsRepo.errorMessage = "API error"

        // When
        viewModel.processIntent(AsteroidsIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isRefreshing)
        assertTrue(state.error != null)
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: ToggleFavorite Intent
    // ═══════════════════════════════════════════════════════

    @Test
    fun `toggle favorite - add to favorites - should update favoriteIds`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite(testAsteroid.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state.favoriteIds.contains(testAsteroid.id))
        assertTrue(fakeFavoritesRepo.isFavorite(testAsteroid.id, "asteroid"))
    }

    @Test
    fun `toggle favorite - remove from favorites - should update favoriteIds`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        fakeFavoritesRepo.addTestFavorite(testAsteroid.id, "asteroid", testAsteroid.name)

        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite(testAsteroid.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.favoriteIds.contains(testAsteroid.id))
        assertFalse(fakeFavoritesRepo.isFavorite(testAsteroid.id, "asteroid"))
    }

    @Test
    fun `toggle favorite - asteroid not found - should not call repository`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Запоминаем состояние до
        val beforeSize = fakeFavoritesRepo.favorites.value.size

        // When - пытаемся добавить несуществующий астероид
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite("unknown-id"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - состояние не изменилось
        assertEquals(beforeSize, fakeFavoritesRepo.favorites.value.size)
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Edge Cases
    // ═══════════════════════════════════════════════════════

    @Test
    fun `load with empty list - should update state with empty asteroids`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = emptyList()

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state.asteroids.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `multiple load calls - should handle correctly`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        viewModel.processIntent(AsteroidsIntent.Load) // Второй вызов
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(listOf(testAsteroid), state.asteroids)
    }
}