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

class LogsManagerMultiple(private val logsManagers: List<LogsManager>) : LogsManager {
    override fun checkLevel(level: Int): Boolean = logsManagers.filter { it.checkLevel(level) }.any()

    override fun log(level: Int, title: String, details: String) = logsManagers.forEach {
        it.log(level, title, details)
    }

    private val instantLogs: MutableList<LongArray> = mutableListOf()

    override fun logInstant(level: Int, title: String, details: String): Long {
        val logs = logsManagers.map { it.logInstant(level, title, details) }.toLongArray()
        return if (logs.contains { it >= 0L }) {
            synchronized(instantLogs) {
                val position = instantLogs.size
                instantLogs.add(logs)
                position.toLong()
            }
        } else {
            -1L
        }
    }

    override fun updateLogInstant(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) {
        if (id >= 0L) {
            val positions = synchronized(instantLogs) { instantLogs[id.toInt()] }
            logsManagers.zip(positions.toList()).forEach { (manager, position) ->
                manager.updateLogInstant(position, update)
            }
        }
    }
}

private inline fun LongArray.contains(predicate: (Long) -> Boolean): Boolean {
    forEach {
        if (predicate(it)) {
            return true
        }
    }
    return false
}