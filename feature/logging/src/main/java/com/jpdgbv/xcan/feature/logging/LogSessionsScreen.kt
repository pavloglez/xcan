package com.jpdgbv.xcan.feature.logging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jpdgbv.xcan.core.model.LogSession
import com.jpdgbv.xcan.core.ui.components.GlassTopAppBar
import com.jpdgbv.xcan.core.ui.components.bounceClick
import com.jpdgbv.xcan.core.ui.components.staggerEnter
import com.jpdgbv.xcan.core.ui.LocalHazeState
import dev.chrisbanes.haze.hazeSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val DeepCharcoal = Color(0xFF0D0D1A)
private val CharcoalSurface = Color(0xFF1A1A2E)
private val ElectricBlue = Color(0xFF00C8FF)
private val LightGrayText = Color(0xFFB0BEC5)
private val NeonRed = Color(0xFFFF2D55)

@Composable
fun LogSessionsRoute(
    onSessionClick: (String) -> Unit,
    viewModel: LogSessionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            containerColor = CharcoalSurface,
            title = { Text("Delete All Sessions?", color = Color.White) },
            text = { Text("This will permanently remove all logged sessions and their data.", color = LightGrayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onIntent(LogSessionsIntent.ConfirmDeleteAll)
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("Cancel") }
            }
        )
    }

    LogSessionsScreen(
        sessions = state.sessions,
        isLoading = state.isLoading,
        onSessionClick = onSessionClick,
        onDeleteSession = { viewModel.onIntent(LogSessionsIntent.DeleteSession(it)) },
        onDeleteAll = { showDeleteAllDialog = true }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSessionsScreen(
    sessions: List<LogSession>,
    isLoading: Boolean,
    onSessionClick: (String) -> Unit,
    onDeleteSession: (LogSession) -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepCharcoal,
        topBar = {
            GlassTopAppBar(
                title = "Logged Sessions (${sessions.size})",
                actions = {
                    if (sessions.isNotEmpty()) {
                        IconButton(onClick = onDeleteAll) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = "Delete All",
                                tint = NeonRed
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (sessions.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = LightGrayText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No sessions yet", color = LightGrayText, fontSize = 16.sp)
                    Text("Start logging from the dashboard", color = LightGrayText.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(DeepCharcoal).hazeSource(LocalHazeState.current),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 80.dp // To account for floating nav bar
                )
            ) {
                itemsIndexed(sessions, key = { _, session -> session.id }) { index, session ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onDeleteSession(session)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        modifier = Modifier.staggerEnter(index = index, baseDelay = 30),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(NeonRed)
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        }
                    ) {
                        SessionCard(
                            session = session,
                            onClick = { onSessionClick(session.id) }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun SessionCard(
    session: LogSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val durationStr = session.durationMs?.let { ms ->
        val mins = TimeUnit.MILLISECONDS.toMinutes(ms)
        val secs = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    } ?: "Active"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepCharcoal)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CharcoalSurface, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = session.carLabel,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = dateFormat.format(Date(session.startTimeMs)),
                        color = LightGrayText,
                        fontSize = 12.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationStr,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                if (session.isActive) {
                    Text("Recording", color = Color(0xFFFF2D55), fontSize = 11.sp)
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color.White.copy(alpha = 0.05f))
    )
}
