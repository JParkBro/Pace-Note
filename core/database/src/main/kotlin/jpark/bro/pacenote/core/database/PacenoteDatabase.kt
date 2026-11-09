package jpark.bro.pacenote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import jpark.bro.pacenote.core.database.dao.GoalDao
import jpark.bro.pacenote.core.database.dao.RunDao
import jpark.bro.pacenote.core.database.dao.SyncCursorDao
import jpark.bro.pacenote.core.database.entity.GoalHistoryEntity
import jpark.bro.pacenote.core.database.entity.RunEntity
import jpark.bro.pacenote.core.database.entity.RunPointEntity
import jpark.bro.pacenote.core.database.entity.RunSplitEntity
import jpark.bro.pacenote.core.database.entity.SyncCursorEntity

/** 출시 전까지 version 1을 고쳐 쓴다 (docs/run/README.md 예외). */
@Database(
    entities = [
        RunEntity::class,
        RunPointEntity::class,
        RunSplitEntity::class,
        GoalHistoryEntity::class,
        SyncCursorEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PacenoteDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao

    abstract fun goalDao(): GoalDao

    abstract fun syncCursorDao(): SyncCursorDao
}
