package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.repository.DeviceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class UsageServiceTest {

    private val deviceRepository: DeviceRepository = mock()
    private val usageService = UsageService(deviceRepository)

    @Test
    fun `sanitize should allow alphanumeric characters`() {
        assertEquals("abc123XYZ", UsageService.sanitize("abc123XYZ"))
    }

    @Test
    fun `sanitize should allow hyphen`() {
        assertEquals("test-device", UsageService.sanitize("test-device"))
    }

    @Test
    fun `sanitize should allow full stop`() {
        assertEquals("0.14.1", UsageService.sanitize("0.14.1"))
    }

    @Test
    fun `sanitize should allow underscore`() {
        assertEquals("ESP32_S3", UsageService.sanitize("ESP32_S3"))
    }

    @Test
    fun `sanitize should remove special characters`() {
        assertEquals("test123", UsageService.sanitize("test!@#\$%^&*()123"))
    }

    @Test
    fun `sanitize should remove spaces`() {
        assertEquals("testdevice", UsageService.sanitize("test device"))
    }

    @Test
    fun `sanitize should remove unicode characters`() {
        assertEquals("Hathpp", UsageService.sanitize("Hathápp"))
    }

    @Test
    fun `sanitizeNullable should handle null input`() {
        assertNull(UsageService.sanitizeNullable(null))
    }

    @Test
    fun `sanitize should handle empty string`() {
        assertEquals("", UsageService.sanitize(""))
    }

    @Test
    fun `sanitize should remove SQL injection attempts`() {
        assertEquals("1OR11", UsageService.sanitize("1' OR '1'='1"))
    }

    @Test
    fun `sanitize should remove script tags`() {
        assertEquals("scriptalertxssscript", UsageService.sanitize("<script>alert('xss')</script>"))
    }

    @Test
    fun `recordUpgradeEvent should sanitize all string fields before saving`() {
        val request = UpgradeEventRequest(
            deviceId = "device!@#123",
            version = "0.14.1<script>",
            previousVersion = "0.13.0",
            releaseName = "Hathápp",
            chip = "ESP32 S3",
            ledCount = 100,
            isMatrix = false,
            bootloaderSHA256 = "abc&123",
            brand = "Test Brand!",
            product = "Test<>Product",
            flashSize = "4MB (size)",
            partitionSizes = "1MB|2MB",
            psramSize = "2MB*"
        )
        val countryCode = "US!"

        whenever(deviceRepository.findById(any<String>())).thenReturn(Optional.empty())
        whenever(deviceRepository.save(any<Device>())).thenAnswer { it.arguments[0] as Device }

        usageService.recordUpgradeEvent(request, countryCode)

        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())

        val savedDevice = deviceCaptor.firstValue
        assertEquals("device123", savedDevice.id)
        assertEquals("0.14.1script", savedDevice.version)
        assertEquals("Hathpp", savedDevice.releaseName)
        assertEquals("ESP32S3", savedDevice.chip)
        assertEquals("abc123", savedDevice.bootloaderSHA256)
        assertEquals("TestBrand", savedDevice.brand)
        assertEquals("TestProduct", savedDevice.product)
        assertEquals("4MBsize", savedDevice.flashSize)
        assertEquals("1MB2MB", savedDevice.partitionSizes)
        assertEquals("2MB", savedDevice.psramSize)
        assertEquals("US", savedDevice.countryCode)
    }

    @Test
    fun `recordUpgradeEvent should handle null optional fields`() {
        val request = UpgradeEventRequest(
            deviceId = "device123",
            version = "0.14.1",
            previousVersion = "0.13.0",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 100,
            isMatrix = false,
            bootloaderSHA256 = "abc123",
            brand = null,
            product = null,
            flashSize = null,
            partitionSizes = null,
            psramSize = null
        )

        whenever(deviceRepository.findById(any<String>())).thenReturn(Optional.empty())
        whenever(deviceRepository.save(any<Device>())).thenAnswer { it.arguments[0] as Device }

        usageService.recordUpgradeEvent(request, null)

        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())

        val savedDevice = deviceCaptor.firstValue
        assertNull(savedDevice.brand)
        assertNull(savedDevice.product)
        assertNull(savedDevice.flashSize)
        assertNull(savedDevice.partitionSizes)
        assertNull(savedDevice.psramSize)
        assertNull(savedDevice.countryCode)
    }

    @Test
    fun `should set ledCount and isMatrix to null for fresh install with empty previousVersion`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-1",
            version = "1.0.0",
            previousVersion = "",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 30,
            isMatrix = false,
            bootloaderSHA256 = "abc123"
        )
        
        whenever(deviceRepository.findById("test-device-1")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertNull(savedDevice.ledCount)
        assertNull(savedDevice.isMatrix)
    }

    @Test
    fun `should set ledCount and isMatrix to null for fresh install when previousVersion equals version`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-2",
            version = "1.0.0",
            previousVersion = "1.0.0",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 30,
            isMatrix = false,
            bootloaderSHA256 = "abc123"
        )
        
        whenever(deviceRepository.findById("test-device-2")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertNull(savedDevice.ledCount)
        assertNull(savedDevice.isMatrix)
    }

    @Test
    fun `should preserve ledCount and isMatrix for upgrade with different previousVersion`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-3",
            version = "1.0.0",
            previousVersion = "0.9.0",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 30,
            isMatrix = false,
            bootloaderSHA256 = "abc123"
        )
        
        whenever(deviceRepository.findById("test-device-3")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertEquals(30, savedDevice.ledCount)
        assertEquals(false, savedDevice.isMatrix)
    }

    @Test
    fun `should preserve ledCount and isMatrix when ledCount is not default value`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-4",
            version = "1.0.0",
            previousVersion = "",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 100,
            isMatrix = true,
            bootloaderSHA256 = "abc123"
        )
        
        whenever(deviceRepository.findById("test-device-4")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertEquals(100, savedDevice.ledCount)
        assertEquals(true, savedDevice.isMatrix)
    }

    @Test
    fun `should set ledCount and isMatrix to null for fresh install with blank previousVersion`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-5",
            version = "1.0.0",
            previousVersion = "   ",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 30,
            isMatrix = false,
            bootloaderSHA256 = "abc123"
        )
        
        whenever(deviceRepository.findById("test-device-5")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertNull(savedDevice.ledCount)
        assertNull(savedDevice.isMatrix)
    }
}
