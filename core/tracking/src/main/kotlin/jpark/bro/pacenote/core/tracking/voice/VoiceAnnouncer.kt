package jpark.bro.pacenote.core.tracking.voice

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.VoiceAnnouncement

/** 음성 안내 재생 (tracking.md 9장). 시점 판단은 `AnnouncementScheduler`가, 재생은 이것이 한다. */
internal interface VoiceAnnouncer {
    /** 기록을 시작할 때 TTS를 준비한다. 이미 준비했으면 아무것도 하지 않는다. */
    fun prepare()

    /** 준비가 안 됐거나 한국어 음성이 없으면 말하지 않는다 (기록은 음성 없이 계속). */
    fun announce(
        announcement: VoiceAnnouncement,
        unit: DistanceUnit,
    )

    /** 기록이 끝나면 TTS를 내린다. */
    fun release()
}
