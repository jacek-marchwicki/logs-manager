package com.jacekmarchwicki.logsmanager

import com.jacekmarchwicki.logsmanager.internal.RemovableInRelease

@RemovableInRelease
object EmptyLogsManager : LogsManager {
    override fun checkLevel(level: Int): Boolean = false
    override fun log(level: Int, title: String, details: String) = Unit
    override fun logInstant(level: Int, title: String, details: String): Long = -1L
    override fun updateLogInstant(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) = Unit
}