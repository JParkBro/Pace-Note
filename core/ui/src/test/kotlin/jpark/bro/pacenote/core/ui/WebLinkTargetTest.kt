package jpark.bro.pacenote.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class WebLinkTargetTest {
    private val page = "apps.example.dev"

    @Test
    fun `같은 사이트 https는 웹뷰 안에서 연다`() {
        assertEquals(WebLinkTarget.IN_PAGE, webLinkTarget(page, "https", page))
    }

    @Test
    fun `play 스토어 https 링크는 스토어로 넘긴다`() {
        assertEquals(WebLinkTarget.PLAY_STORE, webLinkTarget(page, "https", "play.google.com"))
    }

    @Test
    fun `market 링크는 스토어로 넘긴다`() {
        assertEquals(WebLinkTarget.PLAY_STORE, webLinkTarget(page, "market", "details"))
    }

    @Test
    fun `다른 사이트 https는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "https", "evil.example.com"))
    }

    @Test
    fun `같은 사이트라도 http는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "http", page))
    }

    @Test
    fun `play 스토어라도 http는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "http", "play.google.com"))
    }

    @Test
    fun `intent 링크는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "intent", null))
    }

    @Test
    fun `javascript 링크는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "javascript", null))
    }

    @Test
    fun `scheme이 없으면 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, null, null))
    }
}
