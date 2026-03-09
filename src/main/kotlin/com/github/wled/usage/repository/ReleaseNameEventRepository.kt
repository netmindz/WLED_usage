package com.github.wled.usage.repository

import com.github.wled.usage.entity.ReleaseNameEvent
import org.springframework.data.repository.CrudRepository

interface ReleaseNameEventRepository : CrudRepository<ReleaseNameEvent, Long>
