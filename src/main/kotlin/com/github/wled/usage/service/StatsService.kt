package com.github.wled.usage.service

import com.github.wled.usage.dto.ChipStats
import com.github.wled.usage.dto.CountryStats
import com.github.wled.usage.dto.FlashSizeStats
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
}
