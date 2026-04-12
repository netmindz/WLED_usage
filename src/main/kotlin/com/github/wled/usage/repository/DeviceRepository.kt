package com.github.wled.usage.repository

import com.github.wled.usage.entity.Device
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface DeviceRepository : CrudRepository<Device, String> {

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as deviceCount FROM device WHERE created >= :since AND (:repo IS NULL OR repo = :repo) GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countNewDevicesByWeek(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query("SELECT d.countryCode as countryCode, COUNT(d) as deviceCount FROM Device d WHERE d.countryCode IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY d.countryCode")
    fun countDevicesByCountryCode(repo: String? = null): List<Map<String, Any>>
    
    @Query("SELECT d.version as version, COUNT(d) as deviceCount FROM Device d WHERE (:repo IS NULL OR d.repo = :repo) GROUP BY d.version ORDER BY COUNT(d) DESC")
    fun countDevicesByVersion(repo: String? = null): List<Map<String, Any>>
    
    @Query("SELECT d.chip as chip, COUNT(d) as deviceCount FROM Device d WHERE d.chip IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY d.chip ORDER BY COUNT(d) DESC")
    fun countDevicesByChip(repo: String? = null): List<Map<String, Any>>
    
    @Query("SELECT d.isMatrix as isMatrix, COUNT(d) as deviceCount FROM Device d WHERE d.isMatrix IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY d.isMatrix ORDER BY COUNT(d) DESC")
    fun countDevicesByIsMatrix(repo: String? = null): List<Map<String, Any>>
    
    @Query("SELECT d.flashSize as flashSize, COUNT(d) as deviceCount FROM Device d WHERE d.flashSize IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY d.flashSize ORDER BY COUNT(d) DESC")
    fun countDevicesByFlashSize(repo: String? = null): List<Map<String, Any>>
    
    @Query("""
        SELECT CASE 
            WHEN d.psramPresent = false THEN 'None'
            WHEN d.psramPresent = true AND d.psramSize IS NOT NULL THEN d.psramSize
            ELSE 'Unknown'
        END as psramSize, 
        COUNT(d) as deviceCount 
        FROM Device d 
        WHERE d.psramPresent IS NOT NULL AND (:repo IS NULL OR d.repo = :repo)
        GROUP BY CASE 
            WHEN d.psramPresent = false THEN 'None'
            WHEN d.psramPresent = true AND d.psramSize IS NOT NULL THEN d.psramSize
            ELSE 'Unknown'
        END 
        ORDER BY COUNT(d) DESC
    """)
    fun countDevicesByPsramSize(repo: String? = null): List<Map<String, Any>>
    
    @Query("SELECT d.releaseName as releaseName, COUNT(d) as deviceCount FROM Device d WHERE d.releaseName IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY d.releaseName ORDER BY COUNT(d) DESC")
    fun countDevicesByReleaseName(repo: String? = null): List<Map<String, Any>>

    @Query("SELECT d.ledCount as ledCount, COUNT(d) as deviceCount FROM Device d WHERE d.ledCount IS NOT NULL AND (:repo IS NULL OR d.repo = :repo) GROUP BY d.ledCount ORDER BY d.ledCount ASC")
    fun countDevicesByLedCount(repo: String? = null): List<Map<String, Any>>

    @Query("SELECT DISTINCT d.repo FROM Device d WHERE d.repo IS NOT NULL")
    fun findDistinctRepos(): List<String>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, version, COUNT(*) as deviceCount FROM device WHERE created >= :since AND (:repo IS NULL OR repo = :repo) GROUP BY weekStart, version ORDER BY weekStart, version",
        nativeQuery = true
    )
    fun countNewDevicesByWeekAndVersion(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, chip, COUNT(*) as deviceCount FROM device WHERE created >= :since AND chip IS NOT NULL AND (:repo IS NULL OR repo = :repo) GROUP BY weekStart, chip ORDER BY weekStart, chip",
        nativeQuery = true
    )
    fun countNewDevicesByWeekAndChip(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as deviceCount FROM device WHERE created >= :since AND led_count IS NULL AND (:repo IS NULL OR repo = :repo) GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countGenuineNewDevicesByWeek(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as deviceCount FROM device WHERE created >= :since AND led_count IS NOT NULL AND (:repo IS NULL OR repo = :repo) GROUP BY weekStart ORDER BY weekStart",
        nativeQuery = true
    )
    fun countLegacyNewDevicesByWeek(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    @Query(
        value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, chip, COUNT(*) as deviceCount FROM device WHERE created >= :since AND led_count IS NULL AND chip IS NOT NULL AND (:repo IS NULL OR repo = :repo) GROUP BY weekStart, chip ORDER BY weekStart, chip",
        nativeQuery = true
    )
    fun countGenuineNewDevicesByWeekAndChip(since: LocalDateTime, repo: String? = null): List<Map<String, Any>>

    fun findAllByRepo(repo: String): List<Device>

}