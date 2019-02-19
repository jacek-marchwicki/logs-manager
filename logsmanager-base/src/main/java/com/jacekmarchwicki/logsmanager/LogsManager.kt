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

import com.jacekmarchwicki.logsmanager.internal.RemovableInRelease
import java.io.PrintWriter
import java.io.StringWriter

@RemovableInRelease
interface LogsManager {
    data class EntryData(val title: String, val details: String)
    data class EntryLevelData(val level: Int, val title: String, val details: String)

    fun checkLevel(level: Int): Boolean

    fun log(level: Int, title: String, details: String)
    fun logInstant(level: Int, title: String, details: String): Long
    fun updateLogInstant(id: Long, update: (EntryLevelData) -> EntryLevelData)
}

inline fun <T> LogsManager.logFailure(
    level: Int,
    func: () -> T,
    message: (Throwable) -> LogsManager.EntryData = {
        LogsManager.EntryData(
            title = it.message ?: "Unknown error",
            details = it.printStackTraceString()
        )
    }
): T {
    return if (checkLevel(level)) {
        try {
            func()
        } catch (e: Throwable) {
            logEntry(level) { message(e) }
            throw e
        }
    } else {
        func()
    }
}

@RemovableInRelease
@Suppress("NOTHING_TO_INLINE")
inline fun LogsManager.setSeverityInstant(id: Long, level: Int) = updateLogInstant(id) { it.copy(level = level) }

@RemovableInRelease
@Suppress("NOTHING_TO_INLINE")
inline fun LogsManager.appendLogInstant(id: Long, moreDetails: String) = updateLogInstant(id) { it.copy(details = it.details + moreDetails) }

@RemovableInRelease
@Suppress("NOTHING_TO_INLINE")
inline fun LogsManager.log(level: Int, message: String, throwable: Throwable) {
    if (checkLevel(level)) {
        log(level, message, throwable.printStackTraceString())
    }
}

@RemovableInRelease
@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printStackTraceString(): String = StringWriter().use {
    PrintWriter(it).use { printStackTrace(it) }
    it.toString()
}

@RemovableInRelease
@Suppress("NOTHING_TO_INLINE")
inline fun LogsManager.log(level: Int, title: String) {
    log(level, title, title)
}

@RemovableInRelease
inline fun LogsManager.log(level: Int, title: () -> String, details: () -> String) {
    if (checkLevel(level)) {
        log(level, title(), details())
    }
}

@RemovableInRelease
inline fun LogsManager.logEntry(level: Int, entryData: () -> LogsManager.EntryData) {
    if (checkLevel(level)) {
        val data = entryData()
        log(level, data.title, data.details)
    }
}

@RemovableInRelease
inline fun LogsManager.logPair(level: Int, entryData: () -> Pair<String, String>) {
    if (checkLevel(level)) {
        val data = entryData()
        log(level, data.first, data.second)
    }
}

@RemovableInRelease
inline fun LogsManager.logTitle(level: Int, title: () -> String) {
    if (checkLevel(level)) {
        val text = title()
        log(level, text, text)
    }
}