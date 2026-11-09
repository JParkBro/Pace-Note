package jpark.bro.pacenote.core.model

import java.time.LocalDate

/** [effectiveWeekStart]가 속한 주부터 적용되는 주간 목표. [value]는 거리 목표면 미터, 횟수 목표면 회. */
data class Goal(
    val id: String,
    val ownerUserId: String?,
    val type: GoalType,
    val value: Double,
    val effectiveWeekStart: LocalDate,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
