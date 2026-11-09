package jpark.bro.pacenote.core.location

import org.w3c.dom.Element
import java.io.InputStream
import java.time.Instant
import javax.xml.parsers.DocumentBuilderFactory

/** GPX 트랙 점 하나. [accuracyM]은 `<extensions><accuracy>`가 없으면 null */
data class GpxPoint(
    val lat: Double,
    val lng: Double,
    val altitude: Double?,
    val accuracyM: Float?,
    val timeMs: Long,
)

/** `<trkpt>`를 순서대로 읽는다. 시각(`<time>`)이 없는 점은 재생할 수 없어 건너뛴다. */
object GpxParser {
    fun parse(input: InputStream): List<GpxPoint> {
        val factory =
            DocumentBuilderFactory.newInstance().apply {
                // 외부 엔티티를 읽지 않는다 (XXE 방지)
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                isExpandEntityReferences = false
            }
        val nodes = factory.newDocumentBuilder().parse(input).getElementsByTagName("trkpt")
        return List(nodes.length) { nodes.item(it) as Element }.mapNotNull { it.toGpxPoint() }
    }

    private fun Element.toGpxPoint(): GpxPoint? {
        val time = childText("time") ?: return null
        return GpxPoint(
            lat = getAttribute("lat").toDouble(),
            lng = getAttribute("lon").toDouble(),
            altitude = childText("ele")?.toDoubleOrNull(),
            accuracyM = childText("accuracy")?.toFloatOrNull(),
            timeMs = Instant.parse(time).toEpochMilli(),
        )
    }

    private fun Element.childText(tag: String): String? =
        getElementsByTagName(tag)
            .item(0)
            ?.textContent
            ?.trim()
            ?.ifEmpty { null }
}
