package jpark.bro.pacenote.core.testing.fake

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/** 시각을 고정 · 이동할 수 있는 Clock. 기본은 샘플 시나리오의 오늘 2026.10.29 (목) 오전 9:00 KST. */
class FakeClock(
    var now: Instant = SAMPLE_NOW,
    private val zoneId: ZoneId = SEOUL,
) : Clock() {
    override fun getZone(): ZoneId = zoneId

    override fun withZone(zone: ZoneId): Clock = FakeClock(now, zone)

    override fun instant(): Instant = now

    companion object {
        val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")
        val SAMPLE_NOW: Instant = ZonedDateTime.of(2026, 10, 29, 9, 0, 0, 0, SEOUL).toInstant()
    }
}
