package com.pavloglez.xcan.feature.dashboard

import com.pavloglez.xcan.core.model.ObdConstants
import androidx.compose.ui.res.stringResource
import com.pavloglez.xcan.core.ui.R

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
import com.pavloglez.xcan.core.ui.components.bounceClick
import com.pavloglez.xcan.core.ui.components.pressBounce
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.pavloglez.xcan.core.ui.components.staggerEnter
import com.pavloglez.xcan.core.bluetooth.ScannedDevice
import com.pavloglez.xcan.core.ui.LocalHazeState
import dev.chrisbanes.haze.hazeSource
import com.pavloglez.xcan.core.ui.components.GlassTopAppBar
import com.pavloglez.xcan.core.ui.theme.DeepCharcoal
import com.pavloglez.xcan.core.ui.theme.ElectricBlue
import com.pavloglez.xcan.core.ui.theme.NeonAccent
import androidx.compose.ui.tooling.preview.Preview
import com.pavloglez.xcan.core.ui.theme.XCanTheme
import com.pavloglez.xcan.core.ui.theme.XCanEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.ui.Alignment
import com.pavloglez.xcan.core.model.CarProfile
import com.pavloglez.xcan.core.model.TelemetryFrame
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.feature.dashboard.ui.LogFloatingControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    carProfileViewModel: CarProfileViewModel = hiltViewModel(),
    loggingViewModel: LoggingViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dashboardState by viewModel.state.collectAsStateWithLifecycle()
    val connectionState by connectionViewModel.state.collectAsStateWithLifecycle()
    val carState by carProfileViewModel.state.collectAsStateWithLifecycle()
    val loggingViewState by loggingViewModel.state.collectAsStateWithLifecycle()

    // Combine into a single facade state for DashboardScreen compatibility
    val state = remember(dashboardState, connectionState, carState, loggingViewState) {
        DashboardUIState(
            connectionStatus = connectionState.connectionStatus,
            telemetry = if (connectionState.isConnected) dashboardState.telemetry else null,
            connectionLogs = connectionState.connectionLogs,
            discoveredDevices = connectionState.discoveredDevices,
            isScanning = connectionState.isScanning,
            cars = carState.cars,
            activeCar = carState.activeCar,
            useMetric = dashboardState.useMetric,
            supportedSensors = dashboardState.supportedSensors,
            selectedSensors = dashboardState.selectedSensors,
            allKnownSensors = dashboardState.allKnownSensors,
            loggingState = loggingViewState.loggingState,
            isTrackMode = dashboardState.isTrackMode
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var showAddCarDialog by remember { mutableStateOf(false) }
    var showCarSelectDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showConfigSheet by remember { mutableStateOf(false) }
    var showCarPickerForLogging by remember { mutableStateOf(false) }

    val configSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val carPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val list = mutableListOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val hasPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val hasBluetooth = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
        hasBluetooth && hasNotification
    } else {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    val permissionBluetoothRequiredStr = stringResource(R.string.permission_bluetooth_required)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val hasRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val bluetoothOk = result[Manifest.permission.BLUETOOTH_SCAN] == true &&
                        result[Manifest.permission.BLUETOOTH_CONNECT] == true
                val notificationOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result[Manifest.permission.POST_NOTIFICATIONS] == true
                } else true
                bluetoothOk && notificationOk
            } else {
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            }
            if (hasRequired) {
                showDialog = true
                connectionViewModel.startScanning()
            } else {
                android.widget.Toast.makeText(
                    context,
                    permissionBluetoothRequiredStr,
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    if (showDialog) {
        DeviceSelectionDialog(
            devices = state.discoveredDevices,
            onDeviceSelected = { mac ->
                showDialog = false
                connectionViewModel.connect(mac)
            },
            onDismiss = {
                showDialog = false
                connectionViewModel.stopScanning()
            }
        )
    }

    if (showCarSelectDialog) {
        AlertDialog(
            onDismissRequest = { showCarSelectDialog = false },
            title = { Text(stringResource(R.string.dialog_title_select_vehicle)) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.cars) { car ->
                        val isActive = car.id == state.activeCar?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick {
                                    carProfileViewModel.selectCar(car.id)
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
                                    contentDescription = stringResource(R.string.cd_active_vehicle),
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
                    Text(stringResource(R.string.btn_add_new_vehicle))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCarSelectDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
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
            title = { Text(stringResource(R.string.dialog_title_add_vehicle)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = make,
                        onValueChange = { make = it },
                        label = { Text(stringResource(R.string.label_car_make_hint)) }
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text(stringResource(R.string.label_car_model_hint)) }
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text(stringResource(R.string.label_car_year_hint)) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    carProfileViewModel.addCar(
                        make,
                        model,
                        year.toIntOrNull() ?: ObdConstants.DEFAULT_FALLBACK_CAR_YEAR
                    )
                    showAddCarDialog = false
                }) {
                    Text(stringResource(R.string.action_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCarDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
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
        com.pavloglez.xcan.feature.dashboard.ui.DashboardConfigBottomSheet(
            allSensors = state.supportedSensors,
            selectedPids = state.selectedSensors,
            sheetState = configSheetState,
            onToggleSensor = { pid, checked ->
                val newSet =
                    if (checked) state.selectedSensors + pid else state.selectedSensors - pid
                viewModel.setSelectedSensors(newSet)
            },
            onDismiss = { showConfigSheet = false }
        )
    }

    // Car picker for logging
    if (showCarPickerForLogging) {
        com.pavloglez.xcan.feature.dashboard.ui.CarPickerBottomSheet(
            cars = state.cars,
            sheetState = carPickerSheetState,
            onCarSelected = { car ->
                showCarPickerForLogging = false
                loggingViewModel.startLogging(car.id, car.name)
            },
            onDismiss = { showCarPickerForLogging = false }
        )
    }

    val toastConnectVehicleFirstStr = stringResource(R.string.toast_connect_vehicle_first)
    DashboardScreen(
        state = state,
        onConnect = {
            if (hasPermissions) {
                showDialog = true
                connectionViewModel.startScanning()
            } else {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        },
        onDisconnect = { connectionViewModel.disconnect() },
        onShowCarSelect = { showCarSelectDialog = true },
        onShowLogs = { showLogsDialog = true },
        onShowConfig = { showConfigSheet = true },
        onStartLog = {
            if (!state.isConnected) {
                android.widget.Toast.makeText(
                    context,
                    toastConnectVehicleFirstStr,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                showCarPickerForLogging = true
            }
        },
        onPauseLog = { loggingViewModel.pauseLogging() },
        onResumeLog = { loggingViewModel.resumeLogging() },
        onStopLog = { loggingViewModel.stopLogging() },
        onToggleTrackMode = { viewModel.toggleTrackMode() }
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
        title = { Text(stringResource(R.string.dialog_title_select_adapter)) },
        text = {
            if (devices.isEmpty()) {
                Text(stringResource(R.string.scanning_for_devices))
            } else {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    items(devices) { device ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick { onDeviceSelected(device.macAddress) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(device.name, fontWeight = FontWeight.Bold)
                            Text(
                                device.macAddress,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUIState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onShowCarSelect: () -> Unit,
    onShowLogs: () -> Unit,
    onShowConfig: () -> Unit,
    onStartLog: () -> Unit,
    onPauseLog: () -> Unit,
    onResumeLog: () -> Unit,
    onStopLog: () -> Unit,
    onToggleTrackMode: () -> Unit
) {
    val neonRed = Color(0xFFFF2D55)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepCharcoal,
        topBar = {
            val defaultTitle = stringResource(R.string.app_name)
            GlassTopAppBar(
                titleContent = {
                    Text(defaultTitle, color = MaterialTheme.colorScheme.onSurface)
                },

                actions = {
                    IconButton(onClick = onShowLogs) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = stringResource(R.string.cd_view_logs),
                            tint = ElectricBlue
                        )
                    }
                    IconButton(onClick = onShowConfig) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_configure_dashboard),
                            tint = ElectricBlue
                        )
                    }
                    IconButton(onClick = onShowCarSelect) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = stringResource(R.string.dialog_title_select_vehicle),
                            tint = ElectricBlue
                        )
                    }

                    val infiniteTransition = rememberInfiniteTransition(label = "trackPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (state.isTrackMode) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(32.dp)
                                    .width(80.dp)
                                    .scale(pulseScale)
                                    .background(
                                        neonRed.copy(alpha = pulseAlpha),
                                        RoundedCornerShape(16.dp)
                                    )
                            )
                        }

                        val pulsingButtonColor = if (state.isTrackMode) {
                            androidx.compose.ui.graphics.lerp(
                                start = neonRed,
                                stop = Color(0x0CFF8888),
                                fraction = (0.5f - pulseAlpha) * 2f
                            )
                        } else {
                            NeonAccent
                        }

                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = pulsingButtonColor,
                            onClick = onToggleTrackMode,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(32.dp)
                                .defaultMinSize(minWidth = 80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.btn_track_mode),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepCharcoal)
                    .hazeSource(LocalHazeState.current),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = 200.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(state.selectedSensors.toList()) { index, pid ->
                    val sensorEnum = state.allKnownSensors.find { it.pid == pid }
                    if (sensorEnum != null) {
                        val rawValue = state.telemetry?.sensors?.get(pid) ?: 0f
                        val displayValue =
                            if (sensorEnum.pid == ObdConstants.PID_VEHICLE_SPEED && !state.useMetric) {
                                (rawValue * ObdConstants.KMH_TO_MPH)
                            } else if (sensorEnum.unit == stringResource(R.string.unit_celsius) && !state.useMetric) {
                                (rawValue * 9 / 5) + 32
                            } else {
                                rawValue
                            }
                        val unit = if (sensorEnum.pid == ObdConstants.PID_VEHICLE_SPEED) {
                            if (state.useMetric) stringResource(R.string.unit_kmh) else stringResource(
                                R.string.unit_mph
                            )
                        } else if (sensorEnum.unit == stringResource(R.string.unit_celsius)) {
                            if (state.useMetric) stringResource(R.string.unit_celsius) else stringResource(
                                R.string.unit_fahrenheit
                            )
                        } else {
                            sensorEnum.unit
                        }

                        val maxVal =
                            if (sensorEnum.pid == ObdConstants.PID_ENGINE_RPM) ObdConstants.DEFAULT_MAX_RPM
                            else if (sensorEnum.pid == ObdConstants.PID_VEHICLE_SPEED) (if (state.useMetric) ObdConstants.DEFAULT_MAX_SPEED_KMH else ObdConstants.DEFAULT_MAX_SPEED_MPH)
                            else 100f // Fallback for others

                        TelemetryDial(
                            title = sensorEnum.displayName,
                            value = displayValue,
                            maxValue = maxVal,
                            label = unit,
                            color = ElectricBlue,
                            isTrackMode = state.isTrackMode,
                            modifier = Modifier.staggerEnter(index)
                        )
                    }
                }
            }

            // Connection Button - floating bottom center
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 100.dp, end = 16.dp)
            ) {
                if (state.isConnected) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = onDisconnect,
                        interactionSource = interactionSource,
                        modifier = Modifier.pressBounce(interactionSource),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.btn_disconnect))
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
                            disabledContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.btn_connecting))
                    }
                } else {
                    val interactionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = onConnect,
                        interactionSource = interactionSource,
                        modifier = Modifier.pressBounce(interactionSource),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text(stringResource(R.string.btn_connect_obd))
                    }
                }
            }

            // Glassmorphism floating log control — bottom-center
            if (state.connectionStatus == ConnectionStatus.CONNECTED) {
                LogFloatingControl(
                    loggingState = state.loggingState,
                    onPause = onPauseLog,
                    onResume = onResumeLog,
                    onStop = onStopLog,
                    onStartLog = onStartLog,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 160.dp)
                )
            }
        }
    }
}

@Composable
fun TelemetryDial(
    title: String,
    value: Float,
    maxValue: Float,
    label: String,
    color: Color,
    isTrackMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 300, easing = XCanEasing.EaseOut)
    )
    val sweepAngle = (animatedValue / maxValue) * 240f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val baseStrokeWidth = if (isTrackMode) 24.dp.toPx() else 14.dp.toPx()
                val strokeWidth = baseStrokeWidth
                val arcSize = size.minDimension - strokeWidth
                val radius = arcSize / 2f
                val verticalShift = arcSize / 8f
                val arcOffset = Offset(
                    (size.width - arcSize) / 2f,
                    (size.height - arcSize) / 2f + verticalShift
                )
                val center = Offset(
                    arcOffset.x + radius,
                    arcOffset.y + radius
                )

                withTransform({
                    rotate(degrees = 150f, pivot = center)
                }) {
                    // Background track
                    drawArc(
                        color = Color.White.copy(alpha = 0.3f).copy(alpha = 0.4f),
                        startAngle = 0f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        topLeft = arcOffset,
                        size = Size(arcSize, arcSize)
                    )

                    if (sweepAngle > 0.5f) {
                        val brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                            0f to color.copy(alpha = 0.1f),
                            (sweepAngle / 360f) to color.copy(alpha = 0.7f),
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
                                glowPaint.asFrameworkPaint().maskFilter =
                                    android.graphics.BlurMaskFilter(
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
                        val innerX = center.x + (radius - needleLength / 2f) * kotlin.math.cos(
                            currentAngleRadians
                        )
                        val innerY = center.y + (radius - needleLength / 2f) * kotlin.math.sin(
                            currentAngleRadians
                        )
                        val outerX = center.x + (radius + needleLength / 2f) * kotlin.math.cos(
                            currentAngleRadians
                        )
                        val outerY = center.y + (radius + needleLength / 2f) * kotlin.math.sin(
                            currentAngleRadians
                        )

                        drawLine(
                            color = Color.White,
                            start = Offset(innerX, innerY),
                            end = Offset(outerX, outerY),
                            strokeWidth = strokeWidth * 0.35f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value.toInt().toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (isTrackMode) 60.sp else 30.sp,
                    fontWeight = if (isTrackMode) FontWeight.Black else FontWeight.Bold
                )
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = if (isTrackMode) 20.sp else 16.sp,
                    fontWeight = if (isTrackMode) FontWeight.Bold else FontWeight.Normal
                )
            }
        } // Closes the Box

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = if (isTrackMode) 20.sp else 16.sp,
            fontWeight = if (isTrackMode) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
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
        title = { Text(stringResource(R.string.dialog_title_connection_logs)) },
        text = {
            if (logs.isEmpty()) {
                Text(
                    stringResource(R.string.no_connection_logs),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            } else {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)) {
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
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    XCanTheme {
        DashboardScreen(
            state = DashboardUIState(
                connectionStatus = ConnectionStatus.CONNECTED,
                activeCar = CarProfile(
                    id = "1",
                    name = "Test Car",
                    make = "Toyota",
                    model = "Camry",
                    year = 2020,
                    isActive = true
                ),
                useMetric = true,
                selectedSensors = setOf(
                    ObdConstants.PID_ENGINE_RPM,
                    ObdConstants.PID_VEHICLE_SPEED
                ),
                telemetry = TelemetryFrame(
                    id = "1",
                    timestampMs = System.currentTimeMillis(),
                    sensors = mapOf(
                        ObdConstants.PID_ENGINE_RPM to 2500f,
                        ObdConstants.PID_VEHICLE_SPEED to 60f
                    )
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
            onStopLog = {},
            onToggleTrackMode = {}
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
            ElectricBlue,
            isTrackMode = true
        )
    }
}
