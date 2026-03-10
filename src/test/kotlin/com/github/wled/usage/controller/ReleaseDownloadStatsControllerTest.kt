package com.github.wled.usage.controller

import com.github.wled.usage.dto.ReleaseDownloadStats
import com.github.wled.usage.service.GitHubReleaseService
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

@WebMvcTest(ReleaseDownloadStatsController::class)
class ReleaseDownloadStatsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var gitHubReleaseService: GitHubReleaseService

    @Test
    fun `getReleaseDownloadStats should return list of release download statistics`() {
        val mockStats = listOf(
            ReleaseDownloadStats("Aircoookie/WLED", "v0.14.2", "WLED_0.14.2_ESP32.bin", 10000L, 250L, "2026-03-09T00:00:00"),
            ReleaseDownloadStats("Aircoookie/WLED", "v0.14.2", "WLED_0.14.2_ESP8266.bin", 5000L, 100L, "2026-03-09T00:00:00"),
            ReleaseDownloadStats("Aircoookie/WLED", "v0.14.1", "WLED_0.14.1_ESP32.bin", 20000L, 50L, "2026-03-09T00:00:00")
        )

        whenever(gitHubReleaseService.getReleaseDownloadStats()).thenReturn(mockStats)

        mockMvc.perform(
            get("/api/stats/release-downloads")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].repoName").value("Aircoookie/WLED"))
            .andExpect(jsonPath("$[0].tagName").value("v0.14.2"))
            .andExpect(jsonPath("$[0].assetName").value("WLED_0.14.2_ESP32.bin"))
            .andExpect(jsonPath("$[0].downloadCount").value(10000))
            .andExpect(jsonPath("$[0].delta").value(250))
            .andExpect(jsonPath("$[0].created").value("2026-03-09T00:00:00"))
            .andExpect(jsonPath("$[1].tagName").value("v0.14.2"))
            .andExpect(jsonPath("$[1].assetName").value("WLED_0.14.2_ESP8266.bin"))
            .andExpect(jsonPath("$[2].tagName").value("v0.14.1"))
            .andExpect(jsonPath("$[2].assetName").value("WLED_0.14.1_ESP32.bin"))
    }

    @Test
    fun `getReleaseDownloadStats should return empty list when no data exists`() {
        whenever(gitHubReleaseService.getReleaseDownloadStats()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/stats/release-downloads")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
}
