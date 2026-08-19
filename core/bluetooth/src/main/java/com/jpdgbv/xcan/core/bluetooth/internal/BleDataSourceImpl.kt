package com.jpdgbv.xcan.core.bluetooth.internal

import android.annotation.SuppressLint
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.jpdgbv.xcan.core.bluetooth.BleDataSource
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.bluetooth.ObdParser
import com.jpdgbv.xcan.core.bluetooth.ScannedDevice
import com.jpdgbv.xcan.core.model.TelemetryFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.cancelAndJoin
import com.jpdgbv.xcan.core.model.SensorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleDataSourceImpl @Inject constructor(
    private val sensorRepo: SensorRepository
) : BleDataSource {

    private var peripheral: Peripheral? = null
    private var connectionJob: Job? = null
    private var pollingJob: Job? = null
    
    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionState: Flow<ConnectionStatus> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(TelemetryFrame(java.util.UUID.randomUUID().toString(), 0L, emptyMap()))
    override val telemetry: Flow<TelemetryFrame> = _telemetry.asStateFlow()

    private val _connectionLogs = MutableSharedFlow<String>(extraBufferCapacity = 50)
    override val connectionLogs: Flow<String> = _connectionLogs.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var activePids = listOf("010C", "010D", "0104", "0105")

    override fun setPollingPids(pids: List<String>) {
        activePids = pids
    }

    private fun log(message: String) {
        _connectionLogs.tryEmit(message)
    }

    @SuppressLint("MissingPermission")
    override fun scanForDevices(): Flow<List<ScannedDevice>> {
        return Scanner().advertisements
            .filter { advertisement ->
                val name = advertisement.name?.uppercase() ?: ""
                name.contains("OBD") || name.contains("ELM") || name.contains("LINK") || name.contains("V-LINK")
            }
            .map { ScannedDevice(it.name ?: "Unknown OBD Device", it.address) }
            .scan(emptyList<ScannedDevice>()) { acc, device ->
                if (acc.any { it.macAddress == device.macAddress }) acc else acc + device
            }
            .catch { emit(emptyList()) }
    }

    @SuppressLint("MissingPermission")
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
                        log("Connecting to ECU...")
                        peripheral?.connect()
                        _connectionState.value = ConnectionStatus.CONNECTED
                        log("Connected.")
                        retryCount = 0
                        
                        startPolling()
                        
                        // Wait until disconnected
                        peripheral?.state?.first { it is com.juul.kable.State.Disconnected }
                        pollingJob?.cancel()
                    } catch (e: Exception) {
                        log("Connection failed: ${e.message}. Retrying in ${retryCount}s...")
                        e.printStackTrace()
                    }
                    _connectionState.value = ConnectionStatus.CONNECTING
                    retryCount++
                    delay(minOf(retryCount * 1000L, 5000L)) // Exponential backoff max 5s
                }
            } catch (e: Exception) {
                log("Disconnected: ${e.message}")
                e.printStackTrace()
                _connectionState.value = ConnectionStatus.DISCONNECTED
            }
        }
    }

    private fun startPolling() {
        val rxCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString()
        )
        val txCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString()
        )

        pollingJob = scope.launch {
            try {
                // Initialize ELM327
                log("Tx: AT Z")
                peripheral?.write(txCharacteristic, "AT Z\r".toByteArray())
                delay(500)
                log("Tx: AT SP 0")
                peripheral?.write(txCharacteristic, "AT SP 0\r".toByteArray())
                delay(500)

                // Start listening to Rx
                launch {
                    var buffer = ""
                    peripheral?.observe(rxCharacteristic)?.collect { bytes ->
                        val chunk = String(bytes)
                        buffer += chunk
                        if (buffer.contains("\r") || buffer.contains(">")) {
                            val lines = buffer.split("\r", ">")
                            for (line in lines) {
                                val cleanLine = line.trim()
                                if (cleanLine.isNotEmpty()) {
                                    ObdParser.parse(cleanLine, sensorRepo)?.let { newFrame ->
                                        _telemetry.value = newFrame
                                    }
                                }
                            }
                            buffer = ""
                        }
                    }
                }

                // Poll PIDs sequentially
                while (true) {
                    val pidsToPoll = activePids.toList()
                    if (pidsToPoll.isEmpty()) {
                        delay(1000)
                        continue
                    }
                    for (pid in pidsToPoll) {
                        log("Tx: $pid")
                        peripheral?.write(txCharacteristic, "$pid\r".toByteArray())
                        delay(250) // Wait before next request to avoid overwhelming
                    }
                }
            } catch (e: Exception) {
                log("Error during polling: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override suspend fun disconnect() {
        log("Disconnecting...")
        connectionJob?.cancel()
        connectionJob = null
        pollingJob?.cancel()
        pollingJob = null
        try {
            peripheral?.disconnect()
            log("Disconnected.")
        } catch (e: Exception) {
            log("Disconnect error: ${e.message}")
            e.printStackTrace()
        }
        peripheral = null
        _connectionState.value = ConnectionStatus.DISCONNECTED
    }

    override suspend fun requestFaultCodes(): List<com.jpdgbv.xcan.core.model.DiagnosticTroubleCode> {
        val dtcs = mutableListOf<com.jpdgbv.xcan.core.model.DiagnosticTroubleCode>()
        if (_connectionState.value != ConnectionStatus.CONNECTED) return dtcs
        
        // 1. Cancel polling
        pollingJob?.cancelAndJoin()
        
        val rxCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString()
        )
        val txCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString()
        )
        
        try {
            // Helper to collect one response
            suspend fun getResponseFor(req: String): String {
                var response = ""
                peripheral?.write(txCharacteristic, "$req\r".toByteArray())
                try {
                    withTimeout(3000) {
                        peripheral?.observe(rxCharacteristic)?.takeWhile { bytes ->
                            val chunk = String(bytes)
                            response += chunk
                            !response.contains(">")
                        }?.collect()
                    }
                } catch (e: Exception) {
                    // Timeout or other error
                }
                return response
            }
            
            val stored = getResponseFor("03")
            dtcs.addAll(com.jpdgbv.xcan.core.bluetooth.DtcParser.parse(stored, com.jpdgbv.xcan.core.model.DtcType.STORED))
            
            val pending = getResponseFor("07")
            dtcs.addAll(com.jpdgbv.xcan.core.bluetooth.DtcParser.parse(pending, com.jpdgbv.xcan.core.model.DtcType.PENDING))
            
            val permanent = getResponseFor("0A")
            dtcs.addAll(com.jpdgbv.xcan.core.bluetooth.DtcParser.parse(permanent, com.jpdgbv.xcan.core.model.DtcType.PERMANENT))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Resume telemetry polling
        startPolling()
        
        return dtcs
    }

    override suspend fun clearFaultCodes(): Boolean {
        if (_connectionState.value != ConnectionStatus.CONNECTED) return false
        
        // 1. Cancel polling
        pollingJob?.cancelAndJoin()
        
        val rxCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString()
        )
        val txCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString()
        )
        
        var success = false
        try {
            var response = ""
            peripheral?.write(txCharacteristic, "04\r".toByteArray())
            try {
                withTimeout(3000) {
                    peripheral?.observe(rxCharacteristic)?.takeWhile { bytes ->
                        val chunk = String(bytes)
                        response += chunk
                        !response.contains(">")
                    }?.collect()
                }
            } catch (e: Exception) {
                // Timeout or other error
            }
            
            // ELM327 typically returns "44" on success or just "OK" depending on mode
            success = response.contains("44") || response.contains("OK")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Resume telemetry polling
        startPolling()
        
        return success
    }

    override suspend fun getSupportedSensors(): List<com.jpdgbv.xcan.core.model.ObdSensor> {
        val supported = mutableListOf<com.jpdgbv.xcan.core.model.ObdSensor>()
        if (_connectionState.value != ConnectionStatus.CONNECTED) return supported

        pollingJob?.cancelAndJoin()

        val rxCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_RX_CHARACTERISTIC_UUID.toString()
        )
        val txCharacteristic = characteristicOf(
            service = ObdParser.OBD_SERVICE_UUID.toString(),
            characteristic = ObdParser.OBD_TX_CHARACTERISTIC_UUID.toString()
        )

        try {
            suspend fun getResponseFor(req: String): String {
                var response = ""
                peripheral?.write(txCharacteristic, "$req\r".toByteArray())
                try {
                    withTimeout(3000) {
                        peripheral?.observe(rxCharacteristic)?.takeWhile { bytes ->
                            val chunk = String(bytes)
                            response += chunk
                            !response.contains(">")
                        }?.collect()
                    }
                } catch (e: Exception) {
                    // Timeout
                }
                return response.replace(" ", "").replace("\r", "").replace(">", "")
            }

            // Check PIDs 01-20
            val pids1to20 = getResponseFor("0100")
            if (pids1to20.startsWith("4100") && pids1to20.length >= 12) {
                val hexData = pids1to20.substring(4, 12)
                val bitmask = hexData.toLong(16)
                for (i in 1..32) {
                    if ((bitmask and (1L shl (32 - i))) != 0L) {
                        val pidStr = String.format("01%02X", i)
                        supported.add(sensorRepo.getSensorByPidSync(pidStr))
                    }
                }
            }

            // Check PIDs 21-40
            val pids21to40 = getResponseFor("0120")
            if (pids21to40.startsWith("4120") && pids21to40.length >= 12) {
                val hexData = pids21to40.substring(4, 12)
                val bitmask = hexData.toLong(16)
                for (i in 1..32) {
                    if ((bitmask and (1L shl (32 - i))) != 0L) {
                        val pidStr = String.format("01%02X", i + 32)
                        supported.add(sensorRepo.getSensorByPidSync(pidStr))
                    }
                }
            }

            // Check PIDs 41-60
            val pids41to60 = getResponseFor("0140")
            if (pids41to60.startsWith("4140") && pids41to60.length >= 12) {
                val hexData = pids41to60.substring(4, 12)
                val bitmask = hexData.toLong(16)
                for (i in 1..32) {
                    if ((bitmask and (1L shl (32 - i))) != 0L) {
                        val pidStr = String.format("01%02X", i + 64)
                        supported.add(sensorRepo.getSensorByPidSync(pidStr))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startPolling()
        return supported
    }
}
