package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCrimson
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcTeal
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun VoiceListeningDialog(
    isListening: Boolean,
    rmsDb: Float,
    partialText: String,
    lastError: String?,
    onDismiss: () -> Unit,
    onConfirmCommand: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ArcCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("voice_listening_dialog"),
            color = CyberSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isListening) ArcCyan else ArcCrimson)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "ESCUTANDO COMANDOS..." else "PROCESSANDO ÁUDIO",
                            color = if (isListening) ArcCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_voice_modal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Audio Waveform / Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDarkBg)
                        .border(0.5.dp, CyberBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    VoiceWaveformVisualizer(
                        rmsDb = rmsDb,
                        isListening = isListening,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .padding(horizontal = 14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transcribed Text Display or Prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardBg)
                        .border(0.5.dp, ArcCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (partialText.isNotBlank()) {
                        Text(
                            text = "\"$partialText\"",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    } else if (lastError != null) {
                        Text(
                            text = "Aviso: $lastError\nToque no microfone para tentar novamente.",
                            color = ArcCrimson,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Fale agora: \"Criar tarefa...\", \"Protocolo foco\", \"Calcular...\", \"Status\"...",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Wake-word hint
                Text(
                    text = "Palavras-chave: \"Jo'On\", \"Jarvis\" ou qualquer um dos 40+ comandos offline",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("cancel_voice_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Text("Cancelar", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    ElevatedButton(
                        onClick = {
                            if (partialText.isNotBlank()) {
                                onConfirmCommand(partialText)
                            }
                        },
                        enabled = partialText.isNotBlank(),
                        modifier = Modifier.weight(1.2f).testTag("confirm_voice_btn"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = ArcCyan,
                            contentColor = Color(0xFF041E49)
                        )
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Executar", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceWaveformVisualizer(
    rmsDb: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barsCount = 28
        val barWidth = (width / barsCount) * 0.6f
        val barSpacing = width / barsCount

        for (i in 0 until barsCount) {
            val normalizedX = i.toFloat() / barsCount
            val wave = kotlin.math.sin(normalizedX * 4 * Math.PI.toFloat() + phase)
            val dynamicScale = if (isListening) {
                (rmsDb * 0.8f + 0.2f) * (0.4f + 0.6f * kotlin.math.abs(wave))
            } else {
                0.1f + 0.05f * kotlin.math.abs(wave)
            }

            val barHeight = (height * 0.85f * dynamicScale).coerceAtLeast(4.dp.toPx())
            val x = i * barSpacing + (barSpacing - barWidth) / 2f
            val top = centerY - barHeight / 2f

            val brush = Brush.verticalGradient(
                colors = listOf(
                    ArcCyan,
                    ArcTeal,
                    ArcBlue
                ),
                startY = top,
                endY = top + barHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }

        // Center line glow
        drawLine(
            color = ArcCyan.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
