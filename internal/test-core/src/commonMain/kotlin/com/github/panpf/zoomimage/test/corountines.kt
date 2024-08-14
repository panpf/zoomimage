package com.github.panpf.zoomimage.test

import kotlinx.coroutines.delay
import kotlin.time.TimeSource

suspend fun delayUntil(timeoutMillis: Long, condition: () -> Boolean) {
    val time = TimeSource.Monotonic.markNow()
    while (!condition() && time.elapsedNow().inWholeMilliseconds < timeoutMillis) {
        delay(10)
    }
}