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
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.ViewAssertion
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.assertThat
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.espresso.util.HumanReadables
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import android.view.View
import com.jacekmarchwicki.logsmanager.LogsManagerAndroid
import com.jacekmarchwicki.logsmanager.LogsManagerAndroidSettings
import com.jacekmarchwicki.logsmanager.LogsManagerMultiple
import com.jacekmarchwicki.logsmanager.LogsSingleton
import com.jacekmarchwicki.logsmanager.R
import org.hamcrest.Matchers.`is`
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
        val logsManagerAndroid = LogsManagerAndroid(
            LogsManagerAndroidSettings(
                InstrumentationRegistry.getTargetContext(),
                Log.VERBOSE,
                "Logs manager"
            )
        )
        LogsSingleton.setup(logsManagerAndroid)
        LogsManagerAndroid.default = logsManagerAndroid

        LogsActivity.executor = IdlingExectutorWrapper(LogsActivity.executor)
    }

    @Test
    fun whenLoggedMessageAndStartLogManager_errorTitleIsDisplayed() {
        LogsSingleton.logInstant(Log.WARN, "Error title", "Error details")

        activityTestRule.launchActivity(null)

        onView(withText("Error title"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun whenUserGoToDetails_heSeesErrorDetails() {
        LogsSingleton.logInstant(Log.WARN, "Error title", "Error details")

        activityTestRule.launchActivity(null)

        onView(withText("Error title"))
            .perform(click())

        onView(withText("Error details"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun whenLogsManagerIsNotSet_displayErrorMessage() {
        LogsManagerAndroid.default = null

        activityTestRule.launchActivity(null)

        onView(withText(R.string.logsmanager_logs_activity_not_set))
            .check(matches(isDisplayed()))
        onView(withId(R.id.logs_activity_progress_bar))
            .check(isNotPresented())
    }

    @Test
    fun whenLogsManagerIsNotSet_doNotShowMenuItems() {
        LogsManagerAndroid.default = null

        activityTestRule.launchActivity(null)

        try {
            // menu items might not be available
            Espresso.openContextualActionModeOverflowMenu()
        } catch (ignore: android.support.test.espresso.NoMatchingViewException) {}

        onView(withText("Clear logs"))
            .check(isNotPresented())
        onView(withText("Send logs"))
            .check(isNotPresented())
        onView(withText("Search"))
            .check(isNotPresented())
    }

    @Test
    fun whenUseSearchWrapper_displayTheSameResults() {
        val logsManagerAndroid = LogsManagerAndroid(
            LogsManagerAndroidSettings(
                InstrumentationRegistry.getTargetContext(),
                Log.VERBOSE,
                "Logs manager"
            )
        )
        LogsSingleton.setup(LogsManagerMultiple(listOf(logsManagerAndroid)))
        LogsManagerAndroid.default = logsManagerAndroid
        LogsSingleton.logInstant(Log.WARN, "Error title", "Error details")

        activityTestRule.launchActivity(null)

        onView(withText("Error title"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun whenUserSearch_showCorrectResults() {
        LogsSingleton.logInstant(Log.WARN, "Error1/2", "Error details")
        LogsSingleton.logInstant(Log.WARN, "Error2/2", "Error details")

        activityTestRule.launchActivity(null)

        onView(withText("Error1/2")).check(matches(isDisplayed()))
        onView(withText("Error2/2")).check(matches(isDisplayed()))

        onView(withText("Search"))
            .perform(click())
        onView(searchView())
            .perform(typeText("Error2\n"))

        onView(withText("Error1/2")).check(isNotPresented())
        onView(withText("Error2/2")).check(matches(isDisplayed()))

        onView(searchViewClose()).perform(click())

        onView(withText("Error1/2")).check(matches(isDisplayed()))
        onView(withText("Error2/2")).check(matches(isDisplayed()))
    }

    private fun searchView() = withId(android.support.design.R.id.search_src_text)
    private fun searchViewClose() = withContentDescription(android.support.design.R.string.abc_toolbar_collapse_description)

    private fun isNotPresented(): ViewAssertion = object : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (view != null) {

                if (view.visibility != View.VISIBLE) {
                    return
                }
                var searchView: View = view
                while (searchView.parent != null && searchView.parent is View) {
                    searchView = searchView.parent as View
                    if (searchView.visibility != View.VISIBLE) {
                        return
                    }
                }
                assertThat<Boolean>(
                    "View is present in the hierarchy and it is visible" + HumanReadables.describe(view),
                    true,
                    `is`(false)
                )
            }
        }
    }
}