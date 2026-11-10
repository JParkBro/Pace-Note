package jpark.bro.pacenote.core.tracking

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.database.PacenoteDatabase
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RawLocation
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.model.VoiceAnnouncement
import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.testing.fake.FakeAuthRepository
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeHomeWidgets
import jpark.bro.pacenote.core.testing.fake.FakeIdGenerator
import jpark.bro.pacenote.core.testing.fake.FakeLocationSource
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class) // 가상 시간을 단조 시계로 쓰기 위해 testScheduler 사용
@RunWith(AndroidJUnit4::class)
class TrackingEngineTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var db: PacenoteDatabase
    private val location = FakeLocationSource()
    private val settings = FakeSettingsRepository()
    private val auth = FakeAuthRepository()
    private val ids = FakeIdGenerator("run")
    private val voice = FakeVoiceAnnouncer()
    private val widgets = FakeHomeWidgets()

    /** 북쪽으로 1초에 4m씩 */
    private var northM = 0.0

    @Before
    fun createDb() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PacenoteDatabase::class.java)
                .allowMainThreadQueries()
                .setQueryCoroutineContext(dispatcher)
                .build()
    }

    @After
    fun closeDb() = db.close()

    private fun TestScope.engine() =
        TrackingEngine(
            locationSource = location,
            recorder = RunRecorder(db.runTrackingDao(), db.runRecoveryDao(), auth, FakeClock(), ids, widgets),
            settingsRepository = settings,
            voiceAnnouncer = voice,
            elapsedClock = ElapsedClock { testScheduler.currentTime },
            scope = backgroundScope,
        )

    /** [stepMs] 지나고 점 하나. 기본은 1초에 4m */
    private fun TestScope.runOneSecond(
        accuracyM: Float = 5f,
        stepMs: Long = 1_000,
    ) {
        advanceTimeBy(stepMs)
        northM += 4.0 * stepMs / 1_000
        val now = testScheduler.currentTime
        location.emit(RawLocation(northM / METERS_PER_DEGREE, 0.0, null, accuracyM, null, now, now))
        runCurrent()
    }

    private fun TestScope.runSeconds(seconds: Int) = repeat(seconds) { runOneSecond() }

    private val recording get() = engineState as TrackingState.Recording
    private var engineState: TrackingState = TrackingState.Idle

    private suspend fun TestScope.started(engine: TrackingEngine): String {
        engine.prepare()
        runCurrent()
        return engine.start().getOrThrow()
    }

    private suspend fun run(id: String) = db.runRecoveryDao().findRun(id)!!

    @Test
    fun `준비 중 정확한 점이 2개 오면 GOOD, 위치 요청 시작`() =
        runTest(dispatcher) {
            val engine = engine()
            engine.prepare()
            runCurrent()
            assertTrue(location.isSubscribed.value)
            assertEquals(TrackingState.Preparing(GpsStatus.LOST), engine.state.value)

            runSeconds(2)

            assertEquals(TrackingState.Preparing(GpsStatus.GOOD), engine.state.value)
        }

    @Test
    fun `준비 취소하면 Idle, 위치 요청 해제`() =
        runTest(dispatcher) {
            val engine = engine()
            engine.prepare()
            runCurrent()

            engine.cancelPreparing()
            runCurrent()

            assertEquals(TrackingState.Idle, engine.state.value)
            assertFalse(location.isSubscribed.value)
        }

    @Test
    fun `준비 없이 시작하면 실패`() =
        runTest(dispatcher) {
            assertTrue(engine().start().isFailure)
        }

    @Test
    fun `시작하면 ACTIVE 기록을 만들고 점이 쌓이면 거리가 늘어남`() =
        runTest(dispatcher) {
            val engine = engine()
            val runId = started(engine)
            assertEquals("ACTIVE", run(runId).status)

            runSeconds(11)
            engineState = engine.state.value

            // 첫 점은 기준점, 이후 10구간 × 4m
            assertEquals(40.0, recording.metrics.distanceM, 0.5)
            assertEquals(11, recording.route.size)
            assertEquals(PauseState.RUNNING, recording.pause)
        }

    @Test
    fun `점 10개가 쌓이면 바로 저장`() =
        runTest(dispatcher) {
            val engine = engine()
            val runId = started(engine)

            // 5초가 되기 전에 점 10개 (0.4초 간격)
            repeat(9) { runOneSecond(stepMs = 400) }
            assertNull(db.runRecoveryDao().pointSummary(runId).maxSeq)

            runOneSecond(stepMs = 400)
            assertEquals(9, db.runRecoveryDao().pointSummary(runId).maxSeq)
        }

    @Test
    fun `점이 없어도 5초마다 운동 시간을 저장`() =
        runTest(dispatcher) {
            val engine = engine()
            val runId = started(engine)

            advanceTimeBy(5_001)
            runCurrent()

            assertEquals(5_000L, run(runId).movingMs)
        }

    @Test
    fun `일시정지하면 PAUSED로 저장하고 그동안의 시간은 운동 시간에서 뺌`() =
        runTest(dispatcher) {
            val engine = engine()
            val runId = started(engine)
            runSeconds(10)

            engine.pause()
            assertEquals("PAUSED", run(runId).status)
            assertEquals(1, run(runId).pauseCount)

            advanceTimeBy(60_000)
            engine.resume()
            runSeconds(10)
            engineState = engine.state.value

            assertEquals("ACTIVE", run(runId).status)
            assertEquals(20_000L, recording.metrics.movingMsAtEmit)
        }

    @Test
    fun `저장하고 마치면 FINISHED, 경로 칼로리를 쓰고 위치 요청 해제`() =
        runTest(dispatcher) {
            settings.profile.value = UserProfile(65.0, DistanceUnit.KM)
            val engine = engine()
            val runId = started(engine)
            runSeconds(30)

            val result = engine.finish(save = true)
            runCurrent()

            assertEquals(runId, result.getOrThrow())
            assertEquals(TrackingState.Idle, engine.state.value)
            assertFalse(location.isSubscribed.value)
            val saved = run(runId)
            assertEquals("FINISHED", saved.status)
            assertNotNull(saved.routePolyline)
            assertEquals(8, saved.calories) // 65kg × 0.116km × 1.036
            assertEquals("LOCAL", saved.syncState)
            assertEquals(1, widgets.requestCount)
        }

    @Test
    fun `로그인 사용자 기록은 PENDING으로 끝남`() =
        runTest(dispatcher) {
            auth.userId.value = "user-1"
            val engine = engine()
            val runId = started(engine)
            runSeconds(3)

            engine.finish(save = true)

            assertEquals("PENDING", run(runId).syncState)
            assertEquals("user-1", run(runId).ownerUserId)
        }

    @Test
    fun `저장하지 않고 마치면 DISCARDED`() =
        runTest(dispatcher) {
            val engine = engine()
            val runId = started(engine)
            runSeconds(3)

            assertNull(engine.finish(save = false).getOrThrow())
            assertEquals("DISCARDED", run(runId).status)
        }

    @Test
    fun `1km를 넘으면 구간을 기록 중에 바로 저장`() =
        runTest(dispatcher) {
            val engine = engine()
            val runId = started(engine)

            runSeconds(260) // 1,036m
            advanceTimeBy(5_000)
            runCurrent()

            assertEquals(1, db.runRecoveryDao().splits(runId).size)
        }

    @Test
    fun `프로세스가 죽은 뒤 복구하면 저장된 거리 운동 시간에서 이어가고 새 segment로 그림`() =
        runTest(dispatcher) {
            val first = engine()
            val runId = started(first)
            runSeconds(20) // 점 20개 → 저장 2번
            // 프로세스 종료: 엔진을 버리고 새로 만든다 (단조 시계는 계속 흐르지만 저장값만 믿는다)
            val saved = run(runId)

            val second = engine()
            val recoverable = second.recoverableRun()!!
            assertEquals(runId, recoverable.runId)

            second.prepare()
            runCurrent()
            second.start(recoverRunId = runId).getOrThrow()
            runSeconds(5)
            engineState = second.state.value

            assertEquals(saved.movingMs + 5_000L, recording.metrics.movingMsAtEmit)
            assertTrue(recording.metrics.distanceM >= saved.distanceM)
            assertEquals(1, recording.route.last().segment)
            assertEquals("ACTIVE", run(runId).status)
        }

    @Test
    fun `복구 다이얼로그에서 저장하고 마치면 저장된 값으로 FINISHED`() =
        runTest(dispatcher) {
            val runId = started(engine())
            runSeconds(20)
            val saved = run(runId)

            val second = engine()
            second.finishRecovered(runId, save = true).getOrThrow()

            val finished = run(runId)
            assertEquals("FINISHED", finished.status)
            assertEquals(saved.distanceM, finished.distanceM, 0.0)
            assertEquals(saved.movingMs, finished.movingMs)
            assertNull(second.recoverableRun())
        }

    @Test
    fun `기록 중이면 복구할 기록이 없다고 답함`() =
        runTest(dispatcher) {
            val engine = engine()
            started(engine)

            assertNull(engine.recoverableRun())
        }

    @Test
    fun `1km를 지나면 음성으로 한 번 안내하고 끝나면 TTS를 내림`() =
        runTest(dispatcher) {
            val engine = engine()
            started(engine)
            assertTrue(voice.prepared)

            runSeconds(260) // 1,036m

            val progress = voice.announced.filterIsInstance<VoiceAnnouncement.Progress>()
            assertEquals(1, progress.size)
            assertEquals(1_000.0, progress.single().distanceM, 0.0)

            engine.finish(save = true)
            assertFalse(voice.prepared)
        }

    @Test
    fun `5분 간격이면 점이 없어도 1초 틱으로 안내`() =
        runTest(dispatcher) {
            settings.runSettings.value = RunSettings(voiceInterval = VoiceInterval.MINUTES_5)
            val engine = engine()
            started(engine)

            advanceTimeBy(300_001)
            runCurrent()

            assertEquals(
                300_000L,
                voice.announced
                    .filterIsInstance<VoiceAnnouncement.Progress>()
                    .single()
                    .movingMs,
            )
        }

    @Test
    fun `음성 안내를 끄면 준비도 안내도 하지 않음`() =
        runTest(dispatcher) {
            settings.runSettings.value = RunSettings(voiceEnabled = false)
            val engine = engine()
            started(engine)

            runSeconds(260)

            assertFalse(voice.prepared)
            assertTrue(voice.announced.isEmpty())
        }

    @Test
    fun `복구해서 이어 달리면 이미 지난 간격은 다시 안내하지 않음`() =
        runTest(dispatcher) {
            val runId = started(engine())
            runSeconds(260) // 1km 안내 1번
            advanceTimeBy(5_000)
            runCurrent()

            val second = engine()
            second.prepare()
            runCurrent()
            second.start(recoverRunId = runId).getOrThrow()
            runSeconds(10)

            assertEquals(1, voice.announced.filterIsInstance<VoiceAnnouncement.Progress>().size)
        }

    private companion object {
        const val METERS_PER_DEGREE = 111_194.92664455873
    }
}
