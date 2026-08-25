package com.pavloglez.xcan.feature.logging

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavloglez.xcan.core.ui.components.staggerEnter
import com.pavloglez.xcan.core.ui.LocalHazeState
import dev.chrisbanes.haze.hazeSource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pavloglez.xcan.core.model.LogEntry
import com.pavloglez.xcan.core.ui.components.GlassTopAppBar
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


private val ChartColors = listOf(
    Color(0xFF00C8FF),
    Color(0xFF39FF14),
    Color(0xFFFFBF00),
    Color(0xFFFF2D55),
    Color(0xFFBF5AF2),
    Color(0xFFFF9500)
)

@Composable
fun LogSessionDetailRoute(
    sessionId: String,
    viewModel: LogSessionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LogSessionDetailScreen(state = state)
}

@Composable
fun LogSessionDetailScreen(
    state: LogSessionDetailState,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val title = if (state.session != null) "${state.session.carLabel} Session" else "Log Session"
            GlassTopAppBar(title = title)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Subtitle / Duration Info
            state.session?.let { session ->
                Column(modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(top = innerPadding.calculateTopPadding())
                ) {
                    Text(
                        text = dateFormat.format(Date(session.startTimeMs)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp
                    )
                    session.durationMs?.let { ms ->
                        val mins = TimeUnit.MILLISECONDS.toMinutes(ms)
                        val secs = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                        Text(
                            text = "Duration: ${if (mins > 0) "${mins}m " else ""}${secs}s",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.entriesByPid.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data recorded in this session", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).hazeSource(LocalHazeState.current),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    )
                ) {
                        itemsIndexed(
                            items = state.entriesByPid.entries.toList(),
                            key = { _, item -> item.key }
                        ) { index, (pid, entries) ->
                            val colorIndex = state.entriesByPid.keys.indexOf(pid) % ChartColors.size
                            MetricChart(
                                pid = pid,
                                entries = entries,
                                color = ChartColors[colorIndex],
                                modifier = Modifier.staggerEnter(index)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChart(
    pid: String,
    entries: List<LogEntry>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember(pid, entries) {
        CartesianChartModelProducer()
    }
    
    androidx.compose.runtime.LaunchedEffect(pid, entries) {
        val xValues = entries.mapIndexed { i, _ -> i.toFloat() }
        val yValues = entries.map { it.value }
        modelProducer.runTransaction {
            lineSeries { series(xValues, yValues) }
        }
    }

    val minVal = entries.minOfOrNull { it.value } ?: 0f
    val maxVal = entries.maxOfOrNull { it.value } ?: 0f
    val avgVal = if (entries.isNotEmpty()) entries.sumOf { it.value.toDouble() }.toFloat() / entries.size else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pid,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${entries.size} pts",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatChip("Min", "%.1f".format(minVal), color)
            StatChip("Avg", "%.1f".format(avgVal), color)
            StatChip("Max", "%.1f".format(maxVal), color)
        }

        Spacer(Modifier.height(12.dp))

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(fill(color))
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom()
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
        Text(value, color = color, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
