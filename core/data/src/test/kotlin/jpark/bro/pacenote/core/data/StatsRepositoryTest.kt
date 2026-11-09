package jpark.bro.pacenote.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.data.repository.DefaultStatsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
internal class StatsRepositoryTest : RepositoryTest() {
    private val repository by lazy { DefaultStatsRepository(db.runDao(), auth, clock) }
    private val thisWeek = LocalDate.of(2026, 10, 26)

    private suspend fun insertSampleWeek() {
        // product-spec.md 9장: 이번 주 12.40 km, 3회
        db.runDao().upsertRun(
            runEntity("1028", startedAt = seoulMs(10, 28, 6, 40), distanceM = 5_210.0, movingMs = 1_812_000),
        )
        db.runDao().upsertRun(
            runEntity("1027", startedAt = seoulMs(10, 27, 20, 15), distanceM = 3_210.0, movingMs = 1_103_000),
        )
        db.runDao().upsertRun(
            runEntity("1026", startedAt = seoulMs(10, 26, 7, 5), distanceM = 3_980.0, movingMs = 1_445_000),
        )
    }

    @Test
    fun `이번 주 합계는 샘플 시나리오와 같음`() =
        runTest {
            insertSampleWeek()

            val summary = repository.observeWeeklySummary(thisWeek).first()

            assertEquals(12_400.0, summary.distanceM, 0.001)
            assertEquals(3, summary.runCount)
            assertEquals(4_360_000L, summary.movingMs)
        }

    @Test
    fun `일요일 23시 59분 기록은 지난 주, 월요일 0시 기록은 이번 주`() =
        runTest {
            db.runDao().upsertRun(runEntity("sun", startedAt = seoulMs(10, 25, 23, 59)))
            db.runDao().upsertRun(runEntity("mon", startedAt = seoulMs(10, 26, 0, 0)))

            assertEquals(1, repository.observeWeeklySummary(thisWeek).first().runCount)
            assertEquals(1, repository.observeWeeklySummary(thisWeek.minusWeeks(1)).first().runCount)
        }

    @Test
    fun `버린 기록과 다른 사용자 기록은 합계에 넣지 않음`() =
        runTest {
            insertSampleWeek()
            db.runDao().upsertRun(runEntity("discarded").copy(status = "DISCARDED"))
            db.runDao().upsertRun(runEntity("other", owner = ME))

            assertEquals(3, repository.observeWeeklySummary(thisWeek).first().runCount)
            val totals = repository.observeTotals().first()
            assertEquals(12_400.0, totals.distanceM, 0.001)
            assertEquals(3, totals.runCount)
        }
}
