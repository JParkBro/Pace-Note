package jpark.bro.pacenote.feature.record.impl.detail

import app.cash.turbine.test
import jpark.bro.pacenote.core.domain.stats.DefaultRunTitle
import jpark.bro.pacenote.core.domain.stats.TimeOfDay
import jpark.bro.pacenote.core.model.RunSplit
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeRunRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class RecordDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val run = testRun().copy(splits = listOf(RunSplit(1, 1_000.0, 352_000), RunSplit(2, 210.0, 79_000)))
    private val runs = FakeRunRepository().apply { runs.value = listOf(run) }

    private fun viewModel(runId: String = run.id) = RecordDetailViewModel(runId, runs, FakeSettingsRepository())

    @Test
    fun `기록 요약 · 구간 · 기본 제목 재료를 보여줌`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            val state = vm.state.value
            assertFalse(state.loading)
            assertEquals("수요일 아침 러닝", state.title)
            assertEquals(DefaultRunTitle(DayOfWeek.WEDNESDAY, TimeOfDay.MORNING), state.defaultTitle)
            assertEquals(347.79, state.averagePaceSecPerKm!!, 0.01)
            assertEquals(listOf(1_000.0, 1_210.0), state.splits.map { it.endDistanceM })
            assertEquals("한강 바람 시원했음", state.memo)
        }

    @Test
    fun `편집에서 고친 제목이 바로 반영됨`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            runs.runs.update { list -> list.map { it.copy(title = null) } }
            runCurrent()

            assertEquals(null, vm.state.value.title)
        }

    @Test
    fun `삭제된 기록이면 찾을 수 없음, 편집으로 가지 않음`() =
        runTest {
            runs.runs.update { list -> list.map { it.copy(deletedAt = 1L) } }
            val vm = viewModel()
            runCurrent()

            assertTrue(vm.state.value.missing)
            vm.event.test {
                vm.onAction(RecordDetailAction.EditClicked)
                vm.onAction(RecordDetailAction.BackClicked)
                assertEquals(RecordDetailEvent.NavigateBack, awaitItem())
            }
        }

    @Test
    fun `편집을 누르면 편집 화면으로`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RecordDetailAction.EditClicked)
                assertEquals(RecordDetailEvent.OpenEdit(run.id), awaitItem())
            }
        }
}
