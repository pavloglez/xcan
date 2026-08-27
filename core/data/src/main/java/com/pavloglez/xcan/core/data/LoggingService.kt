package com.pavloglez.xcan.core.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoggingService : Service() {

    @Inject lateinit var loggingRepository: LoggingRepository
    @Inject lateinit var bleDataSource: BleDataSource
    @Inject lateinit var dispatcherProvider: com.pavloglez.xcan.core.model.DispatcherProvider

    private lateinit var serviceScope: CoroutineScope
    private var loggingJob: Job? = null

    companion object {
        private val _loggingState = MutableStateFlow<LoggingState>(LoggingState.Idle)
        val loggingState: StateFlow<LoggingState> = _loggingState.asStateFlow()

        const val ACTION_START = "com.pavloglez.xcan.logging.START"
        const val ACTION_PAUSE = "com.pavloglez.xcan.logging.PAUSE"
        const val ACTION_RESUME = "com.pavloglez.xcan.logging.RESUME"
        const val ACTION_STOP = "com.pavloglez.xcan.logging.STOP"

        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_CAR_LABEL = "car_label"
        const val EXTRA_START_MS = "start_ms"

        private const val NOTIFICATION_ID = 1001
        private const val BATCH_FLUSH_INTERVAL_MS = 500L
        private const val MILLIS_PER_SECOND = 1000L
        private const val SECONDS_PER_MINUTE = 60
        private const val DURATION_FORMAT = "%02d:%02d"
        private const val CHANNEL_ID = "xcan_logging"

        fun startIntent(context: Context, sessionId: String, carLabel: String, startMs: Long) =
            Intent(context, LoggingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_CAR_LABEL, carLabel)
                putExtra(EXTRA_START_MS, startMs)
            }

        fun pauseIntent(context: Context, sessionId: String) =
            Intent(context, LoggingService::class.java).apply {
                action = ACTION_PAUSE
                putExtra(EXTRA_SESSION_ID, sessionId)
            }

        fun resumeIntent(context: Context, sessionId: String) =
            Intent(context, LoggingService::class.java).apply {
                action = ACTION_RESUME
                putExtra(EXTRA_SESSION_ID, sessionId)
            }

        fun stopIntent(context: Context, sessionId: String) =
            Intent(context, LoggingService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + dispatcherProvider.io)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                val carLabel = intent.getStringExtra(EXTRA_CAR_LABEL) ?: ""
                val startMs = intent.getLongExtra(EXTRA_START_MS, System.currentTimeMillis())
                startLogging(sessionId, carLabel, startMs)
            }
            ACTION_PAUSE -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                pauseLogging(sessionId)
            }
            ACTION_RESUME -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                resumeLogging(sessionId)
            }
            ACTION_STOP -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                stopLogging(sessionId)
            }
        }
        return START_STICKY
    }

    private fun startLogging(sessionId: String, carLabel: String, startMs: Long) {
        val state = LoggingState.Recording(sessionId, carLabel, startMs)
        _loggingState.value = state
        startForeground(NOTIFICATION_ID, buildNotification(carLabel, startMs, isPaused = false))

        loggingJob?.cancel()
        loggingJob = serviceScope.launch {
            // Collect telemetry at 500ms intervals and batch-write to DB
            val pendingEntries = mutableMapOf<String, Float>()
            launch {
                bleDataSource.telemetry.collect { frame ->
                    val currentState = _loggingState.value
                    if (currentState is LoggingState.Recording) {
                        pendingEntries.putAll(frame.sensors)
                    }
                }
            }
            // Flush batch every 500ms
            while (true) {
                delay(BATCH_FLUSH_INTERVAL_MS)
                val currentState = _loggingState.value
                if (currentState is LoggingState.Recording && pendingEntries.isNotEmpty()) {
                    loggingRepository.recordBatch(sessionId, pendingEntries.toMap())
                    pendingEntries.clear()
                }
                updateNotificationDuration(carLabel, startMs)
            }
        }
    }

    private fun pauseLogging(sessionId: String) {
        val current = _loggingState.value as? LoggingState.Recording ?: return
        _loggingState.value = LoggingState.Paused(
            sessionId = current.sessionId,
            carLabel = current.carLabel,
            startMs = current.startMs,
            pausedAtMs = System.currentTimeMillis()
        )
        serviceScope.launch {
            loggingRepository.pauseSession(sessionId)
        }
        updateNotification(current.carLabel, current.startMs, isPaused = true)
    }

    private fun resumeLogging(sessionId: String) {
        val current = _loggingState.value as? LoggingState.Paused ?: return
        _loggingState.value = LoggingState.Recording(
            sessionId = current.sessionId,
            carLabel = current.carLabel,
            startMs = current.startMs
        )
        serviceScope.launch {
            loggingRepository.resumeSession(sessionId)
        }
        updateNotification(current.carLabel, current.startMs, isPaused = false)
    }

    private fun stopLogging(sessionId: String) {
        loggingJob?.cancel()
        loggingJob = null
        serviceScope.launch {
            loggingRepository.stopSession(sessionId)
        }
        _loggingState.value = LoggingState.Idle
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotificationDuration(carLabel: String, startMs: Long) {
        val isPaused = _loggingState.value is LoggingState.Paused
        updateNotification(carLabel, startMs, isPaused)
    }

    private fun updateNotification(carLabel: String, startMs: Long, isPaused: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(carLabel, startMs, isPaused))
    }

    private fun buildNotification(carLabel: String, startMs: Long, isPaused: Boolean): Notification {
        val durationSec = (System.currentTimeMillis() - startMs) / 1000
        val mins = durationSec / 60
        val secs = durationSec % 60
        val durationStr = DURATION_FORMAT.format(mins, secs)
        val statusText = if (isPaused) "Paused · $durationStr" else "Recording · $durationStr"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("XCan · $carLabel")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isPaused) "Resume" else "Pause",
                PendingIntent.getService(
                    this, 1,
                    if (isPaused) resumeIntent(this, "") else pauseIntent(this, ""),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Stop",
                PendingIntent.getService(
                    this, 2,
                    stopIntent(this, ""),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Telemetry Logging",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active OBD2 telemetry logging session"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
