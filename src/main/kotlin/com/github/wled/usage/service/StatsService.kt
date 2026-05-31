package com.github.wled.usage.service

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.ChipWeeklyStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FeatureStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.FsTotalStats
import com.github.wled.usage.dto.FsUsageRangeStats
import com.github.wled.usage.dto.LedCountRangeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PreviousVersionStats
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
    companion object {
        private const val DEFAULT_LED_COUNT = 30
        private const val UNKNOWN_VERSION = "unknown"
    }

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

    fun getDeviceCountByPreviousVersion(repo: String? = null): List<PreviousVersionStats> {
        val devices = if (repo != null) deviceRepository.findAllByRepo(repo) else deviceRepository.findAll().toList()
        val allEvents = if (repo != null) {
            upgradeEventRepository.findAllWithDeviceByRepo(repo)
        } else {
            upgradeEventRepository.findAllWithDevice()
        }

        val eventsByDeviceId = allEvents.groupBy { it.device.id }
            .mapValues { (_, events) -> events.sortedBy { it.created } }

        val counts = mutableMapOf<String, Long>()

        for (device in devices) {
            val deviceEvents = eventsByDeviceId[device.id] ?: emptyList()

            // Find the most recent actual upgrade (oldVersion differs from newVersion)
            val latestActualUpgrade = deviceEvents
                .filter { it.oldVersion != it.newVersion && it.created != null }
                .maxByOrNull { it.created!! }

            if (latestActualUpgrade != null) {
                counts.merge(latestActualUpgrade.oldVersion, 1, Long::plus)
            } else {
                // No actual upgrade found — classify by ledCount
                val ledCount = device.ledCount
                if (ledCount != null && ledCount != DEFAULT_LED_COUNT) {
                    // Not a fresh install: previous version is unknown
                    counts.merge(UNKNOWN_VERSION, 1, Long::plus)
                }
                // ledCount is null or DEFAULT_LED_COUNT → fresh install, skip
            }
        }

        return counts.entries
            .sortedByDescending { it.value }
            .map { (version, count) -> PreviousVersionStats(previousVersion = version, deviceCount = count) }
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
    
    fun getDeviceCountByLedFeatures(repo: String? = null): List<FeatureStats> {
        return deviceRepository.countDevicesByLedFeatures(repo).map {
            FeatureStats(
                feature = it["feature"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }

    fun getDeviceCountByPeripherals(repo: String? = null): List<FeatureStats> {
        return deviceRepository.countDevicesByPeripherals(repo).map {
            FeatureStats(
                feature = it["feature"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }

    fun getDeviceCountByIntegrations(repo: String? = null): List<FeatureStats> {
        return deviceRepository.countDevicesByIntegrations(repo).map {
            FeatureStats(
                feature = it["feature"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }

    fun getDeviceCountByUsermods(repo: String? = null): List<FeatureStats> {
        return deviceRepository.countDevicesByUsermods(repo).map {
            FeatureStats(
                feature = it["feature"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }

    fun getDeviceCountByBusTypes(repo: String? = null): List<FeatureStats> {
        val rawData = deviceRepository.countDevicesByBusTypes(repo)

        // Normalize each bus-type list: split by comma, count occurrences of each type,
        // sort alphabetically, then format as "Nx type" (or just "type" when count is 1).
        // Aggregate entries that normalize to the same summary (order-independent).
        val aggregated = mutableMapOf<String, Long>()
        for (row in rawData) {
            val rawFeature = row["feature"] as String
            val deviceCount = row["deviceCount"] as Long

            val typeCounts = rawFeature.split(",")
                .map { it.trim() }
                .groupingBy { it }
                .eachCount()

            val summary = typeCounts.entries
                .sortedBy { it.key }
                .joinToString(", ") { (type, count) ->
                    if (count > 1) "${count}x $type" else type
                }

            aggregated.merge(summary, deviceCount, Long::plus)
        }

        return aggregated.entries
            .sortedByDescending { it.value }
            .map { (feature, count) -> FeatureStats(feature = feature, deviceCount = count) }
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
    
    fun getDeviceCountByFsTotal(repo: String? = null): List<FsTotalStats> {
        return deviceRepository.countDevicesByFsTotal(repo).map {
            FsTotalStats(
                fsTotal = it["fsTotal"].toString(),
                deviceCount = (it["deviceCount"] as Number).toLong()
            )
        }
    }

    fun getDeviceCountByFsUsage(repo: String? = null): List<FsUsageRangeStats> {
        val rawData = deviceRepository.countDevicesByFsUsed(repo)
        if (rawData.isEmpty()) {
            return emptyList()
        }

        // Convert raw data to (fsUsed bytes, deviceCount) pairs
        val fsUsedCounts = rawData.map {
            Pair((it["fsUsed"] as Number).toLong(), (it["deviceCount"] as Number).toLong())
        }

        // Fixed buckets by absolute byte usage - fine-grained at low end (current data),
        // with larger buckets up to ~1 MB (the realistic max filesystem size on WLED devices)
        val buckets = listOf(
            0L..0L               to "0 B",
            1L..32L              to "1 – 32 B",
            33L..64L             to "33 – 64 B",
            65L..128L            to "65 – 128 B",
            129L..512L           to "129 – 512 B",
            513L..1023L          to "513 B – 1 KB",
            1024L..4095L         to "1 – 4 KB",
            4096L..65535L        to "4 – 64 KB",
            65536L..524287L      to "64 – 512 KB",
            524288L..1048575L    to "512 KB – 1 MB",
            1048576L..Long.MAX_VALUE to "> 1 MB"
        )

        return buckets.map { (range, label) ->
            val count = fsUsedCounts.filter { it.first in range }.sumOf { it.second }
            FsUsageRangeStats(range = label, deviceCount = count)
        }.filter { it.deviceCount > 0 }
    }

    fun getUpgradeVsInstallationStats(repo: String? = null): List<UpgradeVsInstallationWeeklyStats> {
        val since = LocalDateTime.now().minusMonths(6)

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
        val since = LocalDateTime.now().minusMonths(6)

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
        val since = LocalDateTime.now().minusMonths(6)

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
        val since = LocalDateTime.now().minusMonths(6)

        val countsByWeekAndChip = mutableMapOf<Pair<String, String>, Long>()

        // Only count genuine new installations (led_count IS NULL, indicating a fresh install with default settings)
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
        val since = LocalDateTime.now().minusMonths(6)

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

        // Limit to top 15 versions by total event count across all weeks
        val top15Versions = countsByWeekAndVersion.entries
            .groupBy { it.key.second }
            .mapValues { (_, entries) -> entries.sumOf { it.value } }
            .entries
            .sortedByDescending { it.value }
            .take(15)
            .map { it.key }
            .toSet()

        return countsByWeekAndVersion.entries
            .filter { it.key.second in top15Versions }
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

        return (0..25)
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
