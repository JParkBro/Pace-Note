package jpark.bro.pacenote.core.tracking

import jpark.bro.pacenote.core.common.IdGenerator
import jpark.bro.pacenote.core.data.repository.AuthRepository
import jpark.bro.pacenote.core.database.dao.FinishedRunUpdate
import jpark.bro.pacenote.core.database.dao.RunProgressUpdate
import jpark.bro.pacenote.core.database.dao.RunRecoveryDao
import jpark.bro.pacenote.core.database.dao.RunTrackingDao
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.RunPointEntity
import jpark.bro.pacenote.core.database.entity.RunSplitEntity
import jpark.bro.pacenote.core.database.entity.SyncState
import jpark.bro.pacenote.core.domain.route.PolylineCodec
import jpark.bro.pacenote.core.domain.route.PolylineSimplifier
import jpark.bro.pacenote.core.domain.tracking.CalorieEstimator
import jpark.bro.pacenote.core.domain.tracking.PointOutcome
import jpark.bro.pacenote.core.domain.tracking.RunMetricsAccumulator
import jpark.bro.pacenote.core.domain.tracking.RunProgress
import jpark.bro.pacenote.core.domain.tracking.RunTotals
import jpark.bro.pacenote.core.model.ActivityType
import jpark.bro.pacenote.core.model.RawLocation
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.model.RunSource
import jpark.bro.pacenote.core.model.RunSplit
import jpark.bro.pacenote.core.model.RunStatus
import kotlinx.coroutines.flow.first
import java.time.Clock

/** 이어서 달릴 기록의 저장된 진행 값과 경로 */
internal data class ResumedRun(
    val runId: String,
    val progress: RunProgress,
    val route: List<RoutePoint>,
)

/**
 * 기록 하나의 Room 쓰기 (tracking.md 6장). 점을 버퍼에 모았다가 5초마다 또는 점 10개마다 한 트랜잭션으로 쓴다.
 * 쓰기에 실패하면 버퍼를 비우지 않아 다음 저장 때 다시 쓴다.
 */
internal class RunRecorder(
    private val trackingDao: RunTrackingDao,
    private val recoveryDao: RunRecoveryDao,
    private val authRepository: AuthRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    private var runId = ""
    private val buffer = mutableListOf<RunPointEntity>()
    private var nextSeq = 0
    private var savedSplitCount = 0
    private var lastFlushElapsedMs = 0L

    /** 지금 사용자(게스트면 null)의 새 기록을 만든다. */
    suspend fun create(nowElapsedMs: Long): String {
        val ownerUserId = authRepository.observeUserId().first()
        val now = clock.millis()
        val id = idGenerator.newId()
        trackingDao.insertRun(newRunEntity(id, ownerUserId, now, clock.zone.id))
        attach(id, nextSeq = 0, savedSplitCount = 0, nowElapsedMs)
        return id
    }

    /** 진행 중 기록을 다시 열고 상태를 ACTIVE로 되돌린다. 없거나 끝난 기록이면 실패 */
    suspend fun resume(
        runId: String,
        nowElapsedMs: Long,
    ): ResumedRun {
        val resumed = recoveryDao.loadInProgress(runId)
        val summary = recoveryDao.pointSummary(runId)
        attach(runId, (summary.maxSeq ?: -1) + 1, resumed.progress.completedSplits.size, nowElapsedMs)
        trackingDao.updateStatus(runId, RunStatus.ACTIVE.name, pauseIncrement = 0, updatedAt = clock.millis())
        return resumed
    }

    fun add(
        point: RawLocation,
        outcome: PointOutcome,
    ) {
        buffer += point.toEntity(runId, nextSeq++, outcome)
    }

    fun isFlushDue(nowElapsedMs: Long): Boolean =
        buffer.size >= FLUSH_POINT_COUNT || nowElapsedMs - lastFlushElapsedMs >= FLUSH_INTERVAL_MS

    suspend fun flush(
        distanceM: Double,
        movingMs: Long,
        completedSplits: List<RunSplit>,
        nowElapsedMs: Long,
    ) {
        val newSplits = completedSplits.drop(savedSplitCount)
        trackingDao.flush(
            points = buffer.toList(),
            splits = newSplits.map { it.toEntity(runId) },
            progress = RunProgressUpdate(runId, distanceM, movingMs, clock.millis()),
        )
        buffer.clear()
        savedSplitCount += newSplits.size
        lastFlushElapsedMs = nowElapsedMs
    }

    suspend fun setStatus(
        status: RunStatus,
        pauseIncrement: Int,
    ) {
        trackingDao.updateStatus(runId, status.name, pauseIncrement, clock.millis())
    }

    /** [endedAt]이 null이면 지금 시각으로 끝낸다. */
    suspend fun finish(
        totals: RunTotals,
        weightKg: Double?,
        endedAt: Long? = null,
    ) {
        val run = requireNotNull(recoveryDao.findRun(runId)) { "기록 없음" }
        val end = endedAt ?: clock.millis()
        val route =
            PolylineSimplifier.simplify(
                recoveryDao.acceptedPoints(runId).map {
                    RoutePoint(it.lat, it.lng, it.segment)
                },
            )
        trackingDao.finish(
            splits = totals.splits.map { it.toEntity(runId) },
            values =
                FinishedRunUpdate(
                    id = runId,
                    status = RunStatus.FINISHED.name,
                    endedAt = end,
                    distanceM = totals.distanceM,
                    movingMs = totals.movingMs,
                    elapsedMs = (end - run.startedAt).coerceAtLeast(totals.movingMs),
                    calories = CalorieEstimator.estimate(weightKg, totals.distanceM),
                    routePolyline = route.points.takeIf { it.isNotEmpty() }?.let(PolylineCodec::encode),
                    routeSegments = route.segmentCounts.takeIf { it.isNotEmpty() }?.joinToString(",", "[", "]"),
                    updatedAt = clock.millis(),
                    syncState = if (run.ownerUserId == null) SyncState.LOCAL.name else SyncState.PENDING.name,
                ),
        )
    }

    suspend fun findInProgress(): RecoverableRun? =
        recoveryDao.findInProgress()?.let { RecoverableRun(it.id, it.startedAt, it.distanceM, it.movingMs) }

    /** P8: 마지막 저장된 점까지로 끝내거나 버린다. */
    suspend fun finishRecovered(
        runId: String,
        save: Boolean,
        weightKg: Double?,
    ) {
        val resumed = recoveryDao.loadInProgress(runId)
        this.runId = runId
        if (!save) return setStatus(RunStatus.DISCARDED, pauseIncrement = 0)
        val totals = RunMetricsAccumulator(0L, autoPauseEnabled = false, restored = resumed.progress).finish(0L)
        val run = requireNotNull(recoveryDao.findRun(runId))
        finish(totals, weightKg, endedAt = recoveryDao.pointSummary(runId).lastRecordedAt ?: run.updatedAt)
    }

    private fun attach(
        runId: String,
        nextSeq: Int,
        savedSplitCount: Int,
        nowElapsedMs: Long,
    ) {
        this.runId = runId
        this.nextSeq = nextSeq
        this.savedSplitCount = savedSplitCount
        lastFlushElapsedMs = nowElapsedMs
        buffer.clear()
    }

    private companion object {
        const val FLUSH_POINT_COUNT = 10
        const val FLUSH_INTERVAL_MS = 5_000L
    }
}

private const val SCHEMA_VERSION = 1

private suspend fun RunRecoveryDao.loadInProgress(runId: String): ResumedRun {
    val run = requireNotNull(findRun(runId)) { "기록 없음" }
    check(run.status == RunStatus.ACTIVE.name || run.status == RunStatus.PAUSED.name) { "진행 중 기록이 아님" }
    val summary = pointSummary(runId)
    val progress =
        RunProgress(
            distanceM = run.distanceM,
            movingMs = run.movingMs,
            nextSegment = (summary.maxSegment ?: -1) + 1,
            completedSplits = splits(runId).map { RunSplit(it.idx, it.distanceM, it.movingMs) },
        )
    val route = acceptedPoints(runId).map { RoutePoint(it.lat, it.lng, it.segment) }
    return ResumedRun(runId, progress, route)
}

private fun newRunEntity(
    id: String,
    ownerUserId: String?,
    now: Long,
    zoneId: String,
) = RunEntity(
    id = id,
    ownerUserId = ownerUserId,
    status = RunStatus.ACTIVE.name,
    startedAt = now,
    endedAt = null,
    zoneId = zoneId,
    distanceM = 0.0,
    movingMs = 0L,
    elapsedMs = 0L,
    calories = null,
    title = null,
    memo = null,
    source = RunSource.GPS.name,
    activityType = ActivityType.OUTDOOR_RUN.name,
    planId = null,
    routePolyline = null,
    routeSegments = null,
    pauseCount = 0,
    schemaVersion = SCHEMA_VERSION,
    createdAt = now,
    updatedAt = now,
    deletedAt = null,
    // 끝나기 전에는 올리지 않는다. 끝낼 때 소유자에 따라 정한다
    syncState = SyncState.LOCAL.name,
)

private fun RawLocation.toEntity(
    runId: String,
    seq: Int,
    outcome: PointOutcome,
) = RunPointEntity(
    runId = runId,
    seq = seq,
    lat = lat,
    lng = lng,
    altitude = altitude,
    accuracy = accuracyM,
    speed = speedMps,
    recordedAt = recordedAt,
    segment = outcome.segment,
    accepted = outcome.accepted,
)

private fun RunSplit.toEntity(runId: String) = RunSplitEntity(runId, index, distanceM, movingMs)
