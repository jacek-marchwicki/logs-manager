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

import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Test

class LogsManagerFakeTest {

    private val logsManager =  LogsManagerFake(Log.DEBUG)

    @Test
    fun `when log data, it is present in fake`() {
        logsManager.log(Log.DEBUG, "title", "details")

        assertEquals(listOf(LogsManager.EntryLevelData(Log.DEBUG, "title", "details")), logsManager.logs)
    }

    @Test
    fun `when log data with too low level, it is not persited`() {
        logsManager.log(Log.VERBOSE, "title", "details")

        assertEquals(listOf<LogsManager.EntryLevelData>(), logsManager.logs)
    }

    @Test
    fun `when logInstant data, it is present in fake`() {
        logsManager.logInstant(Log.DEBUG, "title", "details")

        assertEquals(listOf(LogsManager.EntryLevelData(Log.DEBUG, "title", "details")), logsManager.logs)
    }

    @Test
    fun `when logInstant data with too low level, it is not persited`() {
        logsManager.logInstant(Log.VERBOSE, "title", "details")

        assertEquals(listOf<LogsManager.EntryLevelData>(), logsManager.logs)
    }

    @Test
    fun `when updateLogInstant data, it is present in fake`() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")
        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        assertEquals(listOf(LogsManager.EntryLevelData(Log.WARN, "updated title", "updated details")), logsManager.logs)
    }

    @Test
    fun `when updateLogInstant data with too low level, it is not persited`() {
        val id = logsManager.logInstant(Log.VERBOSE, "title", "details")
        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        assertEquals(listOf<LogsManager.EntryLevelData>(), logsManager.logs)
    }

}