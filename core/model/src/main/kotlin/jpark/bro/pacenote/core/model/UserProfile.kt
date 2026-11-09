package jpark.bro.pacenote.core.model

/** [weightKg]가 null이면 칼로리를 표시하지 않는다. */
data class UserProfile(
    val weightKg: Double?,
    val distanceUnit: DistanceUnit,
)
