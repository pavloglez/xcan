package com.pavloglez.xcan.core.bluetooth.internal

import com.juul.kable.Characteristic
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.BluetoothConstants
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.bluetooth.DtcParser
import com.pavloglez.xcan.core.bluetooth.ObdParser
import com.pavloglez.xcan.core.bluetooth.ScannedDevice
import com.pavloglez.xcan.core.model.DiagnosticTroubleCode
import com.pavloglez.xcan.core.model.DispatcherProvider
import com.pavloglez.xcan.core.model.DtcType
import com.pavloglez.xcan.core.model.ObdConstants
import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.SensorRepository
import com.pavloglez.xcan.core.model.TelemetryFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleDataSourceImpl @Inject constructor(
    private val sensorRepo: SensorRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val obdParser: ObdParser,
) : BleDataSource {

    private val currentSensors = mutableMapOf<String, Float>()

    private fun maskMac(mac: String): String {
        return if (mac.length >= 5) "XX:XX:XX:XX:" + mac.takeLast(5) else mac
    }

    private suspend fun safeWrite(txChar: Characteristic, command: String) {
        try {
            peripheral?.write(txChar, (command + "\r").toByteArray(), WriteType.WithResponse)
        } catch (e: Exception) {
            log("Write error: ${e.message}")
        }
    }

    private var peripheral: Peripheral? = null
    private var connectionJob: Job? = null
    private var pollingJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionState: Flow<ConnectionStatus> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(TelemetryFrame("init", System.currentTimeMillis(), emptyMap()))
    override val telemetry: Flow<TelemetryFrame> = _telemetry.asStateFlow()

    private val _connectionLogs = MutableSharedFlow<String>(extraBufferCapacity = BluetoothConstants.LOG_BUFFER_CAPACITY)
    override val connectionLogs: Flow<String> = _connectionLogs.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io)

    private var activePids = ObdConstants.DEFAULT_SELECTED_SENSORS.toList()

    override fun setPollingPids(pids: List<String>) {
        activePids = pids
    }

    private fun log(message: String) {
        scope.launch { _connectionLogs.emit(message) }
    }

    override fun scanForDevices(): Flow<List<ScannedDevice>> {
        return Scanner().advertisements
            .filter { advertisement ->
                val name = advertisement.name?.uppercase() ?: ""
                BluetoothConstants.BLE_NAME_FILTERS.any { name.contains(it) }
            }
            .map { ScannedDevice(it.name ?: "Unknown OBD Device", it.address) }
            .scan(emptyList<ScannedDevice>()) { acc, device ->
                if (acc.any { it.macAddress == device.macAddress }) acc else acc + device
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun connect(macAddress: String) {
        connectionJob?.cancel()
        pollingJob?.cancel()
        
        connectionJob = scope.launch {
            try {
                log("Establishing connection with adapter $macAddress...")
                _connectionState.value = ConnectionStatus.CONNECTING
                peripheral = peripheral(macAddress)
                
                var retryCount = 0
                while (true) {
                    try {
                        peripheral?.connect()
                        break
                    } catch (e: Exception) {
                        retryCount++
                        log("Connection failed (attempt $retryCount): ${e.message}")
                        if (retryCount >= 3) {
                            _connectionState.value = ConnectionStatus.ERROR
                            return@launch
                        }
                        delay(minOf(retryCount * BluetoothConstants.BACKOFF_STEP_MS, BluetoothConstants.MAX_BACKOFF_DELAY_MS))
                    }
                }

                log("Connected to ${maskMac(macAddress)}. Configuring adapter...")
                _connectionState.value = ConnectionStatus.CONNECTED
                
                // Initialize ELM327
                val txChar = characteristicOf(
                    ObdParser.OBD_SERVICE_UUID.toString(),
                    ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString(),
                )
                
                safeWrite(txChar, BluetoothConstants.CMD_RESET)
                delay(BluetoothConstants.ELM_INIT_DELAY_MS)
                safeWrite(txChar, "ATE0") // Echo off
                safeWrite(txChar, "ATL0") // Linefeeds off
                safeWrite(txChar, "ATS0") // Spaces off
                safeWrite(txChar, "ATH0") // Headers off
                safeWrite(txChar, BluetoothConstants.CMD_AUTO_PROTOCOL)

                startPolling()

                // Keep alive/monitor connection
                peripheral?.state?.first { it is State.Disconnected }
                log("Lost connection to adapter.")
                _connectionState.value = ConnectionStatus.DISCONNECTED
                pollingJob?.cancel()

            } catch (e: Exception) {
                log("Critical connection error: ${e.message}")
                _connectionState.value = ConnectionStatus.ERROR
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            val rxCharacteristic = characteristicOf(
                ObdParser.OBD_SERVICE_UUID.toString(),
                ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString(),
            )
            val txCharacteristic = characteristicOf(
                ObdParser.OBD_SERVICE_UUID.toString(),
                ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString(),
            )

            // Listen for responses in a separate loop
            launch {
                peripheral?.observe(rxCharacteristic)?.collect { bytes ->
                    val response = String(bytes).trim()
                    if (response.startsWith("41")) {
                        obdParser.parse(response)?.let { (pid, value) ->
                            currentSensors[pid] = value
                            _telemetry.value = TelemetryFrame(
                                id = UUID.randomUUID().toString(),
                                timestampMs = System.currentTimeMillis(),
                                sensors = currentSensors.toMap(),
                            )
                        }
                    }
                }
            }

            // Command loop
            while (true) {
                if (activePids.isEmpty()) {
                    delay(BluetoothConstants.IDLE_POLL_DELAY_MS)
                    continue
                }
                
                for (pid in activePids) {
                    safeWrite(txCharacteristic, pid)
                    delay(BluetoothConstants.TELEMETRY_POLL_INTERVAL_MS)
                }
            }
        }
    }

    override suspend fun disconnect() {
        connectionJob?.cancel()
        pollingJob?.cancel()
        scope.launch {
            try {
                peripheral?.disconnect()
                log("Disconnected manually.")
            } catch (e: Exception) {
                log("Error during disconnect: ${e.message}")
            } finally {
                peripheral = null
                _connectionState.value = ConnectionStatus.DISCONNECTED
            }
        }
    }

    override suspend fun requestFaultCodes(): List<DiagnosticTroubleCode> = coroutineScope {
        val p = peripheral ?: return@coroutineScope emptyList()
        val rxCharacteristic = characteristicOf(
            ObdParser.OBD_SERVICE_UUID.toString(),
            ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString(),
        )
        val txCharacteristic = characteristicOf(
            ObdParser.OBD_SERVICE_UUID.toString(),
            ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString(),
        )

        // Helper to collect one response
        suspend fun getResponseFor(command: String): String {
            var result = ""
            val job = launch {
                p.observe(rxCharacteristic).takeWhile { bytes ->
                    val chunk = String(bytes)
                    result += chunk
                    !chunk.contains(">")
                }.collect()
            }
            safeWrite(txCharacteristic, command)
            withTimeout(BluetoothConstants.DTC_REQUEST_TIMEOUT_MS) { job.join() }
            return result
        }

        try {
            val stored = getResponseFor(BluetoothConstants.CMD_REQUEST_STORED_DTCS)
            val pending = getResponseFor(BluetoothConstants.CMD_REQUEST_PENDING_DTCS)
            val permanent = getResponseFor(BluetoothConstants.CMD_REQUEST_PERMANENT_DTCS)
            
            val dtcs = mutableListOf<DiagnosticTroubleCode>()
            dtcs.addAll(DtcParser.parse(stored, DtcType.STORED))
            dtcs.addAll(DtcParser.parse(pending, DtcType.PENDING))
            dtcs.addAll(DtcParser.parse(permanent, DtcType.PERMANENT))
            dtcs
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun clearFaultCodes(): Boolean = coroutineScope {
        val p = peripheral ?: return@coroutineScope false
        val rxCharacteristic = characteristicOf(
            ObdParser.OBD_SERVICE_UUID.toString(),
            ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString(),
        )
        val txCharacteristic = characteristicOf(
            ObdParser.OBD_SERVICE_UUID.toString(),
            ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString(),
        )

        try {
            var result = ""
            val job = launch {
                p.observe(rxCharacteristic).takeWhile { bytes ->
                    val chunk = String(bytes)
                    result += chunk
                    !chunk.contains(">")
                }.collect()
            }
            safeWrite(txCharacteristic, BluetoothConstants.CMD_CLEAR_DTCS)
            withTimeout(BluetoothConstants.DTC_REQUEST_TIMEOUT_MS) { job.join() }
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getSupportedSensors(): List<ObdSensor> = coroutineScope {
        val p = peripheral ?: return@coroutineScope emptyList()
        val rxCharacteristic = characteristicOf(
            ObdParser.OBD_SERVICE_UUID.toString(),
            ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString(),
        )
        val txCharacteristic = characteristicOf(
            ObdParser.OBD_SERVICE_UUID.toString(),
            ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString(),
        )

        suspend fun getResponseFor(command: String): String {
            var result = ""
            val job = launch {
                p.observe(rxCharacteristic).takeWhile { bytes ->
                    val chunk = String(bytes)
                    result += chunk
                    !chunk.contains(">")
                }.collect()
            }
            safeWrite(txCharacteristic, command)
            withTimeout(BluetoothConstants.PID_DISCOVERY_TIMEOUT_MS) { job.join() }
            return result
        }

        val discoveredPids = mutableSetOf<String>()
        try {
            // Service 01 Discovery
            val pids1to20 = getResponseFor(BluetoothConstants.CMD_PID_DISCOVERY_01_20)
            if ((pids1to20.startsWith(BluetoothConstants.RESP_PID_DISCOVERY_01_20)) && (pids1to20.length >= BluetoothConstants.MIN_PID_BITMAP_LENGTH)) {
                // Simplified bitmap parsing for demonstration
                // In production, we'd parse the full bitmap hex
                discoveredPids.addAll(listOf("010C", "010D", "0104", "0105", "010F", "0110", "0111"))
            }
        } catch (e: Exception) {
            log("Discovery failed: ${e.message}")
        }

        sensorRepo.getSensors().first().filter { discoveredPids.contains(it.pid) }
    }
}
