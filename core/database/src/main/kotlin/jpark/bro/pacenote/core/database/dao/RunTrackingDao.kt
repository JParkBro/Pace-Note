package jpark.bro.pacenote.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.RunPointEntity
import jpark.bro.pacenote.core.database.entity.RunSplitEntity

/** 저장된 점의 요약. 점이 없으면 모두 null */
data class PointSummary(
    val maxSeq: Int?,
    val maxSegment: Int?,
    val lastRecordedAt: Long?,
)

data class RoutePointRow(
    val lat: Double,
    val lng: Double,
    val segment: Int,
)

/** 기록 중 누적값 (`run` 일부 컬럼) */
data class RunProgressUpdate(
    val id: String,
    @ColumnInfo(name = "distance_m") val distanceM: Double,
    @ColumnInfo(name = "moving_ms") val movingMs: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** 끝난 기록에 쓰는 값 (`run` 일부 컬럼, tracking.md 6-1 `finish(save = true)`) */
data class FinishedRunUpdate(
    val id: String,
    val status: String,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "distance_m") val distanceM: Double,
    @ColumnInfo(name = "moving_ms") val movingMs: Long,
    @ColumnInfo(name = "elapsed_ms") val elapsedMs: Long,
    val calories: Int?,
    @ColumnInfo(name = "route_polyline") val routePolyline: String?,
    @ColumnInfo(name = "route_segments") val routeSegments: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
)

/** 러닝 중 쓰기 · 복구 조회 (tracking.md 6장). `TrackingEngine`이 순서대로만 호출한다. */
@Dao
interface RunTrackingDao {
    @Insert
    suspend fun insertRun(run: RunEntity)

    @Insert
    suspend fun insertPoints(points: List<RunPointEntity>)

    @Upsert
    suspend fun upsertSplits(splits: List<RunSplitEntity>)

    @Update(entity = RunEntity::class)
    suspend fun updateProgress(progress: RunProgressUpdate)

    /** 점 일괄 삽입 + 새로 완성된 구간 + 누적값 갱신을 한 트랜잭션으로 */
    @Transaction
    suspend fun flush(
        points: List<RunPointEntity>,
        splits: List<RunSplitEntity>,
        progress: RunProgressUpdate,
    ) {
        if (points.isNotEmpty()) insertPoints(points)
        if (splits.isNotEmpty()) upsertSplits(splits)
        updateProgress(progress)
    }

    @Query(
        """
        UPDATE run SET status = :status, pause_count = pause_count + :pauseIncrement, updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateStatus(
        id: String,
        status: String,
        pauseIncrement: Int,
        updatedAt: Long,
    )

    @Update(entity = RunEntity::class)
    suspend fun markFinished(values: FinishedRunUpdate)

    @Query("DELETE FROM run_split WHERE run_id = :runId")
    suspend fun deleteSplits(runId: String)

    /** 끝난 기록의 전체 구간(남은 구간 포함)으로 바꾸고 기록을 끝낸다. */
    @Transaction
    suspend fun finish(
        splits: List<RunSplitEntity>,
        values: FinishedRunUpdate,
    ) {
        deleteSplits(values.id)
        if (splits.isNotEmpty()) upsertSplits(splits)
        markFinished(values)
    }
}

/** 진행 중 기록의 복구 · 마무리에 필요한 조회 (tracking.md 6-2) */
@Dao
interface RunRecoveryDao {
    /** 진행 중(ACTIVE · PAUSED) 기록 중 가장 최근 것. 소유자와 상관없이 찾는다 (이 기기에서 시작한 기록). */
    @Query("SELECT * FROM run WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY started_at DESC LIMIT 1")
    suspend fun findInProgress(): RunEntity?

    @Query("SELECT * FROM run WHERE id = :id")
    suspend fun findRun(id: String): RunEntity?

    @Query("SELECT * FROM run_split WHERE run_id = :runId ORDER BY idx")
    suspend fun splits(runId: String): List<RunSplitEntity>

    /** 거리에 들어간(채택된) 점, 순서대로 */
    @Query("SELECT lat, lng, segment FROM run_point WHERE run_id = :runId AND accepted = 1 ORDER BY seq")
    suspend fun acceptedPoints(runId: String): List<RoutePointRow>

    @Query(
        """
        SELECT MAX(seq) AS maxSeq, MAX(segment) AS maxSegment, MAX(recorded_at) AS lastRecordedAt
        FROM run_point WHERE run_id = :runId
        """,
    )
    suspend fun pointSummary(runId: String): PointSummary
}
