package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.entity.UpgradeEvent
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.UpgradeEventRepository
import org.springframework.stereotype.Service

@Service
class UsageService(
    val deviceRepository: DeviceRepository,
    val upgradeEventRepository: UpgradeEventRepository
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
                repo = request.repo
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
        deviceRepository.save(device)
    }

}
