package ru.omc.myspaceapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform