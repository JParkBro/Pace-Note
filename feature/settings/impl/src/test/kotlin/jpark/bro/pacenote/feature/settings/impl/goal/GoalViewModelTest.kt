package jpark.bro.pacenote.feature.settings.impl.goal

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeGoalRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.fake.FakeStatsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val goals = FakeGoalRepository()
    private val stats = FakeStatsRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel() = GoalViewModel(goals, stats, settings, FakeClock())

    private fun seoulMs(dateTime: String) =
        LocalDateTime
            .parse(dateTime)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toEpochMilli()

    @Test
    fun `목표가 없으면 주간 거리 15km로 시작`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertEquals(GoalType.DISTANCE, vm.state.value.type)
            assertEquals(15, vm.state.value.amount)
            assertNull(vm.state.value.recentDistanceM)
        }

    @Test
    fun `이번 주 목표로 시작하고, 4주 기록이 있으면 최근 평균`() =
        runTest {
            goals.goal.value = Goal("g", null, GoalType.COUNT, 4.0, LocalDate.of(2026, 10, 26), 0, 0, null)
            stats.runs.value =
                listOf(
                    testRun(id = "w1", startedAt = seoulMs("2026-09-28T07:00"), distanceM = 10_000.0),
                    testRun(id = "w4", startedAt = seoulMs("2026-10-19T07:00"), distanceM = 6_000.0),
                )
            val vm = viewModel()
            runCurrent()

            assertEquals(GoalType.COUNT, vm.state.value.type)
            assertEquals(4, vm.state.value.amount)
            assertEquals(4_000.0, vm.state.value.recentDistanceM!!, 0.001)
            assertEquals(0.5, vm.state.value.recentCount!!, 0.001)
        }

    @Test
    fun `횟수로 바꾸면 주 3회부터, 1회 아래로는 못 내림`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(GoalAction.TypeSelected(GoalType.COUNT))
            assertEquals(3, vm.state.value.amount)
            repeat(5) { vm.onAction(GoalAction.DecreaseClicked) }

            assertEquals(1, vm.state.value.amount)
            assertFalse(vm.state.value.canDecrease)
        }

    @Test
    fun `저장하면 거리 목표를 미터로 저장하고 닫음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(GoalAction.IncreaseClicked)
                vm.onAction(GoalAction.SaveClicked)
                assertEquals(GoalEvent.Close, awaitItem())
            }
            assertEquals(GoalType.DISTANCE to 16_000.0, goals.lastSet)
        }

    @Test
    fun `마일 단위면 마일로 보여주고 미터로 저장`() =
        runTest {
            settings.profile.value = UserProfile(weightKg = null, distanceUnit = DistanceUnit.MILE)
            val vm = viewModel()
            runCurrent()

            assertEquals(9, vm.state.value.amount)
            vm.event.test {
                vm.onAction(GoalAction.SaveClicked)
                assertEquals(GoalEvent.Close, awaitItem())
            }
            assertEquals(9 * 1_609.344, goals.lastSet!!.second, 0.001)
        }

    @Test
    fun `저장에 실패하면 오류 안내`() =
        runTest {
            goals.writeResult = Result.failure(IllegalStateException())
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(GoalAction.SaveClicked)
                assertEquals(GoalEvent.ShowError, awaitItem())
            }
        }
}
