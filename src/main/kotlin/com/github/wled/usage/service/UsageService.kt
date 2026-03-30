package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.entity.ReleaseNameHistory
import com.github.wled.usage.entity.RepoHistory
import com.github.wled.usage.entity.UpgradeEvent
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.ReleaseNameHistoryRepository
import com.github.wled.usage.repository.RepoHistoryRepository
import com.github.wled.usage.repository.UpgradeEventRepository
import org.springframework.stereotype.Service

@Service
class UsageService(
    val deviceRepository: DeviceRepository,
    val upgradeEventRepository: UpgradeEventRepository,
    val releaseNameHistoryRepository: ReleaseNameHistoryRepository,
    val repoHistoryRepository: RepoHistoryRepository
) {
    
    companion object {
        private const val DEFAULT_LED_COUNT = 30
    }
    
    /**
     * Detects if this is a fresh install based on previousVersion and ledCount.
     * A fresh install is detected when:
     * - previousVersion is empty, null, or equals the current version
     * - AND ledCount equals 30 (the default value)
     */
    private fun isFreshInstall(request: UpgradeEventRequest): Boolean {
        val previousVersionIsEmpty = request.previousVersion.isNullOrBlank() ||
                request.previousVersion == request.version
        return previousVersionIsEmpty && request.ledCount == DEFAULT_LED_COUNT
    }
    
    fun recordUpgradeEvent(request: UpgradeEventRequest, countryCode: String?) {
        val freshInstall = isFreshInstall(request)
        
        // For fresh installs, set ledCount and isMatrix to null since they represent default values
        val ledCount = if (freshInstall) null else request.ledCount
        val isMatrix = if (freshInstall) null else request.isMatrix
        
        val existingDevice = deviceRepository.findById(request.deviceId)
        val isNewDevice = existingDevice.isEmpty
        
        val device = existingDevice.orElse(
            Device(
                id = request.deviceId,
                version = request.version,
                releaseName = request.releaseName,
                chip = request.chip,
                ledCount = ledCount,
                isMatrix = isMatrix,
                bootloaderSHA256 = request.bootloaderSHA256,
                brand = request.brand,
                product = request.product,
                flashSize = request.flashSize,
                partitionSizes = request.partitionSizes,
                psramSize = request.psramSize,
                psramPresent = request.psramPresent,
                countryCode = countryCode,
                repo = request.repo,
                fsUsed = request.fsUsed,
                fsTotal = request.fsTotal,
                busCount = request.busCount,
                busTypes = request.busTypes?.joinToString(","),
                hasRGBW = request.hasRGBW,
                hasCCT = request.hasCCT,
                ablEnabled = request.ablEnabled,
                cctFromRgb = request.cctFromRgb,
                whiteBalanceCorrection = request.whiteBalanceCorrection,
                gammaCorrection = request.gammaCorrection,
                autoSegments = request.autoSegments,
                nightlightEnabled = request.nightlightEnabled,
                relayConfigured = request.relayConfigured,
                buttonCount = request.buttonCount,
                i2cConfigured = request.i2cConfigured,
                spiConfigured = request.spiConfigured,
                ethernetEnabled = request.ethernetEnabled,
                hueEnabled = request.hueEnabled,
                mqttEnabled = request.mqttEnabled,
                alexaEnabled = request.alexaEnabled,
                wledSyncSend = request.wledSyncSend,
                espNowEnabled = request.espNowEnabled,
                espNowSync = request.espNowSync,
                espNowRemoteCount = request.espNowRemoteCount,
                usermods = request.usermods?.joinToString(","),
                usermodIds = request.usermodIds?.joinToString(","),
            )
        )
        
        // Create UpgradeEvent only when updating an existing device
        if (!isNewDevice) {
            val oldVersion = device.version
            val upgradeEvent = UpgradeEvent(
                device = device,
                oldVersion = oldVersion,
                newVersion = request.version
            )
            upgradeEventRepository.save(upgradeEvent)

            if (device.releaseName != request.releaseName) {
                val releaseNameHistory = ReleaseNameHistory(
                    device = device,
                    releaseName = device.releaseName,
                    deviceLastUpdate = device.lastUpdate
                )
                releaseNameHistoryRepository.save(releaseNameHistory)
            }

            if (device.repo != null && device.repo != request.repo) {
                val repoHistory = RepoHistory(
                    device = device,
                    repo = device.repo!!,
                    deviceLastUpdate = device.lastUpdate
                )
                repoHistoryRepository.save(repoHistory)
            }
        }
        
        device.releaseName = request.releaseName
        device.version = request.version
        device.ledCount = ledCount
        device.isMatrix = isMatrix
        device.bootloaderSHA256 = request.bootloaderSHA256
        device.brand = request.brand
        device.product = request.product
        device.flashSize = request.flashSize
        device.partitionSizes = request.partitionSizes
        device.psramSize = request.psramSize
        device.psramPresent = request.psramPresent
        device.countryCode = countryCode
        device.repo = request.repo
        device.fsUsed = request.fsUsed
        device.fsTotal = request.fsTotal
        device.busCount = request.busCount
        device.busTypes = request.busTypes?.joinToString(",")
        device.hasRGBW = request.hasRGBW
        device.hasCCT = request.hasCCT
        device.ablEnabled = request.ablEnabled
        device.cctFromRgb = request.cctFromRgb
        device.whiteBalanceCorrection = request.whiteBalanceCorrection
        device.gammaCorrection = request.gammaCorrection
        device.autoSegments = request.autoSegments
        device.nightlightEnabled = request.nightlightEnabled
        device.relayConfigured = request.relayConfigured
        device.buttonCount = request.buttonCount
        device.i2cConfigured = request.i2cConfigured
        device.spiConfigured = request.spiConfigured
        device.ethernetEnabled = request.ethernetEnabled
        device.hueEnabled = request.hueEnabled
        device.mqttEnabled = request.mqttEnabled
        device.alexaEnabled = request.alexaEnabled
        device.wledSyncSend = request.wledSyncSend
        device.espNowEnabled = request.espNowEnabled
        device.espNowSync = request.espNowSync
        device.espNowRemoteCount = request.espNowRemoteCount
        device.usermods = request.usermods?.joinToString(",")
        device.usermodIds = request.usermodIds?.joinToString(",")
        deviceRepository.save(device)
    }

}
