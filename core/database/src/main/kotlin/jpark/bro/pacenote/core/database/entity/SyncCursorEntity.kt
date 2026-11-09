package jpark.bro.pacenote.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 백업 Pull 위치. [key]는 `runs:{uid}` · `goals:{uid}`. 시각은 Firestore 서버 시각의 마이크로초다. */
@Entity(tableName = "sync_cursor")
data class SyncCursorEntity(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "last_pulled_at_micros") val lastPulledAtMicros: Long,
)
