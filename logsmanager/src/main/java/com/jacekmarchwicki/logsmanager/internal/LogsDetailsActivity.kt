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
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.jacekmarchwicki.logsmanager.LogsManagerAndroid
import com.jacekmarchwicki.logsmanager.LogsSingleton
import com.jacekmarchwicki.logsmanager.R
import java.lang.IllegalStateException
import java.util.Date

internal class LogsDetailsActivity : AppCompatActivity() {
    private val logsManager = LogsSingleton.instance as? LogsManagerAndroid
        ?: throw IllegalStateException("logs singleton not set")
    companion object {
        fun newIntent(context: Context, id: Long): Intent = Intent(context, LogsDetailsActivity::class.java)
                .putExtra("ID", id)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logsmanager_details_activity)

        val id = intent.getLongExtra("ID", -1L)

        val toolbar: Toolbar = findViewById(R.id.logs_details_activity_toolbar)
        val detailsTextView: TextView = findViewById(R.id.logs_details_activity_details)
        val details = logsManager.getDetails(id) ?: throw IllegalStateException("No id $id")
        toolbar.setNavigationIcon(R.drawable.logsmanager_close)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.title = "(${details.level}): ${details.title}"
        detailsTextView.text = details.details
        toolbar.menu.add("Send this entry")
                .setOnMenuItemClickListener {
                    LogsHelper.sendLogs(this) { writer ->
                        writer.write(
                            "${LogsHelper.timeFormat().format(
                                Date(
                                    details.timeInMillis
                                )
                            )}, Log level: ${details.level}\n"
                        )
                        writer.write(details.title)
                        writer.write("\n")
                        writer.write(details.details)
                        writer.write("\n")
                    }
                    true
                }
    }
}