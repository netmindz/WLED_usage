package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class UsageService(val deviceRepository: DeviceRepository) {
    fun recordUpgradeEvent(request: UpgradeEventRequest, countryCode: String?) {
        val device = deviceRepository.findById(request.deviceId).orElse(
            Device(
                id = request.deviceId,
                version = request.version,
                releaseName = request.releaseName,
                chip = request.chip,
                ledCount = request.ledCount,
                isMatrix = request.isMatrix,
                bootloaderSHA256 = request.bootloaderSHA256,
                brand = request.brand,
                product = request.product,
                flashSize = request.flashSize,
                partitionSizes = request.partitionSizes,
                psramSize = request.psramSize,
                countryCode = countryCode
            )
        )
        device.releaseName = request.releaseName
        device.version = request.version
        device.ledCount = request.ledCount
        device.isMatrix = request.isMatrix
        device.bootloaderSHA256 = request.bootloaderSHA256
        device.brand = request.brand
        device.product = request.product
        device.flashSize = request.flashSize
        device.partitionSizes = request.partitionSizes
        device.psramSize = request.psramSize
        device.countryCode = countryCode
        deviceRepository.save(device)
    }

}
