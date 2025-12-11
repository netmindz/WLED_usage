package com.github.wled.usage.repository

import com.github.wled.usage.entity.UpgradeEvent
import org.springframework.data.repository.CrudRepository

interface UpgradeEventRepository : CrudRepository<UpgradeEvent, Long>
