package com.github.wled.usage.controller

import com.github.wled.usage.dto.ReleaseDownloadStats
import com.github.wled.usage.service.GitHubReleaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
class ReleaseDownloadStatsController(val gitHubReleaseService: GitHubReleaseService) {

    @GetMapping("/release-downloads")
    fun getReleaseDownloadStats(): List<ReleaseDownloadStats> {
        return gitHubReleaseService.getReleaseDownloadStats()
    }
}
