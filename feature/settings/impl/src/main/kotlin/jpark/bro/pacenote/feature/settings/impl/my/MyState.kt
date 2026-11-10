package jpark.bro.pacenote.feature.settings.impl.my

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.Goal

/** @property goal 이번 주 목표. 없으면 "정하지 않음" */
internal data class MyState(
    val loading: Boolean = true,
    val totalDistanceM: Double = 0.0,
    val totalRuns: Int = 0,
    val goal: Goal? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
)
