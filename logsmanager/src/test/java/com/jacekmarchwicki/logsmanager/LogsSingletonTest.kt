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
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogsSingletonTest {

    private val logsManager: LogsManager = mock { }

    @Before
    fun setUp() {
        LogsSingleton.setup(logsManager)
    }

    @Test
    fun `checkLevel, returns correct value from logsManager`() {
        logsManager.stub {
            on { checkLevel(any()) } doAnswer {
                it.getArgument<Int>(0) >= Log.WARN
            }
        }

        assertTrue(LogsSingleton.checkLevel(Log.ASSERT))
        assertTrue(LogsSingleton.checkLevel(Log.ERROR))
        assertTrue(LogsSingleton.checkLevel(Log.WARN))
        assertFalse(LogsSingleton.checkLevel(Log.INFO))
        assertFalse(LogsSingleton.checkLevel(Log.DEBUG))
        assertFalse(LogsSingleton.checkLevel(Log.VERBOSE))
    }

    @Test
    fun `log, is forwarded to logManager`() {
        LogsSingleton.log(Log.WARN, "title", "details")

        verify(logsManager).log(Log.WARN, "title", "details")
    }

    @Test
    fun `logInstant, is forwarded to logManager`() {
        logsManager.stub {
            on { logInstant(any(), any(), any()) } doReturn 3L
        }
        val ret = LogsSingleton.logInstant(Log.WARN, "title", "details")

        verify(logsManager).logInstant(Log.WARN, "title", "details")
        assertEquals(3L, ret)
    }

    @Test
    fun `updateLogInstant, is forwarded to logManager`() {
        val func: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData = { it }
        LogsSingleton.updateLogInstant(3L, func)

        verify(logsManager).updateLogInstant(3L, func)
    }
}