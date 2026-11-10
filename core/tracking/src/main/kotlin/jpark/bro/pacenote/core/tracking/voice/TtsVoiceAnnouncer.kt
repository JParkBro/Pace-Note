package jpark.bro.pacenote.core.tracking.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.VoiceAnnouncement
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * TextToSpeech(한국어) 재생 (tracking.md 9장). 음악은 잠시 줄였다가(덕킹) 안내가 끝나면 되돌린다.
 * TTS 생성 · 재생 · 해제는 모두 메인 스레드에서 한다.
 */
internal class TtsVoiceAnnouncer(
    private val context: Context,
) : VoiceAnnouncer {
    private val script = VoiceScript(context.resources)
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val utteranceIds = AtomicInteger()
    private val attributes =
        AudioAttributes
            .Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    private val focusRequest =
        AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .build()

    private var tts: TextToSpeech? = null

    @Volatile private var ready = false

    override fun prepare() =
        mainExecutor.execute {
            if (tts != null) return@execute
            tts =
                TextToSpeech(context) { status ->
                    val engine = tts ?: return@TextToSpeech
                    val language =
                        if (status ==
                            TextToSpeech.SUCCESS
                        ) {
                            engine.setLanguage(Locale.KOREAN)
                        } else {
                            TextToSpeech.ERROR
                        }
                    // 한국어 음성 데이터가 없으면 음성 없이 기록을 이어간다 (설치 안내는 M3에서)
                    ready = language >= TextToSpeech.LANG_AVAILABLE
                }.apply {
                    setAudioAttributes(attributes)
                    setOnUtteranceProgressListener(FocusReleaser())
                }
        }

    override fun announce(
        announcement: VoiceAnnouncement,
        unit: DistanceUnit,
    ) {
        val text = script.text(announcement, unit) ?: return
        mainExecutor.execute {
            val engine = tts?.takeIf { ready } ?: return@execute
            audioManager?.requestAudioFocus(focusRequest)
            engine.speak(text, TextToSpeech.QUEUE_ADD, null, "voice-${utteranceIds.incrementAndGet()}")
        }
    }

    override fun release() =
        mainExecutor.execute {
            tts?.stop()
            tts?.shutdown()
            tts = null
            ready = false
            audioManager?.abandonAudioFocusRequest(focusRequest)
        }

    /** 줄 서 있는 안내가 모두 끝나면 오디오 포커스를 돌려준다. */
    private inner class FocusReleaser : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) = releaseIfIdle()

        @Deprecated("API 21에서 대체됨", ReplaceWith("onError(utteranceId, errorCode)"))
        override fun onError(utteranceId: String?) = releaseIfIdle()

        override fun onError(
            utteranceId: String?,
            errorCode: Int,
        ) = releaseIfIdle()

        private fun releaseIfIdle() =
            mainExecutor.execute {
                if (tts?.isSpeaking != true) audioManager?.abandonAudioFocusRequest(focusRequest)
            }
    }
}
