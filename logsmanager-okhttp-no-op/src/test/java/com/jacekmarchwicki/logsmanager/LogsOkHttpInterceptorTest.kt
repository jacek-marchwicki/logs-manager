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
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LogsOkHttpInterceptorTest {
    private val logsManager: LogsManager = mock {}
    private val client = OkHttpClient.Builder()
        .addInterceptor(LogsOkHttpInterceptor(logsManager, Log.DEBUG))
        .build()
    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start()
    }

    @Test
    fun `when request is executed, it returns correct values`() {
        mockWebServer.enqueue(MockResponse().setBody("krowa"))
        val response = executeRequest()

        Assert.assertEquals(200, response.code())
        Assert.assertEquals("krowa", response.body()?.string())

        verifyZeroInteractions(logsManager)
    }

    private fun executeRequest(): Response =
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/base"))
                .build()
        )
            .execute()
}