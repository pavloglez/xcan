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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jpdgbv.xcan.core.model.LogEntry
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

private val DeepCharcoal = Color(0xFF0D0D1A)
private val CharcoalSurface = Color(0xFF1A1A2E)
private val ElectricBlue = Color(0xFF00C8FF)
private val LightGrayText = Color(0xFFB0BEC5)
private val NeonAccent = Color(0xFF39FF14)

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            state.session?.let { session ->
                Text(
                    text = session.carLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = dateFormat.format(Date(session.startTimeMs)),
                    color = LightGrayText,
                    fontSize = 13.sp
                )
                session.durationMs?.let { ms ->
                    val mins = TimeUnit.MILLISECONDS.toMinutes(ms)
                    val secs = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                    Text(
                        text = "Duration: ${if (mins > 0) "${mins}m " else ""}${secs}s",
                        color = ElectricBlue,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricBlue)
            }
        } else if (state.entriesByPid.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data recorded in this session", color = LightGrayText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                items(
                    items = state.entriesByPid.entries.toList(),
                    key = { it.key }
                ) { (pid, entries) ->
                    val colorIndex = state.entriesByPid.keys.indexOf(pid) % ChartColors.size
                    MetricChart(
                        pid = pid,
                        entries = entries,
                        color = ChartColors[colorIndex]
                    )
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
            .background(CharcoalSurface, RoundedCornerShape(16.dp))
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
                color = LightGrayText,
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
        Text(label, color = LightGrayText, fontSize = 10.sp)
        Text(value, color = color, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
