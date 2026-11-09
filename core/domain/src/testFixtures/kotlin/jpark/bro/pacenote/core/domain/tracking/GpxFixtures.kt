package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RawLocation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.w3c.dom.Element
import java.io.InputStream
import java.time.Instant
import javax.xml.parsers.DocumentBuilderFactory

/** 재생 결과가 맞춰야 하는 값. 해당하지 않는 항목은 null이다. */
data class GpxExpected(
    val distanceM: Double?,
    val distanceTolerancePct: Double?,
    val maxDistanceM: Double?,
    val movingMs: Long?,
    val movingToleranceMs: Long?,
    val splitCount: Int?,
    val splitMovingMs: Long?,
    val splitToleranceMs: Long?,
    val autoPauseCount: Int,
    val segmentCount: Int,
)

/** `scripts/generate-gpx-fixtures.py`가 만든 fixture를 읽는다. */
object GpxFixtures {
    fun points(name: String): List<RawLocation> =
        resource("$name.gpx").use { stream ->
            val nodes =
                DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(stream)
                    .getElementsByTagName("trkpt")
            val elements = List(nodes.length) { nodes.item(it) as Element }
            val startMs = elements.firstOrNull()?.timeMs() ?: 0L
            elements.map { it.toRawLocation(startMs) }
        }

    fun expected(name: String): GpxExpected {
        val json = resource("$name.expected.json").use { Json.parseToJsonElement(it.reader().readText()).jsonObject }
        return GpxExpected(
            distanceM = json.long("distanceM")?.toDouble(),
            distanceTolerancePct = json.long("distanceTolerancePct")?.toDouble(),
            maxDistanceM = json.long("maxDistanceM")?.toDouble(),
            movingMs = json.long("movingMs"),
            movingToleranceMs = json.long("movingToleranceMs"),
            splitCount = json.long("splitCount")?.toInt(),
            splitMovingMs = json.long("splitMovingMs"),
            splitToleranceMs = json.long("splitToleranceMs"),
            autoPauseCount = requireNotNull(json.long("autoPauseCount")).toInt(),
            segmentCount = requireNotNull(json.long("segmentCount")).toInt(),
        )
    }

    private fun resource(file: String): InputStream =
        requireNotNull(GpxFixtures::class.java.getResourceAsStream("/gpx/$file")) { "fixture 없음: $file" }

    private fun JsonObject.long(key: String): Long? = get(key)?.jsonPrimitive?.long

    private fun Element.text(tag: String): String = getElementsByTagName(tag).item(0).textContent

    private fun Element.timeMs(): Long = Instant.parse(text("time")).toEpochMilli()

    private fun Element.toRawLocation(startMs: Long): RawLocation =
        RawLocation(
            lat = getAttribute("lat").toDouble(),
            lng = getAttribute("lon").toDouble(),
            altitude = null,
            accuracyM = text("accuracy").toFloat(),
            speedMps = null,
            recordedAt = timeMs(),
            elapsedRealtimeMs = timeMs() - startMs,
        )
}
