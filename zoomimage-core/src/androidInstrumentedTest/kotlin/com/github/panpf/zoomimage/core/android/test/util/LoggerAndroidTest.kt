package com.github.panpf.zoomimage.core.android.test.util

import com.github.panpf.zoomimage.util.AndroidLogPipeline
import com.github.panpf.zoomimage.util.defaultLogPipeline
import kotlin.test.Test
import kotlin.test.assertSame

class LoggerAndroidTest {

    @Test
    fun testDefaultLogPipeline() {
        assertSame(AndroidLogPipeline, defaultLogPipeline())
    }

    @Test
    fun testToString() {
        assertSame("AndroidLogPipeline", AndroidLogPipeline.toString())
    }
}