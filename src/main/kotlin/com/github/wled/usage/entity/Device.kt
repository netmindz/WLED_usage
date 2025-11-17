package com.github.wled.usage.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Device(
    @Id
    val id: String,
    var version: String,
    var releaseName: String,
    val chip: String,
    var ledCount: Int,
    var isMatrix: Boolean,
    var bootloaderSHA256: String,
)
