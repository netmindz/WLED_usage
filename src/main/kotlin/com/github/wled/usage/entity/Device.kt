package com.github.wled.usage.entity

import jakarta.persistence.Column
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
    @Column(name = "has_rgbw")
    var hasRGBW: Boolean? = null,
    @Column(name = "has_cct")
    var hasCCT: Boolean? = null,
    var ablEnabled: Boolean? = null,
    @Column(name = "cct_from_rgb")
    var cctFromRgb: Boolean? = null,
    var whiteBalanceCorrection: Boolean? = null,
    var gammaCorrection: Boolean? = null,
    var autoSegments: Boolean? = null,
    var nightlightEnabled: Boolean? = null,
    var relayConfigured: Boolean? = null,
    var buttonCount: Int? = null,
    @Column(name = "i2c_configured")
    var i2cConfigured: Boolean? = null,
    @Column(name = "spi_configured")
    var spiConfigured: Boolean? = null,
    var ethernetEnabled: Boolean? = null,
    var hueEnabled: Boolean? = null,
    @Column(name = "mqtt_enabled")
    var mqttEnabled: Boolean? = null,
    var alexaEnabled: Boolean? = null,
    @Column(name = "wled_sync_send")
    var wledSyncSend: Boolean? = null,
    @Column(name = "esp_now_enabled")
    var espNowEnabled: Boolean? = null,
    @Column(name = "esp_now_sync")
    var espNowSync: Boolean? = null,
    @Column(name = "esp_now_remote_count")
    var espNowRemoteCount: Int? = null,
    // Usermods (stored as comma-separated strings)
    var usermods: String? = null,
    var usermodIds: String? = null,

    @CreationTimestamp
    val created: LocalDateTime? = null,

    @UpdateTimestamp
    var lastUpdate: LocalDateTime? = null
)
