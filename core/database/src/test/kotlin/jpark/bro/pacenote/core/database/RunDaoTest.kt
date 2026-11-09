package jpark.bro.pacenote.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.database.dao.RunTotalsRow
import jpark.bro.pacenote.core.database.entity.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RunDaoTest : DatabaseTest() {
    private val dao get() = db.runDao()

    private suspend fun recentIds(owner: String? = ME) = dao.observeRecent(owner, 10).first().map { it.id }

    @Test
    fun `최근 기록은 시작 시각 내림차순`() =
        runTest {
            dao.upsertRun(testRun("old", startedAt = 1))
            dao.upsertRun(testRun("new", startedAt = 2))

            assertEquals(listOf("new", "old"), recentIds())
        }

    @Test
    fun `최근 기록은 개수를 제한함`() =
        runTest {
            (1..3).forEach { dao.upsertRun(testRun("r$it", startedAt = it.toLong())) }

            assertEquals(listOf("r3", "r2"), dao.observeRecent(ME, 2).first().map { it.id })
        }

    @Test
    fun `게스트 기록과 계정 기록을 나눠 봄`() =
        runTest {
            dao.upsertRun(testRun("guest", owner = null))
            dao.upsertRun(testRun("mine", owner = ME))
            dao.upsertRun(testRun("other", owner = "user-2"))

            assertEquals(listOf("guest"), recentIds(owner = null))
            assertEquals(listOf("mine"), recentIds(owner = ME))
        }

    @Test
    fun `끝나지 않았거나 버린 기록은 보이지 않음`() =
        runTest {
            listOf("ACTIVE", "PAUSED", "DISCARDED").forEach { dao.upsertRun(testRun(it, status = it)) }
            dao.upsertRun(testRun("done"))

            assertEquals(listOf("done"), recentIds())
        }

    @Test
    fun `삭제한 기록은 목록과 상세에서 보이지 않음`() =
        runTest {
            dao.upsertRun(testRun("a"))

            dao.softDelete("a", deletedAt = 500, syncState = SyncState.PENDING.name)

            assertEquals(emptyList<String>(), recentIds())
            assertNull(dao.observeRun("a").first())
        }

    @Test
    fun `범위는 시작 포함 끝 제외`() =
        runTest {
            dao.upsertRun(testRun("before", startedAt = 99))
            dao.upsertRun(testRun("first", startedAt = 100))
            dao.upsertRun(testRun("last", startedAt = 199))
            dao.upsertRun(testRun("after", startedAt = 200))

            assertEquals(listOf("last", "first"), dao.observeInRange(ME, 100, 200).first().map { it.id })
        }

    @Test
    fun `누적 합계는 끝난 내 기록만 더함`() =
        runTest {
            dao.upsertRun(testRun("a", distanceM = 1_000.0))
            dao.upsertRun(testRun("b", distanceM = 2_000.0))
            dao.upsertRun(testRun("active", status = "ACTIVE"))
            dao.upsertRun(testRun("deleted").copy(deletedAt = 1))
            dao.upsertRun(testRun("other", owner = "user-2"))

            assertEquals(RunTotalsRow(3_000.0, 2), dao.observeTotals(ME).first())
        }

    @Test
    fun `기록이 없으면 누적 합계는 0`() =
        runTest {
            assertEquals(RunTotalsRow(0.0, 0), dao.observeTotals(ME).first())
        }

    @Test
    fun `상세는 구간을 함께 돌려줌`() =
        runTest {
            dao.upsertRun(testRun("a"))
            dao.upsertSplits(listOf(testSplit("a", 1), testSplit("a", 2)))

            assertEquals(
                setOf(1, 2),
                dao
                    .observeRun("a")
                    .first()
                    ?.splits
                    ?.map { it.idx }
                    ?.toSet(),
            )
        }

    @Test
    fun `제목과 메모를 고치면 수정 시각과 백업 상태가 바뀜`() =
        runTest {
            dao.upsertRun(testRun("a"))

            dao.updateTitleAndMemo("a", "수요일 아침 러닝", "시원했음", updatedAt = 9_000, syncState = SyncState.PENDING.name)

            val run = requireNotNull(dao.observeRun("a").first()).run
            assertEquals("수요일 아침 러닝", run.title)
            assertEquals("시원했음", run.memo)
            assertEquals(9_000, run.updatedAt)
            assertEquals(SyncState.PENDING.name, run.syncState)
        }

    @Test
    fun `같은 id를 다시 저장해도 구간이 남음`() =
        runTest {
            dao.upsertRun(testRun("a"))
            dao.upsertSplits(listOf(testSplit("a", 1)))

            dao.upsertRun(testRun("a", distanceM = 5.0))

            assertEquals(
                1,
                dao
                    .observeRun("a")
                    .first()
                    ?.splits
                    ?.size,
            )
        }

    @Test
    fun `실제 삭제하면 점과 구간도 함께 지워짐`() =
        runTest {
            dao.upsertRun(testRun("a"))
            dao.upsertSplits(listOf(testSplit("a", 1)))
            db.openHelper.writableDatabase.execSQL(
                "INSERT INTO run_point (run_id, seq, lat, lng, accuracy, recorded_at, segment, accepted) " +
                    "VALUES ('a', 0, 0, 0, 5, 0, 0, 1)",
            )
            assertEquals(1, pointCount())

            db.openHelper.writableDatabase.execSQL("DELETE FROM run WHERE id = 'a'")

            assertEquals(0, pointCount())
            assertEquals(0, splitCount())
        }

    private fun pointCount() = count("run_point")

    private fun splitCount() = count("run_split")

    private fun count(table: String): Int =
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
