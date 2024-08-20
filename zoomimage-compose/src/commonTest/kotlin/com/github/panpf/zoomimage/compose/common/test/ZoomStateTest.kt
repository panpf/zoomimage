package com.github.panpf.zoomimage.compose.common.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ZoomStateTest {

    @Test
    fun testRememberZoomState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState = rememberZoomState()
                assertEquals(
                    expected = "ZoomImage",
                    actual = zoomState.logger.tag
                )
                assertEquals(
                    expected = zoomState.logger,
                    actual = zoomState.zoomable.logger
                )
                assertEquals(
                    expected = zoomState.logger,
                    actual = zoomState.subsampling.logger
                )
            }
        }
    }

    // TODO setImageSource
}