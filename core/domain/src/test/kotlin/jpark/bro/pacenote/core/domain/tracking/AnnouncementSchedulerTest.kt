package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.VoiceAnnouncement
import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.model.VoiceItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AnnouncementSchedulerTest {
    private val items = setOf(VoiceItem.DISTANCE, VoiceItem.AVERAGE_PACE)

    private fun scheduler(interval: VoiceInterval = VoiceInterval.KM_1) = AnnouncementScheduler(interval, items)

    @Test
    fun `1km 전에는 안내하지 않음`() {
        assertNull(scheduler().onProgress(999.9, 299_000))
    }

    @Test
    fun `1km를 넘으면 1km로 안내`() {
        val progress = requireNotNull(scheduler().onProgress(1_002.0, 301_000))

        assertEquals(1_000.0, progress.distanceM, 0.0)
        assertEquals(items, progress.items)
    }

    @Test
    fun `같은 간격은 한 번만 안내`() {
        val scheduler = scheduler().apply { onProgress(1_000.0, 300_000) }

        assertNull(scheduler.onProgress(1_500.0, 450_000))
    }

    @Test
    fun `0점5km 간격`() {
        val scheduler = scheduler(VoiceInterval.KM_0_5)

        assertNull(scheduler.onProgress(499.0, 150_000))
        assertNotNull(scheduler.onProgress(500.0, 151_000))
        assertNull(scheduler.onProgress(999.0, 300_000))
        assertNotNull(scheduler.onProgress(1_000.0, 301_000))
    }

    @Test
    fun `5분 간격은 운동 시간 기준`() {
        val scheduler = scheduler(VoiceInterval.MINUTES_5)

        assertNull(scheduler.onProgress(900.0, 299_999))
        assertEquals(300_000, requireNotNull(scheduler.onProgress(901.0, 300_500)).movingMs)
    }

    @Test
    fun `여러 간격을 한 번에 넘어도 한 번 안내하고 다음 간격부터 이어감`() {
        val scheduler = scheduler()

        assertEquals(3_000.0, requireNotNull(scheduler.onProgress(3_100.0, 900_000)).distanceM, 0.0)
        assertNull(scheduler.onProgress(3_900.0, 1_100_000))
        assertNotNull(scheduler.onProgress(4_000.0, 1_200_000))
    }

    @Test
    fun `구간 페이스는 직전 안내 이후 값`() {
        val scheduler = scheduler().apply { onProgress(1_000.0, 300_000) }

        val progress = requireNotNull(scheduler.onProgress(2_000.0, 660_000))

        assertEquals(360.0, requireNotNull(progress.splitPaceSecPerKm), 0.001)
        assertEquals(330.0, requireNotNull(progress.averagePaceSecPerKm), 0.001)
    }

    @Test
    fun `자동 일시정지와 재개를 안내`() {
        val scheduler = scheduler()

        assertEquals(VoiceAnnouncement.AutoPaused, scheduler.onAutoPauseChanged(true, 0))
        assertEquals(VoiceAnnouncement.AutoResumed, scheduler.onAutoPauseChanged(false, 10_000))
    }

    @Test
    fun `10초 안에 반복되면 생략`() {
        val scheduler = scheduler().apply { onAutoPauseChanged(true, 0) }

        assertNull(scheduler.onAutoPauseChanged(false, 9_999))
        assertNull(scheduler.onAutoPauseChanged(true, 15_000))
    }
}
