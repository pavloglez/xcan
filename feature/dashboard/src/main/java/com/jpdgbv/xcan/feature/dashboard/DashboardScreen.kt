package com.jpdgbv.xcan.feature.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.jpdgbv.xcan.core.ui.components.bounceClick
import com.jpdgbv.xcan.core.ui.components.pressBounce
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jpdgbv.xcan.core.bluetooth.ScannedDevice
import com.jpdgbv.xcan.core.ui.theme.DeepCharcoal
import com.jpdgbv.xcan.core.ui.theme.ElectricBlue
import com.jpdgbv.xcan.core.ui.theme.NeonAccent
import androidx.compose.ui.tooling.preview.Preview
import com.jpdgbv.xcan.core.ui.theme.XCanTheme
import com.jpdgbv.xcan.core.ui.theme.XCanEasing
import androidx.compose.animation.core.LinearEasing
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.model.TelemetryFrame
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var showAddCarDialog by remember { mutableStateOf(false) }
    var showCarSelectDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showConfigSheet by remember { mutableStateOf(false) }
    var showCarPickerForLogging by remember { mutableStateOf(false) }

    val configSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val carPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val hasPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
        androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
        androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val hasRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                result[Manifest.permission.BLUETOOTH_SCAN] == true &&
                result[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else {
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            }
            if (hasRequired) {
                showDialog = true
                viewModel.onIntent(DashboardIntent.StartScanning)
            } else {
                android.widget.Toast.makeText(context, "Bluetooth permissions are required", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.onIntent(DashboardIntent.ScanSensors)
    }

    if (showDialog) {
        DeviceSelectionDialog(
            devices = state.discoveredDevices,
            onDeviceSelected = { mac ->
                showDialog = false
                viewModel.onIntent(DashboardIntent.Connect(mac))
            },
            onDismiss = { 
                showDialog = false
                viewModel.onIntent(DashboardIntent.StopScanning)
            }
        )
    }

    if (showCarSelectDialog) {
        AlertDialog(
            onDismissRequest = { showCarSelectDialog = false },
            title = { Text("Select Vehicle") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.cars) { car ->
                        val isActive = car.id == state.activeCar?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick { 
                                    viewModel.onIntent(DashboardIntent.SelectCar(car.id))
                                    showCarSelectDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = car.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isActive) ElectricBlue else MaterialTheme.colorScheme.onSurface
                            )
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = ElectricBlue
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showCarSelectDialog = false
                    showAddCarDialog = true 
                }) {
                    Text("Add New")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCarSelectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showAddCarDialog) {
        var make by remember { mutableStateOf("") }
        var model by remember { mutableStateOf("") }
        var year by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCarDialog = false },
            title = { Text("Add Vehicle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = make,
                        onValueChange = { make = it },
                        label = { Text("Make (e.g. Toyota)") }
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model (e.g. Camry)") }
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year (e.g. 2018)") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.onIntent(DashboardIntent.AddCar(make, model, year.toIntOrNull() ?: 2000))
                    showAddCarDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCarDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogsDialog) {
        ConnectionLogsDialog(
            logs = state.connectionLogs,
            onDismiss = { showLogsDialog = false }
        )
    }

    // Config bottom sheet
    if (showConfigSheet) {
        com.jpdgbv.xcan.feature.dashboard.ui.DashboardConfigBottomSheet(
            allSensors = state.allKnownSensors,
            selectedPids = state.selectedSensors,
            sheetState = configSheetState,
            onToggleSensor = { pid, checked ->
                val newSet = if (checked) state.selectedSensors + pid else state.selectedSensors - pid
                viewModel.onIntent(DashboardIntent.SetSelectedSensors(newSet))
            },
            onDismiss = { showConfigSheet = false }
        )
    }

    // Car picker for logging
    if (showCarPickerForLogging) {
        com.jpdgbv.xcan.feature.dashboard.ui.CarPickerBottomSheet(
            cars = state.cars,
            sheetState = carPickerSheetState,
            onCarSelected = { car ->
                showCarPickerForLogging = false
                viewModel.onIntent(DashboardIntent.StartLogging(car.id, car.name))
            },
            onDismiss = { showCarPickerForLogging = false }
        )
    }

    DashboardScreen(
        state = state,
        onConnect = {
            if (hasPermissions) {
                showDialog = true
                viewModel.onIntent(DashboardIntent.StartScanning)
            } else {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        },
        onDisconnect = { viewModel.onIntent(DashboardIntent.Disconnect) },
        onShowCarSelect = { showCarSelectDialog = true },
        onShowLogs = { showLogsDialog = true },
        onShowConfig = { showConfigSheet = true },
        onStartLog = { showCarPickerForLogging = true },
        onPauseLog = { viewModel.onIntent(DashboardIntent.PauseLogging) },
        onResumeLog = { viewModel.onIntent(DashboardIntent.ResumeLogging) },
        onStopLog = { viewModel.onIntent(DashboardIntent.StopLogging) }
    )
}

@Composable
fun DeviceSelectionDialog(
    devices: List<ScannedDevice>,
    onDeviceSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select OBD-II Adapter") },
        text = {
            if (devices.isEmpty()) {
                Text("Scanning for devices...")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    items(devices) { device ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick { onDeviceSelected(device.macAddress) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(device.name, fontWeight = FontWeight.Bold)
                            Text(device.macAddress, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onShowCarSelect: () -> Unit,
    onShowLogs: () -> Unit,
    onShowConfig: () -> Unit,
    onStartLog: () -> Unit,
    onPauseLog: () -> Unit,
    onResumeLog: () -> Unit,
    onStopLog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "XCan Telemetry",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = state.activeCar?.name ?: "No Vehicle Selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricBlue,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row {
                IconButton(onClick = onShowLogs) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = "View Logs",
                        tint = ElectricBlue
                    )
                }
                IconButton(onClick = onShowConfig) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure",
                        tint = ElectricBlue
                    )
                }
                IconButton(onClick = onShowCarSelect) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Select Vehicle",
                        tint = ElectricBlue
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.selectedSensors.toList()) { pid ->
                val sensorEnum = state.allKnownSensors.find { it.pid == pid }
                if (sensorEnum != null) {
                    val rawValue = state.telemetry?.sensors?.get(pid) ?: 0f
                    val displayValue = if (sensorEnum.pid == "010D" && !state.useMetric) {
                        (rawValue * 0.621371).toFloat()
                    } else if (sensorEnum.unit == "°C" && !state.useMetric) {
                        (rawValue * 9/5) + 32
                    } else {
                        rawValue
                    }
                    val unit = if (sensorEnum.pid == "010D") {
                        if (state.useMetric) "km/h" else "mph"
                    } else if (sensorEnum.unit == "°C") {
                        if (state.useMetric) "°C" else "°F"
                    } else {
                        sensorEnum.unit
                    }
                    
                    val maxVal = if (sensorEnum.pid == "010C") 8000f 
                                 else if (sensorEnum.pid == "010D") (if(state.useMetric) 220f else 140f) 
                                 else 100f // Fallback for others

                    TelemetryDial(
                        title = sensorEnum.displayName,
                        value = displayValue,
                        maxValue = maxVal,
                        label = unit,
                        color = ElectricBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isConnected) {
            val interactionSource = remember { MutableInteractionSource() }
            Button(
                onClick = onDisconnect,
                interactionSource = interactionSource,
                modifier = Modifier.pressBounce(interactionSource),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Disconnect")
            }
        } else if (state.isConnecting) {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.alpha(alpha),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = ElectricBlue,
                    disabledContentColor = Color.White
                )
            ) {
                Text("Connecting...")
            }
        } else {
            val interactionSource = remember { MutableInteractionSource() }
            Button(
                onClick = onConnect,
                interactionSource = interactionSource,
                modifier = Modifier.pressBounce(interactionSource),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Connect OBD-II")
            }
        }
    } // end Column

    // Glassmorphism floating log control — bottom-center
    com.jpdgbv.xcan.feature.dashboard.ui.LogFloatingControl(
        loggingState = state.loggingState,
        onPause = onPauseLog,
        onResume = onResumeLog,
        onStop = onStopLog,
        onStartLog = onStartLog,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(end = 16.dp, bottom = 16.dp)
    )
    } // end Box
}

@Composable
fun TelemetryDial(
    title: String,
    value: Float,
    maxValue: Float,
    label: String,
    color: Color
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 300, easing = XCanEasing.EaseOut)
    )
    val sweepAngle = (animatedValue / maxValue) * 240f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
                //.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.minDimension - strokeWidth
            val radius = arcSize / 2f
            val verticalShift = arcSize / 8f
            val arcOffset = androidx.compose.ui.geometry.Offset(
                (size.width - arcSize) / 2f,
                (size.height - arcSize) / 2f + verticalShift
            )
            val center = androidx.compose.ui.geometry.Offset(
                arcOffset.x + radius,
                arcOffset.y + radius
            )

            withTransform({
                rotate(degrees = 150f, pivot = center)
            }) {
                // Background track
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.4f),
                    startAngle = 0f,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = arcOffset,
                    size = Size(arcSize, arcSize)
                )

                if (sweepAngle > 0.5f) {
                    val brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                        0f to color.copy(alpha = 0.0f),
                        (sweepAngle / 360f) to color,
                        1.0f to Color.Transparent,
                        center = center
                    )
                    
                    // Tip coordinates for glow and needle
                    val currentAngleRadians = Math.toRadians(sweepAngle.toDouble()).toFloat()
                    val tipX = center.x + radius * kotlin.math.cos(currentAngleRadians)
                    val tipY = center.y + radius * kotlin.math.sin(currentAngleRadians)
                    val tipOffset = androidx.compose.ui.geometry.Offset(tipX, tipY)

                    // Clip path to cut the glow strictly at the needle's edge
                    val glowClipPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x, center.y)
                        val outerRadius = radius + strokeWidth * 10f
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                center.x - outerRadius,
                                center.y - outerRadius,
                                center.x + outerRadius,
                                center.y + outerRadius
                            ),
                            startAngleDegrees = -30f,
                            sweepAngleDegrees = sweepAngle + 30f,
                            forceMoveTo = false
                        )
                        close()
                    }

                    clipPath(glowClipPath) {
                        // Gradient Glow (Blurred Arc)
                        drawIntoCanvas { canvas ->
                            val glowPaint = androidx.compose.ui.graphics.Paint().apply {
                                this.strokeWidth = strokeWidth * 2f
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeCap = StrokeCap.Butt
                            }
                            glowPaint.asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
                                strokeWidth * 1.5f,
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                            brush.applyTo(size, glowPaint, 1f)
                            
                            canvas.drawArc(
                                left = arcOffset.x,
                                top = arcOffset.y,
                                right = arcOffset.x + arcSize,
                                bottom = arcOffset.y + arcSize,
                                startAngle = 0f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                paint = glowPaint
                            )
                        }

                        // Tip Glow
                        val glowRadius = strokeWidth * 3.5f
                        drawCircle(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.65f), Color.Transparent),
                                center = tipOffset,
                                radius = glowRadius
                            ),
                            radius = glowRadius,
                            center = tipOffset
                        )
                    }

                    // Foreground value (gradient)
                    drawArc(
                        brush = brush,
                        startAngle = 0f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        topLeft = arcOffset,
                        size = Size(arcSize, arcSize)
                    )

                    // Needle (White line)
                    val needleLength = strokeWidth * 1.5f
                    val innerX = center.x + (radius - needleLength / 2f) * kotlin.math.cos(currentAngleRadians)
                    val innerY = center.y + (radius - needleLength / 2f) * kotlin.math.sin(currentAngleRadians)
                    val outerX = center.x + (radius + needleLength / 2f) * kotlin.math.cos(currentAngleRadians)
                    val outerY = center.y + (radius + needleLength / 2f) * kotlin.math.sin(currentAngleRadians)

                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(innerX, innerY),
                        end = androidx.compose.ui.geometry.Offset(outerX, outerY),
                        strokeWidth = strokeWidth * 0.35f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toInt().toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        } // Closes the Box
        
        Text(
            text = title,
            color = Color.LightGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding( bottom = 8.dp)
        )
    } // Closes the outer Column
}


@Composable
fun ConnectionLogsDialog(
    logs: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connection Logs") },
        text = {
            if (logs.isEmpty()) {
                Text("No logs available.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    XCanTheme {
        DashboardScreen(
            state = DashboardState(
                connectionStatus = ConnectionStatus.CONNECTED,
                activeCar = CarProfile(id = "1", name = "Test Car", make = "Toyota", model = "Camry", year = 2020, isActive = true),
                useMetric = true,
                selectedSensors = setOf("010C", "010D"),
                telemetry = TelemetryFrame(
                    id = "1", 
                    timestampMs = System.currentTimeMillis(), 
                    sensors = mapOf("010C" to 2500f, "010D" to 60f)
                )
            ),
            onConnect = {},
            onDisconnect = {},
            onShowCarSelect = {},
            onShowLogs = {},
            onShowConfig = {},
            onStartLog = {},
            onPauseLog = {},
            onResumeLog = {},
            onStopLog = {}
        )
    }
}

@Preview
@Composable
fun TelemetryDialPreview() {
    XCanTheme {
        TelemetryDial(
            "RPM",
            value = 4000F,
            maxValue = 9000F,
            label = "Rpms",
            ElectricBlue
        )
    }
}
