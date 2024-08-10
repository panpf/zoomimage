package com.github.panpf.zoomimage.compose.sketch.core.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
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
            }
        }
    }
}