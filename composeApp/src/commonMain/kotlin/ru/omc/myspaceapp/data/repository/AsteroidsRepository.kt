package ru.omc.myspaceapp.data.repository

import kotlinx.datetime.*
import ru.omc.myspaceapp.Asteroids
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AsteroidDto
import ru.omc.myspaceapp.data.model.DiameterRange
import ru.omc.myspaceapp.data.model.EstimatedDiameter
import ru.omc.myspaceapp.db.AppDatabase
import kotlin.time.Instant
import kotlin.time.Clock as StdClock
import kotlinx.datetime.Instant as KtxInstant

class AsteroidsRepository(
    private val spaceApi: SpaceApi,
    private val database: AppDatabase
) {
    private val queries = database.appDatabaseQueries

    suspend fun getAsteroids(
        forceRefresh: Boolean = false,
        cacheHours: Int = 24
    ): Result<List<AsteroidDto>> {
        return try {
            // ✅ Если не форсим — проверяем кэш
            if (!forceRefresh) {
                // ✅ Получаем сырые данные из кэша (SQLDelight класс)
                val cached = queries.getCachedAsteroids().executeAsList()
                if (cached.isNotEmpty()) {
                    // ✅ Проверяем свежесть по cached_at (есть в Asteroids, но не в AsteroidDto)
                    if (isCacheFresh(cached.first().cached_at, cacheHours)) {
                        println("📦 [Cache] Returning ${cached.size} asteroids from cache")
                        return Result.success(cached.map { it.toAsteroidDto() })
                    } else {
                        println("🕐 [Cache] Cache expired (${cacheHours}h), refreshing...")
                    }
                }
            }

            // ✅ Загружаем из API
            val apiResult = fetchFromApi()
            if (apiResult.isSuccess) {
                val asteroids = apiResult.getOrNull()!!
                saveToCache(asteroids)
                println("💾 [Cache] Saved ${asteroids.size} asteroids to cache")
            }
            apiResult

        } catch (e: Exception) {
            println("❌ [API] Error: ${e.message}, trying cache...")
            val cached = queries.getCachedAsteroids().executeAsList()
            if (cached.isNotEmpty()) {
                println("📦 [Cache] Returning ${cached.size} asteroids from cache (offline)")
                Result.success(cached.map { it.toAsteroidDto() })
            } else {
                Result.failure(e)
            }
        }
    }

    // === Приватные методы ===

    private suspend fun fetchFromApi(): Result<List<AsteroidDto>> {
        val now = StdClock.System.now()
        val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
        val today: LocalDate = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startDate = today.minus(7, DateTimeUnit.DAY)

        val response = spaceApi.getNearEarthObjects(
            startDate = startDate.toString(),
            endDate = today.toString()
        )

        val asteroids = response.nearEarthObjects.values.flatten()
        return Result.success(asteroids)
    }

    // ✅ Метод расширения для конвертации SQLDelight → AsteroidDto
    private fun Asteroids.toAsteroidDto(): AsteroidDto {
        return AsteroidDto(
            id = this.id,
            name = this.name,
            isPotentiallyHazardous = this.is_potentially_hazardous == 1L,
            closeApproachData = listOf(),
            estimatedDiameter = EstimatedDiameter(
                kilometers = DiameterRange(
                    min = this.diameter_min,
                    max = this.diameter_max
                )
            )
        )
    }

    private fun saveToCache(asteroids: List<AsteroidDto>) {
        val now = StdClock.System.now()
        val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
        val currentTime = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

        queries.transaction {
            queries.deleteCachedAsteroids()
            asteroids.forEach { asteroid ->
                queries.insertCachedAsteroid(
                    id = asteroid.id,
                    name = asteroid.name,
                    is_potentially_hazardous = if (asteroid.isPotentiallyHazardous) 1L else 0L,
                    diameter_min = asteroid.estimatedDiameter.kilometers.min,
                    diameter_max = asteroid.estimatedDiameter.kilometers.max,
                    close_approach_date = asteroid.closeApproachData.firstOrNull()?.closeApproachDate,
                    cached_at = currentTime  // ✅ snake_case как в SQL
                )
            }
        }
    }

    private fun isCacheFresh(cachedAt: String, cacheHours: Int): Boolean {
        return try {
            val cacheTime = Instant.parse(cachedAt)

            val now = StdClock.System.now()
            val currentInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())

            val millisDiff = currentInstant.toEpochMilliseconds() - cacheTime.toEpochMilliseconds()
            val hoursDiff = millisDiff / (1000 * 60 * 60)

            println("🕐 [Cache] Hours since cache: $hoursDiff (max: $cacheHours)")

            hoursDiff < cacheHours
        } catch (e: Exception) {
            println("❌ [Cache] Error checking freshness: ${e.message}")
            false
        }
    }
}