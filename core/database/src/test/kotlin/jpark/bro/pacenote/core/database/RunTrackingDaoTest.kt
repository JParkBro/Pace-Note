package jpark.bro.pacenote.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.database.dao.FinishedRunUpdate
import jpark.bro.pacenote.core.database.dao.PointSummary
import jpark.bro.pacenote.core.database.dao.RoutePointRow
import jpark.bro.pacenote.core.database.dao.RunProgressUpdate
import jpark.bro.pacenote.core.database.entity.RunPointEntity
import jpark.bro.pacenote.core.database.entity.SyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RunTrackingDaoTest : DatabaseTest() {
    private val dao get() = db.runTrackingDao()
    private val reads get() = db.runRecoveryDao()

    private fun point(
        seq: Int,
        segment: Int = 0,
        accepted: Boolean = true,
    ) = RunPointEntity("r", seq, seq.toDouble(), 0.0, null, 5f, null, 1_000L + seq, segment, accepted)

    @Test
    fun `flush는 점과 구간을 넣고 누적값을 고침`() =
        runTest {
            dao.insertRun(testRun("r", status = "ACTIVE"))

            dao.flush(
                listOf(point(0), point(1)),
                listOf(testSplit("r", 1)),
                RunProgressUpdate("r", 1_020.0, 310_000, 9),
            )

            val run = reads.findRun("r")!!
            assertEquals(1_020.0, run.distanceM, 0.0)
            assertEquals(310_000L, run.movingMs)
            assertEquals(1, reads.pointSummary("r").maxSeq)
            assertEquals(1, reads.splits("r").size)
        }

    @Test
    fun `일시정지마다 pause_count가 늘어남`() =
        runTest {
            dao.insertRun(testRun("r", status = "ACTIVE"))

            dao.updateStatus("r", "PAUSED", pauseIncrement = 1, updatedAt = 5)
            dao.updateStatus("r", "ACTIVE", pauseIncrement = 0, updatedAt = 6)
            dao.updateStatus("r", "PAUSED", pauseIncrement = 1, updatedAt = 7)

            assertEquals(2, reads.findRun("r")!!.pauseCount)
            assertEquals("PAUSED", reads.findRun("r")!!.status)
        }

    @Test
    fun `진행 중 기록은 ACTIVE와 PAUSED 중 가장 최근 것`() =
        runTest {
            dao.insertRun(testRun("done", startedAt = 3))
            dao.insertRun(testRun("old", status = "PAUSED", startedAt = 1))
            dao.insertRun(testRun("new", status = "ACTIVE", startedAt = 2, owner = null))

            assertEquals("new", reads.findInProgress()!!.id)
        }

    @Test
    fun `점이 없으면 요약은 모두 null`() =
        runTest {
            dao.insertRun(testRun("r", status = "ACTIVE"))

            assertEquals(PointSummary(null, null, null), reads.pointSummary("r"))
        }

    @Test
    fun `진행 중 기록이 없으면 null`() =
        runTest {
            dao.insertRun(testRun("done"))

            assertNull(reads.findInProgress())
        }

    @Test
    fun `채택된 점만 순서대로, 최대 segment와 마지막 시각`() =
        runTest {
            dao.insertRun(testRun("r", status = "ACTIVE"))
            dao.insertPoints(listOf(point(2, segment = 1), point(0), point(1, accepted = false)))

            assertEquals(listOf(RoutePointRow(0.0, 0.0, 0), RoutePointRow(2.0, 0.0, 1)), reads.acceptedPoints("r"))
            assertEquals(PointSummary(maxSeq = 2, maxSegment = 1, lastRecordedAt = 1_002L), reads.pointSummary("r"))
        }

    @Test
    fun `finish는 구간을 전체 목록으로 바꾸고 FINISHED로 저장`() =
        runTest {
            dao.insertRun(testRun("r", status = "ACTIVE"))
            dao.upsertSplits(listOf(testSplit("r", 1)))

            dao.finish(
                listOf(testSplit("r", 1), testSplit("r", 2)),
                FinishedRunUpdate(
                    "r",
                    "FINISHED",
                    9_000,
                    1_500.0,
                    450_000,
                    460_000,
                    100,
                    "abc",
                    "[3]",
                    9_000,
                    SyncState.LOCAL.name,
                ),
            )

            val run = reads.findRun("r")!!
            assertEquals("FINISHED", run.status)
            assertEquals("abc", run.routePolyline)
            assertEquals(100, run.calories)
            assertEquals(2, reads.splits("r").size)
        }
}
