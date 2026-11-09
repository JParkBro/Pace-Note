package jpark.bro.pacenote.core.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {
    private class CounterViewModel : MviViewModel<Int, String, String>(0) {
        override fun onAction(action: String) {
            setState { this + 1 }
            sendEvent("$action:$currentState")
        }
    }

    @Before
    fun setMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun resetMain() = Dispatchers.resetMain()

    @Test
    fun `Action으로 State가 바뀌고 Event는 수집자가 늦게 와도 한 번 전달됨`() =
        runTest {
            val viewModel = CounterViewModel()

            viewModel.onAction("a")

            assertEquals(1, viewModel.state.value)
            viewModel.event.test { assertEquals("a:1", awaitItem()) }
        }
}
