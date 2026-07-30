package com.github.wled.usage.repository

import com.github.wled.usage.entity.MapFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MapFileRepository : JpaRepository<MapFile, Long> {
    fun findByVersion(version: String): Optional<MapFile>
}
