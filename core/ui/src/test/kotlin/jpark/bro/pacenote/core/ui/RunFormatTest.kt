package jpark.bro.pacenote.core.ui

import jpark.bro.pacenote.core.model.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class RunFormatTest {
    @Test
    fun `거리는 소수 둘째 자리에서 내림`() {
        assertEquals("5.21", RunFormat.distance(5_210.0, DistanceUnit.KM))
        assertEquals("5.21", RunFormat.distance(5_219.9, DistanceUnit.KM))
        assertEquals("0.00", RunFormat.distance(9.9, DistanceUnit.KM))
        assertEquals("12.40", RunFormat.distance(12_400.0, DistanceUnit.KM))
    }

    @Test
    fun `mile이면 1609_344m 기준`() {
        assertEquals("1.00", RunFormat.distance(1_609.344, DistanceUnit.MILE))
        assertEquals("3.23", RunFormat.distance(5_210.0, DistanceUnit.MILE))
    }

    @Test
    fun `1시간 미만은 분 초, 이상은 시 분 초`() {
        assertEquals("30:12", RunFormat.duration(1_812_000))
        assertEquals("0:59", RunFormat.duration(59_999))
        assertEquals("59:59", RunFormat.duration(3_599_999))
        assertEquals("1:00:00", RunFormat.duration(3_600_000))
        assertEquals("1:12:40", RunFormat.duration(4_360_000))
    }

    @Test
    fun `페이스는 프라임 기호, 초는 반올림`() {
        // 샘플: 5.21km를 30:12에 → 347.8초/km
        assertEquals("5′48″", RunFormat.pace(1_812.0 / 5.21, DistanceUnit.KM))
        assertEquals("6′00″", RunFormat.pace(359.6, DistanceUnit.KM))
        assertEquals("5′05″", RunFormat.pace(305.0, DistanceUnit.KM))
    }

    @Test
    fun `mile 페이스는 1_609344배`() {
        assertEquals("8′03″", RunFormat.pace(300.0, DistanceUnit.MILE))
    }

    @Test
    fun `페이스가 없으면 대시`() {
        assertEquals("–′––″", RunFormat.pace(null, DistanceUnit.KM))
        assertEquals("–′––″", RunFormat.pace(0.0, DistanceUnit.KM))
    }
}
