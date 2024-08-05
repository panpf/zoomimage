package com.github.panpf.zoomimage.compose.common.test.subsampling

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.test.TestLifecycle
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubsamplingStateTest {
    // TODO test

    @Test
    fun testRememberSubsamplingState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomableState = rememberZoomableState()
                val subsamplingState = rememberSubsamplingState(zoomableState)
                assertEquals(
                    expected = zoomableState.logger,
                    actual = subsamplingState.logger
                )
                assertEquals(
                    expected = zoomableState,
                    actual = subsamplingState.zoomableState
                )
            }
        }
    }
}