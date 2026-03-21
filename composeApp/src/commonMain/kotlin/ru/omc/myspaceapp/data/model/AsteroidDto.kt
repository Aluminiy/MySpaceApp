package ru.omc.myspaceapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearEarthObjectsResponse(
    @SerialName("near_earth_objects")
    val nearEarthObjects: Map<String, List<AsteroidDto>>
)

@Serializable
data class AsteroidDto(
    val id: String,
    val name: String,
    @SerialName("is_potentially_hazardous_asteroid")
    val isPotentiallyHazardous: Boolean,
    @SerialName("close_approach_data")
    val closeApproachData: List<CloseApproachData>,
    @SerialName("estimated_diameter")
    val estimatedDiameter: EstimatedDiameter
)

@Serializable
data class CloseApproachData(
    @SerialName("close_approach_date")
    val closeApproachDate: String,
    @SerialName("relative_velocity")
    val relativeVelocity: RelativeVelocity,
    @SerialName("miss_distance")
    val missDistance: MissDistance
)

@Serializable
data class RelativeVelocity(
    @SerialName("kilometers_per_hour")
    val kilometersPerHour: String
)

@Serializable
data class MissDistance(
    val kilometers: String
)

@Serializable
data class EstimatedDiameter(
    val kilometers: DiameterRange
)

@Serializable
data class DiameterRange(
    @SerialName("estimated_diameter_min")
    val min: Double,
    @SerialName("estimated_diameter_max")
    val max: Double
)