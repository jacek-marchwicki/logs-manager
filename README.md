# Logger in notification window

LogsManager is simple and powerfull logger that allows display all logs from your application by simply tapping on its notification.

--- Video here

# Reasons
1. All requests sent to a server via OkHttp are automtaically logged with curl's and response body.
2. All http 4xx/5xx errors are visible during tests.
3. In case of failure you can send all logs at once to debug the problem.
4. A tester after finding issue in your application can send all logs to you including all requests and responses.
5. LogsManager have no-op version for production builds, so don't worry about slowing-down your app.
6. If you rely on buggy server as a mobile developer you can always do `LogsSingleton.log(Log.WARN, "This is not my fault, this is buggy response from the server, look at issue #1234")`.
7. You can use the LogsManager to check if analytics events are prepared correctly for Firebase/Adobe/Mixpanel etc.
8. You can easy log all exceptions that shouldn't be displayed to a user, but during testing might be interesting.

# Configuration

Configure dependencies:

```groovy
dependencies {
    debugImplementation project(":logsmanager")
    releaseImplementation project(':logsmanager-no-op')
    debugImplementation project(":logsmanager-okhttp")
    releaseImplementation project(':logsmanager-okhttp-no-op')
}
```

In case of proguard: don't need to do anything

Setup singleton in your application:

```kotlin
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogsSingleton.setup(LogsManagerAndroid(LogsManagerAndroidSettings(this, Log.VERBOSE)))
    }
}
```

Log your events:

```kotlin
LogsSingleton.log(Log.VERBOSE, { "Activities" }, { "Start main activity v1" })
```

Configuration for OkHttp:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(LogsOkHttpInterceptor(LogsSingleton, Log.DEBUG))
    // Your other interceptors
    .build()
```

# FAQ

## How I can log?
```kotlin
LogSingleton.log(Log.WARN, "A title")
```

```kotlin
LogSinglegon.logTitle(Log.WARN) {"A title with calculation ${2 + 3}"}
```

```kotlin
LogSinglegon.log(Log.WARN, {"Title"}) {"Very long description with a calculation ${2 + 3}"}
```

```kotlin
LogSinglegon.logPair(Log.WARN) {"Title" to "Very long description with a calculation ${2 + 3}"}
```

```kotlin
LogSinglegon.logEntry(Log.WARN) {LogsManager.EntryData("Title", "Very long description with a calculation ${2 + 3}")}
```


## What is the difference between log and logTitle?

Examples:

```kotlin
fun timeConsumingCalculation() = 2 + 3

LogSingleton.log(Log.DEBUG, "A title ${timeConsumingCalculation()}")

LogSingleton.logTitle(Log.DEBUG) { "A title ${timeConsumingCalculation()}" }
```

`logTitle` is more optimal if you logging is disabled, in such a case `timeConsumingCalculation()` will not be executed.


## Can I log everything to console?

Yes

```kotlin
LogsSingleton.setup(LogsManagerLogCat(Log.DEBUG))
```


## Can I use it with Java

Yes


## How can I log exception

```kotlin
val fileContent = LogsSingleton.logFailure(Log.WARN) {
    readFile()
}
```

or

```kotlin
val fileContent = try {
    readFile()
} catch (e: Exception) {
  LogSingleton.log(Log.WARN, "Can't read file", e)
}
```


# Todo
- Test with proguard
- Check if proguard removes all logging code

# License

```
Copyright (C) 2019 Jacek Marchwicki <jacek.marchwicki@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```