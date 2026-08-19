package com.jpdgbv.xcan.core.data.fakes

import com.jpdgbv.xcan.core.network.XCanApiService
import com.jpdgbv.xcan.core.network.model.MaintenanceLogDto

class FakeXCanApiService : XCanApiService {
    var logsToReturn = listOf<MaintenanceLogDto>()
    var shouldThrowError = false
    val syncedLogs = mutableListOf<MaintenanceLogDto>()

    override suspend fun fetchMaintenanceLogs(): List<MaintenanceLogDto> {
        if (shouldThrowError) throw Exception("Network error")
        return logsToReturn
    }

    override suspend fun syncMaintenanceLogs(logs: List<MaintenanceLogDto>) {
        if (shouldThrowError) throw Exception("Network error")
        syncedLogs.addAll(logs)
    }
}
