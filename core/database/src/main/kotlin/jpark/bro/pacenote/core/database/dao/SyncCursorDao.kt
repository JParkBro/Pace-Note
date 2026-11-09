package jpark.bro.pacenote.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.pacenote.core.database.entity.SyncCursorEntity

@Dao
interface SyncCursorDao {
    @Query("SELECT last_pulled_at_micros FROM sync_cursor WHERE `key` = :key")
    suspend fun getCursor(key: String): Long?

    @Upsert
    suspend fun upsert(cursor: SyncCursorEntity)
}
