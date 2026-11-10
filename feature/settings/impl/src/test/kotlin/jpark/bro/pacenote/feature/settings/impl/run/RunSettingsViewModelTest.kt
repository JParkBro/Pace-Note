package jpark.bro.pacenote.feature.settings.impl.run

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.model.VoiceItem
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
class RunSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val settings = FakeSettingsRepository().apply { batteryGuideShown.value = true }

    private fun viewModel() = RunSettingsViewModel(settings)

    @Test
    fun `기본값은 음성 안내 켬 · 1km · 거리와 평균 페이스`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertEquals(RunSettings(), vm.state.value.settings)
            assertTrue(vm.state.value.settings.voiceEnabled)
        }

    @Test
    fun `바꾸면 바로 저장`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunSettingsAction.IntervalSelected(VoiceInterval.MINUTES_5))
            vm.onAction(RunSettingsAction.ItemToggled(VoiceItem.TIME))
            vm.onAction(RunSettingsAction.AutoPauseToggled(false))
            vm.onAction(RunSettingsAction.CountdownSelected(0))
            vm.onAction(RunSettingsAction.KeepScreenOnToggled(true))
            vm.onAction(RunSettingsAction.VoiceToggled(false))
            runCurrent()

            val saved = settings.runSettings.value
            assertEquals(VoiceInterval.MINUTES_5, saved.voiceInterval)
            assertEquals(setOf(VoiceItem.DISTANCE, VoiceItem.AVERAGE_PACE, VoiceItem.TIME), saved.voiceItems)
            assertFalse(saved.autoPauseEnabled)
            assertEquals(0, saved.countdownSeconds)
            assertTrue(saved.keepScreenOn)
            assertFalse(saved.voiceEnabled)
        }

    @Test
    fun `안내 항목은 하나 이상 남김`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(RunSettingsAction.ItemToggled(VoiceItem.DISTANCE))
            vm.onAction(RunSettingsAction.ItemToggled(VoiceItem.AVERAGE_PACE))
            runCurrent()

            assertEquals(setOf(VoiceItem.AVERAGE_PACE), settings.runSettings.value.voiceItems)
        }

    @Test
    fun `배터리 최적화 안내 다시 보기`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(RunSettingsAction.BatteryGuideResetClicked)
                assertEquals(RunSettingsEvent.ShowBatteryGuideReset, awaitItem())
            }
            assertFalse(settings.batteryGuideShown.value)
        }

    @Test
    fun `저장에 실패하면 오류 안내`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            settings.writeResult = Result.failure(IllegalStateException())

            vm.event.test {
                vm.onAction(RunSettingsAction.KeepScreenOnToggled(true))
                assertEquals(RunSettingsEvent.ShowError, awaitItem())
            }
        }
}
