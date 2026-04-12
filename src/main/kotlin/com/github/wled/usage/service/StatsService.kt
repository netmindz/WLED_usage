package com.github.wled.usage.service

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.ChipWeeklyStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.LedCountRangeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PsramSizeStats
import com.github.wled.usage.dto.ReleaseNameStats
import com.github.wled.usage.dto.UpgradeVsInstallationWeeklyStats
import com.github.wled.usage.dto.VersionStats
import com.github.wled.usage.dto.VersionWeeklyStats
import com.github.wled.usage.entity.Device
import com.github.wled.usage.entity.UpgradeEvent
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.UpgradeEventRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StatsService(
    val deviceRepository: DeviceRepository,
    val upgradeEventRepository: UpgradeEventRepository
) {
    fun getKnownRepos(): List<String> = deviceRepository.findDistinctRepos()

    fun getDeviceCountByCountry(repo: String? = null): List<CountryStats> {
        return deviceRepository.countDevicesByCountryCode(repo).map {
            CountryStats(
                countryCode = it["countryCode"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByVersion(repo: String? = null): List<VersionStats> {
        return deviceRepository.countDevicesByVersion(repo).map {
            VersionStats(
                version = it["version"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByChip(repo: String? = null): List<ChipStats> {
        return deviceRepository.countDevicesByChip(repo).map {
            ChipStats(
                chip = it["chip"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByIsMatrix(repo: String? = null): List<MatrixStats> {
        return deviceRepository.countDevicesByIsMatrix(repo).map {
            MatrixStats(
                isMatrix = it["isMatrix"] as Boolean,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByFlashSize(repo: String? = null): List<FlashSizeStats> {
        return deviceRepository.countDevicesByFlashSize(repo).map {
            FlashSizeStats(
                flashSize = it["flashSize"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByPsramSize(repo: String? = null): List<PsramSizeStats> {
        return deviceRepository.countDevicesByPsramSize(repo).map {
            PsramSizeStats(
                psramSize = it["psramSize"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByReleaseName(repo: String? = null): List<ReleaseNameStats> {
        return deviceRepository.countDevicesByReleaseName(repo).map {
            ReleaseNameStats(
                releaseName = it["releaseName"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByLedCountRange(repo: String? = null): List<LedCountRangeStats> {
        val rawData = deviceRepository.countDevicesByLedCount(repo)
        if (rawData.isEmpty()) {
            return emptyList()
        }
        
        // Convert raw data to list of pairs (ledCount, deviceCount)
        val ledCounts = rawData.map { 
            Pair(
                (it["ledCount"] as Number).toInt(), 
                (it["deviceCount"] as Number).toLong()
            )
        }
        
        // Calculate dynamic ranges based on data distribution
        val ranges = calculateDynamicRanges(ledCounts)
        
        // Aggregate device counts into ranges
        return aggregateIntoRanges(ledCounts, ranges)
    }
    
    fun getUpgradeVsInstallationStats(repo: String? = null): List<UpgradeVsInstallationWeeklyStats> {
        val since = LocalDateTime.now().minusMonths(3)

        val upgradeEventsByWeek = upgradeEventRepository.countUpgradeEventsByWeek(since, repo)
            .associate { it["weekStart"].toString() to (it["eventCount"] as Number).toLong() }

        // Legacy installs: new devices with led_count != null (not default 30), treated as upgrades for graphs
        val legacyInstallsByWeek = deviceRepository.countLegacyNewDevicesByWeek(since, repo)
            .associate { it["weekStart"].toString() to (it["deviceCount"] as Number).toLong() }

        // Genuine new installations: new devices with led_count = null (default 30, fresh installs)
        val genuineNewDevicesByWeek = deviceRepository.countGenuineNewDevicesByWeek(since, repo)
            .associate { it["weekStart"].toString() to (it["deviceCount"] as Number).toLong() }

        val allWeeks = (upgradeEventsByWeek.keys + legacyInstallsByWeek.keys + genuineNewDevicesByWeek.keys).sorted()

        return allWeeks.map { week ->
            UpgradeVsInstallationWeeklyStats(
                week = week,
                upgrades = (upgradeEventsByWeek[week] ?: 0) + (legacyInstallsByWeek[week] ?: 0),
                newInstallations = genuineNewDevicesByWeek[week] ?: 0
            )
        }
    }

    fun getRunningVersionsStats(repo: String? = null): List<VersionWeeklyStats> {
        val since = LocalDateTime.now().minusMonths(3)

        val weekStarts = generateWeekStarts(since)
        val devices = if (repo != null) {
            deviceRepository.findAllByRepo(repo)
        } else {
            deviceRepository.findAll().toList()
        }
        val allEvents = if (repo != null) {
            upgradeEventRepository.findAllWithDeviceByRepo(repo)
        } else {
            upgradeEventRepository.findAllWithDevice()
        }
        val eventsByDeviceId = allEvents.groupBy { it.device.id }
            .mapValues { (_, events) -> events.sortedBy { it.created } }

        val results = mutableListOf<VersionWeeklyStats>()

        for (weekStart in weekStarts) {
            val weekEnd = weekStart.plusDays(7)
            val versionCounts = mutableMapOf<String, Long>()

            for (device in devices) {
                if (device.created != null && !device.created.isBefore(weekEnd)) continue

                val deviceEvents = eventsByDeviceId[device.id] ?: emptyList()
                val version = determineVersionAtTime(device, deviceEvents, weekEnd)
                versionCounts.merge(version, 1, Long::plus)
            }

            versionCounts.entries
                .sortedBy { it.key }
                .forEach { (version, count) ->
                    results.add(VersionWeeklyStats(
                        week = weekStart.toLocalDate().toString(),
                        version = version,
                        count = count
                    ))
                }
        }

        return results
    }

    fun getChipOverTimeStats(repo: String? = null): List<ChipWeeklyStats> {
        val since = LocalDateTime.now().minusMonths(3)

        val countsByWeekAndChip = mutableMapOf<Pair<String, String>, Long>()

        upgradeEventRepository.countUpgradeEventsByWeekAndChip(since, repo).forEach {
            val key = Pair(it["weekStart"].toString(), it["chip"] as String)
            countsByWeekAndChip.merge(key, (it["eventCount"] as Number).toLong(), Long::plus)
        }

        deviceRepository.countNewDevicesByWeekAndChip(since, repo).forEach {
            val key = Pair(it["weekStart"].toString(), it["chip"] as String)
            countsByWeekAndChip.merge(key, (it["deviceCount"] as Number).toLong(), Long::plus)
        }

        return countsByWeekAndChip.entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second }))
            .map { (key, count) ->
                ChipWeeklyStats(
                    week = key.first,
                    chip = key.second,
                    count = count
                )
            }
    }

    fun getInstallChipOverTimeStats(repo: String? = null): List<ChipWeeklyStats> {
        val since = LocalDateTime.now().minusMonths(3)

        val countsByWeekAndChip = mutableMapOf<Pair<String, String>, Long>()

        // Only count genuine new installations (led_count IS NULL = fresh installs with default 30 LEDs)
        deviceRepository.countGenuineNewDevicesByWeekAndChip(since, repo).forEach {
            val key = Pair(it["weekStart"].toString(), it["chip"] as String)
            countsByWeekAndChip.merge(key, (it["deviceCount"] as Number).toLong(), Long::plus)
        }

        return countsByWeekAndChip.entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second }))
            .map { (key, count) ->
                ChipWeeklyStats(
                    week = key.first,
                    chip = key.second,
                    count = count
                )
            }
    }

    fun getVersionOverTimeStats(repo: String? = null): List<VersionWeeklyStats> {
        val since = LocalDateTime.now().minusMonths(3)

        // Combine upgrade events and new installations by week and version
        val countsByWeekAndVersion = mutableMapOf<Pair<String, String>, Long>()

        upgradeEventRepository.countUpgradeEventsByWeekAndVersion(since, repo).forEach {
            val key = Pair(it["weekStart"].toString(), it["version"] as String)
            countsByWeekAndVersion.merge(key, (it["eventCount"] as Number).toLong(), Long::plus)
        }

        deviceRepository.countNewDevicesByWeekAndVersion(since, repo).forEach {
            val key = Pair(it["weekStart"].toString(), it["version"] as String)
            countsByWeekAndVersion.merge(key, (it["deviceCount"] as Number).toLong(), Long::plus)
        }

        return countsByWeekAndVersion.entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second }))
            .map { (key, count) ->
                VersionWeeklyStats(
                    week = key.first,
                    version = key.second,
                    count = count
                )
            }
    }

    private fun calculateDynamicRanges(ledCounts: List<Pair<Int, Long>>): List<IntRange> {
        if (ledCounts.isEmpty()) {
            return emptyList()
        }
        
        val maxLedCount = ledCounts.maxOf { it.first }
        
        // Define range boundaries using powers of 10 and intermediate values
        // This creates ranges like: 1-10, 11-50, 51-100, 101-250, 251-500, 501-1000, etc.
        val boundaries = mutableListOf<Int>()
        boundaries.add(0)
        boundaries.add(10)
        boundaries.add(50)
        boundaries.add(100)
        boundaries.add(250)
        boundaries.add(500)
        boundaries.add(1000)
        
        // Add higher ranges dynamically based on max value
        var current = 1000
        while (current < maxLedCount) {
            when {
                current < 5000 -> current += 1000
                current < 10000 -> current += 2500
                else -> current += 5000
            }
            boundaries.add(current)
        }
        
        // Create ranges, only including those that have data
        // Note: First boundary is 0, so first range starts at 1 (LED count 0 is not meaningful)
        val ranges = mutableListOf<IntRange>()
        for (i in 0 until boundaries.size - 1) {
            val rangeStart = boundaries[i] + 1
            val rangeEnd = boundaries[i + 1]
            
            // Check if any data falls in this range
            val hasData = ledCounts.any { it.first in rangeStart..rangeEnd }
            if (hasData) {
                ranges.add(rangeStart..rangeEnd)
            }
        }
        
        // Handle any values above the last boundary
        val lastBoundary = boundaries.last()
        val hasDataAboveLast = ledCounts.any { it.first > lastBoundary }
        if (hasDataAboveLast) {
            ranges.add((lastBoundary + 1)..Int.MAX_VALUE)
        }
        
        return ranges
    }
    
    private fun aggregateIntoRanges(ledCounts: List<Pair<Int, Long>>, ranges: List<IntRange>): List<LedCountRangeStats> {
        return ranges.map { range ->
            val count = ledCounts
                .filter { it.first in range }
                .sumOf { it.second }
            
            val rangeLabel = if (range.last == Int.MAX_VALUE) {
                "${range.first}+"
            } else {
                "${range.first}-${range.last}"
            }
            
            LedCountRangeStats(range = rangeLabel, deviceCount = count)
        }.filter { it.deviceCount > 0 }
    }

    internal fun generateWeekStarts(since: LocalDateTime): List<LocalDateTime> {
        val sinceDate = since.toLocalDate()
        val mondayOfSinceWeek = sinceDate.with(DayOfWeek.MONDAY)
        val today = LocalDate.now()

        return (0..12)
            .map { n -> mondayOfSinceWeek.plusWeeks(n.toLong()) }
            .filter { !it.isAfter(today) }
            .map { it.atStartOfDay() }
    }

    internal fun determineVersionAtTime(
        device: Device,
        sortedEvents: List<UpgradeEvent>,
        time: LocalDateTime
    ): String {
        // Find most recent event before 'time'
        val latestBefore = sortedEvents
            .lastOrNull { it.created?.isBefore(time) == true }

        if (latestBefore != null) {
            return latestBefore.newVersion
        }

        // No events before 'time'. Use old_version of the earliest event (which must be after 'time').
        val earliestEvent = sortedEvents.firstOrNull()
        if (earliestEvent != null) {
            return earliestEvent.oldVersion
        }

        // No events at all, use current version
        return device.version
    }
}
