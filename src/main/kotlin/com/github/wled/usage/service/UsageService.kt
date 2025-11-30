package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class UsageService(val deviceRepository: DeviceRepository) {

    companion object {
        private const val DEFAULT_LED_COUNT = 30
        private val ALLOWED_CHARS_REGEX = Regex("[^a-zA-Z0-9._-]")

        fun sanitize(input: String): String = input.replace(ALLOWED_CHARS_REGEX, "")

        fun sanitizeNullable(input: String?): String? = input?.replace(ALLOWED_CHARS_REGEX, "")
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

        val sanitizedDeviceId = sanitize(request.deviceId)
        val sanitizedVersion = sanitize(request.version)
        val sanitizedReleaseName = sanitize(request.releaseName)
        val sanitizedChip = sanitize(request.chip)
        val sanitizedBootloaderSHA256 = sanitize(request.bootloaderSHA256)
        val sanitizedBrand = sanitizeNullable(request.brand)
        val sanitizedProduct = sanitizeNullable(request.product)
        val sanitizedFlashSize = sanitizeNullable(request.flashSize)
        val sanitizedPartitionSizes = sanitizeNullable(request.partitionSizes)
        val sanitizedPsramSize = sanitizeNullable(request.psramSize)
        val sanitizedCountryCode = sanitizeNullable(countryCode)

        val device = deviceRepository.findById(sanitizedDeviceId).orElse(
            Device(
                id = sanitizedDeviceId,
                version = sanitizedVersion,
                releaseName = sanitizedReleaseName,
                chip = sanitizedChip,
                ledCount = ledCount,
                isMatrix = isMatrix,
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
        device.ledCount = ledCount
        device.isMatrix = isMatrix
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
