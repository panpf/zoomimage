package com.github.panpf.zoomimage.test

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.time.TimeSource


@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitMillis(millis: Long) {
    val startTime = TimeSource.Monotonic.markNow()
    waitUntil(timeoutMillis = millis * 2) {
        startTime.elapsedNow().inWholeMilliseconds >= millis
    }
}