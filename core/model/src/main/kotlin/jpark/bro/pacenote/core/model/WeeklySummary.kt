package jpark.bro.pacenote.core.model

import java.time.LocalDate

/** 월요일 [weekStart]로 시작하는 한 주의 합계. */
data class WeeklySummary(
    val weekStart: LocalDate,
    val distanceM: Double,
    val movingMs: Long,
    val runCount: Int,
)
