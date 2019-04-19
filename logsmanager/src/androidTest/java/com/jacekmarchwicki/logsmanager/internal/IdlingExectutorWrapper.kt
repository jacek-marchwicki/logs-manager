package com.jacekmarchwicki.logsmanager.internal

import android.support.test.espresso.idling.CountingIdlingResource
import java.util.concurrent.Executor

class IdlingExectutorWrapper(
    private val wrapped: Executor,
    private val countingIdlingResource: CountingIdlingResource = CountingIdlingResource("executor")
) : Executor {

    override fun execute(command: Runnable) {
        countingIdlingResource.increment()
        return wrapped.execute {
            try {
                command.run()
            } finally {
                countingIdlingResource.decrement()
            }
        }
    }
}