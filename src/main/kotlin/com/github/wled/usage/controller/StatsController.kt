package com.github.wled.usage.controller

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.ChipWeeklyStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.LedCountRangeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PsramSizeStats
import com.github.wled.usage.dto.ReleaseNameStats
import com.github.wled.usage.dto.UpgradeVsInstallationWeeklyStats
import com.github.wled.usage.dto.VersionStats
import com.github.wled.usage.dto.VersionWeeklyStats
import com.github.wled.usage.service.StatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
class StatsController(val statsService: StatsService) {

    @GetMapping("/country")
    fun getCountryStats(@RequestParam(required = false) repo: String?): List<CountryStats> {
        return statsService.getDeviceCountByCountry(repo)
    }
    
    @GetMapping("/version")
    fun getVersionStats(@RequestParam(required = false) repo: String?): List<VersionStats> {
        return statsService.getDeviceCountByVersion(repo)
    }
    
    @GetMapping("/chip")
    fun getChipStats(@RequestParam(required = false) repo: String?): List<ChipStats> {
        return statsService.getDeviceCountByChip(repo)
    }
    
    @GetMapping("/matrix")
    fun getMatrixStats(@RequestParam(required = false) repo: String?): List<MatrixStats> {
        return statsService.getDeviceCountByIsMatrix(repo)
    }
    
    @GetMapping("/flash-size")
    fun getFlashSizeStats(@RequestParam(required = false) repo: String?): List<FlashSizeStats> {
        return statsService.getDeviceCountByFlashSize(repo)
    }
    
    @GetMapping("/psram-size")
    fun getPsramSizeStats(@RequestParam(required = false) repo: String?): List<PsramSizeStats> {
        return statsService.getDeviceCountByPsramSize(repo)
    }
    
    @GetMapping("/release-name")
    fun getReleaseNameStats(@RequestParam(required = false) repo: String?): List<ReleaseNameStats> {
        return statsService.getDeviceCountByReleaseName(repo)
    }
    
    @GetMapping("/led-count")
    fun getLedCountRangeStats(@RequestParam(required = false) repo: String?): List<LedCountRangeStats> {
        return statsService.getDeviceCountByLedCountRange(repo)
    }

    @GetMapping("/upgrade-vs-installation")
    fun getUpgradeVsInstallationStats(@RequestParam(required = false) repo: String?): List<UpgradeVsInstallationWeeklyStats> {
        return statsService.getUpgradeVsInstallationStats(repo)
    }

    @GetMapping("/chip-over-time")
    fun getChipOverTimeStats(@RequestParam(required = false) repo: String?): List<ChipWeeklyStats> {
        return statsService.getChipOverTimeStats(repo)
    }

    @GetMapping("/version-over-time")
    fun getVersionOverTimeStats(@RequestParam(required = false) repo: String?): List<VersionWeeklyStats> {
        return statsService.getVersionOverTimeStats(repo)
    }

    @GetMapping("/running-versions")
    fun getRunningVersionsStats(@RequestParam(required = false) repo: String?): List<VersionWeeklyStats> {
        return statsService.getRunningVersionsStats(repo)
    }
}
