package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ActivityDao
import com.example.data.local.ActivityEntity
import com.example.data.local.LogDao
import com.example.data.local.LogEntity
import com.example.data.local.ProtocolDao
import com.example.data.local.ProtocolEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class JoOnRepository(
    private val activityDao: ActivityDao,
    private val protocolDao: ProtocolDao,
    private val logDao: LogDao
) {
    val allActivities: Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    val pendingActivities: Flow<List<ActivityEntity>> = activityDao.getPendingActivities()
    val completedActivities: Flow<List<ActivityEntity>> = activityDao.getCompletedActivities()
    val allProtocols: Flow<List<ProtocolEntity>> = protocolDao.getAllProtocols()
    val recentLogs: Flow<List<LogEntity>> = logDao.getRecentLogs()

    val totalCount: Flow<Int> = activityDao.getTotalActivitiesCount()
    val completedCount: Flow<Int> = activityDao.getCompletedActivitiesCount()
    val pendingCount: Flow<Int> = activityDao.getPendingActivitiesCount()

    suspend fun insertActivity(activity: ActivityEntity): Long = withContext(Dispatchers.IO) {
        activityDao.insertActivity(activity)
    }

    suspend fun insertActivities(activities: List<ActivityEntity>) = withContext(Dispatchers.IO) {
        activityDao.insertActivities(activities)
    }

    suspend fun updateActivity(activity: ActivityEntity) = withContext(Dispatchers.IO) {
        activityDao.updateActivity(activity)
    }

    suspend fun toggleActivityStatus(activity: ActivityEntity) = withContext(Dispatchers.IO) {
        val newStatus = if (activity.status == "CONCLUIDO") "PENDENTE" else "CONCLUIDO"
        val completedAt = if (newStatus == "CONCLUIDO") System.currentTimeMillis() else null
        activityDao.updateActivityStatus(activity.id, newStatus, completedAt)
    }

    suspend fun deleteActivityById(id: Long) = withContext(Dispatchers.IO) {
        activityDao.deleteActivityById(id)
    }

    suspend fun clearCompletedActivities() = withContext(Dispatchers.IO) {
        activityDao.clearCompletedActivities()
    }

    suspend fun recordProtocolExecution(protocolId: String) = withContext(Dispatchers.IO) {
        protocolDao.incrementExecution(protocolId)
    }

    suspend fun insertProtocol(protocol: ProtocolEntity) = withContext(Dispatchers.IO) {
        protocolDao.insertProtocol(protocol)
    }

    suspend fun addLog(sender: String, message: String, mode: String, actionTag: String? = null) = withContext(Dispatchers.IO) {
        logDao.insertLog(
            LogEntity(
                sender = sender,
                message = message,
                mode = mode,
                actionTag = actionTag
            )
        )
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        logDao.clearLogs()
    }

    // Direct Gemini Online Query
    suspend fun queryGemini(
        prompt: String,
        contextActivities: List<ActivityEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Chave da API Gemini não configurada no Secrets. O Jo'On continuará operando com inteligência local offline.")
                )
            }

            // System prompt for Jo'On
            val activitySummary = contextActivities.take(10).joinToString("\n") {
                "- [${it.status}] ${it.title} (${it.category}, Prioridade: ${it.priority}, Prazo: ${it.dueDate})"
            }

            val systemPrompt = """
                Você é "Jo'On", um assistente virtual ultra-inteligente, altamente eficiente, leal e polido (inspirado no Jarvis).
                Você ajuda o usuário a organizar e executar diversas atividades da rotina, trabalho, estudos e desenvolvimento pessoal.
                Você tem acesso à base de dados SQLite local com as atividades atuais do usuário:
                $activitySummary

                Instruções:
                1. Responda em Português com tom elegante, conciso, proativo e tecnológico ("Sim, senhor", "Atividade mapeada", "Diretrizes organizadas").
                2. Se o usuário pedir para criar um plano de atividades ou dividir um projeto em tarefas, forneça um roteiro claro e marque as tarefas sugeridas em formato estruturado.
                3. Seja direto, prático e motivador para a execução das tarefas.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GenerationConfig(temperature = 0.6f, topP = 0.95f)
            )

            val response = GeminiClient.service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Jo'On processou o sinal, mas nenhuma resposta de texto foi retornada pela rede neural."

            Result.success(replyText)
        } catch (e: Exception) {
            Log.e("JoOnRepository", "Error querying Gemini", e)
            Result.failure(e)
        }
    }
}
