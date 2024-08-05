package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ZoomableStateTest {
    // TODO test

    @Test
    fun testRememberZoomableState() = runComposeUiTest {
        setContent {
            val zoomableState = rememberZoomableState()
            assertEquals(
                expected = rememberZoomImageLogger(),
                actual = zoomableState.logger
            )

            val logger = rememberZoomImageLogger(tag = "Test")
            val zoomableState2 = rememberZoomableState(logger)
            assertEquals(
                expected = logger,
                actual = zoomableState2.logger
            )
        }
    }
}