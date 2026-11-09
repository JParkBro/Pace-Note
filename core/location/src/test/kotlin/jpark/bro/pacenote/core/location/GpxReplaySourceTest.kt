package jpark.bro.pacenote.core.location

import jpark.bro.pacenote.core.common.ElapsedClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class) // 가상 시간을 단조 시계로 쓰기 위해 testScheduler 사용
class GpxReplaySourceTest {
    private val track =
        listOf(
            GpxPoint(0.0, 0.0, null, null, timeMs = 0),
            GpxPoint(0.0001, 0.0, null, 12f, timeMs = 1_000),
            GpxPoint(0.0002, 0.0, null, 12f, timeMs = 3_000),
        )

    private fun TestScope.replay(speed: Double = 1.0) =
        GpxReplaySource(
            points = track,
            elapsedClock = ElapsedClock { testScheduler.currentTime },
            clock = Clock.fixed(Instant.ofEpochMilli(5_000), ZoneOffset.UTC),
            speed = speed,
        )

    @Test
    fun `원래 간격대로 보내고 시각은 재생 시점 값`() =
        runTest {
            val points = replay().locations().toList()

            assertEquals(listOf(0L, 1_000L, 3_000L), points.map { it.elapsedRealtimeMs })
            assertEquals(5_000L, points.first().recordedAt)
        }

    @Test
    fun `배속이면 간격이 줄어듦`() =
        runTest {
            val points = replay(speed = 10.0).locations().toList()

            assertEquals(listOf(0L, 100L, 300L), points.map { it.elapsedRealtimeMs })
        }

    @Test
    fun `정확도가 없는 점은 5m로 봄`() =
        runTest {
            assertEquals(listOf(5f, 12f, 12f), replay().locations().toList().map { it.accuracyM })
        }
}
