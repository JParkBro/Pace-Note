package jpark.bro.pacenote.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "run",
    indices = [
        Index(value = ["owner_user_id", "started_at"]),
        Index("status"),
        Index("sync_state"),
    ],
)
data class RunEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String?,
    val status: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long?,
    @ColumnInfo(name = "zone_id") val zoneId: String,
    @ColumnInfo(name = "distance_m") val distanceM: Double,
    @ColumnInfo(name = "moving_ms") val movingMs: Long,
    @ColumnInfo(name = "elapsed_ms") val elapsedMs: Long,
    val calories: Int?,
    val title: String?,
    val memo: String?,
    val source: String,
    @ColumnInfo(name = "activity_type") val activityType: String,
    @ColumnInfo(name = "plan_id") val planId: String?,
    @ColumnInfo(name = "route_polyline") val routePolyline: String?,
    /** JSON 배열 "[312,140]" */
    @ColumnInfo(name = "route_segments") val routeSegments: String?,
    @ColumnInfo(name = "pause_count") val pauseCount: Int,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "sync_state") val syncState: String,
)
