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
import org.junit.Assert
import org.junit.Test

class LogsManagerMultipleTest {
    private val debugLogsManager = LogsManagerString(Log.DEBUG)
    private val warnLogsManager = LogsManagerString(Log.WARN)
    private val logsManager = LogsManagerMultiple(listOf(debugLogsManager, warnLogsManager))

    @Test
    fun whenLogMessage_itIsLogged() {
        logsManager.log(Log.DEBUG, "title", "details")

        val debugEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, debugEntries.size)
        val debugEntry = debugEntries[0]
        Assert.assertEquals(Log.DEBUG, debugEntry.data.level)
        Assert.assertEquals("title", debugEntry.data.title)
        Assert.assertEquals("details", debugEntry.data.details)

        val warnEntries = warnLogsManager.getEntries()
        Assert.assertEquals(0, warnEntries.size)
    }

    @Test
    fun whenLogMessage_itIsLogged2() {
        logsManager.log(Log.WARN, "title", "details")

        val debugEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, debugEntries.size)
        val debugEntry = debugEntries[0]
        Assert.assertEquals(Log.WARN, debugEntry.data.level)
        Assert.assertEquals("title", debugEntry.data.title)
        Assert.assertEquals("details", debugEntry.data.details)

        val warnEntries = warnLogsManager.getEntries()
        Assert.assertEquals(1, warnEntries.size)
        val warnEntry = warnEntries[0]
        Assert.assertEquals(Log.WARN, warnEntry.data.level)
        Assert.assertEquals("title", warnEntry.data.title)
        Assert.assertEquals("details", warnEntry.data.details)
    }

    @Test
    fun whenLogMessageWithToLowLevel_itIsNotLogged() {
        logsManager.log(Log.VERBOSE, "title", "details")

        Assert.assertEquals(0, debugLogsManager.getEntries().size)
        Assert.assertEquals(0, warnLogsManager.getEntries().size)
    }

    @Test
    fun whenLogInstantMessage_itIsLogged1() {
        logsManager.logInstant(Log.DEBUG, "title", "details")

        val debugEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, debugEntries.size)
        val debugEntry = debugEntries[0]

        Assert.assertEquals(Log.DEBUG, debugEntry.data.level)
        Assert.assertEquals("title", debugEntry.data.title)
        Assert.assertEquals("details", debugEntry.data.details)

        val warnEntries = warnLogsManager.getEntries()
        Assert.assertEquals(0, warnEntries.size)
    }

    @Test
    fun whenLogInstantMessage_itIsLogged2() {
        logsManager.logInstant(Log.WARN, "title", "details")

        val debugEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, debugEntries.size)
        val debugEntry = debugEntries[0]

        Assert.assertEquals(Log.WARN, debugEntry.data.level)
        Assert.assertEquals("title", debugEntry.data.title)
        Assert.assertEquals("details", debugEntry.data.details)

        val warnEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, warnEntries.size)
        val warnEntry = warnEntries[0]

        Assert.assertEquals(Log.WARN, warnEntry.data.level)
        Assert.assertEquals("title", warnEntry.data.title)
        Assert.assertEquals("details", warnEntry.data.details)
    }

    @Test
    fun whenLogInstantMessageWithToLowLevel_itIsNotLogged() {
        val logInstant = logsManager.logInstant(Log.VERBOSE, "title", "details")

        Assert.assertEquals(0, debugLogsManager.getEntries().size)
        Assert.assertEquals(0, warnLogsManager.getEntries().size)
        Assert.assertEquals(-1L, logInstant)
    }

    @Test
    fun whenUpdateLogInstantMessage_itIsLogged1() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        val debugEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, debugEntries.size)
        val debugEntry = debugEntries[0]

        Assert.assertEquals(Log.WARN, debugEntry.data.level)
        Assert.assertEquals("updated title", debugEntry.data.title)
        Assert.assertEquals("updated details", debugEntry.data.details)

        val warnEntries = warnLogsManager.getEntries()
        Assert.assertEquals(0, warnEntries.size)
    }

    @Test
    fun whenUpdateLogInstantMessage_itIsLogged2() {
        val id = logsManager.logInstant(Log.WARN, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.ERROR, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        val debugEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, debugEntries.size)
        val debugEntry = debugEntries[0]

        Assert.assertEquals(Log.ERROR, debugEntry.data.level)
        Assert.assertEquals("updated title", debugEntry.data.title)
        Assert.assertEquals("updated details", debugEntry.data.details)

        val warnEntries = debugLogsManager.getEntries()
        Assert.assertEquals(1, warnEntries.size)
        val warnEntry = warnEntries[0]

        Assert.assertEquals(Log.ERROR, warnEntry.data.level)
        Assert.assertEquals("updated title", warnEntry.data.title)
        Assert.assertEquals("updated details", warnEntry.data.details)
    }

    @Test
    fun whenUpdateLogInstantMessageWithToLowLevel_itIsNotLogged() {
        val id = logsManager.logInstant(Log.VERBOSE, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }

        Assert.assertEquals(0, debugLogsManager.getEntries().size)
        Assert.assertEquals(0, warnLogsManager.getEntries().size)
    }
}