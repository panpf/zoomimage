package com.github.panpf.zoomimage.compose.sketch4.core.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger.Level.Debug
import com.github.panpf.zoomimage.util.Logger.Level.Info
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SketchZoomStateTest {

    @Test
    fun testRememberSketchZoomState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState = rememberSketchZoomState()
                assertEquals(
                    expected = "SketchZoomAsyncImage",
                    actual = zoomState.logger.tag
                )

                assertEquals(
                    expected = Info,
                    actual = zoomState.logger.level
                )
                val zoomState2 = rememberSketchZoomState(logLevel = Debug)
                assertEquals(
                    expected = Debug,
                    actual = zoomState2.logger.level
                )
            }
        }
    }
}