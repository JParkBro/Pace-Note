package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.common.ElapsedClock

/** 단조 시계. [advanceBy]로 시간을 흘린다. */
class FakeElapsedClock(
    var nowMs: Long = 0L,
) : ElapsedClock {
    fun advanceBy(ms: Long) {
        nowMs += ms
    }

    override fun elapsedRealtimeMs(): Long = nowMs
}
