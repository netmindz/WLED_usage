package com.github.wled.usage.controller

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.VersionStats
import com.github.wled.usage.service.StatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
class StatsController(val statsService: StatsService) {

    @GetMapping("/country")
    fun getCountryStats(): List<CountryStats> {
        return statsService.getDeviceCountByCountry()
    }
    
    @GetMapping("/version")
    fun getVersionStats(): List<VersionStats> {
        return statsService.getDeviceCountByVersion()
    }
    
    @GetMapping("/chip")
    fun getChipStats(): List<ChipStats> {
        return statsService.getDeviceCountByChip()
    }
}
