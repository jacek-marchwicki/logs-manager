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

class LogsManagerFake(var logLevelEnabled: Int) : LogsManager {
    val logs: MutableList<LogsManager.EntryLevelData> = mutableListOf()

    override fun checkLevel(level: Int): Boolean = level >= logLevelEnabled

    override fun log(level: Int, title: String, details: String) {
        logInstant(level, title, details)
    }

    override fun logInstant(level: Int, title: String, details: String): Long =
        if (checkLevel(level)) {
            val id = logs.size
            val log = LogsManager.EntryLevelData(
                level = level,
                title = title,
                details = details
            )
            logs.add(log)
            id.toLong()
        } else {
            -1L
        }

    override fun updateLogInstant(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) {
        val index = id.toInt()
        logs.getOrNull(index)
            ?.let(update)
            ?.also {
                logs[index] = it
            }
    }
}