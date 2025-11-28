package com.github.wled.usage.repository

import com.github.wled.usage.entity.Device
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DeviceRepository : CrudRepository<Device, String> {
    @Query("SELECT d.countryCode as countryCode, COUNT(d) as deviceCount FROM Device d WHERE d.countryCode IS NOT NULL GROUP BY d.countryCode")
    fun countDevicesByCountryCode(): List<Map<String, Any>>
    
    @Query("SELECT d.version as version, COUNT(d) as deviceCount FROM Device d GROUP BY d.version ORDER BY COUNT(d) DESC")
    fun countDevicesByVersion(): List<Map<String, Any>>
    
    @Query("SELECT d.chip as chip, COUNT(d) as deviceCount FROM Device d WHERE d.chip IS NOT NULL GROUP BY d.chip ORDER BY COUNT(d) DESC")
    fun countDevicesByChip(): List<Map<String, Any>>
    
    @Query("SELECT d.isMatrix as isMatrix, COUNT(d) as deviceCount FROM Device d GROUP BY d.isMatrix ORDER BY COUNT(d) DESC")
    fun countDevicesByIsMatrix(): List<Map<String, Any>>
    
    @Query("SELECT d.flashSize as flashSize, COUNT(d) as deviceCount FROM Device d WHERE d.flashSize IS NOT NULL GROUP BY d.flashSize ORDER BY COUNT(d) DESC")
    fun countDevicesByFlashSize(): List<Map<String, Any>>
    
    @Query("SELECT d.psramSize as psramSize, COUNT(d) as deviceCount FROM Device d WHERE d.psramSize IS NOT NULL GROUP BY d.psramSize ORDER BY COUNT(d) DESC")
    fun countDevicesByPsramSize(): List<Map<String, Any>>
    
    @Query("SELECT d.releaseName as releaseName, COUNT(d) as deviceCount FROM Device d GROUP BY d.releaseName ORDER BY COUNT(d) DESC")
    fun countDevicesByReleaseName(): List<Map<String, Any>>
}