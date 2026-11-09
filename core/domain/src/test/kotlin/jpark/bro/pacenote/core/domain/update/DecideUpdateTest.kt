package jpark.bro.pacenote.core.domain.update

import jpark.bro.pacenote.core.model.UpdateDecision
import jpark.bro.pacenote.core.model.UpdatePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class DecideUpdateTest {
    private val day = 86_400_000L
    private val policy = UpdatePolicy(forceMinVersionCode = 100, softMinVersionCode = 200, softIntervalDays = 3)

    @Test
    fun `강제 기준 미만이면 강제`() {
        assertEquals(UpdateDecision.FORCE, decideUpdate(99, policy, lastPromptAt = null, now = 0))
    }

    @Test
    fun `강제 기준과 같으면 강제 아님`() {
        assertEquals(UpdateDecision.SOFT, decideUpdate(100, policy, lastPromptAt = null, now = 0))
    }

    @Test
    fun `선택 기준 이상이면 안내 없음`() {
        assertEquals(UpdateDecision.NONE, decideUpdate(200, policy, lastPromptAt = null, now = 0))
    }

    @Test
    fun `안내 간격이 안 지나면 선택 안내 없음`() {
        assertEquals(UpdateDecision.NONE, decideUpdate(150, policy, lastPromptAt = 0, now = 3 * day - 1))
    }

    @Test
    fun `안내 간격이 지나면 선택 안내`() {
        assertEquals(UpdateDecision.SOFT, decideUpdate(150, policy, lastPromptAt = 0, now = 3 * day))
    }

    @Test
    fun `강제는 안내 간격과 무관`() {
        assertEquals(UpdateDecision.FORCE, decideUpdate(99, policy, lastPromptAt = 0, now = 1))
    }
}
