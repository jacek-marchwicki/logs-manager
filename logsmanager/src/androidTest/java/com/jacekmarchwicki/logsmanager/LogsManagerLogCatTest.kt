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

class LogsManagerLogCatTest {
    private val logsManager = LogsManagerLogCat(Log.DEBUG)

    @Test
    fun whenLogMessage_noCrash() {
        logsManager.log(Log.DEBUG, "title", "details")
    }

    @Test
    fun whenLogMessageWithToLowLevel_noCrash() {
        logsManager.log(Log.VERBOSE, "title", "details")
    }

    @Test
    fun whenLogInstantMessage_noCrash() {
        logsManager.logInstant(Log.DEBUG, "title", "details")
    }

    @Test
    fun whenLogInstantMessageWithToLowLevel_noCrash() {
        logsManager.logInstant(Log.VERBOSE, "title", "details")
    }

    @Test
    fun whenUpdateLogInstantMessage_noCrash() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }
    }

    @Test
    fun whenUpdateLogInstantMessageWithToLowLevel_noCrash() {
        val id = logsManager.logInstant(Log.DEBUG, "title", "details")

        logsManager.updateLogInstant(id) {
            it.copy(level = Log.WARN, title = "updated ${it.title}", details = "updated ${it.details}")
        }
    }
}