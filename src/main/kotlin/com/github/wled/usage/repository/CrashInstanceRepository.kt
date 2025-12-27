package com.github.wled.usage.repository

import com.github.wled.usage.entity.CrashInstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CrashInstanceRepository : JpaRepository<CrashInstance, Long>
