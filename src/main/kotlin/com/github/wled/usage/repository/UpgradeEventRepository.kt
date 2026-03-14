package com.github.wled.usage.repository

import com.github.wled.usage.entity.UpgradeEvent
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface UpgradeEventRepository : CrudRepository<UpgradeEvent, Long> {

    @Query("SELECT ue FROM UpgradeEvent ue JOIN FETCH ue.device ORDER BY ue.device.id, ue.created")
    fun findAllWithDevice(): List<UpgradeEvent>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as eventCount FROM upgrade_event WHERE created >= :since GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeek(since: LocalDateTime): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, new_version as version, COUNT(*) as eventCount FROM upgrade_event WHERE created >= :since GROUP BY weekStart, new_version ORDER BY weekStart, new_version",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeekAndVersion(since: LocalDateTime): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(ue.created, INTERVAL WEEKDAY(ue.created) DAY)) as weekStart, d.chip as chip, COUNT(*) as eventCount FROM upgrade_event ue JOIN device d ON ue.device_id = d.id WHERE ue.created >= :since AND d.chip IS NOT NULL GROUP BY weekStart, d.chip ORDER BY weekStart, d.chip",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeekAndChip(since: LocalDateTime): List<Map<String, Any>>
}
