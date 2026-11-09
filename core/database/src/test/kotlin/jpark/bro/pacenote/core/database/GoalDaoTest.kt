package jpark.bro.pacenote.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GoalDaoTest : DatabaseTest() {
    private val dao get() = db.goalDao()

    @Test
    fun `내 목표만 보이고 지운 목표는 제외`() =
        runTest {
            dao.upsert(testGoal("mine"))
            dao.upsert(testGoal("deleted", deletedAt = 1))
            dao.upsert(testGoal("other", owner = "user-2"))

            assertEquals(listOf("mine"), dao.observeGoals(ME).first().map { it.id })
        }

    @Test
    fun `게스트 목표를 찾음`() =
        runTest {
            dao.upsert(testGoal("guest", owner = null))

            assertEquals(listOf("guest"), dao.observeGoals(null).first().map { it.id })
        }
}
