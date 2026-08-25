package com.pavloglez.xcan.feature.diagnostics

import app.cash.turbine.test
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.model.DiagnosticTroubleCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockBleDataSource: BleDataSource
    private lateinit var viewModel: DiagnosticsViewModel

    private val connectionStateFlow = MutableStateFlow(ConnectionStatus.CONNECTED)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockBleDataSource = mockk(relaxed = true)

        every { mockBleDataSource.connectionState } returns connectionStateFlow

        viewModel = DiagnosticsViewModel(mockBleDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scan fault codes success updates state`() = runTest(testDispatcher) {
        val codes = listOf(DiagnosticTroubleCode("P0133"))
        coEvery { mockBleDataSource.requestFaultCodes() } returns codes

        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(ScanStatus.IDLE, initialState.scanStatus)

            viewModel.onIntent(DiagnosticsIntent.ScanFaultCodes)

            val scanningState = awaitItem()
            assertEquals(ScanStatus.SCANNING, scanningState.scanStatus)

            val successState = awaitItem()
            assertEquals(ScanStatus.SUCCESS, successState.scanStatus)
            assertEquals(codes, successState.faultCodes)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scan fault codes failure updates state to error`() = runTest(testDispatcher) {
        coEvery { mockBleDataSource.requestFaultCodes() } throws RuntimeException("Connection lost")

        viewModel.state.test {
            awaitItem() // IDLE

            viewModel.onIntent(DiagnosticsIntent.ScanFaultCodes)
            awaitItem() // SCANNING

            val errorState = awaitItem()
            assertEquals(ScanStatus.ERROR, errorState.scanStatus)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `clear fault codes success updates state`() = runTest(testDispatcher) {
        val codes = listOf(DiagnosticTroubleCode("P0133"))
        
        // Setup initial state with faults
        coEvery { mockBleDataSource.requestFaultCodes() } returns codes
        coEvery { mockBleDataSource.clearFaultCodes() } returns true

        viewModel.state.test {
            awaitItem() // IDLE
            
            // First scan to get faults
            viewModel.onIntent(DiagnosticsIntent.ScanFaultCodes)
            awaitItem() // SCANNING
            awaitItem() // SUCCESS (with faults)

            // Now clear
            viewModel.onIntent(DiagnosticsIntent.ClearFaultCodes)
            
            val clearingState = awaitItem()
            assertEquals(ScanStatus.CLEARING, clearingState.scanStatus)
            
            val successClearState = awaitItem()
            assertEquals(ScanStatus.IDLE, successClearState.scanStatus)
            assertEquals(emptyList<DiagnosticTroubleCode>(), successClearState.faultCodes)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear fault codes failure updates state to error`() = runTest(testDispatcher) {
        coEvery { mockBleDataSource.clearFaultCodes() } returns false

        viewModel.state.test {
            awaitItem() // IDLE

            viewModel.onIntent(DiagnosticsIntent.ClearFaultCodes)
            
            val clearingState = awaitItem()
            assertEquals(ScanStatus.CLEARING, clearingState.scanStatus)
            
            val errorState = awaitItem()
            assertEquals(ScanStatus.ERROR, errorState.scanStatus)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
