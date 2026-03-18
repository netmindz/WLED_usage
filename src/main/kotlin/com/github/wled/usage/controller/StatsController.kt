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
import com.github.wled.usage.service.GitHubUserService
import com.github.wled.usage.service.StatsService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/stats")
class StatsController(
    val statsService: StatsService,
    val gitHubUserService: GitHubUserService
) {

    private fun validateRepoAccess(repo: String?, authentication: OAuth2AuthenticationToken?) {
        if (repo == null) return
        if (authentication == null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required to filter by repo")
        }
        val allowedRepos = gitHubUserService.getWriteAccessRepos(authentication)
        if (repo !in allowedRepos) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have write access to repo: $repo")
        }
    }

    @GetMapping("/country")
    fun getCountryStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<CountryStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByCountry(repo)
    }
    
    @GetMapping("/version")
    fun getVersionStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<VersionStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByVersion(repo)
    }
    
    @GetMapping("/chip")
    fun getChipStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<ChipStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByChip(repo)
    }
    
    @GetMapping("/matrix")
    fun getMatrixStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<MatrixStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByIsMatrix(repo)
    }
    
    @GetMapping("/flash-size")
    fun getFlashSizeStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<FlashSizeStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByFlashSize(repo)
    }
    
    @GetMapping("/psram-size")
    fun getPsramSizeStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<PsramSizeStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByPsramSize(repo)
    }
    
    @GetMapping("/release-name")
    fun getReleaseNameStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<ReleaseNameStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByReleaseName(repo)
    }
    
    @GetMapping("/led-count")
    fun getLedCountRangeStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<LedCountRangeStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getDeviceCountByLedCountRange(repo)
    }

    @GetMapping("/upgrade-vs-installation")
    fun getUpgradeVsInstallationStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<UpgradeVsInstallationWeeklyStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getUpgradeVsInstallationStats(repo)
    }

    @GetMapping("/chip-over-time")
    fun getChipOverTimeStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<ChipWeeklyStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getChipOverTimeStats(repo)
    }

    @GetMapping("/version-over-time")
    fun getVersionOverTimeStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<VersionWeeklyStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getVersionOverTimeStats(repo)
    }

    @GetMapping("/running-versions")
    fun getRunningVersionsStats(
        @RequestParam(required = false) repo: String?,
        authentication: OAuth2AuthenticationToken?
    ): List<VersionWeeklyStats> {
        validateRepoAccess(repo, authentication)
        return statsService.getRunningVersionsStats(repo)
    }
}
