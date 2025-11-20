package com.github.wled.usage.repository

import com.github.wled.usage.entity.Device
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DeviceRepository : CrudRepository<Device, String> {
    @Query("SELECT d.countryCode as countryCode, COUNT(d) as deviceCount FROM Device d WHERE d.countryCode IS NOT NULL GROUP BY d.countryCode")
    fun countDevicesByCountryCode(): List<Map<String, Any>>
}