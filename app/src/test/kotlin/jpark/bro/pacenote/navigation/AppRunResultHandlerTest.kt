package jpark.bro.pacenote.navigation

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.run.api.RunKey
import jpark.bro.pacenote.feature.run.api.RunResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AppRunResultHandlerTest {
    @Test
    fun `결과에서 완료하면 러닝 흐름을 지우고 홈만 남김`() {
        val navigator = Navigator(HomeKey)
        navigator.goTo(RunKey())

        AppRunResultHandler(navigator).handle(RunResult.Finished)

        assertEquals(listOf(HomeKey), navigator.backStack.toList())
    }
}
