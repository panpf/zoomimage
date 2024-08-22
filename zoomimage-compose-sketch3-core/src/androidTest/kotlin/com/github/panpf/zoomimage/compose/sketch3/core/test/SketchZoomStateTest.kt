package com.github.panpf.zoomimage.compose.sketch3.core.test

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class SketchZoomStateTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testRememberSketchZoomState() {
        rule.setContent {
            TestLifecycle {
                val zoomState = rememberSketchZoomState()
                assertEquals(
                    expected = "SketchZoomAsyncImage",
                    actual = zoomState.logger.tag
                )

                assertEquals(
                    expected = Logger.Level.Info,
                    actual = zoomState.logger.level
                )
                val zoomState2 = rememberSketchZoomState(logLevel = Logger.Level.Debug)
                assertEquals(
                    expected = Logger.Level.Debug,
                    actual = zoomState2.logger.level
                )
            }
        }
    }
}