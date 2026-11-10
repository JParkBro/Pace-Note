package jpark.bro.pacenote.core.data

import androidx.paging.testing.asSnapshot
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.data.repository.DefaultRunRepository
import jpark.bro.pacenote.core.database.entity.RunSplitEntity
import jpark.bro.pacenote.core.database.entity.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RunRepositoryTest : RepositoryTest() {
    private val repository by lazy { DefaultRunRepository(db.runDao(), auth, clock) }

    @Test
    fun `게스트는 게스트 기록만, 로그인하면 그 계정 기록만 봄`() =
        runTest {
            db.runDao().upsertRun(runEntity("guest"))
            db.runDao().upsertRun(runEntity("mine", owner = ME))

            assertEquals(listOf("guest"), repository.observeRecentRuns(10).first().map { it.id })

            auth.userId.value = ME
            assertEquals(listOf("mine"), repository.observeRecentRuns(10).first().map { it.id })
        }

    @Test
    fun `상세는 스플릿을 순서대로 담고 경로 구간을 읽음`() =
        runTest {
            db.runDao().upsertRun(runEntity("r").copy(routeSegments = "[312,140]"))
            db.runDao().upsertSplits(
                listOf(RunSplitEntity("r", 1, 1_000.0, 344_000), RunSplitEntity("r", 0, 1_000.0, 352_000)),
            )

            val run = repository.observeRun("r").first()!!

            assertEquals(listOf(0, 1), run.splits.map { it.index })
            assertEquals(listOf(312, 140), run.routeSegments)
        }

    @Test
    fun `제목 메모 수정은 앞뒤 공백을 지우고 빈 값은 null`() =
        runTest {
            db.runDao().upsertRun(runEntity("r"))

            repository.updateTitleAndMemo("r", "  수요일 아침 러닝 ", "   ")

            val run = repository.observeRun("r").first()!!
            assertEquals("수요일 아침 러닝", run.title)
            assertNull(run.memo)
            assertEquals(clock.millis(), run.updatedAt)
        }

    @Test
    fun `게스트 수정은 LOCAL, 로그인 사용자 수정은 PENDING`() =
        runTest {
            db.runDao().upsertRun(runEntity("guest"))
            db.runDao().upsertRun(runEntity("mine", owner = ME))

            repository.updateTitleAndMemo("guest", "a", null)
            auth.userId.value = ME
            repository.updateTitleAndMemo("mine", "b", null)

            assertEquals(
                SyncState.LOCAL.name,
                db
                    .runDao()
                    .observeRecent(null, 1)
                    .first()
                    .single()
                    .syncState,
            )
            assertEquals(
                SyncState.PENDING.name,
                db
                    .runDao()
                    .observeRecent(ME, 1)
                    .first()
                    .single()
                    .syncState,
            )
        }

    @Test
    fun `삭제한 기록은 목록과 상세에서 사라짐`() =
        runTest {
            db.runDao().upsertRun(runEntity("r"))

            assertTrue(repository.deleteRun("r").isSuccess)

            assertTrue(repository.observeRecentRuns(10).first().isEmpty())
            assertNull(repository.observeRun("r").first())
        }

    @Test
    fun `기록 목록은 끝난 기록만 최신순, 지운 기록은 빠짐`() =
        runTest {
            db.runDao().upsertRun(runEntity("old", startedAt = seoulMs(10, 1, 7, 0)))
            db.runDao().upsertRun(runEntity("new", startedAt = seoulMs(10, 28, 6, 40)))
            db.runDao().upsertRun(runEntity("deleted", startedAt = seoulMs(10, 20, 7, 0)).copy(deletedAt = 1L))
            db.runDao().upsertRun(runEntity("discarded").copy(status = "DISCARDED"))

            val ids = repository.observeRunPages().asSnapshot().map { it.id }

            assertEquals(listOf("new", "old"), ids)
        }
}
