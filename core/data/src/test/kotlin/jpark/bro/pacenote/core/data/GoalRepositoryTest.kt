package jpark.bro.pacenote.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.data.repository.DefaultGoalRepository
import jpark.bro.pacenote.core.database.entity.SyncState
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.testing.fake.FakeIdGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RunWith(AndroidJUnit4::class)
internal class GoalRepositoryTest : RepositoryTest() {
    private val repository by lazy { DefaultGoalRepository(db.goalDao(), auth, clock, FakeIdGenerator("goal")) }
    private val thisWeek = LocalDate.of(2026, 10, 26)

    @Test
    fun `목표는 이번 주 월요일부터 적용되고 지난 주에는 없음`() =
        runTest {
            repository.setGoal(GoalType.DISTANCE, 15_000.0)

            val goal = repository.observeGoalFor(thisWeek).first()!!
            assertEquals(thisWeek, goal.effectiveWeekStart)
            assertEquals(15_000.0, goal.value, 0.0)
            assertNull(repository.observeGoalFor(thisWeek.minusWeeks(1)).first())
        }

    @Test
    fun `같은 주에 다시 바꾸면 행을 늘리지 않고 값만 바꿈`() =
        runTest {
            repository.setGoal(GoalType.DISTANCE, 15_000.0)
            repository.setGoal(GoalType.COUNT, 3.0)

            val rows = db.goalDao().observeGoals(null).first()
            assertEquals(1, rows.size)
            assertEquals("COUNT", rows.single().type)
            assertEquals(SyncState.LOCAL.name, rows.single().syncState)
        }

    @Test
    fun `다음 주에 바꾸면 지난 주 목표는 그대로 남음`() =
        runTest {
            repository.setGoal(GoalType.DISTANCE, 15_000.0)
            clock.now = clock.now.plus(7, ChronoUnit.DAYS)
            repository.setGoal(GoalType.DISTANCE, 20_000.0)

            assertEquals(15_000.0, repository.observeGoalFor(thisWeek).first()!!.value, 0.0)
            assertEquals(20_000.0, repository.observeGoalFor(thisWeek.plusWeeks(1)).first()!!.value, 0.0)
        }

    @Test
    fun `로그인 사용자 목표는 PENDING으로 저장`() =
        runTest {
            auth.userId.value = ME

            repository.setGoal(GoalType.COUNT, 3.0)

            assertEquals(
                SyncState.PENDING.name,
                db
                    .goalDao()
                    .observeGoals(ME)
                    .first()
                    .single()
                    .syncState,
            )
        }

    @Test
    fun `0 이하 목표는 실패하고 저장하지 않음`() =
        runTest {
            assertTrue(repository.setGoal(GoalType.DISTANCE, 0.0).isFailure)

            assertTrue(
                db
                    .goalDao()
                    .observeGoals(null)
                    .first()
                    .isEmpty(),
            )
        }
}
