/*
 * Copyright (C) 2019 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jacekmarchwicki.logsmanager

import android.annotation.SuppressLint
import android.content.Context

object LogsSingleton : LogsSingletonBase {
    override fun checkLevel(level: Int): Boolean = instance.checkLevel(level)

    override fun log(level: Int, title: String, details: String) = instance.log(level, title, details)
    override fun logInstant(level: Int, title: String, details: String): Long = instance.logInstant(level, title, details)
    override fun updateLogInstant(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) = instance.updateLogInstant(id, update)

    @SuppressLint("StaticFieldLeak")
    internal var instance: LogsManager = EmptyLogsManager
    override fun setup(logsManager: LogsManager) {
        instance = logsManager
    }

}