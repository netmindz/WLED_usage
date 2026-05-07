package com.github.wled.usage.repository

import com.github.wled.usage.entity.CrashReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CrashReportRepository : JpaRepository<CrashReport, Long> {
    fun findByStackTraceHash(stackTraceHash: String): Optional<CrashReport>
}
