package com.github.wled.usage.controller

import com.github.wled.usage.dto.UpgradeEventRequest
import com.github.wled.usage.service.UsageService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usage")
class UsageController(val usageService: UsageService) {



    @PostMapping("/upgrade")
    fun postUpgradeEvent(@RequestBody request: UpgradeEventRequest) {
        usageService.recordUpgradeEvent(request)
    }
}