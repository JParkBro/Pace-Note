package jpark.bro.pacenote.core.domain.route

import jpark.bro.pacenote.core.model.RoutePoint
import kotlin.math.roundToLong

/** Google Encoded Polyline (정밀도 1e-5). segment는 문자열에 담지 않고 점 개수 배열로 따로 저장한다. */
object PolylineCodec {
    private const val PRECISION = 1e5
    private const val ASCII_OFFSET = 63
    private const val CHUNK_BITS = 5
    private const val CHUNK_MASK = 0x1f
    private const val CONTINUATION = 0x20

    fun encode(points: List<RoutePoint>): String =
        buildString {
            var lastLat = 0L
            var lastLng = 0L
            points.forEach { point ->
                val lat = (point.lat * PRECISION).roundToLong()
                val lng = (point.lng * PRECISION).roundToLong()
                appendValue(lat - lastLat)
                appendValue(lng - lastLng)
                lastLat = lat
                lastLng = lng
            }
        }

    /** @param segmentCounts segment별 점 개수. 점 개수의 합이 맞지 않으면 예외 */
    fun decode(
        encoded: String,
        segmentCounts: List<Int>,
    ): List<RoutePoint> {
        val coordinates = decodeValues(encoded).chunked(2)
        require(coordinates.size == segmentCounts.sum()) { "점 개수 불일치: ${coordinates.size}" }
        val segments = segmentCounts.flatMapIndexed { segment, count -> List(count) { segment } }
        var lat = 0L
        var lng = 0L
        return coordinates.zip(segments) { (deltaLat, deltaLng), segment ->
            lat += deltaLat
            lng += deltaLng
            RoutePoint(lat / PRECISION, lng / PRECISION, segment)
        }
    }

    private fun StringBuilder.appendValue(value: Long) {
        var rest = (value shl 1) xor (value shr Long.SIZE_BITS - 1)
        while (rest >= CONTINUATION) {
            append(((rest and CHUNK_MASK.toLong()).toInt() or CONTINUATION).plus(ASCII_OFFSET).toChar())
            rest = rest shr CHUNK_BITS
        }
        append((rest.toInt() + ASCII_OFFSET).toChar())
    }

    private fun decodeValues(encoded: String): List<Long> {
        val values = mutableListOf<Long>()
        var shift = 0
        var result = 0L
        encoded.forEach { char ->
            val chunk = char.code - ASCII_OFFSET
            result = result or ((chunk and CHUNK_MASK).toLong() shl shift)
            shift += CHUNK_BITS
            if (chunk < CONTINUATION) {
                values += (result shr 1) xor -(result and 1)
                shift = 0
                result = 0L
            }
        }
        return values
    }
}
