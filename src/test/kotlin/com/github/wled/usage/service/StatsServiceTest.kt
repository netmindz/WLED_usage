package com.github.wled.usage.service

import com.github.wled.usage.entity.Device
import com.github.wled.usage.entity.UpgradeEvent
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.UpgradeEventRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class StatsServiceTest {

    private val deviceRepository: DeviceRepository = mock()
    private val upgradeEventRepository: UpgradeEventRepository = mock()
    private val statsService = StatsService(deviceRepository, upgradeEventRepository)

    private fun createDevice(id: String, version: String, created: LocalDateTime? = null): Device {
        return Device(
            id = id,
            version = version,
            releaseName = "TestRelease",
            chip = "ESP32",
            bootloaderSHA256 = "abc123",
            created = created
        )
    }

    private fun createUpgradeEvent(
        device: Device,
        oldVersion: String,
        newVersion: String,
        created: LocalDateTime
    ): UpgradeEvent {
        return UpgradeEvent(
            id = null,
            device = device,
            oldVersion = oldVersion,
            newVersion = newVersion,
            created = created
        )
    }

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
        whenever(deviceRepository.countNewDevicesByWeekAndVersion(any())).thenReturn(emptyList())

        val result = statsService.getVersionOverTimeStats()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getVersionOverTimeStats should return version data grouped by week combining upgrades and new installations`() {
        val upgradeData = listOf(
            mapOf("weekStart" to "2026-01-05", "version" to "0.14.0", "eventCount" to 10L),
            mapOf("weekStart" to "2026-01-05", "version" to "0.13.3", "eventCount" to 5L),
            mapOf("weekStart" to "2026-01-12", "version" to "0.14.0", "eventCount" to 15L)
        )
        val installationData = listOf(
            mapOf("weekStart" to "2026-01-05", "version" to "0.14.0", "deviceCount" to 3L),
            mapOf("weekStart" to "2026-01-12", "version" to "0.14.0", "deviceCount" to 7L)
        )

        whenever(upgradeEventRepository.countUpgradeEventsByWeekAndVersion(any())).thenReturn(upgradeData)
        whenever(deviceRepository.countNewDevicesByWeekAndVersion(any())).thenReturn(installationData)

        val result = statsService.getVersionOverTimeStats()

        assertEquals(3, result.size)
        assertEquals("2026-01-05", result[0].week)
        assertEquals("0.13.3", result[0].version)
        assertEquals(5L, result[0].count)
        assertEquals("2026-01-05", result[1].week)
        assertEquals("0.14.0", result[1].version)
        assertEquals(13L, result[1].count) // 10 upgrades + 3 new installations
        assertEquals("2026-01-12", result[2].week)
        assertEquals("0.14.0", result[2].version)
        assertEquals(22L, result[2].count) // 15 upgrades + 7 new installations
    }

    @Test
    fun `getRunningVersionsStats should return empty list when no devices exist`() {
        whenever(deviceRepository.findAll()).thenReturn(emptyList())
        whenever(upgradeEventRepository.findAllWithDevice()).thenReturn(emptyList())

        val result = statsService.getRunningVersionsStats()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRunningVersionsStats should return version data grouped by week based on most recent upgrade event`() {
        val now = LocalDateTime.now()
        val weekStart = now.minusWeeks(2)

        val device1 = createDevice("d1", "0.14.0", created = now.minusWeeks(4))
        val device2 = createDevice("d2", "0.14.0", created = now.minusWeeks(4))

        // Device 1 upgraded from 0.13.3 to 0.14.0 one week ago
        val event1 = createUpgradeEvent(device1, "0.13.3", "0.14.0", now.minusWeeks(1))
        // Device 2 was already on 0.14.0 (check-in event)
        val event2 = createUpgradeEvent(device2, "0.14.0", "0.14.0", now.minusWeeks(3))

        whenever(deviceRepository.findAll()).thenReturn(listOf(device1, device2))
        whenever(upgradeEventRepository.findAllWithDevice()).thenReturn(listOf(event1, event2))

        val result = statsService.getRunningVersionsStats()

        assertTrue(result.isNotEmpty())
        // At least verify the structure is returned correctly
        result.forEach {
            assertTrue(it.week.isNotEmpty())
            assertTrue(it.version.isNotEmpty())
            assertTrue(it.count > 0)
        }
    }

    @Test
    fun `determineVersionAtTime should return current version when no events exist`() {
        val device = createDevice("d1", "0.14.0")
        val time = LocalDateTime.now()

        val result = statsService.determineVersionAtTime(device, emptyList(), time)

        assertEquals("0.14.0", result)
    }

    @Test
    fun `determineVersionAtTime should return new_version of most recent event before time`() {
        val device = createDevice("d1", "0.15.0")
        val time = LocalDateTime.of(2026, 2, 20, 0, 0)

        val events = listOf(
            createUpgradeEvent(device, "0.13.0", "0.14.0", LocalDateTime.of(2026, 2, 1, 10, 0)),
            createUpgradeEvent(device, "0.14.0", "0.14.1", LocalDateTime.of(2026, 2, 10, 10, 0))
        )

        val result = statsService.determineVersionAtTime(device, events, time)

        assertEquals("0.14.1", result)
    }

    @Test
    fun `determineVersionAtTime should return old_version of earliest event when all events are after time`() {
        val device = createDevice("d1", "0.15.0")
        val time = LocalDateTime.of(2026, 1, 1, 0, 0)

        val events = listOf(
            createUpgradeEvent(device, "0.13.0", "0.14.0", LocalDateTime.of(2026, 2, 1, 10, 0)),
            createUpgradeEvent(device, "0.14.0", "0.15.0", LocalDateTime.of(2026, 2, 10, 10, 0))
        )

        val result = statsService.determineVersionAtTime(device, events, time)

        assertEquals("0.13.0", result)
    }

    @Test
    fun `determineVersionAtTime should use events before time even when events after exist`() {
        val device = createDevice("d1", "0.16.0")
        val time = LocalDateTime.of(2026, 2, 5, 0, 0)

        val events = listOf(
            createUpgradeEvent(device, "0.13.0", "0.14.0", LocalDateTime.of(2026, 2, 1, 10, 0)),
            createUpgradeEvent(device, "0.14.0", "0.16.0", LocalDateTime.of(2026, 2, 10, 10, 0))
        )

        val result = statsService.determineVersionAtTime(device, events, time)

        assertEquals("0.14.0", result)
    }

    @Test
    fun `getRunningVersionsStats should show old version counts decreasing as devices upgrade`() {
        // This is the core scenario from the issue:
        // All devices start on 0.13.0, some upgrade to 0.14.0 over time
        val threeMonthsAgo = LocalDateTime.now().minusMonths(3)
        val twoWeeksAgo = LocalDateTime.now().minusWeeks(2)
        val oneWeekAgo = LocalDateTime.now().minusWeeks(1)

        // All devices created before the tracking window
        val device1 = createDevice("d1", "0.14.0", created = threeMonthsAgo.minusMonths(1))
        val device2 = createDevice("d2", "0.14.0", created = threeMonthsAgo.minusMonths(1))
        val device3 = createDevice("d3", "0.13.0", created = threeMonthsAgo.minusMonths(1))

        // Device 1 was on 0.13.0, upgraded to 0.14.0 two weeks ago
        val event1a = createUpgradeEvent(device1, "0.13.0", "0.13.0", threeMonthsAgo.plusWeeks(1))
        val event1b = createUpgradeEvent(device1, "0.13.0", "0.14.0", twoWeeksAgo)

        // Device 2 was on 0.13.0, upgraded to 0.14.0 one week ago
        val event2a = createUpgradeEvent(device2, "0.13.0", "0.13.0", threeMonthsAgo.plusWeeks(1))
        val event2b = createUpgradeEvent(device2, "0.13.0", "0.14.0", oneWeekAgo)

        // Device 3 stays on 0.13.0 (check-in only)
        val event3 = createUpgradeEvent(device3, "0.13.0", "0.13.0", threeMonthsAgo.plusWeeks(1))

        whenever(deviceRepository.findAll()).thenReturn(listOf(device1, device2, device3))
        whenever(upgradeEventRepository.findAllWithDevice()).thenReturn(
            listOf(event1a, event1b, event2a, event2b, event3)
        )

        val result = statsService.getRunningVersionsStats()

        // Group by week for analysis
        val byWeek = result.groupBy { it.week }

        // Find a week before any upgrades and a week after all upgrades
        val earlyWeeks = byWeek.entries.sortedBy { it.key }.take(3)
        val lateWeeks = byWeek.entries.sortedBy { it.key }.takeLast(1)

        // In early weeks, all 3 devices should be on 0.13.0
        for ((_, stats) in earlyWeeks) {
            val v13count = stats.find { it.version == "0.13.0" }?.count ?: 0
            assertEquals(3L, v13count, "Early weeks should have all 3 devices on 0.13.0")
        }

        // In the latest week, old version count should have decreased
        for ((_, stats) in lateWeeks) {
            val v13count = stats.find { it.version == "0.13.0" }?.count ?: 0
            val v14count = stats.find { it.version == "0.14.0" }?.count ?: 0
            assertTrue(v13count < 3, "Late weeks should have fewer than 3 devices on 0.13.0, got $v13count")
            assertTrue(v14count > 0, "Late weeks should have devices on 0.14.0, got $v14count")
        }
    }

    @Test
    fun `generateWeekStarts should generate weeks starting from Monday`() {
        val since = LocalDateTime.of(2026, 1, 7, 12, 0) // Wednesday Jan 7
        val result = statsService.generateWeekStarts(since)

        assertTrue(result.isNotEmpty())
        // First week should start on Monday Jan 5
        assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result[0])
        // Second week should start on Monday Jan 12
        if (result.size > 1) {
            assertEquals(LocalDateTime.of(2026, 1, 12, 0, 0), result[1])
        }
    }
}
