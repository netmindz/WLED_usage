package com.github.wled.usage.dto

data class CrashReportRequest(
    val deviceId: String? = null,
    val version: String,
    val chip: String? = null,
    val stackTrace: String,
    val exceptionCause: String? = null
)
