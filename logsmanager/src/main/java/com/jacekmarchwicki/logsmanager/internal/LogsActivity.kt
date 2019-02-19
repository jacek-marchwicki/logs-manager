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

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.ContentLoadingProgressBar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jacekmarchwicki.logsmanager.LogsManagerAndroid
import com.jacekmarchwicki.logsmanager.LogsSingleton
import com.jacekmarchwicki.logsmanager.R
import com.jacekmarchwicki.universaladapter.BaseAdapterItem
import com.jacekmarchwicki.universaladapter.UniversalAdapter
import com.jacekmarchwicki.universaladapter.ViewHolderManager
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object LogsHelper {
    fun timeFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

    fun sendLogs(activity: Activity, func: (writer: OutputStreamWriter) -> Unit) {
        val logsFile = LogsFileProvider.createLogsFile(activity)
        logsFile.file.outputStream().use {
            it.writer().use { writer ->
                func(writer)
            }
        }
        activity.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND)
                    .setDataAndType(logsFile.publicUri, "text/plain")
                    .putExtra(Intent.EXTRA_STREAM, logsFile.publicUri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), "Share logs"
            )
        )
    }
}

internal class LogsActivity : AppCompatActivity() {

    private val logsManager =
        LogsSingleton.instance as? LogsManagerAndroid ?: throw IllegalStateException("logs singleton not set")

    data class ShortEntryItem(val shortEntry: LogsManagerAndroid.ShortEntry) :
        KotlinBaseAdapterItem<Long> {
        override val itemId: Long = shortEntry.id
    }

    class ShortEntryViewHolderManager : ViewHolderManager {
        override fun createViewHolder(
            parent: ViewGroup,
            inflater: LayoutInflater
        ): ViewHolderManager.BaseViewHolder<*> = Holder(
            inflater.inflate(
                R.layout.logsmanager_activity_short_entry_item, parent, false
            )
        )

        override fun matches(baseAdapterItem: BaseAdapterItem): Boolean = baseAdapterItem is ShortEntryItem

        private val format = LogsHelper.timeFormat()

        inner class Holder(val view: View) : KotlinBaseViewHolder<ShortEntryItem>(view) {
            override fun bind(item: ShortEntryItem) {
                view.setBackgroundColor(
                    when (item.shortEntry.level) {
                        in 0..Log.INFO -> Color.TRANSPARENT
                        Log.WARN -> Color.argb(68, 0, 0, 0)
                        else -> Color.argb(68, 255, 0, 0)
                    }
                )
                val level: TextView = view.findViewById(R.id.logs_activity_short_entry_item_level)
                val text: TextView = view.findViewById(R.id.logs_activity_short_entry_item_text)
                val time: TextView = view.findViewById(R.id.logs_activity_short_entry_item_time)
                level.text = "%d".format(Locale.US, item.shortEntry.level)
                text.text = item.shortEntry.title
                time.text = format.format(Date(item.shortEntry.timeInMillis))
                view.setOnClickListener {
                    view.context.startActivity(
                        LogsDetailsActivity.newIntent(
                            view.context,
                            item.itemId
                        )
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logsmanager_activity)

        val adapter = UniversalAdapter(listOf(ShortEntryViewHolderManager()))
        val recyclerView: RecyclerView = findViewById(R.id.logs_activity_recycler_view)
        val toolbar: Toolbar = findViewById(R.id.logs_activity_toolbar)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context!!).apply {
                recycleChildrenOnDetach = true
            }
            this.adapter = adapter
        }
        loadData(adapter, withProgress = true)
        toolbar.setNavigationIcon(R.drawable.logsmanager_close)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.title = logsManager.settings.notificationTitle
        toolbar.menu.add("Clear")
            .setOnMenuItemClickListener {
                logsManager.clear()
                loadData(adapter, withProgress = true)
                true
            }
        toolbar.menu.add("Send logs")
            .setOnMenuItemClickListener {
                LogsHelper.sendLogs(this, ::allEntities)
                true
            }
        val refresh: SwipeRefreshLayout = findViewById(R.id.logs_activity_refresh)
        refresh.setOnRefreshListener {
            loadData(adapter, withProgress = false)
        }
    }

    private fun allEntities(writer: OutputStreamWriter) {
        val timeFormat = LogsHelper.timeFormat()
        val date = timeFormat.format(Date())
        val entities = logsManager.getEntries()
        writer.write("===== Log Start =====\n")
        writer.write("Name: ${logsManager.settings.notificationTitle}")
        writer.write("Now: $date\n")
        writer.write("Log level: ${logsManager.settings.logLevelEnabled}")
        writer.write("===== Log Begin =====\n")
        entities.forEach { entry ->
            writer.write("===== Log Entry =====\n")
            writer.write("${timeFormat.format(Date(entry.timeInMillis))}, Log level: ${entry.level}\n")
            writer.write(entry.title)
            writer.write("\n")
            writer.write(logsManager.getDetails(entry.id)?.details ?: "LOGGER ERROR")
            writer.write("\n\n\n")
        }
        writer.write("===== Log End   =====\n")
    }

    private fun loadData(adapter: UniversalAdapter, withProgress: Boolean) {
        val refresh: SwipeRefreshLayout = findViewById(R.id.logs_activity_refresh)
        val progress: ContentLoadingProgressBar = findViewById(R.id.logs_activity_progress_bar)
        val toolbar: Toolbar = findViewById(R.id.logs_activity_toolbar)

        if (withProgress) {
            progress.show()
            adapter.call(listOf())
            toolbar.subtitle = "Loading..."
        } else {
            refresh.isRefreshing = true
        }

        activityExecute({
            Date() to logsManager.getEntries().map(LogsActivity::ShortEntryItem)
        }, { (time, items) ->
            refresh.isRefreshing = false
            progress.hide()
            toolbar.subtitle = LogsHelper.timeFormat().format(time)
            adapter.call(items)
        })
    }
}

private fun <T> Activity.activityExecute(run: () -> T, onResult: (T) -> Unit) {
    Thread {
        val result = run()
        runOnUiThread {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed) {
                // possible crash on older os versions, but who cares during tests
                onResult(result)
            }
        }
    }.start()
}