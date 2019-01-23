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

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.jacekmarchwicki.logsmanager.LogsManagerAndroid
import com.jacekmarchwicki.logsmanager.LogsManagerAndroidSettings
import com.jacekmarchwicki.logsmanager.LogsSingleton
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogsActivityTest {
    @Rule
    @JvmField
    internal var activityTestRule = ActivityTestRule(LogsActivity::class.java, false, false)

    @Before
    fun setUp() {
        LogsSingleton.setup(LogsManagerAndroid(LogsManagerAndroidSettings(InstrumentationRegistry.getTargetContext(), Log.VERBOSE, "Logs manager")))
    }

    @Test
    fun whenLoggedMessageAndStartLogManager_errorTitleIsDisplayed() {
        LogsSingleton.logInstant(Log.WARN, "Error title", "Error details")

        activityTestRule.launchActivity(null)

        onView(withText("Error title"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun whenUserGoToDeails_heSeesErrorDetails() {
        LogsSingleton.logInstant(Log.WARN, "Error title", "Error details")

        activityTestRule.launchActivity(null)

        onView(withText("Error title"))
            .perform(click())

        onView(withText("Error details"))
            .check(matches(isDisplayed()))
    }
}