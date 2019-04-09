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

class LogsManagerStringTest {
    private val logsManager = LogsManagerString(Log.DEBUG)

    @Test
    fun whenLogMessage_itIsLogged() {
        logsManager.log(Log.DEBUG, "title", "details")

        val entries = logsManager.getEntries()
        assertEquals(1, entries.size)
        val entry = entries[0]
        assertEquals(Log.DEBUG, entry.data.level)
        assertEquals("title", entry.data.title)
        assertEquals("details", entry.data.details)
    }

    @Test
    fun whenLogMessageWithToLowLevel_itIsNotLogged() {
        logsManager.log(Log.VERBOSE, "title", "details")

        assertEquals(0, logsManager.getEntries().size)
    }

    @Test
    fun whenLogInstantMessage_itIsLogged() {
        logsManager.logInstant(Log.DEBUG, "title", "details")

        val entries = logsManager.getEntries()
        assertEquals(1, entries.size)
        val entry = entries[0]

        assertEquals(Log.DEBUG, entry.data.level)
        assertEquals("title", entry.data.title)
        assertEquals("details", entry.data.details)
    }

    @Test
    fun whenLogInstantMessageWithToLowLevel_itIsNotLogged() {
        logsManager.logInstant(Log.VERBOSE, "title", "details")

        assertEquals(0, logsManager.getEntries().size)
    }

    @Test
    fun whenUpdateLogInstantMessage_itIsLogged() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        val entries = logsManager.getEntries()
        assertEquals(1, entries.size)
        val entry = entries[0]

        assertEquals(Log.WARN, entry.data.level)
        assertEquals("updated title", entry.data.title)
        assertEquals("updated details", entry.data.details)
    }

    @Test
    fun whenUpdateLogInstantMessageWithToLowLevel_itIsNotLogged() {
        val id = logsManager.logInstant(Log.VERBOSE, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        assertEquals(0, logsManager.getEntries().size)
    }

    @Test
    fun whenClearLog_noItemsInList() {
        logsManager.logInstant(Log.DEBUG, "title", "details")
        assertEquals(1, logsManager.getEntries().size)

        logsManager.clear()

        assertEquals(0, logsManager.getEntries().size)
    }
}