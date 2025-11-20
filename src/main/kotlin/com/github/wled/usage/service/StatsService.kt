package com.github.wled.usage.service

import com.github.wled.usage.dto.CountryStats
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
}
