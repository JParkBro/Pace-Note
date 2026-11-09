package jpark.bro.pacenote.core.database

import jpark.bro.pacenote.core.database.entity.GoalHistoryEntity
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.RunSplitEntity
import jpark.bro.pacenote.core.database.entity.SyncState

internal const val ME = "user-1"

internal fun testRun(
    id: String,
    owner: String? = ME,
    status: String = "FINISHED",
    startedAt: Long = 1_000,
    distanceM: Double = 1_000.0,
) = RunEntity(
    id = id,
    ownerUserId = owner,
    status = status,
    startedAt = startedAt,
    endedAt = startedAt + 300_000,
    zoneId = "Asia/Seoul",
    distanceM = distanceM,
    movingMs = 300_000,
    elapsedMs = 300_000,
    calories = null,
    title = null,
    memo = null,
    source = "GPS",
    activityType = "OUTDOOR_RUN",
    planId = null,
    routePolyline = null,
    routeSegments = null,
    pauseCount = 0,
    schemaVersion = 1,
    createdAt = startedAt,
    updatedAt = startedAt,
    deletedAt = null,
    syncState = SyncState.LOCAL.name,
)

internal fun testSplit(
    runId: String,
    idx: Int,
) = RunSplitEntity(runId, idx, 1_000.0, 300_000)

internal fun testGoal(
    id: String,
    owner: String? = ME,
    deletedAt: Long? = null,
) = GoalHistoryEntity(id, owner, "DISTANCE", 15_000.0, "2026-10-26", 0, 0, deletedAt, SyncState.LOCAL.name)
