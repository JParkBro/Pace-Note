package jpark.bro.pacenote.core.tracking.voice

import android.content.res.Resources
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.VoiceAnnouncement
import jpark.bro.pacenote.core.model.VoiceItem
import jpark.bro.pacenote.core.tracking.R
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

/**
 * 음성 안내 문장: "1킬로미터. 5분 52초. 평균 페이스 5분 52초." (product-spec.md 2-5).
 * 항목 순서는 거리 · 시간 · 평균 페이스 · 구간 페이스로 고정하고, 값이 없는 페이스는 빼고 읽는다.
 */
internal class VoiceScript(
    private val resources: Resources,
) {
    /** 읽을 것이 없으면 null */
    fun text(
        announcement: VoiceAnnouncement,
        unit: DistanceUnit,
    ): String? =
        when (announcement) {
            VoiceAnnouncement.AutoPaused -> resources.getString(R.string.tracking_voice_auto_paused)
            VoiceAnnouncement.AutoResumed -> resources.getString(R.string.tracking_voice_auto_resumed)
            is VoiceAnnouncement.Progress -> progress(announcement, unit)
        }

    private fun progress(
        announcement: VoiceAnnouncement.Progress,
        unit: DistanceUnit,
    ): String? =
        VoiceItem.entries
            .filter { it in announcement.items }
            .mapNotNull { item ->
                when (item) {
                    VoiceItem.DISTANCE -> {
                        distance(announcement.distanceM, unit)
                    }

                    VoiceItem.TIME -> {
                        duration(announcement.movingMs / MS_PER_SECOND)
                    }

                    VoiceItem.AVERAGE_PACE -> {
                        announcement.averagePaceSecPerKm?.let {
                            resources.getString(R.string.tracking_voice_average_pace, pace(it, unit))
                        }
                    }

                    VoiceItem.SPLIT_PACE -> {
                        announcement.splitPaceSecPerKm?.let {
                            resources.getString(R.string.tracking_voice_split_pace, pace(it, unit))
                        }
                    }
                }
            }.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ". ", postfix = ".")

    /** 소수 둘째 자리에서 반올림, 끝의 0은 읽지 않는다: 1.0 → "1", 0.5 → "0.5" */
    private fun distance(
        meters: Double,
        unit: DistanceUnit,
    ): String {
        val value =
            BigDecimal(meters / unitMeters(unit))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
        val res =
            if (unit ==
                DistanceUnit.KM
            ) {
                R.string.tracking_voice_distance_km
            } else {
                R.string.tracking_voice_distance_mile
            }
        return resources.getString(res, value)
    }

    /** km당 초 → 단위당 "5분 52초" */
    private fun pace(
        secPerKm: Double,
        unit: DistanceUnit,
    ): String = duration((secPerKm * unitMeters(unit) / METERS_PER_KM).roundToLong())

    /** "1시간 2분 3초". 0인 단위는 읽지 않는다 (모두 0이면 "0초") */
    private fun duration(totalSeconds: Long): String {
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = totalSeconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        val parts =
            listOfNotNull(
                hours.takeIf { it > 0 }?.let { resources.getString(R.string.tracking_voice_hours, it.toInt()) },
                minutes.takeIf { it > 0 }?.let { resources.getString(R.string.tracking_voice_minutes, it.toInt()) },
                seconds
                    .takeIf {
                        it > 0 || totalSeconds == 0L
                    }?.let { resources.getString(R.string.tracking_voice_seconds, it.toInt()) },
            )
        return parts.joinToString(" ")
    }

    private fun unitMeters(unit: DistanceUnit): Double =
        when (unit) {
            DistanceUnit.KM -> METERS_PER_KM
            DistanceUnit.MILE -> METERS_PER_MILE
        }

    private companion object {
        const val MS_PER_SECOND = 1_000L
        const val SECONDS_PER_MINUTE = 60L
        const val SECONDS_PER_HOUR = 3_600L
        const val METERS_PER_KM = 1_000.0
        const val METERS_PER_MILE = 1_609.344
    }
}
