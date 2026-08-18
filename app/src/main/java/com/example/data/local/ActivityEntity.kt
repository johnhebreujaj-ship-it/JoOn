package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Geral", // Trabalho, Estudo, Saúde, Pessoal, Projeto, Geral
    val priority: String = "MÉDIA", // ALTA, MÉDIA, BAIXA
    val status: String = "PENDENTE", // PENDENTE, EM_PROGRESSO, CONCLUIDO, CANCELADO
    val dueDate: String = "Hoje",
    val estimatedMinutes: Int = 30,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
