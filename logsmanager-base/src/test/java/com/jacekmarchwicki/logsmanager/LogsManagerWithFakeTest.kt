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

class LogsManagerWithFakeTest {

    private val logsManager = LogsManagerFake(Log.DEBUG)

    @Test
    fun `when setSeverityInstant, severity is changed`() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")
        logsManager.setSeverityInstant(id, Log.WARN)

        assertEquals(listOf(LogsManager.EntryLevelData(Log.WARN, "title", "details")), logsManager.logs)
    }

    @Test
    fun `when appendLogInstant, severity is changed`() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")
        logsManager.appendLogInstant(id, "\nNew data")

        assertEquals(listOf(LogsManager.EntryLevelData(Log.DEBUG, "title", "details\nNew data")), logsManager.logs)
    }
}