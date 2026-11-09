package jpark.bro.pacenote.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_history",
    indices = [Index(value = ["owner_user_id", "effective_week_start"])],
)
data class GoalHistoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String?,
    val type: String,
    val value: Double,
    /** yyyy-MM-dd */
    @ColumnInfo(name = "effective_week_start") val effectiveWeekStart: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "sync_state") val syncState: String,
)
