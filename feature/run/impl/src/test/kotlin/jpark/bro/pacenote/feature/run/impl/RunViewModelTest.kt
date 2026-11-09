package jpark.bro.pacenote.feature.run.impl

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeRunTracker
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import jpark.bro.pacenote.core.tracking.RecoverableRun
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val tracker = FakeRunTracker()
    private val settings = FakeSettingsRepository().apply { batteryGuideShown.value = true }
    private val clock = FakeClock()

    private fun viewModel(recoverRunId: String? = null) = RunViewModel(recoverRunId, tracker, settings, clock)

    private fun device(
        fine: Boolean = true,
        coarse: Boolean = true,
        locationEnabled: Boolean = true,
        ignoringBattery: Boolean = true,
    ) = RunAction.DeviceChecked(fine, coarse, locationEnabled, ignoringBattery)

    @Test
    fun `정밀 위치 권한이 없으면 권한 안내, 대략만 허용했으면 재요청 안내`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(device(fine = false, coarse = true))

            assertEquals(RunStep.PERMISSION, vm.state.value.step)
            assertTrue(vm.state.value.coarseOnly)
            vm.event.test {
                vm.onAction(RunAction.RequestPermissionClicked)
                assertEquals(RunEvent.RequestLocationPermission, awaitItem())
            }
        }

    @Test
    fun `권한을 허용하고 돌아오면 GPS 대기로 넘어가 위치를 받기 시작`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            vm.onAction(device(fine = false, coarse = false))

            vm.onAction(device())
            runCurrent()

            assertEquals(RunStep.GPS, vm.state.value.step)
            assertEquals(listOf("prepare"), tracker.calls)
        }

    @Test
    fun `첫 러닝이고 배터리 최적화 대상이면 한 번 안내하고 다음에 하기는 다시 보이지 않게 저장`() =
        runTest {
            settings.batteryGuideShown.value = false
            val vm = viewModel()
            runCurrent()

            vm.onAction(device(ignoringBattery = false))
            assertEquals(RunStep.BATTERY, vm.state.value.step)

            vm.onAction(RunAction.BatteryLaterClicked)
            runCurrent()

            assertEquals(RunStep.GPS, vm.state.value.step)
            assertTrue(settings.batteryGuideShown.value)
        }

    @Test
    fun `이미 최적화 예외면 배터리 안내를 건너뜀`() =
        runTest {
            settings.batteryGuideShown.value = false
            val vm = viewModel()
            runCurrent()

            vm.onAction(device(ignoringBattery = true))

            assertEquals(RunStep.GPS, vm.state.value.step)
        }

    @Test
    fun `시작하면 카운트다운 3초 뒤 기록이 시작되고 기록 화면으로`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            vm.onAction(device())
            runCurrent()

            vm.onAction(RunAction.StartClicked)
            assertEquals(RunStep.COUNTDOWN, vm.state.value.step)
            assertEquals(3, vm.state.value.countdownRemaining)

            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(1, vm.state.value.countdownRemaining)
            assertFalse(tracker.calls.any { it.startsWith("start") })

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals("start:new", tracker.calls.last())
            assertEquals(RunStep.RECORDING, vm.state.value.step)
        }

    @Test
    fun `카운트다운 중 화면을 탭하면 바로 시작`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            vm.onAction(device())
            vm.onAction(RunAction.StartClicked)

            vm.onAction(RunAction.CountdownTapped)
            runCurrent()
            advanceTimeBy(5_000)
            runCurrent()

            assertEquals(1, tracker.calls.count { it.startsWith("start") })
        }

    @Test
    fun `카운트다운 0초면 바로 시작`() =
        runTest {
            settings.runSettings.value = RunSettings(countdownSeconds = 0)
            val vm = viewModel()
            runCurrent()
            vm.onAction(device())

            vm.onAction(RunAction.StartClicked)
            runCurrent()

            assertEquals("start:new", tracker.calls.last())
        }

    @Test
    fun `시작에 실패하면 GPS 대기로 돌아가 오류를 알림`() =
        runTest {
            tracker.startResult = Result.failure(IllegalStateException())
            settings.runSettings.value = RunSettings(countdownSeconds = 0)
            val vm = viewModel()
            runCurrent()
            vm.onAction(device())

            vm.event.test {
                vm.onAction(RunAction.StartClicked)
                assertEquals(RunEvent.ShowError, awaitItem())
            }
            assertEquals(RunStep.GPS, vm.state.value.step)
        }

    @Test
    fun `이미 기록 중일 때 열면 기록 화면 (알림 탭)`() =
        runTest {
            tracker.state.value = FakeRunTracker.recording()

            val vm = viewModel()
            runCurrent()

            assertEquals(RunStep.RECORDING, vm.state.value.step)
            assertEquals(
                3_420.0,
                vm.state.value.recording!!
                    .metrics.distanceM,
                0.0,
            )
        }

    @Test
    fun `종료 길게 누르면 확인 시트, 저장하면 결과로`() =
        runTest {
            tracker.state.value = FakeRunTracker.recording()
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunAction.StopLongPressed)
            assertTrue(vm.state.value.showFinishSheet)

            vm.event.test {
                vm.onAction(RunAction.SaveClicked)
                assertEquals(RunEvent.OpenResult(FakeRunTracker.RUN_ID), awaitItem())
            }
            assertEquals("finish:true", tracker.calls.last())
        }

    @Test
    fun `저장하지 않고 마치면 흐름을 닫음`() =
        runTest {
            tracker.state.value = FakeRunTracker.recording(distanceM = 50.0)
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RunAction.DiscardClicked)
                assertEquals(RunEvent.Close, awaitItem())
            }
            assertEquals("finish:false", tracker.calls.last())
        }

    @Test
    fun `계속 달리기는 시트를 닫고 재개`() =
        runTest {
            tracker.state.value = FakeRunTracker.recording()
            val vm = viewModel()
            runCurrent()
            vm.onAction(RunAction.StopLongPressed)

            vm.onAction(RunAction.ContinueClicked)
            runCurrent()

            assertFalse(vm.state.value.showFinishSheet)
            assertEquals("resume", tracker.calls.last())
        }

    @Test
    fun `기록 중 뒤로 가기는 앱을 백그라운드로, 시트가 열려 있으면 시트만 닫음`() =
        runTest {
            tracker.state.value = FakeRunTracker.recording()
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RunAction.BackPressed)
                assertEquals(RunEvent.MoveToBackground, awaitItem())

                vm.onAction(RunAction.StopLongPressed)
                vm.onAction(RunAction.BackPressed)
                expectNoEvents()
            }
            assertFalse(vm.state.value.showFinishSheet)
        }

    @Test
    fun `준비 중 뒤로 가기는 위치 요청을 끊고 닫음`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            vm.onAction(device())
            runCurrent()

            vm.event.test {
                vm.onAction(RunAction.BackPressed)
                assertEquals(RunEvent.Close, awaitItem())
            }
            runCurrent()
            assertEquals("cancelPreparing", tracker.calls.last())
        }

    @Test
    fun `잠그고 길게 눌러 풀기`() =
        runTest {
            tracker.state.value = FakeRunTracker.recording()
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunAction.LockClicked)
            assertTrue(vm.state.value.locked)

            vm.onAction(RunAction.UnlockLongPressed)
            assertFalse(vm.state.value.locked)
        }

    @Test
    fun `복구할 기록이 있으면 다이얼로그, 24시간 넘었으면 오래된 기록 표시`() =
        runTest {
            tracker.recoverable = RecoverableRun("old", clock.millis() - 25 * HOUR_MS, 3_420.0, 1_231_000L)

            val vm = viewModel(recoverRunId = "old")
            runCurrent()

            assertEquals(RunStep.RECOVER, vm.state.value.step)
            assertTrue(vm.state.value.isOldRecovery)
        }

    @Test
    fun `이어서 달리기는 준비를 거쳐 카운트다운 없이 같은 기록으로 시작`() =
        runTest {
            tracker.recoverable = RecoverableRun("r1", clock.millis() - HOUR_MS, 3_420.0, 1_231_000L)
            val vm = viewModel(recoverRunId = "r1")
            runCurrent()
            assertFalse(vm.state.value.isOldRecovery)
            vm.onAction(device())

            vm.onAction(RunAction.RecoverContinueClicked)
            runCurrent()
            assertEquals(RunStep.GPS, vm.state.value.step)

            vm.onAction(RunAction.StartClicked)
            runCurrent()

            assertEquals("start:r1", tracker.calls.last())
        }

    @Test
    fun `복구 기록을 저장하고 마치면 결과로, 버리면 닫음`() =
        runTest {
            tracker.recoverable = RecoverableRun("r1", clock.millis(), 3_420.0, 1_231_000L)
            val vm = viewModel(recoverRunId = "r1")
            runCurrent()

            vm.event.test {
                vm.onAction(RunAction.RecoverSaveClicked)
                assertEquals(RunEvent.OpenResult("r1"), awaitItem())
            }
            assertEquals("finishRecovered:r1:true", tracker.calls.last())
        }

    @Test
    fun `복구 대상이 이미 없으면 새 러닝 준비로`() =
        runTest {
            val vm = viewModel(recoverRunId = "gone")
            runCurrent()
            vm.onAction(device())
            runCurrent()

            assertEquals(RunStep.GPS, vm.state.value.step)
            assertEquals(listOf("prepare"), tracker.calls)
        }

    private companion object {
        const val HOUR_MS = 3_600_000L
    }
}
