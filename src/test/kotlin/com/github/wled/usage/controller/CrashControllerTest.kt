package com.github.wled.usage.controller

import com.github.wled.usage.dto.CrashReportRequest
import com.github.wled.usage.dto.MapFileUploadRequest
import com.github.wled.usage.service.CrashService
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

@WebMvcTest(CrashController::class)
class CrashControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var crashService: CrashService

    @Test
    fun `postCrashReport should extract X-Country-Code header and pass to service`() {
        val requestBody = """
            {
                "deviceId": "test-device-123",
                "version": "1.0.0",
                "chip": "ESP32",
                "stackTrace": "Exception at 0x40080000\nBacktrace: 0x40080000:0x3ffb0000",
                "exceptionCause": "LoadProhibited"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/crash/report")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Country-Code", "US")
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<CrashReportRequest>()
        val countryCodeCaptor = argumentCaptor<String>()
        verify(crashService).processCrashReport(requestCaptor.capture(), countryCodeCaptor.capture())

        assertThat(requestCaptor.firstValue.deviceId).isEqualTo("test-device-123")
        assertThat(requestCaptor.firstValue.version).isEqualTo("1.0.0")
        assertThat(requestCaptor.firstValue.chip).isEqualTo("ESP32")
        assertThat(requestCaptor.firstValue.stackTrace).contains("0x40080000")
        assertThat(requestCaptor.firstValue.exceptionCause).isEqualTo("LoadProhibited")
        assertThat(countryCodeCaptor.firstValue).isEqualTo("US")
    }

    @Test
    fun `postCrashReport should handle missing X-Country-Code header`() {
        val requestBody = """
            {
                "version": "1.0.0",
                "stackTrace": "Exception at 0x40080000"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/crash/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<CrashReportRequest>()
        verify(crashService).processCrashReport(requestCaptor.capture(), org.mockito.kotlin.isNull())

        assertThat(requestCaptor.firstValue.version).isEqualTo("1.0.0")
    }

    @Test
    fun `postCrashReport should handle optional fields`() {
        val requestBody = """
            {
                "deviceId": "test-device-456",
                "version": "1.0.0",
                "chip": "ESP32-S3",
                "stackTrace": "Exception at 0x40080000",
                "exceptionCause": "IllegalInstruction"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/crash/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<CrashReportRequest>()
        verify(crashService).processCrashReport(requestCaptor.capture(), org.mockito.kotlin.isNull())

        assertThat(requestCaptor.firstValue.deviceId).isEqualTo("test-device-456")
        assertThat(requestCaptor.firstValue.chip).isEqualTo("ESP32-S3")
        assertThat(requestCaptor.firstValue.exceptionCause).isEqualTo("IllegalInstruction")
    }

    @Test
    fun `uploadMapFile should accept map file upload`() {
        val requestBody = """
            {
                "version": "1.0.0",
                "releaseName": "stable",
                "chip": "ESP32",
                "content": "0x40080000 main\n0x40080100 setup\n0x40080200 loop"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/crash/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<MapFileUploadRequest>()
        verify(crashService).uploadMapFile(requestCaptor.capture())

        assertThat(requestCaptor.firstValue.version).isEqualTo("1.0.0")
        assertThat(requestCaptor.firstValue.releaseName).isEqualTo("stable")
        assertThat(requestCaptor.firstValue.chip).isEqualTo("ESP32")
        assertThat(requestCaptor.firstValue.content).contains("main")
    }

    @Test
    fun `uploadMapFile should handle minimal request`() {
        val requestBody = """
            {
                "version": "1.0.0",
                "content": "0x40080000 main"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/crash/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)

        val requestCaptor = argumentCaptor<MapFileUploadRequest>()
        verify(crashService).uploadMapFile(requestCaptor.capture())

        assertThat(requestCaptor.firstValue.version).isEqualTo("1.0.0")
        assertThat(requestCaptor.firstValue.content).isEqualTo("0x40080000 main")
    }
}
