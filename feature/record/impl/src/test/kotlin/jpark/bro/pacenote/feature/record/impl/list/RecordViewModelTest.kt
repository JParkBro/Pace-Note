package jpark.bro.pacenote.feature.record.impl.list

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import jpark.bro.pacenote.core.domain.stats.StatsPeriod
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeRunRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.fake.FakeStatsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val runs = FakeRunRepository()
    private val stats = FakeStatsRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel() = RecordViewModel(runs, stats, settings, FakeClock())

    private fun seoulMs(dateTime: String) =
        LocalDateTime
            .parse(dateTime)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toEpochMilli()

    @Test
    fun `달이 바뀌는 곳마다 월 머리를 넣고 최신순으로 보여줌`() =
        runTest {
            runs.runs.value =
                listOf(
                    testRun(id = "0929", startedAt = seoulMs("2026-09-29T07:00")),
                    testRun(id = "1028", startedAt = seoulMs("2026-10-28T06:40")),
                    testRun(id = "1026", startedAt = seoulMs("2026-10-26T07:05")),
                )
            val vm = viewModel()
            runCurrent()

            val keys =
                vm.state.value.items
                    .asSnapshot()
                    .map { it.key }

            assertEquals(listOf("month-2026-10", "run-1028", "run-1026", "month-2026-09", "run-0929"), keys)
        }

    @Test
    fun `기록 행에 평균 페이스를 계산해 둠`() =
        runTest {
            runs.runs.value = listOf(testRun())
            val vm = viewModel()
            runCurrent()

            val row =
                vm.state.value.items
                    .asSnapshot()
                    .filterIsInstance<RecordItem.RunRow>()
                    .single()

            assertEquals(347.79, row.averagePaceSecPerKm!!, 0.01)
        }

    @Test
    fun `월 합계와 올해를 상태에 담음`() =
        runTest {
            stats.monthlyTotals.value = mapOf(YearMonth.of(2026, 10) to RunTotals(12_400.0, 3))
            val vm = viewModel()
            runCurrent()

            assertEquals(
                3,
                vm.state.value.monthTotals
                    .getValue(YearMonth.of(2026, 10))
                    .runCount,
            )
            assertEquals(2026, vm.state.value.thisYear)
            assertFalse(vm.state.value.isEmpty)
        }

    @Test
    fun `기록이 없으면 빈 상태`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertTrue(vm.state.value.isEmpty)
        }

    @Test
    fun `빈 상태에서 달리기 시작을 누르면 러닝 준비로`() =
        runTest {
            val vm = viewModel()

            vm.event.test {
                vm.onAction(RecordAction.StartRunClicked)
                assertEquals(RecordEvent.StartRun, awaitItem())
            }
        }

    // 이번 주 3회 12.40km + 지난주 10.30km (product-spec 9장)
    private fun sampleRuns() =
        listOf(
            testRun(id = "1026", startedAt = seoulMs("2026-10-26T07:05"), distanceM = 3_980.0, movingMs = 1_445_000),
            testRun(id = "1027", startedAt = seoulMs("2026-10-27T20:15"), distanceM = 3_210.0, movingMs = 1_103_000),
            testRun(id = "1028", startedAt = seoulMs("2026-10-28T06:40"), distanceM = 5_210.0, movingMs = 1_812_000),
            testRun(id = "1021", startedAt = seoulMs("2026-10-21T07:00"), distanceM = 10_300.0, movingMs = 3_700_000),
        )

    @Test
    fun `통계는 이번 주부터, 지난주 거리와 평균 페이스를 함께 보여줌`() =
        runTest {
            stats.runs.value = sampleRuns()
            val vm = viewModel()
            runCurrent()

            val recordStats = vm.state.value.stats!!
            assertEquals(StatsPeriod.WEEK, recordStats.current.period)
            assertEquals(LocalDate.of(2026, 10, 26), recordStats.current.start)
            assertEquals(12_400.0, recordStats.current.distanceM, 0.001)
            assertEquals(10_300.0, recordStats.previousDistanceM, 0.001)
            assertEquals(351.61, recordStats.averagePaceSecPerKm!!, 0.01)
            assertFalse(recordStats.canGoNext)
        }

    @Test
    fun `이전 기간으로 가면 다음 기간으로 돌아올 수 있음`() =
        runTest {
            stats.runs.value = sampleRuns()
            val vm = viewModel()
            runCurrent()

            vm.onAction(RecordAction.PreviousPeriodClicked)
            runCurrent()

            val recordStats = vm.state.value.stats!!
            assertEquals(LocalDate.of(2026, 10, 19), recordStats.current.start)
            assertEquals(10_300.0, recordStats.current.distanceM, 0.001)
            assertTrue(recordStats.canGoNext)
        }

    @Test
    fun `이번 기간보다 뒤로는 가지 않음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RecordAction.NextPeriodClicked)
            runCurrent()

            assertEquals(
                LocalDate.of(2026, 10, 26),
                vm.state.value.stats!!
                    .current.start,
            )
        }

    @Test
    fun `월간을 고르면 이번 달 통계`() =
        runTest {
            stats.runs.value = sampleRuns()
            val vm = viewModel()
            runCurrent()

            vm.onAction(RecordAction.PeriodSelected(StatsPeriod.MONTH))
            runCurrent()

            val recordStats = vm.state.value.stats!!
            assertEquals(LocalDate.of(2026, 10, 1), recordStats.current.start)
            assertEquals(4, recordStats.current.runCount)
        }

    @Test
    fun `목록 · 통계 전환`() =
        runTest {
            val vm = viewModel()

            vm.onAction(RecordAction.TabSelected(RecordTab.STATS))

            assertEquals(RecordTab.STATS, vm.state.value.tab)
        }
}
