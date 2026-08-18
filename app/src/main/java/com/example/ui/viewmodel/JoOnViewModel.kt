package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ActivityEntity
import com.example.data.local.JoOnDatabase
import com.example.data.local.LogEntity
import com.example.data.local.ProtocolEntity
import com.example.data.repository.JoOnRepository
import com.example.engine.OfflineNLPProcessor
import com.example.engine.ProcessedResult
import com.example.engine.TtsManager
import com.example.engine.VoiceRecognitionEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JoOnViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JoOnDatabase.getDatabase(application, viewModelScope)
    private val repository = JoOnRepository(
        activityDao = database.activityDao(),
        protocolDao = database.protocolDao(),
        logDao = database.logDao()
    )

    val nlpProcessor = OfflineNLPProcessor()
    private val ttsManager = TtsManager(application)
    private val voiceEngine = VoiceRecognitionEngine(application)

    private val _uiState = MutableStateFlow(JoOnUiState())
    val uiState: StateFlow<JoOnUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var clockJob: Job? = null

    init {
        startClock()
        observeDatabase()
        observeTts()
        observeVoiceEngine()
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            while (true) {
                _uiState.update { it.copy(currentTime = format.format(Date())) }
                delay(1000)
            }
        }
    }

    private fun observeTts() {
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            ttsManager.isMuted.collect { muted ->
                _uiState.update { it.copy(isMuted = muted) }
            }
        }
    }

    private fun observeVoiceEngine() {
        voiceEngine.onCommandRecognized = { recognizedText ->
            _uiState.update { it.copy(isVoiceModalOpen = false) }
            sendCommand(recognizedText)
        }

        viewModelScope.launch {
            voiceEngine.isListening.collect { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }
        viewModelScope.launch {
            voiceEngine.rmsDb.collect { rms ->
                _uiState.update { it.copy(rmsDb = rms) }
            }
        }
        viewModelScope.launch {
            voiceEngine.partialResult.collect { partial ->
                _uiState.update { it.copy(partialVoiceText = partial) }
            }
        }
        viewModelScope.launch {
            voiceEngine.lastError.collect { error ->
                _uiState.update { it.copy(lastVoiceError = error) }
            }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.allActivities.collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
        viewModelScope.launch {
            repository.allProtocols.collect { protocols ->
                _uiState.update { it.copy(protocols = protocols) }
            }
        }
        viewModelScope.launch {
            repository.recentLogs.collect { logs ->
                _uiState.update { it.copy(logs = logs) }
            }
        }
        viewModelScope.launch {
            repository.totalCount.collect { total ->
                _uiState.update { it.copy(totalActivitiesCount = total) }
            }
        }
        viewModelScope.launch {
            repository.completedCount.collect { completed ->
                _uiState.update { it.copy(completedActivitiesCount = completed) }
            }
        }
        viewModelScope.launch {
            repository.pendingCount.collect { pending ->
                _uiState.update { it.copy(pendingActivitiesCount = pending) }
            }
        }
    }

    fun startVoiceListening() {
        ttsManager.stop()
        _uiState.update { it.copy(isVoiceModalOpen = true, partialVoiceText = "", lastVoiceError = null) }
        voiceEngine.startListening()
    }

    fun stopVoiceListening() {
        voiceEngine.stopListening()
        _uiState.update { it.copy(isVoiceModalOpen = false) }
    }

    fun toggleVoiceModal(open: Boolean) {
        if (open) {
            startVoiceListening()
        } else {
            stopVoiceListening()
        }
    }

    fun setCommandCatalogOpen(open: Boolean) {
        _uiState.update { it.copy(isCommandCatalogOpen = open) }
    }

    fun setPythonBridgeModalOpen(open: Boolean) {
        _uiState.update { it.copy(isPythonBridgeModalOpen = open) }
    }

    fun onCommandInputChange(input: String) {
        _uiState.update { it.copy(commandInput = input) }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun toggleOnlineMode() {
        val newMode = !_uiState.value.isOnline
        _uiState.update { it.copy(isOnline = newMode) }
        val modeName = if (newMode) "REDE NEURAL ONLINE (Gemini)" else "NÚCLEO LOCAL OFFLINE (SQLite)"
        val msg = "Modo alternado para $modeName."
        viewModelScope.launch {
            repository.addLog("SYSTEM", msg, if (newMode) "ONLINE" else "OFFLINE", "MODE_SWITCH")
        }
        ttsManager.speak(msg)
    }

    fun toggleMute() {
        val isMuted = ttsManager.toggleMute()
        val msg = if (isMuted) "Voz desativada" else "Voz ativada"
        _uiState.update { it.copy(isMuted = isMuted, statusMessage = msg) }
    }

    fun sendCommand(text: String? = null) {
        val input = (text ?: _uiState.value.commandInput).trim()
        if (input.isBlank()) return

        _uiState.update { it.copy(commandInput = "", isProcessing = true) }

        viewModelScope.launch {
            val currentOnline = _uiState.value.isOnline
            val modeStr = if (currentOnline) "ONLINE" else "OFFLINE"

            // Log User message
            repository.addLog("USER", input, modeStr, "USER_PROMPT")

            if (currentOnline) {
                // Online with Gemini API
                processOnlineQuery(input)
            } else {
                // Offline with Local NLP & SQLite (40+ commands)
                processOfflineQuery(input)
            }

            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    private suspend fun processOfflineQuery(input: String) {
        val activities = _uiState.value.activities
        val protocols = _uiState.value.protocols
        val result = nlpProcessor.processCommand(input, activities, protocols)

        when (result) {
            is ProcessedResult.CreateActivity -> {
                repository.insertActivity(result.activity)
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "TASK_CREATED")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.CompleteActivity -> {
                val found = activities.find {
                    it.title.contains(result.targetQuery, ignoreCase = true) ||
                    (result.targetQuery.toLongOrNull() != null && it.id == result.targetQuery.toLong())
                }
                if (found != null) {
                    repository.toggleActivityStatus(found)
                    val msg = "Atividade '${found.title}' marcada como CONCLUÍDA na base SQLite."
                    repository.addLog("JOON", msg, "OFFLINE", "TASK_COMPLETED")
                    ttsManager.speak(msg)
                } else {
                    val msg = "Nenhuma atividade encontrada com o termo '${result.targetQuery}'."
                    repository.addLog("JOON", msg, "OFFLINE")
                    ttsManager.speak(msg)
                }
            }
            is ProcessedResult.DeleteActivity -> {
                val found = activities.find {
                    it.title.contains(result.targetQuery, ignoreCase = true) ||
                    (result.targetQuery.replace("#", "").toLongOrNull() != null && it.id == result.targetQuery.replace("#", "").toLong())
                }
                if (found != null) {
                    repository.deleteActivityById(found.id)
                    val msg = "Atividade '${found.title}' [#${found.id}] excluída do SQLite com sucesso."
                    repository.addLog("JOON", msg, "OFFLINE", "TASK_DELETED")
                    ttsManager.speak(msg)
                } else {
                    val msg = "Registro '${result.targetQuery}' não localizado no SQLite."
                    repository.addLog("JOON", msg, "OFFLINE")
                    ttsManager.speak(msg)
                }
            }
            is ProcessedResult.UpdatePriority -> {
                val found = activities.find { it.title.contains(result.targetQuery, ignoreCase = true) }
                if (found != null) {
                    repository.updateActivity(found.copy(priority = result.newPriority))
                    val msg = "Prioridade da atividade '${found.title}' redefinida para ${result.newPriority}."
                    repository.addLog("JOON", msg, "OFFLINE", "PRIORITY_UPDATED")
                    ttsManager.speak(msg)
                } else {
                    repository.addLog("JOON", result.responseMessage, "OFFLINE")
                    ttsManager.speak(result.responseMessage)
                }
            }
            is ProcessedResult.PostponeActivity -> {
                val found = activities.find { it.title.contains(result.targetQuery, ignoreCase = true) }
                if (found != null) {
                    repository.updateActivity(found.copy(dueDate = result.newDueDate))
                    val msg = "Prazo da atividade '${found.title}' transferido para ${result.newDueDate}."
                    repository.addLog("JOON", msg, "OFFLINE", "DUE_DATE_POSTPONED")
                    ttsManager.speak(msg)
                } else {
                    repository.addLog("JOON", result.responseMessage, "OFFLINE")
                    ttsManager.speak(result.responseMessage)
                }
            }
            is ProcessedResult.ClearCompleted -> {
                repository.clearCompletedActivities()
                val msg = "Todas as atividades concluídas foram expurgadas do banco SQLite."
                repository.addLog("SYSTEM", msg, "OFFLINE", "DB_CLEANUP")
                ttsManager.speak(msg)
            }
            is ProcessedResult.RunProtocol -> {
                val protocol = protocols.find { it.id == result.protocolId }
                if (protocol != null) {
                    executeProtocol(protocol)
                } else {
                    repository.addLog("JOON", result.responseMessage, "OFFLINE", "PROTOCOL_STARTED")
                    ttsManager.speak(result.responseMessage)
                }
            }
            is ProcessedResult.StartTimer -> {
                startFocusTimer(result.minutes, result.title)
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "TIMER_START")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.PauseTimer -> {
                pauseFocusTimer()
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "TIMER_PAUSE")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.ResumeTimer -> {
                if (!_uiState.value.isFocusTimerRunning) {
                    pauseFocusTimer()
                }
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "TIMER_RESUME")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.ResetTimer -> {
                resetFocusTimer()
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "TIMER_RESET")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.Calculate -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "CALCULATION")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.UnitConversion -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "CONVERSION")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.SystemStatus -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "SYSTEM_STATUS")
                ttsManager.speak("Status nominal. Base SQLite com ${_uiState.value.totalActivitiesCount} atividades.")
            }
            is ProcessedResult.DailyBriefing -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "BRIEFING")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.DatabaseStats -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "DB_STATS")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.SaveNote -> {
                _uiState.update { it.copy(quickNotes = it.quickNotes + result.noteContent) }
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "NOTE_SAVED")
                ttsManager.speak("Nota salva no scratchpad.")
            }
            is ProcessedResult.ViewNotes -> {
                val current = _uiState.value.quickNotes
                val msg = if (current.isEmpty()) "Scratchpad vazio." else "Notas salvas:\n" + current.mapIndexed { i, n -> "${i+1}. $n" }.joinToString("\n")
                repository.addLog("JOON", msg, "OFFLINE", "NOTES_VIEW")
                ttsManager.speak(msg)
            }
            is ProcessedResult.ClearNotes -> {
                _uiState.update { it.copy(quickNotes = emptyList()) }
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "NOTES_CLEARED")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.ToggleVoice -> {
                if (result.enable && _uiState.value.isMuted) {
                    toggleMute()
                } else if (!result.enable && !_uiState.value.isMuted) {
                    toggleMute()
                }
                repository.addLog("SYSTEM", result.responseMessage, "OFFLINE", "VOICE_TOGGLED")
            }
            is ProcessedResult.ToggleMode -> {
                if (_uiState.value.isOnline != result.online) {
                    toggleOnlineMode()
                }
            }
            is ProcessedResult.RandomNumber -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "RANDOM_NUMBER")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.CoinFlip -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "COIN_FLIP")
                ttsManager.speak(result.responseMessage)
            }
            is ProcessedResult.ShowCommandManual -> {
                _uiState.update { it.copy(isCommandCatalogOpen = true) }
                repository.addLog("JOON", result.responseMessage, "OFFLINE", "MANUAL_OPEN")
                ttsManager.speak("Abrindo catálogo com mais de 40 comandos offline.")
            }
            is ProcessedResult.ConversationalResponse -> {
                repository.addLog("JOON", result.responseMessage, "OFFLINE", result.actionTag)
                ttsManager.speak(result.responseMessage)
            }
        }
    }

    private suspend fun processOnlineQuery(input: String) {
        val activities = _uiState.value.activities
        val result = repository.queryGemini(input, activities)

        result.onSuccess { reply ->
            repository.addLog("JOON", reply, "ONLINE", "GEMINI_RESPONSE")
            ttsManager.speak(reply)
        }.onFailure { error ->
            val fallbackMsg = "Ponte online inacessível (${error.localizedMessage}). Ativando fallback para o Núcleo Local Jo'On..."
            repository.addLog("SYSTEM", fallbackMsg, "OFFLINE", "FALLBACK_TRIGGERED")
            processOfflineQuery(input)
        }
    }

    fun addActivity(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: String,
        minutes: Int
    ) {
        viewModelScope.launch {
            val activity = ActivityEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDate = dueDate,
                estimatedMinutes = minutes
            )
            val id = repository.insertActivity(activity)
            val msg = "Nova atividade #$id [$title] inserida na base SQLite."
            repository.addLog("JOON", msg, if (_uiState.value.isOnline) "ONLINE" else "OFFLINE", "TASK_INSERTED")
            ttsManager.speak("Atividade '$title' registrada no SQLite, senhor.")
        }
    }

    fun toggleActivityStatus(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.toggleActivityStatus(activity)
            val newStatus = if (activity.status == "CONCLUIDO") "PENDENTE" else "CONCLUIDA"
            val msg = "Atividade '${activity.title}' atualizada para $newStatus."
            repository.addLog("JOON", msg, "OFFLINE", "STATUS_TOGGLE")
        }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            repository.deleteActivityById(id)
            repository.addLog("JOON", "Registro de atividade #$id removido do SQLite.", "OFFLINE", "TASK_DELETED")
        }
    }

    fun clearCompletedActivities() {
        viewModelScope.launch {
            repository.clearCompletedActivities()
            val msg = "Registros concluídos expurgados do SQLite."
            repository.addLog("SYSTEM", msg, "OFFLINE", "DB_CLEANUP")
            ttsManager.speak("Base de dados otimizada e limpa.")
        }
    }

    fun executeProtocol(protocol: ProtocolEntity) {
        viewModelScope.launch {
            repository.recordProtocolExecution(protocol.id)
            _uiState.update { it.copy(activeProtocol = protocol) }

            val steps = protocol.stepsList.split("|")
            val subtasks = steps.mapIndexed { index, stepText ->
                ActivityEntity(
                    title = "[${protocol.name}] $stepText",
                    description = "Etapa ${index + 1} do protocolo ${protocol.name}",
                    category = protocol.category,
                    priority = if (index == 0) "ALTA" else "MÉDIA",
                    dueDate = "Hoje",
                    estimatedMinutes = 15
                )
            }
            repository.insertActivities(subtasks)

            if (protocol.id.contains("FOCUS") || protocol.id.contains("DEEP")) {
                val durationMins = if (protocol.id.contains("DEEP")) 50 else 25
                startFocusTimer(durationMins, protocol.name)
            }

            val response = "Protocolo [${protocol.name}] ativado com sucesso. ${steps.size} etapas alocadas no SQLite."
            repository.addLog("JOON", response, "OFFLINE", "PROTOCOL_ACTIVE")
            ttsManager.speak("Protocolo ${protocol.name} ativado. Etapas geradas na base de dados.")
        }
    }

    fun startFocusTimer(minutes: Int = 25, taskTitle: String = "Sprint de Foco") {
        timerJob?.cancel()
        val totalSecs = minutes * 60
        _uiState.update {
            it.copy(
                isFocusTimerRunning = true,
                focusSecondsRemaining = totalSecs,
                focusTotalSeconds = totalSecs,
                focusTaskTitle = taskTitle
            )
        }

        timerJob = viewModelScope.launch {
            while (_uiState.value.focusSecondsRemaining > 0 && _uiState.value.isFocusTimerRunning) {
                delay(1000)
                _uiState.update { it.copy(focusSecondsRemaining = it.focusSecondsRemaining - 1) }
            }
            if (_uiState.value.focusSecondsRemaining <= 0) {
                _uiState.update { it.copy(isFocusTimerRunning = false) }
                val completeMsg = "Sessão de foco concluída, senhor! Excelente disciplina."
                repository.addLog("JOON", completeMsg, "OFFLINE", "FOCUS_COMPLETE")
                ttsManager.speak(completeMsg)
            }
        }
    }

    fun pauseFocusTimer() {
        val running = !_uiState.value.isFocusTimerRunning
        _uiState.update { it.copy(isFocusTimerRunning = running) }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isFocusTimerRunning = false,
                focusSecondsRemaining = it.focusTotalSeconds
            )
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        clockJob?.cancel()
        voiceEngine.stopListening()
        ttsManager.shutdown()
    }
}
