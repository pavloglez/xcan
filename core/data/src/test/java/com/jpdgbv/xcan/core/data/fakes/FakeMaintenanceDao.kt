package com.jpdgbv.xcan.core.data.fakes

import com.jpdgbv.xcan.core.database.dao.MaintenanceDao
import com.jpdgbv.xcan.core.database.entity.MaintenanceLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMaintenanceDao : MaintenanceDao {
    private val logsFlow = MutableStateFlow<Map<String, MaintenanceLogEntity>>(emptyMap())

    override fun getAllLogs(carId: String): Flow<List<MaintenanceLogEntity>> {
        return logsFlow.map { map -> 
            map.values.filter { it.carId == carId }.sortedByDescending { log -> log.dateMs } 
        }
    }

    override fun insertLog(log: MaintenanceLogEntity): Long {
        val current = logsFlow.value.toMutableMap()
        current[log.id] = log
        logsFlow.value = current
        return 1L
    }

    override fun insertLogs(logs: List<MaintenanceLogEntity>): List<Long> {
        val current = logsFlow.value.toMutableMap()
        logs.forEach { current[it.id] = it }
        logsFlow.value = current
        return logs.map { 1L }
    }

    override fun deleteLog(id: String): Int {
        val current = logsFlow.value.toMutableMap()
        val removed = current.remove(id)
        logsFlow.value = current
        return if (removed != null) 1 else 0
    }
}
