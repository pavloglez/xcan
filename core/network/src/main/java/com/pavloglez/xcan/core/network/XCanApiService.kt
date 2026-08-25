package com.pavloglez.xcan.core.network

import com.pavloglez.xcan.core.network.model.MaintenanceLogDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface XCanApiService {
    @GET("v1/maintenance")
    suspend fun fetchMaintenanceLogs(): List<MaintenanceLogDto>

    @POST("v1/maintenance/sync")
    suspend fun syncMaintenanceLogs(@Body logs: List<MaintenanceLogDto>)
}
