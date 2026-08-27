package com.pavloglez.xcan.core.bluetooth

import com.pavloglez.xcan.core.model.ObdConstants

class ObdCommandScheduler {
    
    // Defines how often to poll different categories of PIDs
    private val fastPids = ObdConstants.DEFAULT_FAST_PIDS // RPM, Speed, Load, Throttle
    private val slowPids = ObdConstants.DEFAULT_SLOW_PIDS // Temps
    
    private var activePids = listOf<String>()
    
    fun updatePids(pids: List<String>) {
        activePids = pids
    }
    
    /**
     * Sequence generator that yields the next PID to poll based on a 
     * simple priority queue / interleaving strategy.
     */
    suspend fun getNextPidToPoll(iteration: Long): String? {
        if (activePids.isEmpty()) return null
        
        // Every 10 iterations, poll a slow PID if one exists
        if (iteration % BluetoothConstants.SLOW_PID_POLL_INTERVAL == 0L) {
            val availableSlow = activePids.filter { it in slowPids }
            if (availableSlow.isNotEmpty()) {
                val index = ((iteration / BluetoothConstants.SLOW_PID_POLL_INTERVAL) % availableSlow.size).toInt()
                return availableSlow[index]
            }
        }
        
        // Otherwise, poll fast and unknown PIDs
        val fastOrUnknown = activePids.filter { it !in slowPids }
        if (fastOrUnknown.isNotEmpty()) {
            val index = (iteration % fastOrUnknown.size).toInt()
            return fastOrUnknown[index]
        }
        
        // Fallback if we only have slow PIDs selected
        return activePids[(iteration % activePids.size).toInt()]
    }
}
