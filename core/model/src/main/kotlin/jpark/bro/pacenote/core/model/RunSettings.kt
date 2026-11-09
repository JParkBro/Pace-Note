package jpark.bro.pacenote.core.model

/** 러닝 설정(M3). 기기별 설정이라 서버에 올리지 않는다. 기본값은 product-spec.md 2-3 · 2-5 · 5-3장. */
data class RunSettings(
    val voiceEnabled: Boolean = true,
    val voiceInterval: VoiceInterval = VoiceInterval.KM_1,
    val voiceItems: Set<VoiceItem> = setOf(VoiceItem.DISTANCE, VoiceItem.AVERAGE_PACE),
    val autoPauseEnabled: Boolean = true,
    val countdownSeconds: Int = DEFAULT_COUNTDOWN_SECONDS,
    val keepScreenOn: Boolean = false,
) {
    companion object {
        const val DEFAULT_COUNTDOWN_SECONDS = 3

        /** 카운트다운 선택지 (초) */
        val COUNTDOWN_OPTIONS = listOf(0, 3, 5, 10)
    }
}
