package jpark.bro.pacenote.feature.onboarding.impl.goal

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType

/** @property amount 거리 목표는 [distanceUnit] 단위(km · mi), 횟수 목표는 회 */
internal data class FirstGoalState(
    val loading: Boolean = true,
    val type: GoalType = GoalType.DISTANCE,
    val amount: Int = 0,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val working: Boolean = false,
) {
    val canDecrease: Boolean get() = amount > MIN_AMOUNT
    val canIncrease: Boolean get() = amount < maxAmount(type)

    companion object {
        const val MIN_AMOUNT = 1
        private const val MAX_DISTANCE = 200
        private const val MAX_COUNT = 14

        fun maxAmount(type: GoalType): Int =
            when (type) {
                GoalType.DISTANCE -> MAX_DISTANCE
                GoalType.COUNT -> MAX_COUNT
            }
    }
}
