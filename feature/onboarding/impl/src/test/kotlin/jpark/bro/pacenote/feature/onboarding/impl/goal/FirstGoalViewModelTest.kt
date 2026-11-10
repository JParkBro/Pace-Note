package jpark.bro.pacenote.feature.onboarding.impl.goal

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.testing.fake.FakeGoalRepository
import jpark.bro.pacenote.core.testing.fake.FakeOnboardingRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class FirstGoalViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val goals = FakeGoalRepository()
    private val onboarding = FakeOnboardingRepository()

    private fun viewModel() = FirstGoalViewModel(goals, onboarding, FakeSettingsRepository())

    @Test
    fun `주간 15km부터, 횟수로 바꾸면 주 3회`() =
        runTest {
            val vm = viewModel()
            runCurrent()
            assertEquals(15, vm.state.value.amount)

            vm.onAction(FirstGoalAction.TypeSelected(GoalType.COUNT))

            assertEquals(3, vm.state.value.amount)
        }

    @Test
    fun `시작하기는 목표를 저장하고 온보딩을 끝냄`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(FirstGoalAction.IncreaseClicked)
                vm.onAction(FirstGoalAction.StartClicked)
                assertEquals(FirstGoalEvent.Done, awaitItem())
            }
            assertEquals(GoalType.DISTANCE to 16_000.0, goals.lastSet)
            assertTrue(onboarding.onboardingCompleted.value)
        }

    @Test
    fun `건너뛰면 목표 없이 온보딩을 끝냄`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(FirstGoalAction.SkipClicked)
                assertEquals(FirstGoalEvent.Done, awaitItem())
            }
            assertNull(goals.lastSet)
            assertTrue(onboarding.onboardingCompleted.value)
        }

    @Test
    fun `목표 저장에 실패하면 온보딩을 끝내지 않음`() =
        runTest {
            goals.writeResult = Result.failure(IllegalStateException())
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(FirstGoalAction.StartClicked)
                assertEquals(FirstGoalEvent.ShowError, awaitItem())
            }
            assertFalse(onboarding.onboardingCompleted.value)
        }
}
