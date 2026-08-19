package com.jpdgbv.xcan.core.data

import com.jpdgbv.xcan.core.database.dao.TelemetryDao
import com.jpdgbv.xcan.core.database.entity.toDomainModel
import com.jpdgbv.xcan.core.database.entity.toEntity
import com.jpdgbv.xcan.core.model.TelemetryFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TelemetryRepository @Inject constructor(
    private val telemetryDao: TelemetryDao
) {

    fun getRecentTelemetry(limit: Int): Flow<List<TelemetryFrame>> {
        return telemetryDao.getRecentTelemetry(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun recordTelemetry(frame: TelemetryFrame) {
        withContext(Dispatchers.IO) {
            telemetryDao.insertTelemetry(frame.toEntity())
        }
    }

    suspend fun pruneOldTelemetry(olderThanMs: Long) {
        withContext(Dispatchers.IO) {
            telemetryDao.deleteOldTelemetry(olderThanMs)
        }
    }
}
