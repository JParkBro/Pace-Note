package jpark.bro.pacenote.core.model

/** 음성 안내 간격. [distanceM]과 [durationMs] 중 하나만 값이 있다. */
enum class VoiceInterval(
    val distanceM: Double? = null,
    val durationMs: Long? = null,
) {
    KM_0_5(distanceM = 500.0),
    KM_1(distanceM = 1_000.0),
    KM_2(distanceM = 2_000.0),
    MINUTES_5(durationMs = 300_000L),
    MINUTES_10(durationMs = 600_000L),
}

enum class VoiceItem {
    DISTANCE,
    TIME,
    AVERAGE_PACE,
    SPLIT_PACE,
}

sealed interface VoiceAnnouncement {
    /** 간격에 도달했을 때 읽을 값. 페이스는 거리가 짧으면 null이다. */
    data class Progress(
        val distanceM: Double,
        val movingMs: Long,
        val averagePaceSecPerKm: Double?,
        val splitPaceSecPerKm: Double?,
        val items: Set<VoiceItem>,
    ) : VoiceAnnouncement

    data object AutoPaused : VoiceAnnouncement

    data object AutoResumed : VoiceAnnouncement
}
