package jpark.bro.pacenote.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

class NavigatorTabTest {
    private data class Key(
        val name: String,
    ) : NavKey

    private val home = Key("home")
    private val record = Key("record")
    private val detail = Key("detail")
    private val run = Key("run")

    private fun navigator() = Navigator(home, setOf(record))

    @Test
    fun `다른 탭으로 가면 시작 탭 스택 위에 그 탭 스택이 보임`() {
        val navigator = navigator()

        navigator.goTo(record)
        navigator.goTo(detail)

        assertEquals(listOf(home, record, detail), navigator.backStack)
        assertEquals(record, navigator.currentTab)
    }

    @Test
    fun `탭을 바꿔도 각 탭의 화면 위치는 유지됨`() {
        val navigator = navigator()
        navigator.goTo(record)
        navigator.goTo(detail)

        navigator.goTo(home)
        navigator.goTo(record)

        assertEquals(listOf(home, record, detail), navigator.backStack)
    }

    @Test
    fun `탭의 첫 화면에서 뒤로 가면 시작 탭으로`() {
        val navigator = navigator()
        navigator.goTo(record)

        navigator.goBack()

        assertEquals(home, navigator.currentTab)
        assertEquals(listOf(home), navigator.backStack)
    }

    @Test
    fun `탭의 첫 화면일 때만 탭바를 보여줌`() {
        val navigator = navigator()
        navigator.goTo(record)
        assertTrue(navigator.isAtTabRoot)

        navigator.goTo(detail)

        assertFalse(navigator.isAtTabRoot)
    }

    @Test
    fun `replaceAll은 모든 탭을 첫 화면으로 되돌리고 시작 탭 위에 쌓음`() {
        val navigator = navigator()
        navigator.goTo(record)
        navigator.goTo(detail)

        navigator.replaceAll(home, run)

        assertEquals(listOf(home, run), navigator.backStack)
        navigator.goTo(record)
        assertEquals(listOf(home, run, record), navigator.backStack)
    }

    @Test
    fun `replaceAll의 첫 키가 탭 키가 아니면 시작 탭 위에 쌓음`() {
        val navigator = navigator()
        navigator.goTo(record)

        navigator.replaceAll(run)

        assertEquals(home, navigator.currentTab)
        assertEquals(listOf(home, run), navigator.backStack)
    }

    @Test
    fun `replaceTop은 탭의 첫 화면을 지우지 않음`() {
        val navigator = navigator()
        navigator.goTo(record)

        navigator.replaceTop(detail)

        assertEquals(listOf(home, record, detail), navigator.backStack)
    }
}
