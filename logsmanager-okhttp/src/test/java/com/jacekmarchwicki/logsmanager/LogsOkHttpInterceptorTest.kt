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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.IOException

class LogsOkHttpInterceptorTest {

    private val logsManager = LogsManagerFake(Log.DEBUG)
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

        assertEquals(200, response.code())
        assertEquals("krowa", response.body()?.string())
    }

    @Test
    fun `when there is 400 in response, show it`() {
        mockWebServer.enqueue(MockResponse().setBody("krowa").setResponseCode(400))
        executeRequest()

        assertEquals(1, logsManager.logs.size)
        val log = logsManager.logs[0]
        assertEquals(Log.WARN, log.level)
        assertEquals("400 HTTP: GET http://localhost:${mockWebServer.port}/base", log.title)

        assertThat(log.details, containsString(
            "CURL:\n" +
                    "curl -X GET \"http://localhost:${mockWebServer.port}/base\"\n"))

        assertThat(log.details, containsString(
            "REQUEST:\n" +
                    "GET' 'http://localhost:${mockWebServer.port}/base\n" +
                    "(no body)\n"))

        assertThat(log.details, containsString(
            "RESPONSE:\n" +
                    "400 Client Error http://localhost:${mockWebServer.port}/base"))

        assertThat(log.details, containsString(
            "BODY:\n" +
                    "krowa\n" +
                    "(5-byte body)\n"))
    }

    @Test
    fun `when there is 500 in response, show it`() {
        mockWebServer.enqueue(MockResponse().setBody("krowa").setResponseCode(500))
        executeRequest()

        assertEquals(1, logsManager.logs.size)
        val log = logsManager.logs[0]
        assertEquals(Log.ERROR, log.level)
        assertEquals("500 HTTP: GET http://localhost:${mockWebServer.port}/base", log.title)

        assertThat(log.details, containsString(
            "CURL:\n" +
                    "curl -X GET \"http://localhost:${mockWebServer.port}/base\"\n"))

        assertThat(log.details, containsString(
            "REQUEST:\n" +
                    "GET' 'http://localhost:${mockWebServer.port}/base\n" +
                    "(no body)\n"))

        assertThat(log.details, containsString(
            "RESPONSE:\n" +
                    "500 Server Error http://localhost:${mockWebServer.port}/base"))

        assertThat(log.details, containsString(
            "BODY:\n" +
                    "krowa\n" +
                    "(5-byte body)\n"))
    }

    @Test
    fun `when there error in response, show it`() {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        executeRequestWithExpectedFailure()

        assertEquals(1, logsManager.logs.size)
        val log = logsManager.logs[0]
        assertEquals(Log.ERROR, log.level)
        assertEquals("EEE HTTP: GET http://localhost:${mockWebServer.port}/base", log.title)

        assertThat(log.details, containsString(
            "CURL:\n" +
                    "curl -X GET \"http://localhost:${mockWebServer.port}/base\"\n"))

        assertThat(log.details, containsString(
            "REQUEST:\n" +
                    "GET' 'http://localhost:${mockWebServer.port}/base\n" +
                    "(no body)\n"))

        assertThat(log.details, containsString(
            "NETWORK EXCEPTION:\n"))
    }

    private fun executeRequestWithExpectedFailure(): IOException {
        try {
            executeRequest()
            throw AssertionError("Exception not thrown")
        } catch (e: IOException) {
            return e
        }
    }

    @Test
    fun `when request is executed, logger contains correct value`() {
        mockWebServer.enqueue(MockResponse().setBody("krowa"))
        executeRequest()

        assertEquals(1, logsManager.logs.size)
        val log = logsManager.logs[0]
        assertEquals(Log.INFO, log.level)
        assertEquals("200 HTTP: GET http://localhost:${mockWebServer.port}/base", log.title)

        assertThat(log.details, containsString(
            "CURL:\n" +
                "curl -X GET \"http://localhost:${mockWebServer.port}/base\"\n"))

        assertThat(log.details, containsString(
            "REQUEST:\n" +
                "GET' 'http://localhost:${mockWebServer.port}/base\n" +
                "(no body)\n"))

        assertThat(log.details, containsString(
            "RESPONSE:\n" +
                    "200 OK http://localhost:${mockWebServer.port}/base"))

        assertThat(log.details, containsString(
            "BODY:\n" +
                    "krowa\n" +
                    "(5-byte body)\n"))
    }

    private fun executeRequest(): Response =
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/base"))
                .build())
            .execute()
}