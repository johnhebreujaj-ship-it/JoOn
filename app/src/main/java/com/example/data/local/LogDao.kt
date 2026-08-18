package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM system_logs ORDER BY id DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<LogEntity>>

    @Query("SELECT * FROM system_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity): Long

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()

    @Query("SELECT COUNT(*) FROM system_logs")
    fun getLogsCount(): Flow<Int>
}
