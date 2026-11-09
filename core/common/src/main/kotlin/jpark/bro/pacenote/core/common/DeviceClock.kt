package jpark.bro.pacenote.core.common

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * 기기 시각 · 시간대. `Clock.systemDefaultZone()`은 만든 순간의 시간대에 고정되므로,
 * 앱이 떠 있는 동안 시간대를 바꿔도 따라가도록 매번 기기 시간대를 읽는다.
 */
object DeviceClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = system(zone)

    override fun instant(): Instant = Instant.now()

    override fun millis(): Long = System.currentTimeMillis()
}
