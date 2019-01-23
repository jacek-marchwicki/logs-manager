package com.jacekmarchwicki.logsmanager

interface LogsSingletonBase : LogsManager {
    fun setup(logsManager: LogsManager)
}