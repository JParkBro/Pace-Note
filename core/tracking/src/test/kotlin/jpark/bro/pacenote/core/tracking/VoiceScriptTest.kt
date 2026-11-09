package jpark.bro.pacenote.core.tracking

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.VoiceAnnouncement
import jpark.bro.pacenote.core.model.VoiceItem
import jpark.bro.pacenote.core.tracking.voice.VoiceScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceScriptTest {
    private val script = VoiceScript(ApplicationProvider.getApplicationContext<android.content.Context>().resources)

    private fun progress(
        items: Set<VoiceItem>,
        distanceM: Double = 1_000.0,
        movingMs: Long = 352_000,
        average: Double? = 352.0,
        split: Double? = 352.0,
    ) = VoiceAnnouncement.Progress(distanceM, movingMs, average, split, items)

    @Test
    fun `문서 예시 문장과 같음`() {
        val text =
            script.text(progress(setOf(VoiceItem.DISTANCE, VoiceItem.TIME, VoiceItem.AVERAGE_PACE)), DistanceUnit.KM)

        assertEquals("1킬로미터. 5분 52초. 평균 페이스 5분 52초.", text)
    }

    @Test
    fun `항목 순서는 고정, 기본값은 거리와 평균 페이스`() {
        val text = script.text(progress(setOf(VoiceItem.AVERAGE_PACE, VoiceItem.DISTANCE)), DistanceUnit.KM)

        assertEquals("1킬로미터. 평균 페이스 5분 52초.", text)
    }

    @Test
    fun `0_5km 간격은 0_5킬로미터, 1시간 넘으면 시간까지`() {
        val text =
            script.text(
                progress(setOf(VoiceItem.DISTANCE, VoiceItem.TIME), distanceM = 500.0, movingMs = 3_723_000),
                DistanceUnit.KM,
            )

        assertEquals("0.5킬로미터. 1시간 2분 3초.", text)
    }

    @Test
    fun `0인 단위는 읽지 않음`() {
        val text = script.text(progress(setOf(VoiceItem.TIME), movingMs = 300_000), DistanceUnit.KM)

        assertEquals("5분.", text)
    }

    @Test
    fun `mile이면 마일 단위로 읽고 페이스도 마일당`() {
        val text =
            script.text(
                progress(setOf(VoiceItem.DISTANCE, VoiceItem.AVERAGE_PACE), distanceM = 1_609.344, average = 300.0),
                DistanceUnit.MILE,
            )

        assertEquals("1마일. 평균 페이스 8분 3초.", text)
    }

    @Test
    fun `페이스가 없으면 빼고, 읽을 것이 없으면 null`() {
        assertEquals(
            "1킬로미터.",
            script.text(progress(setOf(VoiceItem.DISTANCE, VoiceItem.SPLIT_PACE), split = null), DistanceUnit.KM),
        )
        assertNull(script.text(progress(setOf(VoiceItem.AVERAGE_PACE), average = null), DistanceUnit.KM))
    }

    @Test
    fun `자동 일시정지와 재개`() {
        assertEquals("일시정지", script.text(VoiceAnnouncement.AutoPaused, DistanceUnit.KM))
        assertEquals("다시 시작", script.text(VoiceAnnouncement.AutoResumed, DistanceUnit.KM))
    }
}
