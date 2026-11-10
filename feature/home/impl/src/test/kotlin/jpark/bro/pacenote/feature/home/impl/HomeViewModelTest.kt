package jpark.bro.pacenote.feature.home.impl

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.model.WeeklySummary
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeGoalRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    // 오늘 2026.10.29 (목) → 이번 주는 10.26 (월)부터
    private val weekStart = LocalDate.of(2026, 10, 26)
    private val runs = FakeRunRepository()
    private val stats = FakeStatsRepository()
    private val goals = FakeGoalRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel() = HomeViewModel(runs, stats, goals, settings, FakeClock())

    private fun goal(
        type: GoalType,
        value: Double,
    ) = Goal("goal", null, type, value, weekStart, 0, 0, null)

    private fun weekOf(
        distanceM: Double,
        movingMs: Long,
        count: Int,
    ) {
        stats.weeklySummaries.value = mapOf(weekStart to WeeklySummary(weekStart, distanceM, movingMs, count))
    }

    @Test
    fun `이번 주 합계와 평균 페이스를 보여줌`() =
        runTest {
            weekOf(12_400.0, 4_360_000, 3)
            val vm = viewModel()
            runCurrent()

            val state = vm.state.value
            assertFalse(state.loading)
            assertEquals(weekStart, state.weekStart)
            assertEquals(12_400.0, state.distanceM, 0.0)
            assertEquals(3, state.runCount)
            assertEquals(351.61, state.averagePaceSecPerKm!!, 0.01)
        }

    @Test
    fun `거리 목표가 있으면 진행률, 100% 미만이면 달성 아님`() =
        runTest {
            weekOf(12_400.0, 4_360_000, 3)
            goals.goal.value = goal(GoalType.DISTANCE, 15_000.0)
            val vm = viewModel()
            runCurrent()

            val homeGoal = vm.state.value.goal!!
            assertEquals(0.8267, homeGoal.ratio, 0.0001)
            assertFalse(homeGoal.achieved)
        }

    @Test
    fun `횟수 목표를 채우면 달성`() =
        runTest {
            weekOf(12_400.0, 4_360_000, 3)
            goals.goal.value = goal(GoalType.COUNT, 3.0)
            val vm = viewModel()
            runCurrent()

            assertTrue(
                vm.state.value.goal!!
                    .achieved,
            )
        }

    @Test
    fun `목표가 없으면 목표 진행을 숨김`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertNull(vm.state.value.goal)
        }

    @Test
    fun `이번 주 기록이 없으면 평균 페이스 없음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertEquals(0, vm.state.value.runCount)
            assertNull(vm.state.value.averagePaceSecPerKm)
        }

    @Test
    fun `최근 기록은 최신순 2개`() =
        runTest {
            runs.runs.value =
                listOf(
                    testRun(id = "old", startedAt = 1_000L),
                    testRun(id = "new"),
                    testRun(id = "mid", startedAt = 2_000L),
                )
            val vm = viewModel()
            runCurrent()

            assertEquals(
                listOf("new", "mid"),
                vm.state.value.recentRuns
                    .map { it.id },
            )
            assertEquals(
                347.79,
                vm.state.value.recentRuns
                    .first()
                    .averagePaceSecPerKm!!,
                0.01,
            )
        }

    @Test
    fun `단위를 바꾸면 바로 반영`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            settings.profile.value = UserProfile(weightKg = null, distanceUnit = DistanceUnit.MILE)
            runCurrent()

            assertEquals(DistanceUnit.MILE, vm.state.value.distanceUnit)
        }

    @Test
    fun `달리기 시작을 누르면 러닝 준비로`() =
        runTest {
            val vm = viewModel()

            vm.event.test {
                vm.onAction(HomeAction.StartRunClicked)
                assertEquals(HomeEvent.StartRun, awaitItem())
            }
        }
}
