package ru.omc.myspaceapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AstronautResponseDto(
    val message: String,
    val number: Int,
    val people: List<AstronautDto>
)

@Serializable
data class AstronautDto(
    val name: String,
    val craft: String
)