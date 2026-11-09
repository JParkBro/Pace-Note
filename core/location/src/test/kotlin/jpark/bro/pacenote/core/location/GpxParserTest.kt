package jpark.bro.pacenote.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GpxParserTest {
    private fun parse(xml: String) = GpxParser.parse(xml.byteInputStream())

    @Test
    fun `위치 시각 고도 정확도를 읽음`() {
        val points =
            parse(
                """
                <gpx><trk><trkseg>
                  <trkpt lat="37.5" lon="127.0"><ele>12.5</ele><time>2026-10-28T21:40:00Z</time>
                    <extensions><accuracy>8</accuracy></extensions></trkpt>
                </trkseg></trk></gpx>
                """.trimIndent(),
            )

        assertEquals(GpxPoint(37.5, 127.0, 12.5, 8f, 1_793_223_600_000L), points.single())
    }

    @Test
    fun `정확도와 고도가 없으면 null, 시각이 없는 점은 건너뜀`() {
        val points =
            parse(
                """
                <gpx><trk><trkseg>
                  <trkpt lat="0" lon="0"><time>2026-01-01T00:00:00Z</time></trkpt>
                  <trkpt lat="1" lon="1"></trkpt>
                </trkseg></trk></gpx>
                """.trimIndent(),
            )

        assertEquals(1, points.size)
        assertNull(points.single().accuracyM)
        assertNull(points.single().altitude)
    }

    @Test
    fun `DOCTYPE이 있는 파일은 읽지 않음`() {
        assertThrows(Exception::class.java) {
            parse("""<?xml version="1.0"?><!DOCTYPE gpx [<!ENTITY x SYSTEM "file:///etc/passwd">]><gpx>&x;</gpx>""")
        }
    }
}
