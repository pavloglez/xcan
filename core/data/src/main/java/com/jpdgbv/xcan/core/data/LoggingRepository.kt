package com.jpdgbv.xcan.core.data

import com.jpdgbv.xcan.core.database.dao.LogEntryDao
import com.jpdgbv.xcan.core.database.dao.LogSessionDao
import com.jpdgbv.xcan.core.database.entity.LogEntryEntity
import com.jpdgbv.xcan.core.database.entity.LogSessionEntity
import com.jpdgbv.xcan.core.database.entity.toDomainModel
import com.jpdgbv.xcan.core.model.LogEntry
import com.jpdgbv.xcan.core.model.LogSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingRepository @Inject constructor(
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
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
            val session = logSessionDao.getActiveSession() ?: return@withContext
            logSessionDao.updateSession(session.copy(isPaused = true))
        }
    }

    suspend fun resumeSession(sessionId: String) {
        withContext(Dispatchers.IO) {
            val session = logSessionDao.getActiveSession() ?: return@withContext
            logSessionDao.updateSession(session.copy(isPaused = false))
        }
    }

    suspend fun stopSession(sessionId: String) {
        withContext(Dispatchers.IO) {
            val session = logSessionDao.getActiveSession() ?: return@withContext
            logSessionDao.updateSession(
                session.copy(endTimeMs = System.currentTimeMillis(), isPaused = false)
            )
        }
    }

    suspend fun record(sessionId: String, pid: String, value: Float) {
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
            logEntryDao.insertEntries(entries)
        }
    }

    suspend fun deleteSession(session: LogSession) {
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
            logSessionDao.deleteAllSessions()
        }
    }

    suspend fun getEntryCount(sessionId: String): Int =
        withContext(Dispatchers.IO) {
            logEntryDao.getEntryCountForSession(sessionId)
        }
}
