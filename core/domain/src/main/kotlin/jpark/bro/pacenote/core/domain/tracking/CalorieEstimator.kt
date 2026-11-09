package jpark.bro.pacenote.core.domain.tracking

import kotlin.math.roundToInt

object CalorieEstimator {
    private const val KCAL_PER_KG_KM = 1.036
    private const val METERS_PER_KM = 1_000.0

    /** 체중이 없으면 null */
    fun estimate(
        weightKg: Double?,
        distanceM: Double,
    ): Int? = weightKg?.let { (it * (distanceM / METERS_PER_KM) * KCAL_PER_KG_KM).roundToInt() }
}
