package com.github.wled.usage.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.wled.usage.dto.ReleaseDownloadStats
import com.github.wled.usage.entity.ReleaseDownloadSnapshot
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.ReleaseDownloadSnapshotRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.time.LocalTime

data class GitHubRelease(
    @JsonProperty("tag_name") val tagName: String,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val name: String,
    @JsonProperty("download_count") val downloadCount: Long
)

@Service
class GitHubReleaseService(
    private val releaseDownloadSnapshotRepository: ReleaseDownloadSnapshotRepository,
    private val deviceRepository: DeviceRepository,
    @Value("\${github.releases.token:}") private val token: String
) {
    private val logger = LoggerFactory.getLogger(GitHubReleaseService::class.java)
    private val restClient = RestClient.create()

    @Scheduled(cron = "\${github.releases.poll-cron:0 0 2 * * *}")
    fun pollAndStoreReleaseDownloads() {
        val repos = deviceRepository.findDistinctRepos()
        if (repos.isEmpty()) {
            logger.info("No repos found in device table, skipping GitHub release polling")
            return
        }
        logger.info("Polling GitHub releases for ${repos.size} repo(s): $repos")
        repos.forEach { repoName ->
            val parts = repoName.split("/")
            if (parts.size != 2) {
                logger.warn("Skipping repo with unexpected format '$repoName' (expected 'owner/repo')")
                return@forEach
            }
            val (owner, repo) = parts
            try {
                val releases = fetchReleasesFromGitHub(owner, repo)
                processAndStoreSnapshots(releases, repoName)
                logger.info("Stored download snapshots for $repoName (${releases.size} releases)")
            } catch (e: Exception) {
                logger.error("Failed to poll GitHub releases for $repoName", e)
            }
        }
    }

    internal fun fetchReleasesFromGitHub(owner: String, repo: String): List<GitHubRelease> {
        val spec = restClient.get()
            .uri("https://api.github.com/repos/{owner}/{repo}/releases?per_page=100", owner, repo)
            .header("Accept", "application/vnd.github+json")
            .let { s -> if (token.isNotBlank()) s.header("Authorization", "Bearer $token") else s }
        return spec.retrieve()
            .body(object : ParameterizedTypeReference<List<GitHubRelease>>() {}) ?: emptyList()
    }

    internal fun processAndStoreSnapshots(releases: List<GitHubRelease>, repoName: String) {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.atTime(LocalTime.MAX)
        releases.forEach { release ->
            release.assets.forEach { asset ->
                if (releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndCreatedBetween(
                        repoName, release.tagName, asset.name, startOfDay, endOfDay
                    )
                ) {
                    logger.debug("Snapshot already exists for $repoName/${release.tagName}/${asset.name} on $today, skipping")
                    return@forEach
                }

                val previousSnapshot = releaseDownloadSnapshotRepository
                    .findTopByRepoNameAndTagNameAndAssetNameOrderByCreatedDesc(
                        repoName, release.tagName, asset.name
                    )

                val delta = if (previousSnapshot != null) {
                    maxOf(0L, asset.downloadCount - previousSnapshot.downloadCount)
                } else {
                    asset.downloadCount
                }

                if (asset.downloadCount == 0L || delta == 0L) {
                    logger.debug("Skipping snapshot for $repoName/${release.tagName}/${asset.name} on $today: downloadCount=${asset.downloadCount}, delta=$delta")
                    return@forEach
                }

                val snapshot = ReleaseDownloadSnapshot(
                    repoName = repoName,
                    tagName = release.tagName,
                    assetName = asset.name,
                    downloadCount = asset.downloadCount,
                    delta = delta
                )
                releaseDownloadSnapshotRepository.save(snapshot)
            }
        }
    }

    fun getReleaseDownloadStats(): List<ReleaseDownloadStats> {
        return releaseDownloadSnapshotRepository.findLatestSnapshotPerAsset().map {
            ReleaseDownloadStats(
                repoName = it.repoName,
                tagName = it.tagName,
                assetName = it.assetName,
                downloadCount = it.downloadCount,
                delta = it.delta,
                created = it.created?.toString() ?: ""
            )
        }
    }
}
