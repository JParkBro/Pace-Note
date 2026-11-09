package jpark.bro.pacenote.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigatorTest {
    private data class Key(
        val name: String,
    ) : NavKey

    private val home = Key("home")
    private val run = Key("run")
    private val result = Key("result")

    @Test
    fun `이동하고 뒤로 가면 이전 화면`() {
        val navigator = Navigator(home)

        navigator.goTo(run)
        navigator.goBack()

        assertEquals(listOf(home), navigator.backStack.toList())
    }

    @Test
    fun `첫 화면에서 뒤로 가도 백스택은 비지 않음`() {
        val navigator = Navigator(home)

        navigator.goBack()

        assertEquals(listOf(home), navigator.backStack.toList())
    }

    @Test
    fun `replaceAll은 흐름 전체를 지우고 여러 화면으로 바꿀 수 있음`() {
        val navigator = Navigator(home)
        navigator.goTo(run)
        navigator.goTo(result)

        navigator.replaceAll(home, run)

        assertEquals(listOf(home, run), navigator.backStack.toList())
    }

    @Test
    fun `replaceTop은 현재 화면만 바꿈`() {
        val navigator = Navigator(home)
        navigator.goTo(run)

        navigator.replaceTop(result)

        assertEquals(listOf(home, result), navigator.backStack.toList())
    }
}
