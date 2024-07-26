package com.github.panpf.zoomimage.core.nonandroid.test.util

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
    fun testToString() {
        assertSame("PrintlnLogPipeline", PrintlnLogPipeline.toString())
    }
}