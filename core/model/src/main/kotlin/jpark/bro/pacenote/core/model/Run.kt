package jpark.bro.pacenote.core.model

/** 러닝 기록 하나. 거리는 미터, 시간은 밀리초로 저장한다. */
data class Run(
    val id: String,
    val ownerUserId: String?,
    val status: RunStatus,
    val startedAt: Long,
    val endedAt: Long?,
    val zoneId: String,
    val distanceM: Double,
    val movingMs: Long,
    val elapsedMs: Long,
    val calories: Int?,
    val title: String?,
    val memo: String?,
    val source: RunSource,
    val activityType: ActivityType,
    val planId: String?,
    val routePolyline: String?,
    val routeSegments: List<Int>,
    val splits: List<RunSplit>,
    val schemaVersion: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
