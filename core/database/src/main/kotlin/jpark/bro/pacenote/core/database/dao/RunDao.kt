package jpark.bro.pacenote.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.RunSplitEntity
import kotlinx.coroutines.flow.Flow

data class RunWithSplits(
    @Embedded val run: RunEntity,
    @Relation(parentColumn = "id", entityColumn = "run_id")
    val splits: List<RunSplitEntity>,
)

data class RunTotalsRow(
    val distanceM: Double,
    val runCount: Int,
)

/** 월별 합계용 가벼운 행. 월 판단은 기록의 시간대로 해야 해서 SQL이 아니라 코드에서 묶는다. */
data class RunMonthRow(
    val startedAt: Long,
    val zoneId: String,
    val distanceM: Double,
)

/**
 * 끝난 기록의 조회 · 편집. 러닝 중 쓰기는 추적 쪽 DAO가 담당한다.
 * 소유자 비교는 게스트(null)도 찾도록 `=`가 아니라 `IS`를 쓴다.
 */
@Dao
interface RunDao {
    @Upsert
    suspend fun upsertRun(run: RunEntity)

    @Upsert
    suspend fun upsertSplits(splits: List<RunSplitEntity>)

    @Transaction
    @Query("SELECT * FROM run WHERE id = :id AND deleted_at IS NULL")
    fun observeRun(id: String): Flow<RunWithSplits?>

    @Query(
        """
        SELECT * FROM run
        WHERE owner_user_id IS :owner AND status = 'FINISHED' AND deleted_at IS NULL
        ORDER BY started_at DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(
        owner: String?,
        limit: Int,
    ): Flow<List<RunEntity>>

    /** 기록 목록(R1). 최신순으로 페이지 단위로 읽는다. */
    @Query(
        """
        SELECT * FROM run
        WHERE owner_user_id IS :owner AND status = 'FINISHED' AND deleted_at IS NULL
        ORDER BY started_at DESC
        """,
    )
    fun pagingSource(owner: String?): PagingSource<Int, RunEntity>

    @Query(
        """
        SELECT started_at AS startedAt, zone_id AS zoneId, distance_m AS distanceM FROM run
        WHERE owner_user_id IS :owner AND status = 'FINISHED' AND deleted_at IS NULL
        """,
    )
    fun observeMonthRows(owner: String?): Flow<List<RunMonthRow>>

    /** [fromMs] 이상 [untilMs] 미만에 시작한 기록 */
    @Query(
        """
        SELECT * FROM run
        WHERE owner_user_id IS :owner AND status = 'FINISHED' AND deleted_at IS NULL
          AND started_at >= :fromMs AND started_at < :untilMs
        ORDER BY started_at DESC
        """,
    )
    fun observeInRange(
        owner: String?,
        fromMs: Long,
        untilMs: Long,
    ): Flow<List<RunEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(distance_m), 0) AS distanceM, COUNT(*) AS runCount FROM run
        WHERE owner_user_id IS :owner AND status = 'FINISHED' AND deleted_at IS NULL
        """,
    )
    fun observeTotals(owner: String?): Flow<RunTotalsRow>

    @Query(
        """
        UPDATE run SET title = :title, memo = :memo, updated_at = :updatedAt, sync_state = :syncState
        WHERE id = :id
        """,
    )
    suspend fun updateTitleAndMemo(
        id: String,
        title: String?,
        memo: String?,
        updatedAt: Long,
        syncState: String,
    )

    @Query("UPDATE run SET deleted_at = :deletedAt, updated_at = :deletedAt, sync_state = :syncState WHERE id = :id")
    suspend fun softDelete(
        id: String,
        deletedAt: Long,
        syncState: String,
    )
}
