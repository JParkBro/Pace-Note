package jpark.bro.pacenote.feature.run.impl.result

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.RunSplit
import jpark.bro.pacenote.core.model.WeeklySummary
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeGoalRepository
import jpark.bro.pacenote.core.testing.fake.FakeRunRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.fake.FakeStatsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RunResultViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    // 샘플 기록: 10.28 (수) 5.21km 30:12, 메모 "한강 바람 시원했음"
    private val run =
        testRun().copy(
            splits =
                listOf(
                    RunSplit(1, 1_000.0, 352_000),
                    RunSplit(2, 1_000.0, 344_000),
                    RunSplit(3, 1_000.0, 340_000),
                    RunSplit(4, 1_000.0, 350_000),
                    RunSplit(5, 1_000.0, 347_000),
                    RunSplit(6, 210.0, 79_000),
                ),
        )
    private val weekStart = LocalDate.of(2026, 10, 26)
    private val runs = FakeRunRepository().apply { runs.value = listOf(run) }
    private val stats = FakeStatsRepository()
    private val goals = FakeGoalRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel(runId: String = run.id) = RunResultViewModel(runId, runs, stats, goals, settings)

    private fun distanceGoal(meters: Double) = Goal("goal", null, GoalType.DISTANCE, meters, weekStart, 0, 0, null)

    @Test
    fun `기록을 불러와 수치 · 구간 · 메모를 보여줌`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            val state = vm.state.value
            assertFalse(state.loading)
            assertEquals(5_210.0, state.distanceM, 0.0)
            assertEquals(1_812_000L, state.movingMs)
            assertEquals(347.79, state.averagePaceSecPerKm!!, 0.01)
            assertEquals(351, state.calories)
            assertEquals("한강 바람 시원했음", state.memo)
            assertEquals(6, state.splits.size)
            assertEquals(352.0, state.splits.first().paceSecPerKm!!, 0.0001)
            // 마지막 210m 구간은 누적 5,210m에서 끝나고 환산 페이스를 보여준다
            assertEquals(5_210.0, state.splits.last().endDistanceM, 0.0001)
            assertEquals(376.19, state.splits.last().paceSecPerKm!!, 0.01)
        }

    @Test
    fun `이번 주 목표가 있으면 진행률과 남은 거리를 보여줌`() =
        runTest {
            goals.goal.value = distanceGoal(15_000.0)
            stats.weeklySummaries.value = mapOf(weekStart to WeeklySummary(weekStart, 12_400.0, 4_300_000, 3))
            val vm = viewModel()
            runCurrent()

            val goal = vm.state.value.weeklyGoal!!
            assertEquals(0.8267, goal.ratio, 0.0001)
            assertEquals(2_600.0, goal.remaining, 0.001)
        }

    @Test
    fun `목표를 넘기면 남은 거리는 0`() =
        runTest {
            goals.goal.value = distanceGoal(10_000.0)
            stats.weeklySummaries.value = mapOf(weekStart to WeeklySummary(weekStart, 12_400.0, 4_300_000, 3))
            val vm = viewModel()
            runCurrent()

            assertEquals(
                1.24,
                vm.state.value.weeklyGoal!!
                    .ratio,
                0.0001,
            )
            assertEquals(
                0.0,
                vm.state.value.weeklyGoal!!
                    .remaining,
                0.0,
            )
        }

    @Test
    fun `목표가 없으면 목표 카드를 숨김`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertNull(vm.state.value.weeklyGoal)
        }

    @Test
    fun `메모 입력이 0점5초 멈추면 저장`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunResultAction.MemoChanged("바람"))
            advanceTimeBy(400)
            vm.onAction(RunResultAction.MemoChanged("바람 시원"))
            advanceTimeBy(499)
            runCurrent()
            assertEquals(
                "한강 바람 시원했음",
                runs.runs.value
                    .single()
                    .memo,
            )

            advanceTimeBy(2)
            runCurrent()
            val saved = runs.runs.value.single()
            assertEquals("바람 시원", saved.memo)
            assertEquals("수요일 아침 러닝", saved.title)
        }

    @Test
    fun `메모는 500자까지만 받음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunResultAction.MemoChanged("가".repeat(600)))

            assertEquals(500, vm.state.value.memo.length)
        }

    @Test
    fun `메모 저장에 실패하면 오류 안내`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            runs.writeResult = Result.failure(IllegalStateException())

            vm.event.test {
                vm.onAction(RunResultAction.MemoChanged("바람"))
                advanceTimeBy(501)
                assertEquals(RunResultEvent.ShowError, awaitItem())
            }
        }

    @Test
    fun `완료를 누르면 남은 메모를 바로 저장하고 흐름을 끝냄`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RunResultAction.MemoChanged("완료 직전 메모"))
                vm.onAction(RunResultAction.DoneClicked)
                assertEquals(RunResultEvent.Finish, awaitItem())
            }
            assertEquals(
                "완료 직전 메모",
                runs.runs.value
                    .single()
                    .memo,
            )
        }

    @Test
    fun `기록이 없으면 흐름을 끝냄`() =
        runTest {
            val vm = viewModel(runId = "missing")

            vm.event.test {
                assertEquals(RunResultEvent.Finish, awaitItem())
            }
        }

    @Test
    fun `공유 카드 만들기를 누르면 남은 메모를 저장하고 공유 카드로`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RunResultAction.MemoChanged("공유 전 메모"))
                vm.onAction(RunResultAction.ShareClicked)
                assertEquals(RunResultEvent.OpenShareCard(run.id), awaitItem())
            }
            assertEquals(
                "공유 전 메모",
                runs.runs.value
                    .single()
                    .memo,
            )
        }
}
