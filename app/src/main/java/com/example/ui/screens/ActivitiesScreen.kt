package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityEntity
import com.example.ui.components.HudCard
import com.example.ui.components.PriorityChip
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcTeal
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ActivitiesScreen(
    activities: List<ActivityEntity>,
    totalCount: Int,
    completedCount: Int,
    pendingCount: Int,
    onToggleStatus: (ActivityEntity) -> Unit,
    onDeleteActivity: (Long) -> Unit,
    onAddActivity: (String, String, String, String, String, Int) -> Unit,
    onClearCompleted: () -> Unit,
    onStartFocus: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("TODAS") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredActivities = remember(activities, selectedFilter) {
        when (selectedFilter) {
            "PENDENTES" -> activities.filter { it.status != "CONCLUIDO" }
            "CONCLUIDAS" -> activities.filter { it.status == "CONCLUIDO" }
            "ALTA" -> activities.filter { it.priority == "ALTA" }
            "TRABALHO" -> activities.filter { it.category.equals("Trabalho", ignoreCase = true) }
            "ESTUDO" -> activities.filter { it.category.equals("Estudo", ignoreCase = true) }
            "SAUDE" -> activities.filter { it.category.equals("Saúde", ignoreCase = true) }
            else -> activities
        }
    }

    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPercent = (progress * 100).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // 1. Top HUD Overview Card
            HudCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                borderColor = CyberBorder,
                backgroundColor = CyberCardBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIRETÓRIO SQLITE DE ATIVIDADES",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$pendingCount pendentes  •  $completedCount concluídas",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Circular Completion Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = ArcTeal,
                            trackColor = CyberSurfaceVariant,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "$progressPercent%",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Filter Chips Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabChip("Todas ($totalCount)", isSelected = selectedFilter == "TODAS") { selectedFilter = "TODAS" }
                FilterTabChip("Pendentes ($pendingCount)", isSelected = selectedFilter == "PENDENTES") { selectedFilter = "PENDENTES" }
                FilterTabChip("Concluídas ($completedCount)", isSelected = selectedFilter == "CONCLUIDAS") { selectedFilter = "CONCLUIDAS" }
                FilterTabChip("Alta Prioridade", isSelected = selectedFilter == "ALTA") { selectedFilter = "ALTA" }
                FilterTabChip("Trabalho", isSelected = selectedFilter == "TRABALHO") { selectedFilter = "TRABALHO" }
                FilterTabChip("Estudo", isSelected = selectedFilter == "ESTUDO") { selectedFilter = "ESTUDO" }
                FilterTabChip("Saúde", isSelected = selectedFilter == "SAUDE") { selectedFilter = "SAUDE" }
            }

            // 3. Activities List
            if (filteredActivities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Nenhuma atividade",
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhuma atividade encontrada neste filtro.",
                            color = TextTertiary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredActivities, key = { it.id }) { activity ->
                        ActivityItemCard(
                            activity = activity,
                            onToggleStatus = { onToggleStatus(activity) },
                            onDelete = { onDeleteActivity(activity.id) },
                            onStartFocus = { onStartFocus(activity.estimatedMinutes, activity.title) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Activity
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_activity_fab"),
            containerColor = ArcCyan,
            contentColor = Color(0xFF002026),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nova Atividade",
                modifier = Modifier.size(24.dp)
            )
        }

        // Add Activity Dialog
        if (showAddDialog) {
            AddActivityDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, desc, cat, prio, due, mins ->
                    onAddActivity(title, desc, cat, prio, due, mins)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) ArcCyan.copy(alpha = 0.2f) else CyberSurfaceVariant.copy(alpha = 0.6f)
    val border = if (isSelected) ArcCyan else CyberBorder
    val textCol = if (isSelected) ArcCyan else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ActivityItemCard(
    activity: ActivityEntity,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    val isCompleted = activity.status == "CONCLUIDO"
    val borderColor = if (isCompleted) CyberBorder.copy(alpha = 0.4f) else CyberBorder

    HudCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_item_${activity.id}"),
        borderColor = borderColor,
        backgroundColor = if (isCompleted) Color(0xFF141618) else CyberCardBg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Checkbox Icon
            IconButton(
                onClick = onToggleStatus,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Concluir",
                    tint = if (isCompleted) ArcTeal else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PriorityChip(priority = activity.priority)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activity.category,
                            color = ArcBlue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (activity.dueDate.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Prazo",
                                tint = TextTertiary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = activity.dueDate,
                                color = TextTertiary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.title,
                    color = if (isCompleted) TextTertiary else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (activity.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activity.description,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
            }

            // Quick Focus / Delete Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isCompleted) {
                    IconButton(
                        onClick = onStartFocus,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Focar",
                            tint = ArcGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Trabalho") }
    var priority by remember { mutableStateOf("ALTA") }
    var dueDate by remember { mutableStateOf("Hoje") }
    var minutes by remember { mutableStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = {
            Text(
                text = "NOVA ATIVIDADE SQLITE",
                color = ArcCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Atividade", color = TextTertiary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Detalhes (Opcional)", color = TextTertiary, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection
                Text("Categoria:", color = TextSecondary, fontSize = 11.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Trabalho", "Estudo", "Saúde", "Projeto", "Pessoal", "Geral").forEach { cat ->
                        SelectableChip(label = cat, isSelected = category == cat) { category = cat }
                    }
                }

                // Priority selection
                Text("Prioridade:", color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALTA", "MÉDIA", "BAIXA").forEach { prio ->
                        SelectableChip(label = prio, isSelected = priority == prio) { priority = prio }
                    }
                }

                // Due Date selection
                Text("Prazo:", color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Hoje", "Amanhã", "Esta Semana").forEach { due ->
                        SelectableChip(label = due, isSelected = dueDate == due) { dueDate = due }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, category, priority, dueDate, minutes)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArcCyan,
                    contentColor = Color(0xFF002026)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Gravar no SQLite", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextTertiary)
            }
        }
    )
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) ArcCyan.copy(alpha = 0.25f) else CyberSurfaceVariant
    val border = if (isSelected) ArcCyan else CyberBorder
    val textCol = if (isSelected) ArcCyan else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(BorderStroke(0.8.dp, border), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
