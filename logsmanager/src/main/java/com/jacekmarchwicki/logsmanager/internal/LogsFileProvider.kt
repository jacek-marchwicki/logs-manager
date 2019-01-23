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
package com.jacekmarchwicki.logsmanager.internal

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import java.io.File
import java.util.Random
import kotlin.math.absoluteValue

internal class LogsFileProvider : FileProvider() {

    data class PublicFile(val file: File, val publicUri: Uri)
    companion object {
        private fun authority(context: Context): String = "${context.packageName}.LogsFileProvider"

        fun createLogsFile(context: Context): PublicFile {
            val logs = File(context.filesDir, "logs")
            logs.mkdirs()
            val logFile = File(logs, "${randomString()}.txt")
            val uri = FileProvider.getUriForFile(context,
                authority(context), logFile)
            return PublicFile(logFile, uri)
        }

        private val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnoprstuvwxyz"

        private fun randomString(): String {
            val random = Random()
            return (0..10).map {
                source[random.nextInt().absoluteValue % source.length]
            }.joinToString(separator = "")
        }
    }

}