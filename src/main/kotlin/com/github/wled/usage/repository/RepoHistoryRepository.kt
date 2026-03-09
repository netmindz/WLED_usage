package com.github.wled.usage.repository

import com.github.wled.usage.entity.RepoHistory
import org.springframework.data.repository.CrudRepository

interface RepoHistoryRepository : CrudRepository<RepoHistory, Long>
