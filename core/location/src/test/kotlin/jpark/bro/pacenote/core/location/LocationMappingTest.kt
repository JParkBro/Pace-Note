package jpark.bro.pacenote.core.location

import android.location.Location
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationMappingTest {
    @Test
    fun `단조 시계는 나노초를 밀리초로, 없는 고도와 속도는 null`() {
        val location =
            Location("test").apply {
                latitude = 1.0
                longitude = 2.0
                accuracy = 7f
                time = 1_000L
                elapsedRealtimeNanos = 3_500_000_000L
            }

        val raw = location.toRawLocation()

        assertEquals(3_500L, raw.elapsedRealtimeMs)
        assertEquals(1_000L, raw.recordedAt)
        assertEquals(7f, raw.accuracyM)
        assertNull(raw.altitude)
        assertNull(raw.speedMps)
    }
}
