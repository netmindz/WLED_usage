package com.github.wled.usage.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class ReleaseDownloadSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val repoName: String,
    val tagName: String,
    val assetName: String,
    val downloadCount: Long,
    val delta: Long,

    @CreationTimestamp
    val created: LocalDateTime? = null
)
