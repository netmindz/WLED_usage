package com.github.wled.usage.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class CrashReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false, length = 64)
    val stackTraceHash: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val rawStackTrace: String,
    
    @Column(columnDefinition = "TEXT")
    val decodedStackTrace: String? = null,
    
    val exceptionCause: String? = null,
    
    @CreationTimestamp
    val firstSeen: LocalDateTime? = null
)
