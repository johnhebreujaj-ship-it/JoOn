package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "JOON" or "SYSTEM"
    val message: String,
    val mode: String = "OFFLINE", // OFFLINE or ONLINE
    val timestamp: Long = System.currentTimeMillis(),
    val actionTag: String? = null // e.g. "TASK_CREATED", "PROTOCOL_STARTED", "DB_QUERY"
)
