package jpark.bro.pacenote.feature.home.impl

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.RoutePoint
import java.time.LocalDate

internal data class HomeState(
    val loading: Boolean = true,
    /** 이번 주 월요일 */
    val weekStart: LocalDate = LocalDate.MIN,
    val distanceM: Double = 0.0,
    val movingMs: Long = 0L,
    val runCount: Int = 0,
    val averagePaceSecPerKm: Double? = null,
    /** 이번 주 목표. 없으면 이번 주 거리만 보여준다 */
    val goal: HomeGoal? = null,
    /** 최근 기록 최대 2개 */
    val recentRuns: List<HomeRun> = emptyList(),
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
)

/**
 * @property value 목표값 (거리 목표는 m, 횟수 목표는 회)
 * @property ratio 1.0 = 100%. 100%를 넘어도 그대로
 */
internal data class HomeGoal(
    val type: GoalType,
    val value: Double,
    val ratio: Double,
) {
    val achieved: Boolean get() = ratio >= 1.0
}

internal data class HomeRun(
    val id: String,
    val startedAt: Long,
    val zoneId: String,
    val distanceM: Double,
    val movingMs: Long,
    val averagePaceSecPerKm: Double?,
    val route: List<RoutePoint>,
)
