package jpark.bro.pacenote.feature.settings.impl.goal

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType

/**
 * @property amount 화면에 보이는 값. 거리 목표는 [distanceUnit] 단위(km · mi), 횟수 목표는 회
 * @property recentDistanceM 최근 4주 주 평균 거리(m). 기록이 4주 이상 쌓이지 않았으면 null
 * @property recentCount 최근 4주 주 평균 횟수
 */
internal data class GoalState(
    val loading: Boolean = true,
    val type: GoalType = GoalType.DISTANCE,
    val amount: Int = 0,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val recentDistanceM: Double? = null,
    val recentCount: Double? = null,
    val saving: Boolean = false,
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
