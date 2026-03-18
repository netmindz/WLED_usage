package com.github.wled.usage.repository

import com.github.wled.usage.entity.UpgradeEvent
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface UpgradeEventRepository : CrudRepository<UpgradeEvent, Long> {

    @Query("SELECT ue FROM UpgradeEvent ue JOIN FETCH ue.device ORDER BY ue.device.id, ue.created")
    fun findAllWithDevice(): List<UpgradeEvent>

    @Query("SELECT ue FROM UpgradeEvent ue JOIN FETCH ue.device d WHERE d.repo = :repo ORDER BY d.id, ue.created")
    fun findAllWithDeviceByRepo(repo: String): List<UpgradeEvent>

    @Query(
        value = "SELECT DATE(DATE_SUB(ue.created, INTERVAL WEEKDAY(ue.created) DAY)) as weekStart, COUNT(*) as eventCount FROM upgrade_event ue JOIN device d ON ue.device_id = d.id WHERE ue.created >= :since AND (:repo IS NULL OR d.repo = :repo) GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeek(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(ue.created, INTERVAL WEEKDAY(ue.created) DAY)) as weekStart, ue.new_version as version, COUNT(*) as eventCount FROM upgrade_event ue JOIN device d ON ue.device_id = d.id WHERE ue.created >= :since AND (:repo IS NULL OR d.repo = :repo) GROUP BY weekStart, ue.new_version ORDER BY weekStart, ue.new_version",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeekAndVersion(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(ue.created, INTERVAL WEEKDAY(ue.created) DAY)) as weekStart, d.chip as chip, COUNT(*) as eventCount FROM upgrade_event ue JOIN device d ON ue.device_id = d.id WHERE ue.created >= :since AND d.chip IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY weekStart, d.chip ORDER BY weekStart, d.chip",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeekAndChip(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>
}
