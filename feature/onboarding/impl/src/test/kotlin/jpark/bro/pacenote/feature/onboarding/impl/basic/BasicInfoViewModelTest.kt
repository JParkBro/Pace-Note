package jpark.bro.pacenote.feature.onboarding.impl.basic

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BasicInfoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val settings = FakeSettingsRepository()

    private fun viewModel() = BasicInfoViewModel(settings)

    @Test
    fun `체중 · 단위를 저장하고 첫 목표로`() =
        runTest {
            val vm = viewModel()

            vm.event.test {
                vm.onAction(BasicInfoAction.WeightChanged("65"))
                vm.onAction(BasicInfoAction.UnitSelected(DistanceUnit.MILE))
                vm.onAction(BasicInfoAction.NextClicked)
                assertEquals(BasicInfoEvent.OpenGoal, awaitItem())
            }
            assertEquals(UserProfile(65.0, DistanceUnit.MILE), settings.profile.value)
        }

    @Test
    fun `건너뛰면 저장하지 않고 첫 목표로`() =
        runTest {
            val vm = viewModel()

            vm.event.test {
                vm.onAction(BasicInfoAction.WeightChanged("70"))
                vm.onAction(BasicInfoAction.SkipClicked)
                assertEquals(BasicInfoEvent.OpenGoal, awaitItem())
            }
            assertNull(settings.profile.value.weightKg)
        }

    @Test
    fun `체중이 범위 밖이면 저장하지 않고 안내`() =
        runTest {
            val vm = viewModel()

            vm.onAction(BasicInfoAction.WeightChanged("650"))
            vm.onAction(BasicInfoAction.NextClicked)

            assertTrue(vm.state.value.weightInvalid)
            assertNull(settings.profile.value.weightKg)
        }
}
