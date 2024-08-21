package com.github.panpf.zoomimage.core.android.test.util

import com.github.panpf.zoomimage.util.AndroidLogPipeline
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.defaultLogPipeline
import kotlin.test.Test
import kotlin.test.assertSame

class LoggerAndroidTest {

    @Test
    fun testDefaultLogPipeline() {
        assertSame(AndroidLogPipeline, defaultLogPipeline())
    }

    @Test
    fun testAndroidLogPipelineToString() {
        assertSame("AndroidLogPipeline", AndroidLogPipeline.toString())
    }

    @Test
    fun testAndroidLogPipelineLog() {
        AndroidLogPipeline.log(Logger.Level.Verbose, "tag", "msg", null)
        AndroidLogPipeline.log(Logger.Level.Verbose, "tag", "msg", Exception())
    }

    @Test
    fun testAndroidLogPipelineFlush() {
        AndroidLogPipeline.flush()
    }
}