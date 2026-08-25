package com.pavloglez.xcan.core.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.pavloglez.xcan.core.data.MaintenanceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var mockRepository: MaintenanceRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun `doWork returns success when syncLogs succeeds`() = runTest {
        // Given
        coEvery { mockRepository.syncLogs() } returns Unit

        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(
                object : androidx.work.WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: androidx.work.WorkerParameters
                    ): ListenableWorker {
                        return SyncWorker(appContext, workerParameters, mockRepository)
                    }
                }
            )
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { mockRepository.syncLogs() }
    }

    @Test
    fun `doWork returns retry when syncLogs throws exception`() = runTest {
        // Given
        coEvery { mockRepository.syncLogs() } throws RuntimeException("Network Error")

        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(
                object : androidx.work.WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: androidx.work.WorkerParameters
                    ): ListenableWorker {
                        return SyncWorker(appContext, workerParameters, mockRepository)
                    }
                }
            )
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { mockRepository.syncLogs() }
    }
}
