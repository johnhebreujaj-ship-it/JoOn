package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TelemetryTopBar
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcTeal
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.JoOnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: JoOnViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberDarkBg),
                    topBar = {
                        TelemetryTopBar(
                            isOnline = uiState.isOnline,
                            currentTime = uiState.currentTime,
                            totalActivities = uiState.totalActivitiesCount,
                            pendingActivities = uiState.pendingActivitiesCount,
                            onToggleMode = { viewModel.toggleOnlineMode() },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CyberSurface,
                            contentColor = TextPrimary,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.setSelectedTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = "Comando"
                                    )
                                },
                                label = {
                                    Text(
                                        "Comando",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ArcCyan,
                                    selectedTextColor = ArcCyan,
                                    indicatorColor = ArcCyan.copy(alpha = 0.2f),
                                    unselectedIconColor = TextTertiary,
                                    unselectedTextColor = TextTertiary
                                ),
                                modifier = Modifier.testTag("nav_tab_command")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.setSelectedTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Checklist,
                                        contentDescription = "Atividades"
                                    )
                                },
                                label = {
                                    Text(
                                        "Atividades",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ArcTeal,
                                    selectedTextColor = ArcTeal,
                                    indicatorColor = ArcTeal.copy(alpha = 0.2f),
                                    unselectedIconColor = TextTertiary,
                                    unselectedTextColor = TextTertiary
                                ),
                                modifier = Modifier.testTag("nav_tab_activities")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 2,
                                onClick = { viewModel.setSelectedTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Protocolos"
                                    )
                                },
                                label = {
                                    Text(
                                        "Protocolos",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ArcCyan,
                                    selectedTextColor = ArcCyan,
                                    indicatorColor = ArcCyan.copy(alpha = 0.2f),
                                    unselectedIconColor = TextTertiary,
                                    unselectedTextColor = TextTertiary
                                ),
                                modifier = Modifier.testTag("nav_tab_protocols")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 3,
                                onClick = { viewModel.setSelectedTab(3) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Diagnóstico"
                                    )
                                },
                                label = {
                                    Text(
                                        "Diagnóstico",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (uiState.selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ArcCyan,
                                    selectedTextColor = ArcCyan,
                                    indicatorColor = ArcCyan.copy(alpha = 0.2f),
                                    unselectedIconColor = TextTertiary,
                                    unselectedTextColor = TextTertiary
                                ),
                                modifier = Modifier.testTag("nav_tab_diagnostics")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = uiState.selectedTab, label = "TabCrossfade") { tab ->
                            when (tab) {
                                0 -> CommandCenterScreen(
                                    uiState = uiState,
                                    commandCatalog = viewModel.nlpProcessor.commandCatalog,
                                    onCommandChange = { viewModel.onCommandInputChange(it) },
                                    onSendCommand = { viewModel.sendCommand(it) },
                                    onToggleMute = { viewModel.toggleMute() },
                                    onToggleMode = { viewModel.toggleOnlineMode() },
                                    onStartFocus = { mins, title -> viewModel.startFocusTimer(mins, title) },
                                    onPauseFocus = { viewModel.pauseFocusTimer() },
                                    onResetFocus = { viewModel.resetFocusTimer() },
                                    onStartVoiceListening = { viewModel.startVoiceListening() },
                                    onStopVoiceListening = { viewModel.stopVoiceListening() },
                                    onToggleCommandCatalog = { viewModel.setCommandCatalogOpen(it) },
                                    onTogglePythonBridge = { viewModel.setPythonBridgeModalOpen(it) }
                                )
                                1 -> ActivitiesScreen(
                                    activities = uiState.activities,
                                    totalCount = uiState.totalActivitiesCount,
                                    completedCount = uiState.completedActivitiesCount,
                                    pendingCount = uiState.pendingActivitiesCount,
                                    onToggleStatus = { viewModel.toggleActivityStatus(it) },
                                    onDeleteActivity = { viewModel.deleteActivity(it) },
                                    onAddActivity = { title, desc, cat, prio, due, mins ->
                                        viewModel.addActivity(title, desc, cat, prio, due, mins)
                                    },
                                    onClearCompleted = { viewModel.clearCompletedActivities() },
                                    onStartFocus = { mins, title ->
                                        viewModel.startFocusTimer(mins, title)
                                        viewModel.setSelectedTab(0)
                                    }
                                )
                                2 -> ProtocolsScreen(
                                    protocols = uiState.protocols,
                                    onExecuteProtocol = {
                                        viewModel.executeProtocol(it)
                                        viewModel.setSelectedTab(0)
                                    },
                                    onCreateProtocol = {
                                        viewModel.executeProtocol(it)
                                    }
                                )
                                3 -> DiagnosticsScreen(
                                    isOnline = uiState.isOnline,
                                    activities = uiState.activities,
                                    totalActivities = uiState.totalActivitiesCount,
                                    completedActivities = uiState.completedActivitiesCount,
                                    protocolsCount = uiState.protocols.size,
                                    logsCount = uiState.logs.size,
                                    onClearLogs = { viewModel.clearAllLogs() },
                                    onClearCompleted = { viewModel.clearCompletedActivities() },
                                    onToggleMode = { viewModel.toggleOnlineMode() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
