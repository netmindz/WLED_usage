package com.github.wled.usage.service

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.entity.Device
import com.github.wled.usage.repository.DeviceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class UsageServiceTest {

    private val deviceRepository: DeviceRepository = mock()
    private val usageService = UsageService(deviceRepository)

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

    @Test
    fun `should preserve psramPresent when provided`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-6",
            version = "1.0.0",
            previousVersion = "0.9.0",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 50,
            isMatrix = false,
            bootloaderSHA256 = "abc123",
            psramPresent = true
        )
        
        whenever(deviceRepository.findById("test-device-6")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertEquals(true, savedDevice.psramPresent)
    }

    @Test
    fun `should handle null psramPresent`() {
        val request = UpgradeEventRequest(
            deviceId = "test-device-7",
            version = "1.0.0",
            previousVersion = "0.9.0",
            releaseName = "stable",
            chip = "ESP32",
            ledCount = 50,
            isMatrix = false,
            bootloaderSHA256 = "abc123",
            psramPresent = null
        )
        
        whenever(deviceRepository.findById("test-device-7")).thenReturn(Optional.empty())
        
        usageService.recordUpgradeEvent(request, null)
        
        val deviceCaptor = argumentCaptor<Device>()
        verify(deviceRepository).save(deviceCaptor.capture())
        
        val savedDevice = deviceCaptor.firstValue
        assertNull(savedDevice.psramPresent)
    }
}
