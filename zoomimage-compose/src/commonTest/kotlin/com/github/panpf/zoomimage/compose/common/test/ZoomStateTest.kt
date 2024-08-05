package com.github.panpf.zoomimage.compose.common.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
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
                val logger = rememberZoomImageLogger()
                val zoomState = rememberZoomState(logger)
                assertEquals(
                    expected = logger,
                    actual = zoomState.logger
                )
                assertEquals(
                    expected = logger,
                    actual = zoomState.zoomable.logger
                )
                assertEquals(
                    expected = logger,
                    actual = zoomState.subsampling.logger
                )
            }
        }
    }

    @Test
    fun testToString() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState = rememberZoomState()
                assertEquals(
                    expected = "ZoomState(logger=${zoomState.logger}, zoomable=${zoomState.zoomable}, subsampling=${zoomState.subsampling})",
                    actual = zoomState.toString()
                )
            }
        }
    }
}