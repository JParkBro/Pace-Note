package jpark.bro.pacenote.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/** 원시 GPS 점. 기기 밖으로 보내지 않는다. */
@Entity(
    tableName = "run_point",
    primaryKeys = ["run_id", "seq"],
    foreignKeys = [ForeignKey(RunEntity::class, ["id"], ["run_id"], onDelete = ForeignKey.CASCADE)],
)
data class RunPointEntity(
    @ColumnInfo(name = "run_id") val runId: String,
    val seq: Int,
    val lat: Double,
    val lng: Double,
    val altitude: Double?,
    val accuracy: Float,
    val speed: Float?,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    val segment: Int,
    val accepted: Boolean,
)
