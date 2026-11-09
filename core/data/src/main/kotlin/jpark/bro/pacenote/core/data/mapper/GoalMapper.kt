package jpark.bro.pacenote.core.data.mapper

import jpark.bro.pacenote.core.database.entity.GoalHistoryEntity
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import java.time.LocalDate

internal fun GoalHistoryEntity.toModel(): Goal =
    Goal(
        id = id,
        ownerUserId = ownerUserId,
        type = GoalType.valueOf(type),
        value = value,
        effectiveWeekStart = LocalDate.parse(effectiveWeekStart),
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
