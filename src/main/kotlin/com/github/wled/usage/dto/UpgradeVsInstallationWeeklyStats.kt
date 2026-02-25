package com.github.wled.usage.dto

data class UpgradeVsInstallationWeeklyStats(
    val week: String,
    val upgrades: Long,
    val newInstallations: Long
)
