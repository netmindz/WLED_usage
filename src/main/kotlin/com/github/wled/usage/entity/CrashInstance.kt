package com.github.wled.usage.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class CrashInstance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crash_report_id", nullable = false)
    val crashReport: CrashReport,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = true)
    val device: Device? = null,
    
    val version: String,
    val chip: String? = null,
    val countryCode: String? = null,
    
    @CreationTimestamp
    val reportedAt: LocalDateTime? = null
)
