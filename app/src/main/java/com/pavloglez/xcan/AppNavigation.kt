package com.pavloglez.xcan

import kotlinx.serialization.Serializable

@Serializable
object DiagnosticsRoute

@Serializable
object MaintenanceRoute

@Serializable
object DashboardRoute

@Serializable
object LogSessionsRoute

@Serializable
object ConfigRoute

@Serializable
data class LogSessionDetailRoute(val sessionId: String)
