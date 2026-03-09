package com.github.wled.usage.service

import com.github.wled.usage.dto.CrashReportRequest
import com.github.wled.usage.dto.MapFileUploadRequest
import com.github.wled.usage.entity.CrashInstance
import com.github.wled.usage.entity.CrashReport
import com.github.wled.usage.entity.Device
import com.github.wled.usage.entity.MapFile
import com.github.wled.usage.repository.CrashInstanceRepository
import com.github.wled.usage.repository.CrashReportRepository
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.MapFileRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.lenient
import java.util.*

class CrashServiceTest {

    private val crashReportRepository: CrashReportRepository = mock()
    private val crashInstanceRepository: CrashInstanceRepository = mock()
    private val mapFileRepository: MapFileRepository = mock()
    private val deviceRepository: DeviceRepository = mock()

    private val crashService = CrashService(
        crashReportRepository,
        crashInstanceRepository,
        mapFileRepository,
        deviceRepository
    )

    @Test
    fun `processCrashReport should create new crash report for unique stack trace`() {
        val request = CrashReportRequest(
            deviceId = "test-device",
            version = "1.0.0",
            chip = "ESP32",
            stackTrace = "Exception at 0x40080000",
            exceptionCause = "LoadProhibited"
        )

        whenever(crashReportRepository.findByStackTraceHash(any())).thenReturn(Optional.empty())
        whenever(mapFileRepository.findByVersion(any())).thenReturn(Optional.empty())
        whenever(deviceRepository.findById(any())).thenReturn(Optional.empty())
        
        val savedCrashReport = CrashReport(
            id = 1L,
            stackTraceHash = "test-hash",
            rawStackTrace = request.stackTrace,
            decodedStackTrace = null,
            exceptionCause = request.exceptionCause
        )
        whenever(crashReportRepository.save(any<CrashReport>())).thenReturn(savedCrashReport)
        
        val savedCrashInstance = CrashInstance(
            id = 1L,
            crashReport = savedCrashReport,
            device = null,
            version = request.version,
            chip = request.chip,
            countryCode = "US"
        )
        whenever(crashInstanceRepository.save(any<CrashInstance>())).thenReturn(savedCrashInstance)

        crashService.processCrashReport(request, "US")

        verify(crashReportRepository).findByStackTraceHash(any())
        verify(crashReportRepository).save(any())
        verify(crashInstanceRepository).save(any())
    }

    @Test
    fun `processCrashReport should reuse existing crash report with same stack trace`() {
        val request = CrashReportRequest(
            version = "1.0.0",
            stackTrace = "Exception at 0x40080000"
        )

        val existingCrashReport = CrashReport(
            id = 1L,
            stackTraceHash = "existing-hash",
            rawStackTrace = "Exception at 0x40080000",
            decodedStackTrace = null,
            exceptionCause = null
        )
        whenever(crashReportRepository.findByStackTraceHash(any())).thenReturn(Optional.of(existingCrashReport))
        
        val savedCrashReport = existingCrashReport
        whenever(crashReportRepository.save(any<CrashReport>())).thenReturn(savedCrashReport)
        
        val savedCrashInstance = CrashInstance(
            id = 1L,
            crashReport = savedCrashReport,
            device = null,
            version = request.version,
            chip = request.chip,
            countryCode = null
        )
        whenever(crashInstanceRepository.save(any<CrashInstance>())).thenReturn(savedCrashInstance)

        crashService.processCrashReport(request, null)

        verify(crashReportRepository).findByStackTraceHash(any())
        verify(crashReportRepository).save(existingCrashReport)
        verify(crashInstanceRepository).save(any())
    }

    @Test
    fun `processCrashReport should link to device if deviceId is provided`() {
        val request = CrashReportRequest(
            deviceId = "test-device",
            version = "1.0.0",
            stackTrace = "Exception at 0x40080000"
        )

        val device = Device(
            id = "test-device",
            version = "1.0.0",
            releaseName = "stable",
            chip = "ESP32",
            bootloaderSHA256 = "abc123"
        )

        whenever(crashReportRepository.findByStackTraceHash(any())).thenReturn(Optional.empty())
        whenever(mapFileRepository.findByVersion(any())).thenReturn(Optional.empty())
        whenever(deviceRepository.findById("test-device")).thenReturn(Optional.of(device))
        
        val savedCrashReport = CrashReport(
            id = 1L,
            stackTraceHash = "test-hash",
            rawStackTrace = request.stackTrace,
            decodedStackTrace = null,
            exceptionCause = null
        )
        whenever(crashReportRepository.save(any<CrashReport>())).thenReturn(savedCrashReport)
        
        val savedCrashInstance = CrashInstance(
            id = 1L,
            crashReport = savedCrashReport,
            device = device,
            version = request.version,
            chip = request.chip,
            countryCode = null
        )
        whenever(crashInstanceRepository.save(any<CrashInstance>())).thenReturn(savedCrashInstance)

        crashService.processCrashReport(request, null)

        verify(deviceRepository).findById("test-device")
        
        argumentCaptor<CrashInstance>().apply {
            verify(crashInstanceRepository).save(capture())
            assertThat(firstValue.device).isEqualTo(device)
        }
    }

    @Test
    fun `uploadMapFile should create new map file`() {
        val request = MapFileUploadRequest(
            version = "1.0.0",
            releaseName = "stable",
            chip = "ESP32",
            content = "0x40080000 main"
        )

        whenever(mapFileRepository.findByVersion("1.0.0")).thenReturn(Optional.empty())
        
        val savedMapFile = MapFile(
            id = 1L,
            version = request.version,
            releaseName = request.releaseName,
            chip = request.chip,
            content = request.content
        )
        whenever(mapFileRepository.save(any<MapFile>())).thenReturn(savedMapFile)

        crashService.uploadMapFile(request)

        argumentCaptor<MapFile>().apply {
            verify(mapFileRepository).save(capture())
            assertThat(firstValue.version).isEqualTo("1.0.0")
            assertThat(firstValue.releaseName).isEqualTo("stable")
            assertThat(firstValue.chip).isEqualTo("ESP32")
            assertThat(firstValue.content).isEqualTo("0x40080000 main")
        }
    }

    @Test
    fun `uploadMapFile should update existing map file`() {
        val request = MapFileUploadRequest(
            version = "1.0.0",
            releaseName = "stable-updated",
            chip = "ESP32",
            content = "0x40080000 main\n0x40080100 setup"
        )

        val existingMapFile = MapFile(
            id = 1L,
            version = "1.0.0",
            releaseName = "stable",
            chip = "ESP32",
            content = "0x40080000 main"
        )
        whenever(mapFileRepository.findByVersion("1.0.0")).thenReturn(Optional.of(existingMapFile))
        
        val updatedMapFile = existingMapFile.copy(
            releaseName = request.releaseName,
            chip = request.chip,
            content = request.content
        )
        whenever(mapFileRepository.save(any<MapFile>())).thenReturn(updatedMapFile)

        crashService.uploadMapFile(request)

        argumentCaptor<MapFile>().apply {
            verify(mapFileRepository).save(capture())
            assertThat(firstValue.version).isEqualTo("1.0.0")
            assertThat(firstValue.releaseName).isEqualTo("stable-updated")
            assertThat(firstValue.content).contains("setup")
        }
    }
}
