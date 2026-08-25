package com.pavloglez.xcan.core.model

data class CarProfile(
    val id: String,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val isActive: Boolean
)
