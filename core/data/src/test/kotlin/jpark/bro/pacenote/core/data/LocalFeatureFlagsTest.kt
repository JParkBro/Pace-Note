package jpark.bro.pacenote.core.data

import jpark.bro.pacenote.core.data.repository.LocalFeatureFlags
import jpark.bro.pacenote.core.data.repository.UpdatePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFeatureFlagsTest {
    private val flags = LocalFeatureFlags()

    @Test
    fun `차단 스위치 backup_enabled 기본값은 켜짐`() {
        assertTrue(flags.isBackupEnabled)
    }

    @Test
    fun `업데이트 기준 기본값은 어떤 버전도 안내하지 않음`() {
        assertEquals(
            UpdatePolicy(forceMinVersionCode = 1, softMinVersionCode = 1, softIntervalDays = 3),
            flags.updatePolicy,
        )
    }
}
