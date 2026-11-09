package jpark.bro.pacenote.core.common

/** 기기 시각 변경에 흔들리지 않는 단조 시계. Android 구현은 `SystemClock.elapsedRealtime`을 감싼다. */
fun interface ElapsedClock {
    fun elapsedRealtimeMs(): Long
}
