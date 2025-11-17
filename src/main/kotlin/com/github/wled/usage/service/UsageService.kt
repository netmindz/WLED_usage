package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class UsageService(val deviceRepository: DeviceRepository) {
    fun recordUpgradeEvent(request: UpgradeEventRequest) {
        val device = deviceRepository.findById(request.deviceId).orElse(
            Device(
                id = request.deviceId,
                version = request.version,
                releaseName = request.releaseName,
                chip = request.chip,
                ledCount = request.ledCount,
                isMatrix = request.isMatrix,
                bootloaderSHA256 = request.bootloaderSHA256
            )
        )
        device.releaseName = request.releaseName
        device.version = request.version
        device.ledCount = request.ledCount
        device.isMatrix = request.isMatrix
        device.bootloaderSHA256 = request.bootloaderSHA256
        deviceRepository.save(device)
    }

}
