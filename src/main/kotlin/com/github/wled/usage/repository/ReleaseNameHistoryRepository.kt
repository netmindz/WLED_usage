package com.github.wled.usage.repository

import com.github.wled.usage.entity.ReleaseNameHistory
import org.springframework.data.repository.CrudRepository

interface ReleaseNameHistoryRepository : CrudRepository<ReleaseNameHistory, Long>
