package com.github.wled.usage.dto

data class ReleaseDownloadStats(
    val repoName: String,
    val tagName: String,
    val assetName: String,
    val downloadCount: Long,
    val delta: Long,
    val created: String
)
