package jpark.bro.pacenote.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.pacenote.core.database.entity.GoalHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Upsert
    suspend fun upsert(goal: GoalHistoryEntity)

    @Query("SELECT * FROM goal_history WHERE owner_user_id IS :owner AND deleted_at IS NULL")
    fun observeGoals(owner: String?): Flow<List<GoalHistoryEntity>>
}
