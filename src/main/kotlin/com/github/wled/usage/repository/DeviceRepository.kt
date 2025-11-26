package com.github.wled.usage.repository

import com.github.wled.usage.entity.Device
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DeviceRepository : CrudRepository<Device, String> {
    @Query("SELECT d.countryCode as countryCode, COUNT(d) as deviceCount FROM Device d WHERE d.countryCode IS NOT NULL GROUP BY d.countryCode")
    fun countDevicesByCountryCode(): List<Map<String, Any>>
    
    @Query("SELECT d.version as version, COUNT(d) as deviceCount FROM Device d GROUP BY d.version ORDER BY COUNT(d) DESC")
    fun countDevicesByVersion(): List<Map<String, Any>>
    
    @Query("SELECT d.chip as chip, COUNT(d) as deviceCount FROM Device d GROUP BY d.chip ORDER BY COUNT(d) DESC")
    fun countDevicesByChip(): List<Map<String, Any>>
}