package com.github.wled.usage.controller

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PsramSizeStats
import com.github.wled.usage.dto.ReleaseNameStats
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
    
    @GetMapping("/matrix")
    fun getMatrixStats(): List<MatrixStats> {
        return statsService.getDeviceCountByIsMatrix()
    }
    
    @GetMapping("/flash-size")
    fun getFlashSizeStats(): List<FlashSizeStats> {
        return statsService.getDeviceCountByFlashSize()
    }
    
    @GetMapping("/psram-size")
    fun getPsramSizeStats(): List<PsramSizeStats> {
        return statsService.getDeviceCountByPsramSize()
    }
    
    @GetMapping("/release-name")
    fun getReleaseNameStats(): List<ReleaseNameStats> {
        return statsService.getDeviceCountByReleaseName()
    }
}
