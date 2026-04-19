package ru.omc.myspaceapp.data.repository

import ru.omc.myspaceapp.data.model.AsteroidDto

class FakeAsteroidsRepository : AsteroidsRepository {
    var asteroidsToReturn: List<AsteroidDto> = emptyList()
    var shouldFail: Boolean = false
    var errorMessage: String = "Test error"

    var getAsteroidsCallCount = 0
    var lastForceRefreshValue: Boolean = false

    override suspend fun getAsteroids(forceRefresh: Boolean): Result<List<AsteroidDto>> {
        getAsteroidsCallCount++
        lastForceRefreshValue = forceRefresh

        return if (shouldFail) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(asteroidsToReturn)
        }
    }

    override suspend fun refreshAsteroids(): Result<List<AsteroidDto>> {
        return getAsteroids(forceRefresh = true)
    }
}