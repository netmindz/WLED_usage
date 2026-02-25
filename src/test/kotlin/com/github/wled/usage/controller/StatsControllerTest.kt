package com.github.wled.usage.controller

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.LedCountRangeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PsramSizeStats
import com.github.wled.usage.dto.ReleaseNameStats
import com.github.wled.usage.dto.UpgradeVsInstallationWeeklyStats
import com.github.wled.usage.dto.VersionStats
import com.github.wled.usage.dto.VersionWeeklyStats
import com.github.wled.usage.service.StatsService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(StatsController::class)
class StatsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var statsService: StatsService

    @Test
    fun `getCountryStats should return list of country statistics`() {
        val mockStats = listOf(
            CountryStats("US", 100),
            CountryStats("GB", 50),
            CountryStats("DE", 75)
        )

        whenever(statsService.getDeviceCountByCountry()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/country")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].countryCode").value("US"))
            .andExpect(jsonPath("$[0].deviceCount").value(100))
            .andExpect(jsonPath("$[1].countryCode").value("GB"))
            .andExpect(jsonPath("$[1].deviceCount").value(50))
            .andExpect(jsonPath("$[2].countryCode").value("DE"))
            .andExpect(jsonPath("$[2].deviceCount").value(75))
    }

    @Test
    fun `getCountryStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByCountry()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/country")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getVersionStats should return list of version statistics`() {
        val mockStats = listOf(
            VersionStats("0.14.0", 150),
            VersionStats("0.13.3", 100),
            VersionStats("0.14.1", 80)
        )

        whenever(statsService.getDeviceCountByVersion()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/version")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].version").value("0.14.0"))
            .andExpect(jsonPath("$[0].deviceCount").value(150))
            .andExpect(jsonPath("$[1].version").value("0.13.3"))
            .andExpect(jsonPath("$[1].deviceCount").value(100))
            .andExpect(jsonPath("$[2].version").value("0.14.1"))
            .andExpect(jsonPath("$[2].deviceCount").value(80))
    }

    @Test
    fun `getVersionStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByVersion()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/version")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getChipStats should return list of chip statistics`() {
        val mockStats = listOf(
            ChipStats("ESP32", 200),
            ChipStats("ESP8266", 150),
            ChipStats("ESP32-S3", 100)
        )

        whenever(statsService.getDeviceCountByChip()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/chip")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].chip").value("ESP32"))
            .andExpect(jsonPath("$[0].deviceCount").value(200))
            .andExpect(jsonPath("$[1].chip").value("ESP8266"))
            .andExpect(jsonPath("$[1].deviceCount").value(150))
            .andExpect(jsonPath("$[2].chip").value("ESP32-S3"))
            .andExpect(jsonPath("$[2].deviceCount").value(100))
    }

    @Test
    fun `getChipStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByChip()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/chip")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getMatrixStats should return list of matrix statistics`() {
        val mockStats = listOf(
            MatrixStats(false, 200),
            MatrixStats(true, 100)
        )

        whenever(statsService.getDeviceCountByIsMatrix()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/matrix")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].isMatrix").value(false))
            .andExpect(jsonPath("$[0].deviceCount").value(200))
            .andExpect(jsonPath("$[1].isMatrix").value(true))
            .andExpect(jsonPath("$[1].deviceCount").value(100))
    }

    @Test
    fun `getMatrixStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByIsMatrix()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/matrix")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getFlashSizeStats should return list of flash size statistics`() {
        val mockStats = listOf(
            FlashSizeStats("4MB", 200),
            FlashSizeStats("8MB", 150),
            FlashSizeStats("16MB", 100)
        )

        whenever(statsService.getDeviceCountByFlashSize()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/flash-size")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].flashSize").value("4MB"))
            .andExpect(jsonPath("$[0].deviceCount").value(200))
            .andExpect(jsonPath("$[1].flashSize").value("8MB"))
            .andExpect(jsonPath("$[1].deviceCount").value(150))
            .andExpect(jsonPath("$[2].flashSize").value("16MB"))
            .andExpect(jsonPath("$[2].deviceCount").value(100))
    }

    @Test
    fun `getFlashSizeStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByFlashSize()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/flash-size")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getPsramSizeStats should return list of psram size statistics`() {
        val mockStats = listOf(
            PsramSizeStats("2MB", 150),
            PsramSizeStats("4MB", 100),
            PsramSizeStats("None", 50)
        )

        whenever(statsService.getDeviceCountByPsramSize()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/psram-size")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].psramSize").value("2MB"))
            .andExpect(jsonPath("$[0].deviceCount").value(150))
            .andExpect(jsonPath("$[1].psramSize").value("4MB"))
            .andExpect(jsonPath("$[1].deviceCount").value(100))
            .andExpect(jsonPath("$[2].psramSize").value("None"))
            .andExpect(jsonPath("$[2].deviceCount").value(50))
    }

    @Test
    fun `getPsramSizeStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByPsramSize()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/psram-size")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getReleaseNameStats should return list of release name statistics`() {
        val mockStats = listOf(
            ReleaseNameStats("Hathápp", 200),
            ReleaseNameStats("Gänansen", 150),
            ReleaseNameStats("Ingelsull", 100)
        )

        whenever(statsService.getDeviceCountByReleaseName()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/release-name")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].releaseName").value("Hathápp"))
            .andExpect(jsonPath("$[0].deviceCount").value(200))
            .andExpect(jsonPath("$[1].releaseName").value("Gänansen"))
            .andExpect(jsonPath("$[1].deviceCount").value(150))
            .andExpect(jsonPath("$[2].releaseName").value("Ingelsull"))
            .andExpect(jsonPath("$[2].deviceCount").value(100))
    }

    @Test
    fun `getReleaseNameStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByReleaseName()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/release-name")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
    
    @Test
    fun `getLedCountRangeStats should return list of LED count range statistics`() {
        val mockStats = listOf(
            LedCountRangeStats("1-10", 50),
            LedCountRangeStats("11-50", 120),
            LedCountRangeStats("51-100", 200),
            LedCountRangeStats("101-250", 150),
            LedCountRangeStats("251-500", 80)
        )

        whenever(statsService.getDeviceCountByLedCountRange()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/led-count")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].range").value("1-10"))
            .andExpect(jsonPath("$[0].deviceCount").value(50))
            .andExpect(jsonPath("$[1].range").value("11-50"))
            .andExpect(jsonPath("$[1].deviceCount").value(120))
            .andExpect(jsonPath("$[2].range").value("51-100"))
            .andExpect(jsonPath("$[2].deviceCount").value(200))
            .andExpect(jsonPath("$[3].range").value("101-250"))
            .andExpect(jsonPath("$[3].deviceCount").value(150))
            .andExpect(jsonPath("$[4].range").value("251-500"))
            .andExpect(jsonPath("$[4].deviceCount").value(80))
    }

    @Test
    fun `getLedCountRangeStats should return empty list when no devices exist`() {
        whenever(statsService.getDeviceCountByLedCountRange()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/led-count")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `getUpgradeVsInstallationStats should return weekly upgrade vs installation data`() {
        val mockStats = listOf(
            UpgradeVsInstallationWeeklyStats("2026-01-05", 10, 25),
            UpgradeVsInstallationWeeklyStats("2026-01-12", 15, 30),
            UpgradeVsInstallationWeeklyStats("2026-01-19", 20, 18)
        )

        whenever(statsService.getUpgradeVsInstallationStats()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/upgrade-vs-installation")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].week").value("2026-01-05"))
            .andExpect(jsonPath("$[0].upgrades").value(10))
            .andExpect(jsonPath("$[0].newInstallations").value(25))
            .andExpect(jsonPath("$[1].week").value("2026-01-12"))
            .andExpect(jsonPath("$[1].upgrades").value(15))
            .andExpect(jsonPath("$[1].newInstallations").value(30))
            .andExpect(jsonPath("$[2].week").value("2026-01-19"))
            .andExpect(jsonPath("$[2].upgrades").value(20))
            .andExpect(jsonPath("$[2].newInstallations").value(18))
    }

    @Test
    fun `getUpgradeVsInstallationStats should return empty list when no data exists`() {
        whenever(statsService.getUpgradeVsInstallationStats()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/upgrade-vs-installation")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `getVersionOverTimeStats should return weekly version data`() {
        val mockStats = listOf(
            VersionWeeklyStats("2026-01-05", "0.14.0", 10),
            VersionWeeklyStats("2026-01-05", "0.13.3", 5),
            VersionWeeklyStats("2026-01-12", "0.14.0", 15)
        )

        whenever(statsService.getVersionOverTimeStats()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/version-over-time")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].week").value("2026-01-05"))
            .andExpect(jsonPath("$[0].version").value("0.14.0"))
            .andExpect(jsonPath("$[0].count").value(10))
            .andExpect(jsonPath("$[1].week").value("2026-01-05"))
            .andExpect(jsonPath("$[1].version").value("0.13.3"))
            .andExpect(jsonPath("$[1].count").value(5))
            .andExpect(jsonPath("$[2].week").value("2026-01-12"))
            .andExpect(jsonPath("$[2].version").value("0.14.0"))
            .andExpect(jsonPath("$[2].count").value(15))
    }

    @Test
    fun `getVersionOverTimeStats should return empty list when no data exists`() {
        whenever(statsService.getVersionOverTimeStats()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/version-over-time")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `getRunningVersionsStats should return weekly running version data`() {
        val mockStats = listOf(
            VersionWeeklyStats("2026-01-05", "0.13.3", 5),
            VersionWeeklyStats("2026-01-05", "0.14.0", 10),
            VersionWeeklyStats("2026-01-12", "0.14.0", 15)
        )

        whenever(statsService.getRunningVersionsStats()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/running-versions")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].week").value("2026-01-05"))
            .andExpect(jsonPath("$[0].version").value("0.13.3"))
            .andExpect(jsonPath("$[0].count").value(5))
            .andExpect(jsonPath("$[1].week").value("2026-01-05"))
            .andExpect(jsonPath("$[1].version").value("0.14.0"))
            .andExpect(jsonPath("$[1].count").value(10))
            .andExpect(jsonPath("$[2].week").value("2026-01-12"))
            .andExpect(jsonPath("$[2].version").value("0.14.0"))
            .andExpect(jsonPath("$[2].count").value(15))
    }

    @Test
    fun `getRunningVersionsStats should return empty list when no data exists`() {
        whenever(statsService.getRunningVersionsStats()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/running-versions")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
}
