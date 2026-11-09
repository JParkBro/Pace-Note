package jpark.bro.pacenote.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import jpark.bro.pacenote.core.database.PacenoteDatabase
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.SyncState
import jpark.bro.pacenote.core.testing.fake.FakeAuthRepository
import jpark.bro.pacenote.core.testing.fake.FakeClock
import org.junit.After
import org.junit.Before
import java.time.LocalDateTime
import java.time.ZoneId

internal const val ME = "user-1"
internal val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")

/** in-memory Room을 쓰는 Repository 테스트 공통 설정. 기본은 게스트, 오늘은 2026.10.29 (목). */
internal abstract class RepositoryTest {
    protected lateinit var db: PacenoteDatabase
    protected val auth = FakeAuthRepository()
    protected val clock = FakeClock()

    @Before
    fun createDb() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PacenoteDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() = db.close()
}

internal fun seoulMs(
    month: Int,
    day: Int,
    hour: Int,
    minute: Int = 0,
): Long =
    LocalDateTime
        .of(2026, month, day, hour, minute)
        .atZone(SEOUL)
        .toInstant()
        .toEpochMilli()

internal fun runEntity(
    id: String,
    owner: String? = null,
    startedAt: Long = seoulMs(10, 28, 6, 40),
    distanceM: Double = 5_210.0,
    movingMs: Long = 1_812_000L,
) = RunEntity(
    id = id,
    ownerUserId = owner,
    status = "FINISHED",
    startedAt = startedAt,
    endedAt = startedAt + movingMs,
    zoneId = SEOUL.id,
    distanceM = distanceM,
    movingMs = movingMs,
    elapsedMs = movingMs,
    calories = null,
    title = null,
    memo = null,
    source = "GPS",
    activityType = "OUTDOOR_RUN",
    planId = null,
    routePolyline = null,
    routeSegments = null,
    pauseCount = 0,
    schemaVersion = 1,
    createdAt = startedAt,
    updatedAt = startedAt,
    deletedAt = null,
    syncState = if (owner == null) SyncState.LOCAL.name else SyncState.SYNCED.name,
)
