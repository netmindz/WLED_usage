package com.github.wled.usage.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class MapFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val version: String,
    
    val releaseName: String? = null,
    val chip: String? = null,
    
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    val content: String,
    
    @CreationTimestamp
    val uploadedAt: LocalDateTime? = null
)
