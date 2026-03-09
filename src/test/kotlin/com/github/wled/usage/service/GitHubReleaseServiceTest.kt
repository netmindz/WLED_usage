package com.github.wled.usage.service

import com.github.wled.usage.entity.ReleaseDownloadSnapshot
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.ReleaseDownloadSnapshotRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDate

class GitHubReleaseServiceTest {

    private val releaseDownloadSnapshotRepository: ReleaseDownloadSnapshotRepository = mock()
    private val deviceRepository: DeviceRepository = mock()
    private val gitHubReleaseService = GitHubReleaseService(
        releaseDownloadSnapshotRepository,
        deviceRepository,
        token = ""
    )

    private val today = LocalDate.now()
    private val repoName = "Aircoookie/WLED"

    @Test
    fun `processAndStoreSnapshots should set delta equal to downloadCount when no previous snapshot exists`() {
        val releases = listOf(
            GitHubRelease(
                tagName = "v0.14.2",
                assets = listOf(GitHubAsset(name = "WLED_0.14.2_ESP32.bin", downloadCount = 1000L))
            )
        )

        whenever(releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(any(), any(), any(), any()))
            .thenReturn(false)
        whenever(releaseDownloadSnapshotRepository.findTopByRepoNameAndTagNameAndAssetNameOrderBySnapshotDateDesc(any(), any(), any()))
            .thenReturn(null)

        gitHubReleaseService.processAndStoreSnapshots(releases, repoName)

        val captor = argumentCaptor<ReleaseDownloadSnapshot>()
        verify(releaseDownloadSnapshotRepository).save(captor.capture())
        assertEquals(repoName, captor.firstValue.repoName)
        assertEquals("v0.14.2", captor.firstValue.tagName)
        assertEquals("WLED_0.14.2_ESP32.bin", captor.firstValue.assetName)
        assertEquals(1000L, captor.firstValue.downloadCount)
        assertEquals(1000L, captor.firstValue.delta)
    }

    @Test
    fun `processAndStoreSnapshots should calculate correct delta from previous snapshot`() {
        val previousSnapshot = ReleaseDownloadSnapshot(
            id = 1L,
            repoName = repoName,
            tagName = "v0.14.2",
            assetName = "WLED_0.14.2_ESP32.bin",
            downloadCount = 800L,
            delta = 800L,
            snapshotDate = today.minusDays(1)
        )
        val releases = listOf(
            GitHubRelease(
                tagName = "v0.14.2",
                assets = listOf(GitHubAsset(name = "WLED_0.14.2_ESP32.bin", downloadCount = 1000L))
            )
        )

        whenever(releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(any(), any(), any(), any()))
            .thenReturn(false)
        whenever(releaseDownloadSnapshotRepository.findTopByRepoNameAndTagNameAndAssetNameOrderBySnapshotDateDesc(
            repoName, "v0.14.2", "WLED_0.14.2_ESP32.bin"
        )).thenReturn(previousSnapshot)

        gitHubReleaseService.processAndStoreSnapshots(releases, repoName)

        val captor = argumentCaptor<ReleaseDownloadSnapshot>()
        verify(releaseDownloadSnapshotRepository).save(captor.capture())
        assertEquals(200L, captor.firstValue.delta)
        assertEquals(1000L, captor.firstValue.downloadCount)
    }

    @Test
    fun `processAndStoreSnapshots should not save snapshot if one already exists for today`() {
        val releases = listOf(
            GitHubRelease(
                tagName = "v0.14.2",
                assets = listOf(GitHubAsset(name = "WLED_0.14.2_ESP32.bin", downloadCount = 1000L))
            )
        )

        whenever(releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(any(), any(), any(), any()))
            .thenReturn(true)

        gitHubReleaseService.processAndStoreSnapshots(releases, repoName)

        verify(releaseDownloadSnapshotRepository, never()).save(any())
    }

    @Test
    fun `processAndStoreSnapshots should store delta as zero when download count does not increase`() {
        val previousSnapshot = ReleaseDownloadSnapshot(
            id = 1L,
            repoName = repoName,
            tagName = "v0.14.2",
            assetName = "WLED_0.14.2_ESP32.bin",
            downloadCount = 1000L,
            delta = 200L,
            snapshotDate = today.minusDays(1)
        )
        val releases = listOf(
            GitHubRelease(
                tagName = "v0.14.2",
                assets = listOf(GitHubAsset(name = "WLED_0.14.2_ESP32.bin", downloadCount = 1000L))
            )
        )

        whenever(releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(any(), any(), any(), any()))
            .thenReturn(false)
        whenever(releaseDownloadSnapshotRepository.findTopByRepoNameAndTagNameAndAssetNameOrderBySnapshotDateDesc(
            repoName, "v0.14.2", "WLED_0.14.2_ESP32.bin"
        )).thenReturn(previousSnapshot)

        gitHubReleaseService.processAndStoreSnapshots(releases, repoName)

        val captor = argumentCaptor<ReleaseDownloadSnapshot>()
        verify(releaseDownloadSnapshotRepository).save(captor.capture())
        assertEquals(0L, captor.firstValue.delta)
    }

    @Test
    fun `processAndStoreSnapshots should handle multiple releases and assets`() {
        val releases = listOf(
            GitHubRelease(
                tagName = "v0.14.2",
                assets = listOf(
                    GitHubAsset("WLED_0.14.2_ESP32.bin", 1000L),
                    GitHubAsset("WLED_0.14.2_ESP8266.bin", 500L)
                )
            ),
            GitHubRelease(
                tagName = "v0.14.1",
                assets = listOf(GitHubAsset("WLED_0.14.1_ESP32.bin", 2000L))
            )
        )

        whenever(releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(any(), any(), any(), any()))
            .thenReturn(false)
        whenever(releaseDownloadSnapshotRepository.findTopByRepoNameAndTagNameAndAssetNameOrderBySnapshotDateDesc(any(), any(), any()))
            .thenReturn(null)

        gitHubReleaseService.processAndStoreSnapshots(releases, repoName)

        verify(releaseDownloadSnapshotRepository, times(3)).save(any())
    }

    @Test
    fun `processAndStoreSnapshots should handle release with no assets`() {
        val releases = listOf(
            GitHubRelease(tagName = "v0.14.2", assets = emptyList())
        )

        whenever(releaseDownloadSnapshotRepository.existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(any(), any(), any(), any()))
            .thenReturn(false)

        gitHubReleaseService.processAndStoreSnapshots(releases, repoName)

        verify(releaseDownloadSnapshotRepository, never()).save(any())
    }

    @Test
    fun `pollAndStoreReleaseDownloads should skip polling when no repos in device table`() {
        whenever(deviceRepository.findDistinctRepos()).thenReturn(emptyList())

        gitHubReleaseService.pollAndStoreReleaseDownloads()

        verify(releaseDownloadSnapshotRepository, never()).save(any())
    }

    @Test
    fun `pollAndStoreReleaseDownloads should skip repo with invalid format`() {
        whenever(deviceRepository.findDistinctRepos()).thenReturn(listOf("invalid-repo-format"))

        gitHubReleaseService.pollAndStoreReleaseDownloads()

        verify(releaseDownloadSnapshotRepository, never()).save(any())
    }

    @Test
    fun `getReleaseDownloadStats should return mapped stats from repository`() {
        val snapshots = listOf(
            ReleaseDownloadSnapshot(
                id = 1L,
                repoName = repoName,
                tagName = "v0.14.2",
                assetName = "WLED_0.14.2_ESP32.bin",
                downloadCount = 1000L,
                delta = 200L,
                snapshotDate = today
            )
        )

        whenever(releaseDownloadSnapshotRepository.findLatestSnapshotPerAsset()).thenReturn(snapshots)

        val result = gitHubReleaseService.getReleaseDownloadStats()

        assertEquals(1, result.size)
        assertEquals(repoName, result[0].repoName)
        assertEquals("v0.14.2", result[0].tagName)
        assertEquals("WLED_0.14.2_ESP32.bin", result[0].assetName)
        assertEquals(1000L, result[0].downloadCount)
        assertEquals(200L, result[0].delta)
        assertEquals(today.toString(), result[0].snapshotDate)
    }

    @Test
    fun `getReleaseDownloadStats should return empty list when no snapshots exist`() {
        whenever(releaseDownloadSnapshotRepository.findLatestSnapshotPerAsset()).thenReturn(emptyList())

        val result = gitHubReleaseService.getReleaseDownloadStats()

        assertTrue(result.isEmpty())
    }
}
