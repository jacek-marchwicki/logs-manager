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
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertFalse
import org.junit.Test

class LogsManagerAndroidTest {

    private val logsManager  = LogsManagerAndroid(LogsManagerAndroidSettings(mock {  }, Log.WARN))

    @Test
    fun `checkLevel always return false`() {
        assertFalse(logsManager.checkLevel(Log.ASSERT))
        assertFalse(logsManager.checkLevel(Log.ERROR))
        assertFalse(logsManager.checkLevel(Log.WARN))
        assertFalse(logsManager.checkLevel(Log.INFO))
        assertFalse(logsManager.checkLevel(Log.DEBUG))
        assertFalse(logsManager.checkLevel(Log.VERBOSE))
    }

    @Test
    fun `log always succeed`() {
        logsManager.log(Log.VERBOSE, "title", "details")
    }

    @Test
    fun `logInstant and update always succeed`() {
        val ret = logsManager.logInstant(Log.VERBOSE, "title", "details")
        logsManager.updateLogInstant(ret) { it.copy(title = "new title") }
    }

}