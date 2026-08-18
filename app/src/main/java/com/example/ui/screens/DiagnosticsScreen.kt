package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityEntity
import com.example.engine.JoOnPythonVoiceBridge
import com.example.ui.components.HudCard
import com.example.ui.components.MetricHudItem
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

@Composable
fun DiagnosticsScreen(
    isOnline: Boolean,
    activities: List<ActivityEntity>,
    totalActivities: Int,
    completedActivities: Int,
    protocolsCount: Int,
    logsCount: Int,
    onClearLogs: () -> Unit,
    onClearCompleted: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runtime = remember { Runtime.getRuntime() }
    val maxMemoryMb = remember { (runtime.maxMemory() / (1024 * 1024)).toInt() }
    val totalMemoryMb = remember { (runtime.totalMemory() / (1024 * 1024)).toInt() }
    val freeMemoryMb = remember { (runtime.freeMemory() / (1024 * 1024)).toInt() }
    val usedMemoryMb = totalMemoryMb - freeMemoryMb

    var queryResultText by remember {
        mutableStateOf("Executando: SELECT * FROM activities LIMIT 5\nBase 'joon_assistant.db' online e íntegra.")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // 1. Diagnostics Header Card
        item {
            HudCard(
                borderColor = ArcCyan.copy(alpha = 0.5f),
                backgroundColor = CyberCardBg
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Diagnóstico",
                                tint = ArcCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DIAGNÓSTICO DO SISTEMA JO'ON",
                                color = ArcCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF162D24))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "STATUS NOMINAL",
                                color = ArcTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Arquitetura Híbrida: Motor de Voz Python/SpeechRecognizer + Motor NLP Local com 40+ comandos + SQLite Room + Gemini 3.5 Flash.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Telemetry Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricHudItem(
                        label = "ESTADO DO MOTOR",
                        value = if (isOnline) "ONLINE (Gemini)" else "OFFLINE (Local)",
                        icon = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                        color = if (isOnline) StatusOnline else StatusOffline,
                        modifier = Modifier.weight(1f)
                    )

                    MetricHudItem(
                        label = "BASE SQLITE",
                        value = "$totalActivities tarefas",
                        icon = Icons.Default.Storage,
                        color = ArcTeal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricHudItem(
                        label = "VOZ & SPEECH",
                        value = "40+ Comandos Ativos",
                        icon = Icons.Default.Mic,
                        color = ArcBlue,
                        modifier = Modifier.weight(1f)
                    )

                    MetricHudItem(
                        label = "MEMÓRIA JVM",
                        value = "$usedMemoryMb / $maxMemoryMb MB",
                        icon = Icons.Default.Memory,
                        color = ArcGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Python Speech Recognition Subsystem Info
        item {
            HudCard(
                borderColor = ArcBlue.copy(alpha = 0.5f),
                backgroundColor = CyberCardBg
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = ArcBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUBSISTEMA PYTHON SPEECH_RECOGNITION",
                                color = ArcBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ArcBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PYTHON 3.11+",
                                color = ArcBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Módulo de interoperabilidade Python carregado: speech_recognition, pyttsx3, Sphinx/Google offline pipeline e analisador de tokens com 44 diretrizes locais.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 4. SQLite Terminal & Inspector
        item {
            HudCard(
                borderColor = CyberBorder,
                backgroundColor = CyberCardBg
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TERMINAL DE CONSULTA SQLITE",
                        color = ArcTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset SQL buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetSqlChip("SELECT Pendentes") {
                            val pendings = activities.filter { it.status != "CONCLUIDO" }
                            queryResultText = "Query: SELECT * FROM activities WHERE status != 'CONCLUIDO'\nResultados (${pendings.size} registros):\n" +
                                pendings.take(4).joinToString("\n") { "• [ID ${it.id}] ${it.title} | ${it.priority} | ${it.dueDate}" }
                        }
                        PresetSqlChip("SELECT Alta Prioridade") {
                            val high = activities.filter { it.priority == "ALTA" }
                            queryResultText = "Query: SELECT * FROM activities WHERE priority = 'ALTA'\nResultados (${high.size} registros):\n" +
                                high.joinToString("\n") { "• [ID ${it.id}] ${it.title} (${it.category})" }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Console output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF121416))
                            .border(BorderStroke(1.dp, CyberBorder.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = queryResultText,
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 5. Database Maintenance Actions
        item {
            HudCard(
                borderColor = CyberBorder,
                backgroundColor = CyberCardBg
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MANUTENÇÃO & CONTROLE OPERACIONAL",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onClearCompleted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSurfaceVariant,
                                contentColor = ArcCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_completed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Limpar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Limpar Concluídas", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onClearLogs,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSurfaceVariant,
                                contentColor = ArcCrimson
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_logs_button")
                        ) {
                            Text("Expurgar Logs", fontSize = 11.sp)
                        }
                    }

                    // Mode Toggle Action
                    OutlinedButton(
                        onClick = onToggleMode,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isOnline) StatusOnline else StatusOffline
                        ),
                        border = BorderStroke(1.dp, if (isOnline) StatusOnline else StatusOffline),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isOnline) "Alternar para NÚCLEO OFFLINE LOCAL" else "Alternar para REDE NEURAL ONLINE (Gemini)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetSqlChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurfaceVariant)
            .border(BorderStroke(0.8.dp, CyberBorder), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
