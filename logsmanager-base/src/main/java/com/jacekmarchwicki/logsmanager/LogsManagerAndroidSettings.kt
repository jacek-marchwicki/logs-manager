package com.jacekmarchwicki.logsmanager

import android.content.Context

data class LogsManagerAndroidSettings(val context: Context, val logLevelEnabled: Int, val notificationTitle: String = "Logs manager")