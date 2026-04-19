package ru.omc.myspaceapp.data.repository

import kotlinx.datetime.*
import ru.omc.myspaceapp.Astronauts
import ru.omc.myspaceapp.data.api.SpaceApi
import ru.omc.myspaceapp.data.model.AstronautDto
import ru.omc.myspaceapp.db.AppDatabase
import kotlin.time.Instant
import kotlin.time.Clock as StdClock
import kotlinx.datetime.Instant as KtxInstant

class AstronautsRepository(
    private val spaceApi: SpaceApi,
    private val database: AppDatabase
) {
    private val queries = database.appDatabaseQueries

    /**
     * Получает астронавтов: сначала из кэша, при refresh — из API
     * @param forceRefresh если true — всегда загружать из API
     * @param cacheHours сколько часов хранить кэш (по умолчанию 1 для астронавтов)
     */
    suspend fun getAstronauts(
        forceRefresh: Boolean = false,
        cacheHours: Int = 1  // Астронавты меняются часто, кэш на 1 час
    ): Result<List<AstronautDto>> {
        return try {
            // ✅ Если не форсим — проверяем кэш
            if (!forceRefresh) {
                val cached = queries.getCachedAstronauts().executeAsList()
                if (cached.isNotEmpty()) {
                    // Проверяем свежесть кэша
                    if (isCacheFresh(cached.first().cached_at, cacheHours)) {
                        println("📦 [Cache] Returning ${cached.size} astronauts from cache")
                        return Result.success(cached.map { it.toAstronautDto() })
                    } else {
                        println("🕐 [Cache] Cache expired (${cacheHours}h), refreshing...")
                    }
                } else {
                    println("📭 [Cache] No cached data, fetching from API...")
                }
            }

            // ✅ Загружаем из API
            val apiResult = fetchFromApi()
            if (apiResult.isSuccess) {
                val astronauts = apiResult.getOrNull()!!
                // ✅ Сохраняем в кэш
                saveToCache(astronauts)
                println("💾 [Cache] Saved ${astronauts.size} astronauts to cache")
            }
            apiResult

        } catch (e: Exception) {
            // ✅ Если ошибка сети — возвращаем кэш (даже старый)
            println("❌ [API] Error: ${e.message}, trying cache...")
            val cached = queries.getCachedAstronauts().executeAsList()
            if (cached.isNotEmpty()) {
                println("📦 [Cache] Returning ${cached.size} astronauts from cache (offline)")
                Result.success(cached.map { it.toAstronautDto() })
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Принудительное обновление из API
     */
    suspend fun refreshAstronauts(): Result<List<AstronautDto>> {
        return getAstronauts(forceRefresh = true)
    }

    // === Приватные методы ===

    private suspend fun fetchFromApi(): Result<List<AstronautDto>> {
        val response = spaceApi.getAstronauts()
        return Result.success(response.people)
    }

    // ✅ Метод расширения для конвертации SQLDelight → AstronautDto
    private fun Astronauts.toAstronautDto(): AstronautDto {
        return AstronautDto(
            name = this.name,
            craft = this.spacecraft
        )
    }

    private fun saveToCache(astronauts: List<AstronautDto>) {
        val now = StdClock.System.now()
        val ktxInstant = KtxInstant.fromEpochMilliseconds(now.toEpochMilliseconds())
        val currentTime = ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

        queries.transaction {
            queries.deleteCachedAstronauts()
            astronauts.forEach { astronaut ->
                queries.insertCachedAstronaut(
                    name = astronaut.name,
                    spacecraft = astronaut.craft,
                    cached_at = currentTime
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