package jpark.bro.pacenote.feature.settings.impl.my

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.model.ThemeSetting
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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val stats = FakeStatsRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel() = MyViewModel(stats, FakeGoalRepository(), settings, FakeClock())

    @Test
    fun `누적 거리 · 횟수를 보여주고 목표가 없으면 정하지 않음`() =
        runTest {
            stats.totals.value = RunTotals(312_500.0, 54)
            val vm = viewModel()
            runCurrent()

            assertEquals(312_500.0, vm.state.value.totalDistanceM, 0.0)
            assertEquals(54, vm.state.value.totalRuns)
            assertNull(vm.state.value.goal)
        }

    @Test
    fun `목표 · 러닝 설정 · 내 정보 수정으로 이동`() =
        runTest {
            val vm = viewModel()

            vm.event.test {
                vm.onAction(MyAction.GoalClicked)
                assertEquals(MyEvent.OpenGoal, awaitItem())
                vm.onAction(MyAction.RunSettingsClicked)
                assertEquals(MyEvent.OpenRunSettings, awaitItem())
                vm.onAction(MyAction.ProfileClicked)
                assertEquals(MyEvent.OpenProfile, awaitItem())
            }
        }

    @Test
    fun `테마 시트에서 고르면 앱 전체에 미리보기, 적용하면 저장`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(MyAction.ThemeClicked)
            runCurrent()
            assertEquals(ThemeSetting(), vm.state.value.themeSheet)

            vm.onAction(MyAction.ThemeTypeSelected(AppThemeType.RIVER))
            vm.onAction(MyAction.ThemeModeSelected(AppThemeMode.DARK))
            val chosen = ThemeSetting(AppThemeType.RIVER, AppThemeMode.DARK)
            assertEquals(chosen, settings.themePreview.value)

            vm.onAction(MyAction.ThemeApplyClicked)
            runCurrent()

            assertEquals(chosen, settings.savedTheme.value)
            assertNull(settings.themePreview.value)
            assertNull(vm.state.value.themeSheet)
        }

    @Test
    fun `적용하지 않고 닫으면 원래 테마로`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            vm.onAction(MyAction.ThemeClicked)
            runCurrent()

            vm.onAction(MyAction.ThemeTypeSelected(AppThemeType.INK))
            vm.onAction(MyAction.ThemeSheetDismissed)

            assertNull(settings.themePreview.value)
            assertEquals(ThemeSetting(), settings.savedTheme.value)
            assertNull(vm.state.value.themeSheet)
        }

    @Test
    fun `문의하기를 누르면 메일 앱으로`() =
        runTest {
            val vm = viewModel()

            vm.event.test {
                vm.onAction(MyAction.ContactClicked)
                assertEquals(MyEvent.OpenContact, awaitItem())
            }
        }
}
