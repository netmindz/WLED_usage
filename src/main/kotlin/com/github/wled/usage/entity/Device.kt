package com.github.wled.usage.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Device(
    @Id
    val deviceId: String,
    val version: String,
    val release: String,
    val chip: String,
    val totalLEDs: Int,
    val isMatrix: Boolean,
)
