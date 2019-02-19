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

class LogsSingletonTest {

    @Test
    fun `checkLevel always return false`() {
        assertFalse(LogsSingleton.checkLevel(Log.ASSERT))
        assertFalse(LogsSingleton.checkLevel(Log.ERROR))
        assertFalse(LogsSingleton.checkLevel(Log.WARN))
        assertFalse(LogsSingleton.checkLevel(Log.INFO))
        assertFalse(LogsSingleton.checkLevel(Log.DEBUG))
        assertFalse(LogsSingleton.checkLevel(Log.VERBOSE))
    }

    @Test
    fun `log always succeed`() {
        LogsSingleton.log(Log.VERBOSE, "title", "details")
    }

    @Test
    fun `logInstant and update always succeed`() {
        val ret = LogsSingleton.logInstant(Log.VERBOSE, "title", "details")
        LogsSingleton.updateLogInstant(ret) { it.copy(title = "new title") }
    }

    @Test
    fun `setup always succeed`() {
        LogsSingleton.setup(mock { })
        LogsSingleton.setup(mock { })
    }
}