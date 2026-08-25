package com.jpdgbv.xcan.core.data

import com.jpdgbv.xcan.core.database.dao.LogEntryDao
import com.jpdgbv.xcan.core.database.dao.LogSessionDao
import com.jpdgbv.xcan.core.database.entity.LogEntryEntity
import com.jpdgbv.xcan.core.database.entity.LogSessionEntity
import com.jpdgbv.xcan.core.database.entity.toDomainModel
import com.jpdgbv.xcan.core.model.LogEntry
import com.jpdgbv.xcan.core.model.LogSession

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import com.jpdgbv.xcan.core.model.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingRepository @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val logSessionDao: LogSessionDao,
    private val logEntryDao: LogEntryDao
) {

    fun getAllSessions(): Flow<List<LogSession>> =
        logSessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomainModel() }
        }

    fun getSessionById(id: String): Flow<LogSession?> =
        logSessionDao.getSessionById(id).map { it?.toDomainModel() }

    fun getEntriesForSession(sessionId: String): Flow<List<LogEntry>> =
        logEntryDao.getEntriesForSession(sessionId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun startSession(carId: String, carLabel: String): String {
        val id = UUID.randomUUID().toString()
        withContext(dispatchers.io) {
            logSessionDao.insertSession(
                LogSessionEntity(
                    id = id,
                    carId = carId,
                    carLabel = carLabel,
                    startTimeMs = System.currentTimeMillis()
                )
            )
        }
        return id
    }

    suspend fun pauseSession(sessionId: String) {
        withContext(dispatchers.io) {
            val session = logSessionDao.getActiveSession() ?: return@withContext
            logSessionDao.updateSession(session.copy(isPaused = true))
        }
    }

    suspend fun resumeSession(sessionId: String) {
        withContext(dispatchers.io) {
            val session = logSessionDao.getActiveSession() ?: return@withContext
            logSessionDao.updateSession(session.copy(isPaused = false))
        }
    }

    suspend fun stopSession(sessionId: String) {
        withContext(dispatchers.io) {
            val session = logSessionDao.getActiveSession() ?: return@withContext
            logSessionDao.updateSession(
                session.copy(endTimeMs = System.currentTimeMillis(), isPaused = false)
            )
        }
    }

    suspend fun record(sessionId: String, pid: String, value: Float) {
        withContext(dispatchers.io) {
            logEntryDao.insertEntry(
                LogEntryEntity(
                    sessionId = sessionId,
                    timestampMs = System.currentTimeMillis(),
                    pid = pid,
                    value = value
                )
            )
        }
    }

    suspend fun recordBatch(sessionId: String, readings: Map<String, Float>) {
        val now = System.currentTimeMillis()
        val entries = readings.map { (pid, value) ->
            LogEntryEntity(
                sessionId = sessionId,
                timestampMs = now,
                pid = pid,
                value = value
            )
        }
        withContext(dispatchers.io) {
            logEntryDao.insertEntries(entries)
        }
    }

    suspend fun deleteSession(session: LogSession) {
        withContext(dispatchers.io) {
            logSessionDao.deleteSession(
                LogSessionEntity(
                    id = session.id,
                    carId = session.carId,
                    carLabel = session.carLabel,
                    startTimeMs = session.startTimeMs,
                    endTimeMs = session.endTimeMs,
                    isPaused = session.isPaused
                )
            )
        }
    }

    suspend fun deleteAllSessions() {
        withContext(dispatchers.io) {
            logSessionDao.deleteAllSessions()
        }
    }

    suspend fun getEntryCount(sessionId: String): Int =
        withContext(dispatchers.io) {
            logEntryDao.getEntryCountForSession(sessionId)
        }
}
