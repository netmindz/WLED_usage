package com.github.wled.usage.repository

import com.github.wled.usage.entity.UpgradeEvent
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface UpgradeEventRepository : CrudRepository<UpgradeEvent, Long> {

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as eventCount FROM upgrade_event WHERE created >= :since GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countUpgradeEventsByWeek(since: LocalDateTime): List<Map<String, Any>>
}
