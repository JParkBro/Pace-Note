package jpark.bro.pacenote.feature.settings.impl.moreapps

import org.junit.Assert.assertEquals
import org.junit.Test

class MoreAppsUrlTest {
    @Test
    fun `출시 패키지 이름과 라이트 테마만 붙인다`() {
        assertEquals(
            "https://jparkbro-apps.jparkbro.workers.dev/?from=jpark.bro.pacenote&theme=light",
            moreAppsUrl("jpark.bro.pacenote", dark = false),
        )
    }

    @Test
    fun `다크 테마면 theme=dark`() {
        assertEquals(
            "https://jparkbro-apps.jparkbro.workers.dev/?from=jpark.bro.pacenote&theme=dark",
            moreAppsUrl("jpark.bro.pacenote", dark = true),
        )
    }
}
