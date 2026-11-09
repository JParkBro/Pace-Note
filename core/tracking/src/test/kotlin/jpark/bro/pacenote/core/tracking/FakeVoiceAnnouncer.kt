package jpark.bro.pacenote.core.tracking

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.VoiceAnnouncement
import jpark.bro.pacenote.core.tracking.voice.VoiceAnnouncer

internal class FakeVoiceAnnouncer : VoiceAnnouncer {
    var prepared = false
        private set
    val announced = mutableListOf<VoiceAnnouncement>()

    override fun prepare() {
        prepared = true
    }

    override fun announce(
        announcement: VoiceAnnouncement,
        unit: DistanceUnit,
    ) {
        announced += announcement
    }

    override fun release() {
        prepared = false
    }
}
