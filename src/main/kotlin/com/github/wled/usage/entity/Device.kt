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
    var flashSize: Int? = null,
    var partitionSizes: String? = null,
    var psramSize: Int? = null,
    var psramPresent: Boolean? = null,
    var countryCode: String? = null,
    var repo: String? = null,
    // Filesystem
    var fsUsed: Int? = null,
    var fsTotal: Int? = null,
    // Bus / hardware
    var busCount: Int? = null,
    var busTypes: String? = null,
    // Feature flags
    var hasRGBW: Boolean? = null,
    var hasCCT: Boolean? = null,
    var ablEnabled: Boolean? = null,
    var cctFromRgb: Boolean? = null,
    var whiteBalanceCorrection: Boolean? = null,
    var gammaCorrection: Boolean? = null,
    var autoSegments: Boolean? = null,
    var nightlightEnabled: Boolean? = null,
    var relayConfigured: Boolean? = null,
    var buttonCount: Int? = null,
    var i2cConfigured: Boolean? = null,
    var spiConfigured: Boolean? = null,
    var ethernetEnabled: Boolean? = null,
    var hueEnabled: Boolean? = null,
    var mqttEnabled: Boolean? = null,
    var alexaEnabled: Boolean? = null,
    var wledSyncSend: Boolean? = null,
    var espNowEnabled: Boolean? = null,
    var espNowSync: Boolean? = null,
    var espNowRemoteCount: Int? = null,
    // Usermods (stored as comma-separated strings)
    var usermods: String? = null,
    var usermodIds: String? = null,

    @CreationTimestamp
    val created: LocalDateTime? = null,

    @UpdateTimestamp
    var lastUpdate: LocalDateTime? = null
)
