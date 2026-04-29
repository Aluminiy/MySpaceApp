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
    // ТЕСТЫ: Load Intent - isLoading
    // ═══════════════════════════════════════════════════════

    @Test
    fun `load asteroids - isLoading is true during loading`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Load)

        // Then - 1 асерт
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `load asteroids - isLoading is false after success`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertFalse(viewModel.state.value.isLoading)
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Load Intent - asteroids data
    // ═══════════════════════════════════════════════════════

    @Test
    fun `load asteroids - asteroids list is updated on success`() = runTest {
        // Given
        val expectedAsteroids = listOf(testAsteroid)
        fakeAsteroidsRepo.asteroidsToReturn = expectedAsteroids

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertEquals(expectedAsteroids, viewModel.state.value.asteroids)
    }

    @Test
    fun `load asteroids - error is null on success`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `load asteroids - isRefreshing is false after load`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertFalse(viewModel.state.value.isRefreshing)
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Load Intent - error handling
    // ═══════════════════════════════════════════════════════

    @Test
    fun `load asteroids - error message is set on failure`() = runTest {
        // Given
        fakeAsteroidsRepo.shouldFail = true
        fakeAsteroidsRepo.errorMessage = "Network error"

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `load asteroids - error contains correct message`() = runTest {
        // Given
        fakeAsteroidsRepo.shouldFail = true
        val expectedMessage = "Network error"
        fakeAsteroidsRepo.errorMessage = expectedMessage

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertTrue(viewModel.state.value.error?.contains(expectedMessage) == true)
    }

    @Test
    fun `load asteroids - asteroids list is empty on error`() = runTest {
        // Given
        fakeAsteroidsRepo.shouldFail = true

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertTrue(viewModel.state.value.asteroids.isEmpty())
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Refresh Intent
    // ═══════════════════════════════════════════════════════

    @Test
    fun `refresh asteroids - isRefreshing is true during refresh`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Refresh)

        // Then - 1 асерт
        assertTrue(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `refresh asteroids - forceRefresh is true`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertTrue(fakeAsteroidsRepo.lastForceRefreshValue)
    }

    @Test
    fun `refresh asteroids - repository is called once`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertEquals(1, fakeAsteroidsRepo.getAsteroidsCallCount)
    }

    @Test
    fun `refresh asteroids - isRefreshing is false after completion`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)

        // When
        viewModel.processIntent(AsteroidsIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertFalse(viewModel.state.value.isRefreshing)
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: ToggleFavorite - add to favorites
    // ═══════════════════════════════════════════════════════

    @Test
    fun `toggle favorite - asteroid is added to favoriteIds`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite(testAsteroid.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertTrue(viewModel.state.value.favoriteIds.contains(testAsteroid.id))
    }

    @Test
    fun `toggle favorite - repository isFavorite returns true`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite(testAsteroid.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertTrue(fakeFavoritesRepo.isFavorite(testAsteroid.id, "asteroid"))
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: ToggleFavorite - remove from favorites
    // ═══════════════════════════════════════════════════════

    @Test
    fun `toggle favorite - asteroid is removed from favoriteIds`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        fakeFavoritesRepo.addTestFavorite(testAsteroid.id, "asteroid", testAsteroid.name)

        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite(testAsteroid.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertFalse(viewModel.state.value.favoriteIds.contains(testAsteroid.id))
    }

    @Test
    fun `toggle favorite - repository isFavorite returns false after removal`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        fakeFavoritesRepo.addTestFavorite(testAsteroid.id, "asteroid", testAsteroid.name)

        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite(testAsteroid.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertFalse(fakeFavoritesRepo.isFavorite(testAsteroid.id, "asteroid"))
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: ToggleFavorite - edge cases
    // ═══════════════════════════════════════════════════════

    @Test
    fun `toggle favorite - unknown asteroid does not change favorites count`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = listOf(testAsteroid)
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        val beforeSize = fakeFavoritesRepo.favorites.value.size

        // When
        viewModel.processIntent(AsteroidsIntent.ToggleFavorite("unknown-id"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertEquals(beforeSize, fakeFavoritesRepo.favorites.value.size)
    }

    // ═══════════════════════════════════════════════════════
    // ТЕСТЫ: Edge Cases - empty list
    // ═══════════════════════════════════════════════════════

    @Test
    fun `load with empty list - asteroids list is empty`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = emptyList()

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertTrue(viewModel.state.value.asteroids.isEmpty())
    }

    @Test
    fun `load with empty list - isLoading is false`() = runTest {
        // Given
        fakeAsteroidsRepo.asteroidsToReturn = emptyList()

        // When
        viewModel.processIntent(AsteroidsIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 1 асерт
        assertFalse(viewModel.state.value.isLoading)
    }
}