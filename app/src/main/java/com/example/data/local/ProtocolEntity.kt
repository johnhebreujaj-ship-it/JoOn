package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocols")
data class ProtocolEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconName: String = "bolt",
    val category: String = "Foco",
    val stepsList: String, // Pipe-separated or comma-separated steps
    val estimatedDuration: String = "25 min",
    val executionCount: Int = 0,
    val lastExecutedAt: Long? = null
)
