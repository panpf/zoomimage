package com.github.panpf.zoomimage.test

import kotlinx.coroutines.delay
import kotlin.time.TimeSource

suspend fun delayUntil(timeoutMillis: Long, condition: () -> Boolean) {
    val time = TimeSource.Monotonic.markNow()
    while (!condition() && time.elapsedNow().inWholeMilliseconds < timeoutMillis) {
        delay(10)
    }
}

/**
 * Replacement for delay as delay does not work in runTest
 *
 * Note: Because block will really block the current thread, so please do not use it in the UI thread.
 */
fun block(millis: Long) {
    if (millis > 0) {
        val startTime = TimeSource.Monotonic.markNow()
        while (startTime.elapsedNow().inWholeMilliseconds < millis) {
            // Do nothing
        }
    }
}