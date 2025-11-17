package com.github.wled.usage.dto


data class UpgradeEventRequest(
    val deviceId: String,
    val version: String,
    val previousVersion: String,
    val releaseName: String,
    val chip: String,
    val ledCount: Int,
    val isMatrix: Boolean,
    val bootloaderSHA256: String,
)
