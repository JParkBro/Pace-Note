package jpark.bro.pacenote.feature.record.impl.edit

import app.cash.turbine.test
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeRunRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val run = testRun()
    private val runs = FakeRunRepository().apply { runs.value = listOf(run) }

    private fun viewModel(runId: String = run.id) = RunEditViewModel(runId, runs)

    @Test
    fun `저장된 제목 · 메모로 시작`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertEquals("수요일 아침 러닝", vm.state.value.title)
            assertEquals("한강 바람 시원했음", vm.state.value.memo)
            assertNotNull(vm.state.value.defaultTitle)
        }

    @Test
    fun `제목 40자 · 메모 500자까지만 받음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunEditAction.TitleChanged("가".repeat(50)))
            vm.onAction(RunEditAction.MemoChanged("나".repeat(600)))

            assertEquals(40, vm.state.value.title.length)
            assertEquals(500, vm.state.value.memo.length)
        }

    @Test
    fun `저장하면 고친 제목 · 메모를 쓰고 닫음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RunEditAction.TitleChanged("한강 아침 러닝"))
                vm.onAction(RunEditAction.MemoChanged(""))
                vm.onAction(RunEditAction.SaveClicked)
                assertEquals(RunEditEvent.Close, awaitItem())
            }
            val saved = runs.runs.value.single()
            assertEquals("한강 아침 러닝", saved.title)
            assertEquals("", saved.memo)
        }

    @Test
    fun `저장에 실패하면 오류 안내, 화면은 그대로`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            runs.writeResult = Result.failure(IllegalStateException())

            vm.event.test {
                vm.onAction(RunEditAction.SaveClicked)
                assertEquals(RunEditEvent.ShowError, awaitItem())
            }
            assertFalse(vm.state.value.working)
        }

    @Test
    fun `삭제는 확인 다이얼로그를 거쳐 soft delete 후 상세까지 닫음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunEditAction.DeleteClicked)
            assertTrue(vm.state.value.showDeleteDialog)

            vm.event.test {
                vm.onAction(RunEditAction.DeleteConfirmed)
                assertEquals(RunEditEvent.Deleted, awaitItem())
            }
            assertNotNull(
                runs.runs.value
                    .single()
                    .deletedAt,
            )
            assertFalse(vm.state.value.showDeleteDialog)
        }

    @Test
    fun `삭제 다이얼로그에서 취소하면 지우지 않음`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunEditAction.DeleteClicked)
            vm.onAction(RunEditAction.DeleteDismissed)

            assertFalse(vm.state.value.showDeleteDialog)
            assertEquals(
                null,
                runs.runs.value
                    .single()
                    .deletedAt,
            )
        }

    @Test
    fun `기록이 없으면 닫음`() =
        runTest {
            val vm = viewModel(runId = "missing")

            vm.event.test {
                assertEquals(RunEditEvent.Close, awaitItem())
            }
        }
}
