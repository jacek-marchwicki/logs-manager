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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.jacekmarchwicki.logsmanager.internal.LogsActivity
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

private fun defaultThreadPool(): Executor = ThreadPoolExecutor(
    1, 3, 10L, TimeUnit.SECONDS,
    LinkedBlockingQueue(128), object : ThreadFactory {
        private val mCount = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread(r, "LogsManager #" + mCount.getAndIncrement())
        }
    }
)

class LogsManagerAndroid(internal val settings: LogsManagerAndroidSettings, private val executor: Executor = defaultThreadPool()) : LogsManager {

    data class ShortEntry(val id: Long, val timeInMillis: Long, val level: Int, val title: String)
    data class FullEntry(val id: Long, val timeInMillis: Long, val level: Int, val title: String, val details: String)

    private class DBHelper(context: Context) : SQLiteOpenHelper(context, "Logs", null, 4) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE logs (id INTEGER PRIMARY KEY, timeInMillis INTEGER, level INTEGER, title TEXT, details BLOB)")
            db.execSQL("CREATE INDEX logs_time_index ON logs ( timeInMillis )")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP INDEX IF EXISTS logs_time_index")
            db.execSQL("DROP TABLE IF EXISTS logs")
            onCreate(db)
        }

        fun insert(timeInMillis: Long, level: Int, title: String, details: String): Long =
                writableDatabase.insertOrThrow("logs", null, ContentValues().apply {
                    put("timeInMillis", timeInMillis)
                    put("level", level)
                    put("title", title)
                    put("details", details)
                })

        fun getEntries(): List<ShortEntry> = readableDatabase.query(
                "logs",
                arrayOf("id, timeInMillis, level, title"),
                null,
                null,
                null, // don't group the rows
                null,
                "timeInMillis DESC"
        )
                .use {
                    it.toList {
                        ShortEntry(
                            id = it.getLong(0),
                            timeInMillis = it.getLong(1),
                            level = it.getInt(2),
                            title = it.getString(3)
                        )
                    }
                }

        private fun getDetails(db: SQLiteDatabase, id: Long): FullEntry? = db.query("logs",
                arrayOf("id, timeInMillis, level, title, details"),
                "id = ?",
            arrayOf("%d".format(Locale.US, id)),
                null,
                null,
                "timeInMillis DESC")
                .use {
                    it.toList {
                        FullEntry(
                            id = it.getLong(0),
                            timeInMillis = it.getLong(1),
                            level = it.getInt(2),
                            title = it.getString(3),
                            details = it.getString(4)
                        )
                    }
                }.firstOrNull()

        fun getDetails(id: Long): FullEntry? = getDetails(readableDatabase, id)

        fun update(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) =
                writableDatabase.inTransaction { db ->
                    getDetails(writableDatabase, id)
                        ?.let {
                            val new = update(
                                LogsManager.EntryLevelData(
                                    it.level,
                                    it.title,
                                    it.details
                                )
                            )
                            db.update("logs",
                                ContentValues().apply {
                                    put("level", new.level)
                                    put("title", new.title)
                                    put("details", new.details)
                                },
                                "id = ?",
                                arrayOf("%d".format(Locale.US, id)))
                        }
                }

        fun clear() {
            writableDatabase.delete("logs", "", arrayOf())
        }
    }

    private val dbHelper = DBHelper(settings.context)

    internal fun getEntries(): List<ShortEntry> = dbHelper.getEntries()
    internal fun getDetails(id: Long): FullEntry? = dbHelper.getDetails(id)

    private fun showNotification() {
        val channelId = "debug"
        val notificationManger = settings.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Logs manager"
            val descriptionText = "Logging"
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManger.createNotificationChannel(channel)
        }
        val intent = Intent(
            settings.context,
            LogsActivity::class.java
        )
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            settings.context,
            789123,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(settings.context, channelId)
            .setSmallIcon(R.drawable.logsmanager_settings)
                .setContentTitle(settings.notificationTitle)
                .setContentText("Logs manager")
            .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .build()
        notificationManger.notify(789123, notification)
    }

    init {
        showNotification()
    }

    private fun logInternal(level: Int, title: String, details: String) {
        executor.execute {
            dbHelper.insert(System.currentTimeMillis(), level, title.limit(100), details)
        }
    }

    override fun log(level: Int, title: String, details: String) {
        if (checkLevel(level)) {
            logInternal(level, title, details)
        }
    }

    override fun logInstant(level: Int, title: String, details: String): Long {
        return if (checkLevel(level)) {
            dbHelper.insert(System.currentTimeMillis(), level, title.limit(100), details)
        } else {
            -1L
        }
    }

    override fun updateLogInstant(id: Long, update: (LogsManager.EntryLevelData) -> LogsManager.EntryLevelData) {
        if (id > 0L) {
            dbHelper.update(id, update)
        }
    }

    override fun checkLevel(level: Int) = level >= settings.logLevelEnabled
    internal fun clear() {
        dbHelper.clear()
    }
}

private fun <T> Cursor.toList(map: (Cursor) -> T): List<T> {
    val list = mutableListOf<T>()
    while (moveToNext()) {
        list.add(map(this))
    }
    return list.toList()
}

private fun String.limit(max: Int): String = substring(0, minOf(max, length))

private fun <T> SQLiteDatabase.inTransaction(update: (SQLiteDatabase) -> T): T {
    beginTransaction()
    try {
        val ret = update(this)
        setTransactionSuccessful()
        return ret
    } finally {
        endTransaction()
    }
}