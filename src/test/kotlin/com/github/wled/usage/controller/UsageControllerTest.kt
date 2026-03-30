package com.github.wled.usage.controller

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.service.UsageService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.assertj.core.api.Assertions.assertThat

@WebMvcTest(UsageController::class)
class UsageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var usageService: UsageService

    @Test
    fun `postUpgradeEvent should extract X-Country-Code header and pass to service`() {
        val requestBody = """
            {
                "deviceId": "test-device-123",
                "version": "1.0.0",
                "previousVersion": "0.9.0",
                "releaseName": "stable",
                "chip": "ESP32",
                "ledCount": 100,
                "isMatrix": false,
                "bootloaderSHA256": "abc123"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/usage/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Country-Code", "US")
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<UpgradeEventRequest>()
        val countryCodeCaptor = argumentCaptor<String>()
        verify(usageService).recordUpgradeEvent(requestCaptor.capture(), countryCodeCaptor.capture())

        assertThat(requestCaptor.firstValue.deviceId).isEqualTo("test-device-123")
        assertThat(countryCodeCaptor.firstValue).isEqualTo("US")
    }

    @Test
    fun `postUpgradeEvent should handle missing X-Country-Code header`() {
        val requestBody = """
            {
                "deviceId": "test-device-456",
                "version": "1.0.0",
                "previousVersion": "0.9.0",
                "releaseName": "stable",
                "chip": "ESP32",
                "ledCount": 50,
                "isMatrix": false,
                "bootloaderSHA256": "def456"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/usage/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<UpgradeEventRequest>()
        verify(usageService).recordUpgradeEvent(requestCaptor.capture(), org.mockito.kotlin.isNull())

        assertThat(requestCaptor.firstValue.deviceId).isEqualTo("test-device-456")
    }

    @Test
    fun `postUpgradeEvent should handle psramPresent field`() {
        val requestBody = """
            {
                "deviceId": "test-device-789",
                "version": "1.0.0",
                "previousVersion": "0.9.0",
                "releaseName": "stable",
                "chip": "ESP32",
                "ledCount": 100,
                "isMatrix": false,
                "bootloaderSHA256": "ghi789",
                "psramPresent": true
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/usage/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<UpgradeEventRequest>()
        verify(usageService).recordUpgradeEvent(requestCaptor.capture(), org.mockito.kotlin.isNull())

        assertThat(requestCaptor.firstValue.deviceId).isEqualTo("test-device-789")
        assertThat(requestCaptor.firstValue.psramPresent).isEqualTo(true)
    }

    @Test
    fun `postUpgradeEvent should handle repo field`() {
        val requestBody = """
            {
                "deviceId": "test-device-999",
                "version": "1.0.0",
                "previousVersion": "0.9.0",
                "releaseName": "stable",
                "chip": "ESP32",
                "ledCount": 100,
                "isMatrix": false,
                "bootloaderSHA256": "jkl999",
                "repo": "custom-repo"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/usage/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<UpgradeEventRequest>()
        verify(usageService).recordUpgradeEvent(requestCaptor.capture(), org.mockito.kotlin.isNull())

        assertThat(requestCaptor.firstValue.deviceId).isEqualTo("test-device-999")
        assertThat(requestCaptor.firstValue.repo).isEqualTo("custom-repo")
    }
}
