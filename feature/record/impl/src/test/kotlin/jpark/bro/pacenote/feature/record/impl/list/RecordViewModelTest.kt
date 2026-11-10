package jpark.bro.pacenote.feature.record.impl.list

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
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
}
