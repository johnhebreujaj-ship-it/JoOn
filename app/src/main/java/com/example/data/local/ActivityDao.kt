package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY CASE priority WHEN 'ALTA' THEN 1 WHEN 'MÉDIA' THEN 2 ELSE 3 END, id DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE status != 'CONCLUIDO' ORDER BY CASE priority WHEN 'ALTA' THEN 1 WHEN 'MÉDIA' THEN 2 ELSE 3 END, id DESC")
    fun getPendingActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE status = 'CONCLUIDO' ORDER BY completedAt DESC")
    fun getCompletedActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchActivities(query: String): List<ActivityEntity>

    @Query("SELECT COUNT(*) FROM activities")
    fun getTotalActivitiesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM activities WHERE status = 'CONCLUIDO'")
    fun getCompletedActivitiesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM activities WHERE status != 'CONCLUIDO'")
    fun getPendingActivitiesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("UPDATE activities SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateActivityStatus(id: Long, status: String, completedAt: Long?)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivityById(id: Long)

    @Query("DELETE FROM activities WHERE status = 'CONCLUIDO'")
    suspend fun clearCompletedActivities()

    @Query("DELETE FROM activities")
    suspend fun clearAll()
}
