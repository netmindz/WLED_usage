package com.github.wled.usage.repository

import com.github.wled.usage.entity.Device
import org.springframework.data.repository.CrudRepository

interface DeviceRepository : CrudRepository<Device, String> {
}