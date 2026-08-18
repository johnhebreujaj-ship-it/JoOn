package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.LogEntity
import com.example.engine.OfflineCommandInfo
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.CommandCatalogDialog
import com.example.ui.components.HudCard
import com.example.ui.components.PythonBridgeModal
import com.example.ui.components.VoiceListeningDialog
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCrimson
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcTeal
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.JoOnUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandCenterScreen(
    uiState: JoOnUiState,
    commandCatalog: List<OfflineCommandInfo>,
    onCommandChange: (String) -> Unit,
    onSendCommand: (String?) -> Unit,
    onToggleMute: () -> Unit,
    onToggleMode: () -> Unit,
    onStartFocus: (Int, String) -> Unit,
    onPauseFocus: () -> Unit,
    onResetFocus: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onToggleCommandCatalog: (Boolean) -> Unit,
    onTogglePythonBridge: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Permission launcher for RECORD_AUDIO
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            onStartVoiceListening()
        }
    }

    fun triggerVoice() {
        if (hasAudioPermission) {
            onStartVoiceListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto-scroll when new logs arrive
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Voice Modal Dialog
    if (uiState.isVoiceModalOpen) {
        VoiceListeningDialog(
            isListening = uiState.isListening,
            rmsDb = uiState.rmsDb,
            partialText = uiState.partialVoiceText,
            lastError = uiState.lastVoiceError,
            onDismiss = onStopVoiceListening,
            onConfirmCommand = {
                onStopVoiceListening()
                onSendCommand(it)
            }
        )
    }

    // Command Catalog Modal Dialog (40+ commands)
    if (uiState.isCommandCatalogOpen) {
        CommandCatalogDialog(
            commands = commandCatalog,
            onDismiss = { onToggleCommandCatalog(false) },
            onExecuteCommand = { onSendCommand(it) },
            onInsertCommand = { onCommandChange(it) },
            onOpenPythonBridge = {
                onToggleCommandCatalog(false)
                onTogglePythonBridge(true)
            }
        )
    }

    // Python Speech Bridge Modal Dialog
    if (uiState.isPythonBridgeModalOpen) {
        PythonBridgeModal(
            onDismiss = { onTogglePythonBridge(false) },
            onTestExecute = { onSendCommand(it) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(horizontal = 14.dp)
    ) {
        // 1. Center Hero Section: Arc Reactor Core & Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ArcReactorVisualizer(
                    isOnline = uiState.isOnline,
                    isProcessing = uiState.isProcessing,
                    isSpeaking = uiState.isSpeaking,
                    size = 140.dp,
                    onClick = { triggerVoice() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Core Subtitle Status & Voice State
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            uiState.isListening -> "MICROFONE ABERTO (OUVINDO...)"
                            uiState.isProcessing -> "PROCESSANDO SINAL..."
                            uiState.isSpeaking -> "SINTETIZANDO RESPOSTA..."
                            uiState.isOnline -> "NÚCLEO ONLINE (GEMINI 3.5)"
                            else -> "NÚCLEO LOCAL OFFLINE (SQLITE)"
                        },
                        color = when {
                            uiState.isListening -> ArcCyan
                            uiState.isOnline -> ArcCyan
                            else -> StatusOffline
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Audio Mute/Unmute Toggle Button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("audio_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Audio Mute",
                            tint = if (uiState.isMuted) TextTertiary else ArcCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // 2. Active Focus Timer Bar (if running or set)
        if (uiState.isFocusTimerRunning || uiState.focusSecondsRemaining < uiState.focusTotalSeconds) {
            FocusTimerBanner(
                taskTitle = uiState.focusTaskTitle,
                secondsRemaining = uiState.focusSecondsRemaining,
                totalSeconds = uiState.focusTotalSeconds,
                isRunning = uiState.isFocusTimerRunning,
                onPauseToggle = onPauseFocus,
                onReset = onResetFocus
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // 3. Quick Action Holographic Chips & Shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Voice shortcut
            QuickCommandChip(
                "🎙️ Falar",
                accentColor = ArcCyan,
                onClick = { triggerVoice() }
            )
            // 40+ Commands Manual
            QuickCommandChip(
                "📖 Comandos (40+)",
                accentColor = ArcTeal,
                onClick = { onToggleCommandCatalog(true) }
            )
            // Python Engine Inspector
            QuickCommandChip(
                "🐍 Python Engine",
                accentColor = ArcBlue,
                onClick = { onTogglePythonBridge(true) }
            )
            QuickCommandChip("⚡ Foco 25m", onClick = { onStartFocus(25, "Sprint de Foco Alpha") })
            QuickCommandChip("🌅 Briefing", onClick = { onSendCommand("briefing") })
            QuickCommandChip("📊 Status", onClick = { onSendCommand("status") })
            QuickCommandChip("➕ Criar Atividade", onClick = { onCommandChange("Criar tarefa: ") })
            QuickCommandChip("🧮 Calcular", onClick = { onCommandChange("Calcular ") })
            QuickCommandChip("🧹 Limpar Concluídas", onClick = { onSendCommand("limpar concluidas") })
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Live Hologram Log Terminal (Conversations)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REGISTRO DE COMANDOS & RESPOSTAS",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isOnline) ArcCyan else StatusOnline)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${uiState.logs.size} LOGS",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        HudCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            borderColor = CyberBorder.copy(alpha = 0.8f),
            backgroundColor = CyberCardBg
        ) {
            if (uiState.logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = ArcCyan.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aguardando diretrizes de voz ou texto...",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Toque no reator ou no microfone para falar.",
                            color = TextTertiary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        TerminalLogBubble(log = log)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Bottom Interactive Command Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.commandInput,
                onValueChange = onCommandChange,
                placeholder = {
                    Text(
                        text = if (uiState.isOnline) "Instrua Jo'On via Gemini..." else "Diga ou digite um comando offline...",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (uiState.isOnline) ArcCyan else StatusOffline,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ArcCyan,
                    focusedContainerColor = CyberCardBg,
                    unfocusedContainerColor = CyberCardBg
                ),
                leadingIcon = {
                    IconButton(
                        onClick = { onToggleCommandCatalog(true) },
                        modifier = Modifier.size(32.dp).testTag("catalog_dock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "Catálogo 40+",
                            tint = ArcTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Voice Recognition Button
                        IconButton(
                            onClick = { triggerVoice() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isListening) ArcCyan.copy(alpha = 0.3f) else Color.Transparent)
                                .testTag("voice_recognition_dock_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Falar com Jo'On",
                                tint = if (uiState.isListening) ArcCrimson else ArcCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = { onSendCommand(null) },
                enabled = uiState.commandInput.isNotBlank() && !uiState.isProcessing,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (uiState.commandInput.isNotBlank()) ArcCyan else CyberSurfaceVariant
                    )
                    .testTag("send_command_button")
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = if (uiState.commandInput.isNotBlank()) Color(0xFF041E49) else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickCommandChip(
    label: String,
    accentColor: Color = ArcCyan,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberCardBg)
            .border(
                BorderStroke(0.8.dp, accentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun FocusTimerBanner(
    taskTitle: String,
    secondsRemaining: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onPauseToggle: () -> Unit,
    onReset: () -> Unit
) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    val progress = if (totalSeconds > 0) (secondsRemaining.toFloat() / totalSeconds.toFloat()) else 0f

    HudCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("focus_timer_banner"),
        borderColor = ArcGold.copy(alpha = 0.7f),
        backgroundColor = CyberCardBg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ArcGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ArcGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = taskTitle,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isRunning) "EM PROGRESSO • FOCO ATIVO" else "PAUSADO",
                        color = if (isRunning) ArcGold else TextTertiary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeFormatted,
                    color = ArcGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onPauseToggle,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pausar" else "Continuar",
                        tint = ArcGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Resetar",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TerminalLogBubble(log: LogEntity) {
    val isUser = log.sender == "USER"
    val isSystem = log.sender == "SYSTEM"

    val timeString = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Log meta line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isUser) "[VOCÊ]" else if (isSystem) "[SISTEMA]" else "[JO'ON]",
                color = when {
                    isUser -> ArcGold
                    isSystem -> ArcCrimson
                    else -> ArcCyan
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeString,
                color = TextTertiary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• ${log.mode}",
                color = if (log.mode == "ONLINE") ArcCyan else TextTertiary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Message Content Box
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 3.dp,
                        bottomEnd = if (isUser) 3.dp else 12.dp
                    )
                )
                .background(
                    when {
                        isUser -> Color(0xFF1C2430)
                        isSystem -> Color(0xFF251A1D)
                        else -> Color(0xFF161B22)
                    }
                )
                .border(
                    0.8.dp,
                    when {
                        isUser -> ArcGold.copy(alpha = 0.35f)
                        isSystem -> ArcCrimson.copy(alpha = 0.4f)
                        else -> ArcCyan.copy(alpha = 0.25f)
                    },
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 3.dp,
                        bottomEnd = if (isUser) 3.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = log.message,
                color = TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
