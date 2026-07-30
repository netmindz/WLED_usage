package com.github.wled.usage.dto

data class MapFileUploadRequest(
    val version: String,
    val releaseName: String? = null,
    val chip: String? = null,
    val content: String
)
