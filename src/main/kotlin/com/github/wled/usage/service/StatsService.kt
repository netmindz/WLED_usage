package com.github.wled.usage.service

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
import com.github.wled.usage.dto.LedCountRangeStats
import com.github.wled.usage.dto.MatrixStats
import com.github.wled.usage.dto.PsramSizeStats
import com.github.wled.usage.dto.ReleaseNameStats
import com.github.wled.usage.dto.VersionStats
import com.github.wled.usage.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class StatsService(val deviceRepository: DeviceRepository) {
    fun getDeviceCountByCountry(): List<CountryStats> {
        return deviceRepository.countDevicesByCountryCode().map {
            CountryStats(
                countryCode = it["countryCode"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByVersion(): List<VersionStats> {
        return deviceRepository.countDevicesByVersion().map {
            VersionStats(
                version = it["version"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByChip(): List<ChipStats> {
        return deviceRepository.countDevicesByChip().map {
            ChipStats(
                chip = it["chip"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByIsMatrix(): List<MatrixStats> {
        return deviceRepository.countDevicesByIsMatrix().map {
            MatrixStats(
                isMatrix = it["isMatrix"] as Boolean,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByFlashSize(): List<FlashSizeStats> {
        return deviceRepository.countDevicesByFlashSize().map {
            FlashSizeStats(
                flashSize = it["flashSize"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByPsramSize(): List<PsramSizeStats> {
        return deviceRepository.countDevicesByPsramSize().map {
            PsramSizeStats(
                psramSize = it["psramSize"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByReleaseName(): List<ReleaseNameStats> {
        return deviceRepository.countDevicesByReleaseName().map {
            ReleaseNameStats(
                releaseName = it["releaseName"] as String,
                deviceCount = it["deviceCount"] as Long
            )
        }
    }
    
    fun getDeviceCountByLedCountRange(): List<LedCountRangeStats> {
        val rawData = deviceRepository.countDevicesByLedCount()
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
}
