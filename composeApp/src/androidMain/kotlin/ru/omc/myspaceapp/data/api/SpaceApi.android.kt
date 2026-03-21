package ru.omc.myspaceapp.data.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*

actual fun createHttpClient(): HttpClient {
    return HttpClient(CIO) {
        commonConfig()
    }
}