package com.example.ui.viewmodel

import com.example.data.local.ActivityEntity
import com.example.data.local.LogEntity
import com.example.data.local.ProtocolEntity

data class JoOnUiState(
    val isOnline: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val commandInput: String = "",
    val selectedTab: Int = 0,
    val activities: List<ActivityEntity> = emptyList(),
    val protocols: List<ProtocolEntity> = emptyList(),
    val logs: List<LogEntity> = emptyList(),
    val totalActivitiesCount: Int = 0,
    val completedActivitiesCount: Int = 0,
    val pendingActivitiesCount: Int = 0,
    val activeProtocol: ProtocolEntity? = null,
    
    // Voice Recognition Engine state
    val isListening: Boolean = false,
    val rmsDb: Float = 0f,
    val partialVoiceText: String = "",
    val lastVoiceError: String? = null,
    val isVoiceModalOpen: Boolean = false,
    
    // Complex UI dialogs / drawers
    val isCommandCatalogOpen: Boolean = false,
    val isPythonBridgeModalOpen: Boolean = false,
    val quickNotes: List<String> = emptyList(),

    // Focus Timer
    val isFocusTimerRunning: Boolean = false,
    val focusSecondsRemaining: Int = 25 * 60,
    val focusTotalSeconds: Int = 25 * 60,
    val focusTaskTitle: String = "Sprint de Foco Alpha",
    
    // Feedback Banner
    val statusMessage: String? = null,
    val currentTime: String = "00:00:00"
)
