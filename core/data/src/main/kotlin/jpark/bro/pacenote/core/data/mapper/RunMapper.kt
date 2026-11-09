package jpark.bro.pacenote.core.data.mapper

import jpark.bro.pacenote.core.database.dao.RunTotalsRow
import jpark.bro.pacenote.core.database.dao.RunWithSplits
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.RunSplitEntity
import jpark.bro.pacenote.core.model.ActivityType
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunSource
import jpark.bro.pacenote.core.model.RunSplit
import jpark.bro.pacenote.core.model.RunStatus
import jpark.bro.pacenote.core.model.RunTotals

internal fun RunWithSplits.toModel(): Run = run.toModel(splits.sortedBy { it.idx }.map { it.toModel() })

/** 목록용: 스플릿을 읽지 않는다. */
internal fun RunEntity.toModel(splits: List<RunSplit> = emptyList()): Run =
    Run(
        id = id,
        ownerUserId = ownerUserId,
        status = RunStatus.valueOf(status),
        startedAt = startedAt,
        endedAt = endedAt,
        zoneId = zoneId,
        distanceM = distanceM,
        movingMs = movingMs,
        elapsedMs = elapsedMs,
        calories = calories,
        title = title,
        memo = memo,
        source = RunSource.valueOf(source),
        activityType = ActivityType.valueOf(activityType),
        planId = planId,
        routePolyline = routePolyline,
        routeSegments = parseSegments(routeSegments),
        splits = splits,
        schemaVersion = schemaVersion,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun RunSplitEntity.toModel(): RunSplit = RunSplit(index = idx, distanceM = distanceM, movingMs = movingMs)

internal fun RunTotalsRow.toModel(): RunTotals = RunTotals(distanceM = distanceM, runCount = runCount)

/** `route_segments`는 JSON 정수 배열 "[312,140]" */
private fun parseSegments(json: String?): List<Int> =
    json
        ?.removePrefix("[")
        ?.removeSuffix("]")
        ?.split(',')
        ?.mapNotNull { it.trim().toIntOrNull() }
        .orEmpty()
