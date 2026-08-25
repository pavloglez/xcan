package com.pavloglez.xcan.core.model

data class DiagnosticTroubleCode(
    val code: String,
    val description: String? = null,
    val type: DtcType = DtcType.STORED
)

enum class DtcType {
    STORED,
    PENDING,
    PERMANENT
}
