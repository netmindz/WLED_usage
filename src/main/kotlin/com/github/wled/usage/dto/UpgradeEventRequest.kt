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
    val brand: String? = null,
    val product: String? = null,
    val flashSize: Int? = null,
    val partitionSizes: String? = null,
    val psramSize: Int? = null,
    val psramPresent: Boolean? = null,
    val repo: String? = null,
    // Filesystem
    val fsUsed: Int? = null,
    val fsTotal: Int? = null,
    // Bus / hardware
    val busCount: Int? = null,
    val busTypes: List<String>? = null,
    // LED/peripheral/integration capability lists
    val ledFeatures: List<String>? = null,
    val peripherals: List<String>? = null,
    val integrations: List<String>? = null,
    // Usermods
    val usermods: List<String>? = null,
    val usermodIds: List<Int>? = null,
)
