package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocols ORDER BY executionCount DESC, name ASC")
    fun getAllProtocols(): Flow<List<ProtocolEntity>>

    @Query("SELECT * FROM protocols WHERE id = :id")
    suspend fun getProtocolById(id: String): ProtocolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocols(protocols: List<ProtocolEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ProtocolEntity)

    @Update
    suspend fun updateProtocol(protocol: ProtocolEntity)

    @Query("UPDATE protocols SET executionCount = executionCount + 1, lastExecutedAt = :timestamp WHERE id = :id")
    suspend fun incrementExecution(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM protocols")
    suspend fun getCount(): Int
}
