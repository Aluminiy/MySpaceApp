package ru.omc.myspaceapp.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.omc.myspaceapp.data.model.AstronautResponseDto
import ru.omc.myspaceapp.data.model.NearEarthObjectsResponse
import ru.omc.myspaceapp.utils.Constants


expect fun createHttpClient(): HttpClient

fun HttpClientConfig<*>.commonConfig() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
//    install(Logging) {
//        logger = Logger.DEFAULT
//        level = LogLevel.BODY
//    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
}

open class SpaceApi(private val client: HttpClient = createHttpClient()) {

    open suspend fun getAstronauts(): AstronautResponseDto {
        return client.get("${Constants.BASE_URL_OPEN_NOTIFY}${Constants.ENDPOINT_ASTRO}")
            .body<AstronautResponseDto>()
    }

    open suspend fun getNearEarthObjects(startDate: String, endDate: String): NearEarthObjectsResponse {
        return client.get("${Constants.BASE_URL_NEO}/feed") {
            parameter("api_key", Constants.NASA_API_KEY)
            parameter("start_date", startDate)
            parameter("end_date", endDate)
        }.body<NearEarthObjectsResponse>()
    }
}