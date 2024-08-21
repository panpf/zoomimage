package com.github.panpf.zoomimage.core.nonandroid.test.util

import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.PrintlnLogPipeline
import com.github.panpf.zoomimage.util.defaultLogPipeline
import kotlin.test.Test
import kotlin.test.assertSame

class LoggerNonAndroidTest {

    @Test
    fun testDefaultLogPipeline() {
        assertSame(PrintlnLogPipeline, defaultLogPipeline())
    }

    @Test
    fun testPrintlnLogPipelineToString() {
        assertSame("PrintlnLogPipeline", PrintlnLogPipeline.toString())
    }

    @Test
    fun testPrintlnLogPipelineLog() {
        PrintlnLogPipeline.log(Logger.Level.Verbose, "tag", "msg", null)
        PrintlnLogPipeline.log(Logger.Level.Verbose, "tag", "msg", Exception())
    }

    @Test
    fun testPrintlnLogPipelineFlush() {
        PrintlnLogPipeline.flush()
    }
}