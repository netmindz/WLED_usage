package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class UsageService(val deviceRepository: DeviceRepository) {

    companion object {
        private val ALLOWED_CHARS_REGEX = Regex("[^a-zA-Z0-9._-]")

        fun sanitize(input: String?): String? = input?.replace(ALLOWED_CHARS_REGEX, "")
    }

    fun recordUpgradeEvent(request: UpgradeEventRequest, countryCode: String?) {
        val sanitizedDeviceId = sanitize(request.deviceId)!!
        val sanitizedVersion = sanitize(request.version)!!
        val sanitizedReleaseName = sanitize(request.releaseName)!!
        val sanitizedChip = sanitize(request.chip)!!
        val sanitizedBootloaderSHA256 = sanitize(request.bootloaderSHA256)!!
        val sanitizedBrand = sanitize(request.brand)
        val sanitizedProduct = sanitize(request.product)
        val sanitizedFlashSize = sanitize(request.flashSize)
        val sanitizedPartitionSizes = sanitize(request.partitionSizes)
        val sanitizedPsramSize = sanitize(request.psramSize)
        val sanitizedCountryCode = sanitize(countryCode)

        val device = deviceRepository.findById(sanitizedDeviceId).orElse(
            Device(
                id = sanitizedDeviceId,
                version = sanitizedVersion,
                releaseName = sanitizedReleaseName,
                chip = sanitizedChip,
                ledCount = request.ledCount,
                isMatrix = request.isMatrix,
                bootloaderSHA256 = sanitizedBootloaderSHA256,
                brand = sanitizedBrand,
                product = sanitizedProduct,
                flashSize = sanitizedFlashSize,
                partitionSizes = sanitizedPartitionSizes,
                psramSize = sanitizedPsramSize,
                countryCode = sanitizedCountryCode
            )
        )
        device.releaseName = sanitizedReleaseName
        device.version = sanitizedVersion
        device.ledCount = request.ledCount
        device.isMatrix = request.isMatrix
        device.bootloaderSHA256 = sanitizedBootloaderSHA256
        device.brand = sanitizedBrand
        device.product = sanitizedProduct
        device.flashSize = sanitizedFlashSize
        device.partitionSizes = sanitizedPartitionSizes
        device.psramSize = sanitizedPsramSize
        device.countryCode = sanitizedCountryCode
        deviceRepository.save(device)
    }

}
