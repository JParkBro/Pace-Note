package jpark.bro.pacenote.feature.settings.impl.profile

import app.cash.turbine.test
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val settings =
        FakeSettingsRepository().apply { profile.value = UserProfile(weightKg = 65.0, distanceUnit = DistanceUnit.KM) }

    private fun viewModel() = ProfileEditViewModel(settings)

    @Test
    fun `저장된 체중 · 단위로 시작`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            assertEquals("65", vm.state.value.weightText)
            assertEquals(DistanceUnit.KM, vm.state.value.distanceUnit)
        }

    @Test
    fun `체중 · 단위를 바꿔 저장`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(ProfileEditAction.WeightChanged("70.5kg"))
                vm.onAction(ProfileEditAction.UnitSelected(DistanceUnit.MILE))
                vm.onAction(ProfileEditAction.SaveClicked)
                assertEquals(ProfileEditEvent.Close, awaitItem())
            }
            assertEquals(UserProfile(weightKg = 70.5, distanceUnit = DistanceUnit.MILE), settings.profile.value)
        }

    @Test
    fun `체중을 비우면 체중 없이 저장`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.event.test {
                vm.onAction(ProfileEditAction.WeightChanged(""))
                vm.onAction(ProfileEditAction.SaveClicked)
                assertEquals(ProfileEditEvent.Close, awaitItem())
            }
            assertEquals(null, settings.profile.value.weightKg)
        }

    @Test
    fun `20 ~ 300kg 밖이면 저장하지 않고 안내`() =
        runTest {
            val vm = viewModel()
            runCurrent()

            vm.onAction(ProfileEditAction.WeightChanged("650"))
            vm.onAction(ProfileEditAction.SaveClicked)

            assertTrue(vm.state.value.weightInvalid)
            assertEquals(65.0, settings.profile.value.weightKg!!, 0.0)
        }
}
