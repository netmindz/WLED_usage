package com.github.wled.usage.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class ReleaseNameHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    val device: Device,

    val releaseName: String,

    val deviceLastUpdate: LocalDateTime? = null,

    @CreationTimestamp
    val created: LocalDateTime? = null
)
