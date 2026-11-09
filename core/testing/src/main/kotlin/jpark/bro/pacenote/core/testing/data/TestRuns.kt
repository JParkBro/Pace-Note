package jpark.bro.pacenote.core.testing.data

import jpark.bro.pacenote.core.model.ActivityType
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunSource
import jpark.bro.pacenote.core.model.RunStatus
import java.time.LocalDateTime
import java.time.ZoneId

private val SEOUL = ZoneId.of("Asia/Seoul")

// 샘플 시나리오(product-spec.md 9장)의 10.28 (수) 오전 6:40 기록
private val SAMPLE_STARTED_AT =
    LocalDateTime
        .parse("2026-10-28T06:40")
        .atZone(SEOUL)
        .toInstant()
        .toEpochMilli()
private const val SAMPLE_DISTANCE_M = 5_210.0
private const val SAMPLE_MOVING_MS = 1_812_000L
private const val SAMPLE_CALORIES = 351

/** 끝난 게스트 기록. 나머지 값(상태 · 제목 · 스플릿 · 삭제 등)은 `copy`로 바꿔 쓴다. */
fun testRun(
    id: String = "run-1028",
    ownerUserId: String? = null,
    startedAt: Long = SAMPLE_STARTED_AT,
    distanceM: Double = SAMPLE_DISTANCE_M,
    movingMs: Long = SAMPLE_MOVING_MS,
): Run =
    Run(
        id = id,
        ownerUserId = ownerUserId,
        status = RunStatus.FINISHED,
        startedAt = startedAt,
        endedAt = startedAt + movingMs,
        zoneId = SEOUL.id,
        distanceM = distanceM,
        movingMs = movingMs,
        elapsedMs = movingMs,
        calories = SAMPLE_CALORIES,
        title = "수요일 아침 러닝",
        memo = "한강 바람 시원했음",
        source = RunSource.GPS,
        activityType = ActivityType.OUTDOOR_RUN,
        planId = null,
        routePolyline = null,
        routeSegments = emptyList(),
        splits = emptyList(),
        schemaVersion = 1,
        createdAt = startedAt + movingMs,
        updatedAt = startedAt + movingMs,
        deletedAt = null,
    )
