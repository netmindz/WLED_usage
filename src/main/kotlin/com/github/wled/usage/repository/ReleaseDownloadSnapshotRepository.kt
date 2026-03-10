package com.github.wled.usage.repository

import com.github.wled.usage.entity.ReleaseDownloadSnapshot
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface ReleaseDownloadSnapshotRepository : CrudRepository<ReleaseDownloadSnapshot, Long> {

    fun findTopByRepoNameAndTagNameAndAssetNameOrderByCreatedDesc(
        repoName: String,
        tagName: String,
        assetName: String
    ): ReleaseDownloadSnapshot?

    fun existsByRepoNameAndTagNameAndAssetNameAndCreatedBetween(
        repoName: String,
        tagName: String,
        assetName: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): Boolean

    @Query("""
        SELECT r FROM ReleaseDownloadSnapshot r
        WHERE r.created = (
            SELECT MAX(r2.created) FROM ReleaseDownloadSnapshot r2
            WHERE r2.repoName = r.repoName AND r2.tagName = r.tagName AND r2.assetName = r.assetName
        )
        ORDER BY r.repoName, r.tagName, r.assetName
    """)
    fun findLatestSnapshotPerAsset(): List<ReleaseDownloadSnapshot>
}
