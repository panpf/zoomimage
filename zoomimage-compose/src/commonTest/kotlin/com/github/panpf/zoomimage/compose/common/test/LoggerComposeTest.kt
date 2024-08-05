package com.github.panpf.zoomimage.compose.common.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.test.ListPipeline
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.defaultLogPipeline
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LoggerComposeTest {

    @Test
    fun testRememberZoomImageLogger() = runComposeUiTest {
        setContent {
            val logger1 = rememberZoomImageLogger()
            assertEquals("ZoomImage", logger1.tag)
            assertEquals(Logger.Level.Info, logger1.level)
            assertEquals(defaultLogPipeline(), logger1.pipeline)

            val listPipeline = ListPipeline()
            val logger2 = rememberZoomImageLogger(
                tag = "Test",
                level = Logger.Level.Verbose,
                pipeline = listPipeline
            )
            assertEquals("Test", logger2.tag)
            assertEquals(Logger.Level.Verbose, logger2.level)
            assertEquals(listPipeline, logger2.pipeline)
        }
    }
}