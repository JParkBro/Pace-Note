package jpark.bro.pacenote.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.database.entity.SyncCursorEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SyncCursorDaoTest : DatabaseTest() {
    private val dao get() = db.syncCursorDao()

    @Test
    fun `커서가 없으면 null`() =
        runTest {
            assertNull(dao.getCursor("runs:user-1"))
        }

    @Test
    fun `커서를 갱신함`() =
        runTest {
            dao.upsert(SyncCursorEntity("runs:user-1", 1))
            dao.upsert(SyncCursorEntity("runs:user-1", 2))

            assertEquals(2L, dao.getCursor("runs:user-1"))
        }
}
