package com.github.wled.usage.repository

import com.github.wled.usage.entity.ReleaseDownloadSnapshot
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface ReleaseDownloadSnapshotRepository : CrudRepository<ReleaseDownloadSnapshot, Long> {

    fun findTopByRepoNameAndTagNameAndAssetNameOrderBySnapshotDateDesc(
        repoName: String,
        tagName: String,
        assetName: String
    ): ReleaseDownloadSnapshot?

    fun existsByRepoNameAndTagNameAndAssetNameAndSnapshotDate(
        repoName: String,
        tagName: String,
        assetName: String,
        snapshotDate: LocalDate
    ): Boolean

    @Query("""
        SELECT r FROM ReleaseDownloadSnapshot r
        WHERE r.snapshotDate = (
            SELECT MAX(r2.snapshotDate) FROM ReleaseDownloadSnapshot r2
            WHERE r2.repoName = r.repoName AND r2.tagName = r.tagName AND r2.assetName = r.assetName
        )
        ORDER BY r.repoName, r.tagName, r.assetName
    """)
    fun findLatestSnapshotPerAsset(): List<ReleaseDownloadSnapshot>
}
