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

class LogsManagerString(private val logLevelEnabled: Int) : LogsManager {
    data class Entry(val time: Long, val data: LogsManager.EntryLevelData)
    private val list: MutableList<Entry> = mutableListOf()
    override fun checkLevel(level: Int): Boolean = level >= logLevelEnabled

    override fun log(level: Int, title: String, details: String) = logInstant(level, title, details).let { Unit }
    override fun logInstant(level: Int, title: String, details: String): Long = synchronized(list) {
        if (checkLevel(level)) {
            val position = list.size
            list.add(Entry(System.currentTimeMillis(), LogsManager.EntryLevelData(level, title, details)))
            position.toLong()
        } else {
            -1L
        }
    }

    override fun updateLogInstant(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) =
        if (id >= 0L) {
            synchronized(list) {
                list[id.toInt()] = list[id.toInt()].let { it.copy(data = update(it.data)) }
            }
        } else { }

    fun getEntries(): List<Entry> = synchronized(list) { list.toList() }
    fun clear() = synchronized(list) { list.clear() }
}