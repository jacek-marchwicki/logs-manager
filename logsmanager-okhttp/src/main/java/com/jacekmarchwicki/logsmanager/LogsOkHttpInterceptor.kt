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
import com.moczul.ok2curl.CurlBuilder
import com.moczul.ok2curl.Options
import okhttp3.Connection
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import org.json.JSONObject
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class LogsOkHttpInterceptor constructor(private val logsManager: LogsManager, private val level: Int) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response =
            if (logsManager.checkLevel(level)) {

                val request = chain.request()
                val connection = chain.connection()

                val title = "HTTP: ${request.method()} ${request.url()}"
                val curl = CurlBuilder(request, 1024L * 1024L, listOf(), Options.EMPTY).build()
                val id = logsManager.logInstant(Log.INFO, "--- $title", "CURL:\n$curl\n\n\n")
                logsManager.appendLogInstant(id, "REQUEST:\n${parseReqeuest(connection, request)}\n\n")
                val startNs = System.nanoTime()
                val response: Response
                try {
                    response = chain.proceed(request)
                    val code = response.code()
                    val level = when (code) {
                        in 200..299 -> Log.INFO
                        in 400..499 -> Log.WARN
                        else -> Log.ERROR
                    }
                    logsManager.setSeverityInstant(id, level)
                    logsManager.updateLogInstant(id) { it.copy(title = "$code $title") }
                    logsManager.appendLogInstant(id, "RESPONSE:\n${parseResposne(response, startNs)}\n")
                    logsManager.appendLogInstant(id, "BODY:\n${parseResposneBody(response)}\n\n")
                } catch (e: Exception) {
                    if (e is InterruptedIOException || e.message == "Canceled") {
                        logsManager.updateLogInstant(id) { it.copy(title = "WWW $title") }
                        logsManager.setSeverityInstant(id, Log.WARN)
                        logsManager.appendLogInstant(id, "REQUEST INTERRUPTED:\n${e.printStackTraceString()}\n\n\n")
                    } else {
                        logsManager.updateLogInstant(id) { it.copy(title = "EEE $title") }
                        logsManager.setSeverityInstant(id, Log.ERROR)
                        logsManager.appendLogInstant(id, "NETWORK EXCEPTION:\n${e.printStackTraceString()}\n\n\n")
                    }
                    throw e
                }
                response
            } else {
                chain.proceed(chain.request())
            }

    private fun parseReqeuest(connection: Connection?, request: Request): String {
        val requestBody = request.body()
        val requestStartMessage = "${request.method()}' '${request.url()}${connection?.protocol()
                ?: ""}\n"
        val startingText = StringBuilder()

        startingText.append(requestStartMessage)

        if (requestBody != null) {
            // Request body headers are only present when installed as a network interceptor. Force
            // them to be included (when available) so there values are known.
            startingText.append("Content-Type: ${requestBody.contentType() ?: "<not specified>"}\n")
            startingText.append("Content-Length: ${if (requestBody.contentLength() != -1L) requestBody.contentLength().toString() else "<not specified>"} \n")
        }

        val headers = request.headers()
        var i = 0
        val count = headers.size()
        while (i < count) {
            val name = headers.name(i)
            // Skip headers from the request body as they are explicitly logged above.
            if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                startingText.append("$name: ${headers.value(i)}\n")
            }
            i++
        }

        if (requestBody == null) {
            startingText.append("(no body)\n")
        } else if (bodyEncoded(request.headers())) {
            startingText.append("(encoded body omitted)\n")
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val charset: Charset = requestBody.contentType()?.charset(UTF8) ?: UTF8

            startingText.append("\n")
            if (isPlaintext(buffer)) {
                startingText.append(buffer.readString(charset))
                startingText.append("(${requestBody.contentLength()}-byte body)\n")
            } else {
                startingText.append("(binary ${requestBody.contentLength()}-byte body omitted)\n")
            }
        }
        return startingText.toString()
    }

    private fun parseResposne(response: Response, startNs: Long): String {
        val startingText = StringBuilder()
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        startingText.append("${response.code()}${if (response.message().isEmpty()) "" else ' ' + response.message()} ${response.request().url()} (${tookMs}ms)\n")

        val responseHeaders = response.headers()

        var outI = 0
        val outCount = responseHeaders.size()
        while (outI < outCount) {
            startingText.append("${responseHeaders.name(outI)}: ${responseHeaders.value(outI)}\n")
            outI++
        }

        return startingText.toString()
    }

    private fun parseResposneBody(response: Response): String {
        val responseBody = response.body()
        return if (responseBody == null || !HttpHeaders.hasBody(response)) {
            "(no body)\n"
        } else if (bodyEncoded(response.headers())) {
            "(encoded body omitted)\n"
        } else {
            val contentLength = responseBody.contentLength()
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            val charset: Charset = responseBody.contentType()?.charset(UTF8) ?: UTF8

            if (!isPlaintext(buffer)) {
                "(binary ${buffer.size()}-byte body omitted)\n"
            } else if (contentLength != 0L) {
                val body = buffer.clone().readString(charset)

                val formattedBody = if (isJson(responseBody)) {
                    try {
                        JSONObject(body).toString(2)
                    } catch (e: Throwable) {
                        body
                    }
                } else {
                    body
                }
                "$formattedBody\n(${buffer.size()}-byte body)\n"
            } else {
                "(wired body)\n"
            }
        }
    }

    private fun isJson(responseBody: ResponseBody): Boolean =
            responseBody.contentType()?.let { it.type() == "application" && it.subtype() == "json" } ?: false

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }
        }
    }
}
