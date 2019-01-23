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

package com.example.logsmanagerexample

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.jacekmarchwicki.logsmanager.LogsOkHttpInterceptor
import com.jacekmarchwicki.logsmanager.LogsSingleton
import com.jacekmarchwicki.logsmanager.log
import kotlinx.android.synthetic.main.main_activity.*
import okhttp3.*
import java.io.IOException

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogsSingleton.log(Log.VERBOSE, { "Activities" }, { "Start main activity" })
        setContentView(R.layout.main_activity)

        app_main_activity_result.text = "Click to execute request"
        app_main_activity_do_request_button.setOnClickListener {
            doRequest()
        }

    }

    private fun doRequest() {
        app_main_activity_result.text = "Loading..."
        val client = OkHttpClient.Builder()
            .addInterceptor(LogsOkHttpInterceptor(LogsSingleton, Log.DEBUG))
            .build()

        Thread {
            try {
                val response = client.newCall(
                    Request.Builder()
                        .url("https://www.gravatar.com/205e460b479e2e5b48aec07710c08d50.json")
                        .build()
                )
                    .execute()
                    .body()?.string() ?: "No body"
                runOnUiThread {
                    app_main_activity_result.text = "Response $response"
                }
            } catch (e: IOException) {
                runOnUiThread {
                    app_main_activity_result.text = "Failed"
                }
            }
        }.start()


    }
}
