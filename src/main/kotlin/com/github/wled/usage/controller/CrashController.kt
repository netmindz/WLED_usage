package com.github.wled.usage.controller

import com.github.wled.usage.dto.CrashReportRequest
import com.github.wled.usage.dto.MapFileUploadRequest
import com.github.wled.usage.service.CrashService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/crash")
class CrashController(val crashService: CrashService) {

    @PostMapping("/report")
    fun postCrashReport(
        @RequestBody request: CrashReportRequest,
        @RequestHeader("X-Country-Code", required = false) countryCode: String?
    ) {
        crashService.processCrashReport(request, countryCode)
    }

    @PostMapping("/map")
    fun uploadMapFile(@RequestBody request: MapFileUploadRequest) {
        crashService.uploadMapFile(request)
    }
}
