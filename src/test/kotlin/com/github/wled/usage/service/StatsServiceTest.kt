package com.github.wled.usage.service

import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.UpgradeEventRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class StatsServiceTest {

    private val deviceRepository: DeviceRepository = mock()
    private val upgradeEventRepository: UpgradeEventRepository = mock()
    private val statsService = StatsService(deviceRepository, upgradeEventRepository)

    @Test
    fun `getDeviceCountByLedCountRange should return empty list when no devices exist`() {
        whenever(deviceRepository.countDevicesByLedCount()).thenReturn(emptyList())
        
        val result = statsService.getDeviceCountByLedCountRange()
        
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getDeviceCountByLedCountRange should group devices into appropriate ranges`() {
        val mockData = listOf(
            mapOf("ledCount" to 5, "deviceCount" to 10L),
            mapOf("ledCount" to 15, "deviceCount" to 20L),
            mapOf("ledCount" to 75, "deviceCount" to 30L),
            mapOf("ledCount" to 150, "deviceCount" to 40L),
            mapOf("ledCount" to 800, "deviceCount" to 50L)
        )
        
        whenever(deviceRepository.countDevicesByLedCount()).thenReturn(mockData)
        
        val result = statsService.getDeviceCountByLedCountRange()
        
        // Should have aggregated counts in ranges
        assertTrue(result.isNotEmpty())
        
        // Check that 1-10 range has the device with 5 LEDs (10 count)
        val range1to10 = result.find { it.range == "1-10" }
        assertEquals(10L, range1to10?.deviceCount)
        
        // Check that 11-50 range has the device with 15 LEDs (20 count)
        val range11to50 = result.find { it.range == "11-50" }
        assertEquals(20L, range11to50?.deviceCount)
        
        // Check that 51-100 range has the device with 75 LEDs (30 count)
        val range51to100 = result.find { it.range == "51-100" }
        assertEquals(30L, range51to100?.deviceCount)
        
        // Check that 101-250 range has the device with 150 LEDs (40 count)
        val range101to250 = result.find { it.range == "101-250" }
        assertEquals(40L, range101to250?.deviceCount)
        
        // Check that 501-1000 range has the device with 800 LEDs (50 count)
        val range501to1000 = result.find { it.range == "501-1000" }
        assertEquals(50L, range501to1000?.deviceCount)
    }

    @Test
    fun `getDeviceCountByLedCountRange should handle high LED counts with dynamic ranges`() {
        val mockData = listOf(
            mapOf("ledCount" to 100, "deviceCount" to 10L),
            mapOf("ledCount" to 5000, "deviceCount" to 20L),
            mapOf("ledCount" to 8000, "deviceCount" to 15L),
            mapOf("ledCount" to 12000, "deviceCount" to 5L)
        )
        
        whenever(deviceRepository.countDevicesByLedCount()).thenReturn(mockData)
        
        val result = statsService.getDeviceCountByLedCountRange()
        
        assertTrue(result.isNotEmpty())
        
        // Check that 51-100 range has the device with 100 LEDs
        val range51to100 = result.find { it.range == "51-100" }
        assertEquals(10L, range51to100?.deviceCount)
        
        // Check that high LED counts are captured
        val totalDeviceCount = result.sumOf { it.deviceCount }
        assertEquals(50L, totalDeviceCount)
    }

    @Test
    fun `getDeviceCountByLedCountRange should aggregate multiple devices in same range`() {
        val mockData = listOf(
            mapOf("ledCount" to 20, "deviceCount" to 10L),
            mapOf("ledCount" to 30, "deviceCount" to 15L),
            mapOf("ledCount" to 40, "deviceCount" to 25L)
        )
        
        whenever(deviceRepository.countDevicesByLedCount()).thenReturn(mockData)
        
        val result = statsService.getDeviceCountByLedCountRange()
        
        // All should be in 11-50 range
        val range11to50 = result.find { it.range == "11-50" }
        assertEquals(50L, range11to50?.deviceCount) // 10 + 15 + 25 = 50
    }

    @Test
    fun `getDeviceCountByPsramSize should return devices with PSRAM sizes including None for devices with psram_present false`() {
        val mockData = listOf(
            mapOf("psramSize" to "2MB", "deviceCount" to 100L),
            mapOf("psramSize" to "4MB", "deviceCount" to 50L),
            mapOf("psramSize" to "None", "deviceCount" to 200L)
        )
        
        whenever(deviceRepository.countDevicesByPsramSize()).thenReturn(mockData)
        
        val result = statsService.getDeviceCountByPsramSize()
        
        assertEquals(3, result.size)
        
        val psram2mb = result.find { it.psramSize == "2MB" }
        assertEquals(100L, psram2mb?.deviceCount)
        
        val psram4mb = result.find { it.psramSize == "4MB" }
        assertEquals(50L, psram4mb?.deviceCount)
        
        val psramNone = result.find { it.psramSize == "None" }
        assertEquals(200L, psramNone?.deviceCount)
    }

    @Test
    fun `getUpgradeVsInstallationStats should return empty list when no data exists`() {
        whenever(upgradeEventRepository.countUpgradeEventsByWeek(any())).thenReturn(emptyList())
        whenever(deviceRepository.countNewDevicesByWeek(any())).thenReturn(emptyList())

        val result = statsService.getUpgradeVsInstallationStats()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getUpgradeVsInstallationStats should combine upgrade and installation data by week`() {
        val upgradeData = listOf(
            mapOf("weekStart" to "2026-01-05", "eventCount" to 10L),
            mapOf("weekStart" to "2026-01-12", "eventCount" to 15L)
        )
        val installationData = listOf(
            mapOf("weekStart" to "2026-01-05", "deviceCount" to 25L),
            mapOf("weekStart" to "2026-01-12", "deviceCount" to 30L)
        )

        whenever(upgradeEventRepository.countUpgradeEventsByWeek(any())).thenReturn(upgradeData)
        whenever(deviceRepository.countNewDevicesByWeek(any())).thenReturn(installationData)

        val result = statsService.getUpgradeVsInstallationStats()

        assertEquals(2, result.size)
        assertEquals("2026-01-05", result[0].week)
        assertEquals(10L, result[0].upgrades)
        assertEquals(25L, result[0].newInstallations)
        assertEquals("2026-01-12", result[1].week)
        assertEquals(15L, result[1].upgrades)
        assertEquals(30L, result[1].newInstallations)
    }

    @Test
    fun `getUpgradeVsInstallationStats should handle weeks with only upgrades or only installations`() {
        val upgradeData = listOf(
            mapOf("weekStart" to "2026-01-05", "eventCount" to 10L)
        )
        val installationData = listOf(
            mapOf("weekStart" to "2026-01-12", "deviceCount" to 25L)
        )

        whenever(upgradeEventRepository.countUpgradeEventsByWeek(any())).thenReturn(upgradeData)
        whenever(deviceRepository.countNewDevicesByWeek(any())).thenReturn(installationData)

        val result = statsService.getUpgradeVsInstallationStats()

        assertEquals(2, result.size)
        assertEquals("2026-01-05", result[0].week)
        assertEquals(10L, result[0].upgrades)
        assertEquals(0L, result[0].newInstallations)
        assertEquals("2026-01-12", result[1].week)
        assertEquals(0L, result[1].upgrades)
        assertEquals(25L, result[1].newInstallations)
    }

    @Test
    fun `getVersionOverTimeStats should return empty list when no data exists`() {
        whenever(upgradeEventRepository.countUpgradeEventsByWeekAndVersion(any())).thenReturn(emptyList())

        val result = statsService.getVersionOverTimeStats()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getVersionOverTimeStats should return version data grouped by week`() {
        val mockData = listOf(
            mapOf("weekStart" to "2026-01-05", "version" to "0.14.0", "eventCount" to 10L),
            mapOf("weekStart" to "2026-01-05", "version" to "0.13.3", "eventCount" to 5L),
            mapOf("weekStart" to "2026-01-12", "version" to "0.14.0", "eventCount" to 15L)
        )

        whenever(upgradeEventRepository.countUpgradeEventsByWeekAndVersion(any())).thenReturn(mockData)

        val result = statsService.getVersionOverTimeStats()

        assertEquals(3, result.size)
        assertEquals("2026-01-05", result[0].week)
        assertEquals("0.14.0", result[0].version)
        assertEquals(10L, result[0].count)
        assertEquals("2026-01-05", result[1].week)
        assertEquals("0.13.3", result[1].version)
        assertEquals(5L, result[1].count)
        assertEquals("2026-01-12", result[2].week)
        assertEquals("0.14.0", result[2].version)
        assertEquals(15L, result[2].count)
    }
}
