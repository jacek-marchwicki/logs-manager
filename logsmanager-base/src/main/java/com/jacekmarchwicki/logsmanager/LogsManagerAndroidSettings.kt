package com.jacekmarchwicki.logsmanager

import android.content.Context
import android.util.Log
import com.jacekmarchwicki.logsmanager.internal.RemovableInRelease

@RemovableInRelease
data class LogsManagerAndroidSettings(val context: Context, val logLevelEnabled: Int = Log.VERBOSE, val notificationTitle: String = "Logs manager")