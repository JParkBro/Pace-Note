package jpark.bro.pacenote.feature.record.impl.share

import app.cash.turbine.test
import jpark.bro.pacenote.core.testing.data.testRun
import jpark.bro.pacenote.core.testing.fake.FakeRunRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ShareCardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val run = testRun()
    private val runs = FakeRunRepository().apply { runs.value = listOf(run) }

    private fun viewModel(runId: String = run.id) = ShareCardViewModel(runId, runs, FakeSettingsRepository())

    @Test
    fun `기본 배경, 거리 · 시간 · 페이스로 시작`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            val state = vm.state.value
            assertFalse(state.loading)
            assertEquals(ShareBackground.DEFAULT, state.background)
            assertEquals(setOf(ShareField.DISTANCE, ShareField.TIME, ShareField.PACE), state.fields)
            assertEquals(347.79, state.averagePaceSecPerKm!!, 0.01)
        }

    @Test
    fun `표시 정보는 눌러서 켜고 끔`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(ShareCardAction.FieldToggled(ShareField.DATE))
            vm.onAction(ShareCardAction.FieldToggled(ShareField.PACE))

            assertEquals(setOf(ShareField.DISTANCE, ShareField.TIME, ShareField.DATE), vm.state.value.fields)
        }

    @Test
    fun `사진 배경은 사진을 고른 뒤에야 바뀜`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(ShareCardAction.BackgroundSelected(ShareBackground.PHOTO))
                assertEquals(ShareCardEvent.OpenPhotoPicker, awaitItem())
            }
            assertEquals(ShareBackground.DEFAULT, vm.state.value.background)

            vm.onAction(ShareCardAction.PhotoPicked(null))
            assertEquals(ShareBackground.DEFAULT, vm.state.value.background)

            vm.onAction(ShareCardAction.PhotoPicked("content://photo/1"))
            assertEquals(ShareBackground.PHOTO, vm.state.value.background)
            assertEquals("content://photo/1", vm.state.value.photoUri)
        }

    @Test
    fun `투명 배경 선택`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(ShareCardAction.BackgroundSelected(ShareBackground.TRANSPARENT))

            assertEquals(ShareBackground.TRANSPARENT, vm.state.value.background)
        }

    @Test
    fun `저장을 누르면 이미지 만들기를 요청하고, 끝나면 저장 완료 안내`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(ShareCardAction.SaveClicked)
                assertEquals(ShareCardEvent.ExportToGallery, awaitItem())
                assertTrue(vm.state.value.working)

                vm.onAction(ShareCardAction.SaveClicked)
                vm.onAction(ShareCardAction.ExportFinished(success = true, saved = true))
                assertEquals(ShareCardEvent.ShowSaved, awaitItem())
            }
            assertFalse(vm.state.value.working)
        }

    @Test
    fun `공유가 실패하면 오류 안내`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(ShareCardAction.ShareClicked)
                assertEquals(ShareCardEvent.ExportToShareSheet, awaitItem())
                vm.onAction(ShareCardAction.ExportFinished(success = false, saved = false))
                assertEquals(ShareCardEvent.ShowError, awaitItem())
            }
        }

    @Test
    fun `기록이 없으면 닫음`() =
        runTest {
            val vm = viewModel(runId = "missing")

            vm.event.test {
                assertEquals(ShareCardEvent.NavigateBack, awaitItem())
            }
        }
}
