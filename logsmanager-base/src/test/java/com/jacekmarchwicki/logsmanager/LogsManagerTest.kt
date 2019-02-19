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
import org.junit.Test
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.io.IOException

class LogsManagerTest {

    private val logsManager: LogsManager = mock {
        on { checkLevel(any()) } doReturn true
    }

    @Test
    fun `when log with only title, title is used as details`() {
        logsManager.log(Log.VERBOSE, "title")

        verify(logsManager).log(Log.VERBOSE, "title", "title")
    }

    @Test
    fun `when log with title, execute log`() {
        logsManager.logTitle(Log.VERBOSE) { "title" }

        verify(logsManager).log(Log.VERBOSE, "title", "title")
    }

    @Test
    fun `when log with title, don't execute if log level isn't enough`() {
        var executed = false
        logsManager.stub { on { checkLevel(any()) } doReturn false }

        logsManager.logTitle(Log.VERBOSE) {
            executed = true
            "title"
        }

        assertFalse(executed)
    }

    @Test
    fun `when log with entry, execute log`() {
        logsManager.logEntry(Log.VERBOSE) { LogsManager.EntryData("title", "details") }

        verify(logsManager).log(Log.VERBOSE, "title", "details")
    }

    @Test
    fun `when log with entry, don't execute if log level isn't enough`() {
        var executed = false
        logsManager.stub { on { checkLevel(any()) } doReturn false }

        logsManager.logEntry(Log.VERBOSE) {
            executed = true
            LogsManager.EntryData("title", "details")
        }

        assertFalse(executed)
    }

    @Test
    fun `when log with pair, execute log`() {
        logsManager.logPair(Log.VERBOSE) { "title" to "details" }

        verify(logsManager).log(Log.VERBOSE, "title", "details")
    }

    @Test
    fun `when log with pair, don't execute if log level isn't enough`() {
        var executed = false
        logsManager.stub { on { checkLevel(any()) } doReturn false }

        logsManager.logPair(Log.VERBOSE) {
            executed = true
            "title" to "details"
        }

        assertFalse(executed)
    }

    @Test
    fun `when log as a function, execute log`() {
        logsManager.log(Log.VERBOSE, { "title" }, { "details" })

        verify(logsManager).log(Log.VERBOSE, "title", "details")
    }

    @Test
    fun `when log as a function, don't execute if log level isn't enough`() {
        var executed = false
        logsManager.stub { on { checkLevel(any()) } doReturn false }

        logsManager.log(Log.VERBOSE,
            {
                executed = true
                "title"
            },
            {
                executed = true
                "details"
            }
        )

        assertFalse(executed)
    }

    @Test
    fun `when exception is raised, log function name`() {
        val e = getThrownException()
        val text = e.printStackTraceString()
        assertTrue(text.contains("IOException"))
        assertTrue(text.contains("functionThatThrowsError"))
    }

    @Test
    fun `when log with exception, execute log`() {
        logsManager.log(Log.WARN, "title", getThrownException())

        verify(logsManager).log(
            eq(Log.WARN),
            eq("title"),
            argThat {
                contains("IOException") && contains("functionThatThrowsError")
            })
    }

    @Test
    fun `when logFailure, execute log`() {
        var functionExecutions = 0
        try {
            logsManager.logFailure(Log.WARN, {
                functionExecutions += 1
                functionThatThrowsError() })
            throw AssertionError("Exception not thrown")
        } catch (e: IOException) {
        }

        verify(logsManager).log(
            eq(Log.WARN),
            eq("Some error"),
            argThat {
                contains("IOException") && contains("functionThatThrowsError")
            })
        assertEquals(1, functionExecutions)
    }

    @Test
    fun `when log with logFailure, don't execute if log level isn't enough`() {
        var functionExecutions = 0
        var executed = false
        logsManager.stub { on { checkLevel(any()) } doReturn false }

        try {
            logsManager.logFailure(Log.WARN, {
                functionExecutions += 1
                functionThatThrowsError()
            }, {
                executed = true
                LogsManager.EntryData("", "")
            })
            throw AssertionError("Exception not thrown")
        } catch (e: IOException) {
        }

        assertFalse(executed)
        assertEquals(1, functionExecutions)
    }

    private fun getThrownException(): IOException {
        try {
            functionThatThrowsError()
            throw AssertionError("Exception not thrown")
        } catch (e: IOException) {
            return e
        }
    }

    private fun functionThatThrowsError() {
        throw IOException("Some error")
    }
}