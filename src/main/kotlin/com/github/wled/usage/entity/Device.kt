package com.github.wled.usage.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
data class Device(
    @Id
    val id: String,
    var version: String,
    var releaseName: String,
    val chip: String,
    var ledCount: Int? = null,
    var isMatrix: Boolean? = null,
    var bootloaderSHA256: String,
    var brand: String? = null,
    var product: String? = null,
    var flashSize: String? = null,
    var partitionSizes: String? = null,
    var psramSize: String? = null,
    var psramPresent: Boolean? = null,
    var countryCode: String? = null,
    
    @CreationTimestamp
    val created: LocalDateTime? = null,
    
    @UpdateTimestamp
    var lastUpdate: LocalDateTime? = null,
)
