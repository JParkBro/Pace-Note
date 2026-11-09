package jpark.bro.pacenote.core.tracking

import android.os.SystemClock
import jpark.bro.pacenote.core.common.ElapsedClock

/** 부팅 후 경과 시간. 기기 시각 · 시간대 변경에 흔들리지 않는다 (tracking.md 1장). */
internal object SystemElapsedClock : ElapsedClock {
    override fun elapsedRealtimeMs(): Long = SystemClock.elapsedRealtime()
}
