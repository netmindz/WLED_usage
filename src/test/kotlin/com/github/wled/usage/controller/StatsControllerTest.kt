package com.github.wled.usage.controller

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PsramSizeStats
import com.github.wled.usage.dto.ReleaseNameStats
import com.github.wled.usage.dto.VersionStats
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
            PsramSizeStats("8MB", 50)
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
            .andExpect(jsonPath("$[2].psramSize").value("8MB"))
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
}
