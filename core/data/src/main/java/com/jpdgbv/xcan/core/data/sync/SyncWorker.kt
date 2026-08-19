package com.jpdgbv.xcan.core.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jpdgbv.xcan.core.data.MaintenanceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val maintenanceRepository: MaintenanceRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            maintenanceRepository.syncLogs()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
