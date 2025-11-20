package com.github.wled.usage.controller

import com.github.wled.usage.dto.CountryStats
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
}
