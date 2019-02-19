package com.jacekmarchwicki.logsmanager

import android.content.Context
import com.jacekmarchwicki.logsmanager.internal.RemovableInRelease

@RemovableInRelease
data class LogsManagerAndroidSettings(val context: Context, val logLevelEnabled: Int, val notificationTitle: String = "Logs manager")