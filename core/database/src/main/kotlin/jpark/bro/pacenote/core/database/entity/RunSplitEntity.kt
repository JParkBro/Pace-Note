package jpark.bro.pacenote.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "run_split",
    primaryKeys = ["run_id", "idx"],
    foreignKeys = [ForeignKey(RunEntity::class, ["id"], ["run_id"], onDelete = ForeignKey.CASCADE)],
)
data class RunSplitEntity(
    @ColumnInfo(name = "run_id") val runId: String,
    val idx: Int,
    @ColumnInfo(name = "distance_m") val distanceM: Double,
    @ColumnInfo(name = "moving_ms") val movingMs: Long,
)
