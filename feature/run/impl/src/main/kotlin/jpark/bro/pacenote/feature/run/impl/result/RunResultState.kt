package jpark.bro.pacenote.feature.run.impl.result

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.RoutePoint

internal data class RunResultState(
    val loading: Boolean = true,
    val startedAt: Long = 0L,
    val zoneId: String = "UTC",
    val distanceM: Double = 0.0,
    val movingMs: Long = 0L,
    val averagePaceSecPerKm: Double? = null,
    val calories: Int? = null,
    val route: List<RoutePoint> = emptyList(),
    val splits: List<ResultSplit> = emptyList(),
    /** 이 기록이 속한 주의 목표. 목표가 없으면 null */
    val weeklyGoal: ResultGoal? = null,
    val memo: String = "",
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
) {
    companion object {
        const val MEMO_MAX_LENGTH = 500
    }
}

/**
 * 구간 표 한 줄. 구간은 항상 km 단위다 (tracking.md 5장).
 *
 * @property endDistanceM 이 구간이 끝난 누적 거리. 마지막 짧은 구간은 1km 배수가 아니다
 */
internal data class ResultSplit(
    val index: Int,
    val endDistanceM: Double,
    val paceSecPerKm: Double?,
)

/**
 * @property ratio 1.0 = 100%. 100%를 넘어도 그대로
 * @property remaining 남은 양 (거리 목표는 m, 횟수 목표는 회). 달성했으면 0
 */
internal data class ResultGoal(
    val type: GoalType,
    val ratio: Double,
    val remaining: Double,
)
