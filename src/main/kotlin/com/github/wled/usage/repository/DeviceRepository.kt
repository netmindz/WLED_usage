package com.github.wled.usage.repository

import com.github.wled.usage.entity.Device
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface DeviceRepository : CrudRepository<Device, String> {

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as deviceCount FROM device WHERE created >= :since GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countNewDevicesByWeek(since: LocalDateTime): List<Map<String, Any>>
    @Query("SELECT d.countryCode as countryCode, COUNT(d) as deviceCount FROM Device d WHERE d.countryCode IS NOT NULL GROUP BY d.countryCode")
    fun countDevicesByCountryCode(): List<Map<String, Any>>
    
    @Query("SELECT d.version as version, COUNT(d) as deviceCount FROM Device d GROUP BY d.version ORDER BY COUNT(d) DESC")
    fun countDevicesByVersion(): List<Map<String, Any>>
    
    @Query("SELECT d.chip as chip, COUNT(d) as deviceCount FROM Device d WHERE d.chip IS NOT NULL GROUP BY d.chip ORDER BY COUNT(d) DESC")
    fun countDevicesByChip(): List<Map<String, Any>>
    
    @Query("SELECT d.isMatrix as isMatrix, COUNT(d) as deviceCount FROM Device d WHERE d.isMatrix IS NOT NULL GROUP BY d.isMatrix ORDER BY COUNT(d) DESC")
    fun countDevicesByIsMatrix(): List<Map<String, Any>>
    
    @Query("SELECT d.flashSize as flashSize, COUNT(d) as deviceCount FROM Device d WHERE d.flashSize IS NOT NULL GROUP BY d.flashSize ORDER BY COUNT(d) DESC")
    fun countDevicesByFlashSize(): List<Map<String, Any>>
    
    @Query("""
        SELECT CASE 
            WHEN d.psramPresent = false THEN 'None'
            WHEN d.psramPresent = true AND d.psramSize IS NOT NULL THEN d.psramSize
            ELSE 'Unknown'
        END as psramSize, 
        COUNT(d) as deviceCount 
        FROM Device d 
        WHERE d.psramPresent IS NOT NULL 
        GROUP BY CASE 
            WHEN d.psramPresent = false THEN 'None'
            WHEN d.psramPresent = true AND d.psramSize IS NOT NULL THEN d.psramSize
            ELSE 'Unknown'
        END 
        ORDER BY COUNT(d) DESC
    """)
    fun countDevicesByPsramSize(): List<Map<String, Any>>
    
    @Query("SELECT d.releaseName as releaseName, COUNT(d) as deviceCount FROM Device d WHERE d.releaseName IS NOT NULL GROUP BY d.releaseName ORDER BY COUNT(d) DESC")
    fun countDevicesByReleaseName(): List<Map<String, Any>>

    @Query("SELECT d.ledCount as ledCount, COUNT(d) as deviceCount FROM Device d WHERE d.ledCount IS NOT NULL GROUP BY d.ledCount ORDER BY d.ledCount ASC")
    fun countDevicesByLedCount(): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, version, COUNT(*) as deviceCount FROM device WHERE created >= :since GROUP BY weekStart, version ORDER BY weekStart, version",
        nativeQuery = true
    )
    fun countNewDevicesByWeekAndVersion(since: LocalDateTime): List<Map<String, Any>>

    @Query(
        value = """
            SELECT
                weeks.weekStart,
                COALESCE(
                    (SELECT ue.new_version
                     FROM upgrade_event ue
                     WHERE ue.device_id = d.id
                     AND ue.created < DATE_ADD(weeks.weekStart, INTERVAL 7 DAY)
                     ORDER BY ue.created DESC
                     LIMIT 1),
                    d.version
                ) as version,
                COUNT(*) as deviceCount
            FROM device d
            CROSS JOIN (
                SELECT DATE_ADD(DATE(DATE_SUB(:since, INTERVAL WEEKDAY(:since) DAY)), INTERVAL (n * 7) DAY) AS weekStart
                FROM (SELECT 0 as n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
                      UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12) nums
                WHERE DATE_ADD(DATE(DATE_SUB(:since, INTERVAL WEEKDAY(:since) DAY)), INTERVAL (n * 7) DAY) <= CURDATE()
            ) weeks
            WHERE d.created < DATE_ADD(weeks.weekStart, INTERVAL 7 DAY)
            GROUP BY weeks.weekStart, version
            ORDER BY weeks.weekStart, version
        """,
        nativeQuery = true
    )
    fun countRunningVersionsByWeek(since: LocalDateTime): List<Map<String, Any>>
}