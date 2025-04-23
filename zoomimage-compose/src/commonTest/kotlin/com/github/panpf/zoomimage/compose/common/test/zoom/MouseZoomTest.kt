package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.zoom.MouseZoomNode
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseZoomTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testConvertAddScale() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 6.0f,
                actual = zoomable.transform.scaleX.format(2)
            )

            val mouseZoomNode = MouseZoomNode(zoomable)
            assertEquals(
                expected = false,
                actual = zoomable.reverseMouseWheelScale
            )
            assertEquals(
                expected = -0.99f,
                actual = mouseZoomNode.convertAddScale(3f).format(2)
            )
            assertEquals(
                expected = 0.99f,
                actual = mouseZoomNode.convertAddScale(-3f).format(2)
            )

            zoomable.reverseMouseWheelScale = true
            assertEquals(
                expected = true,
                actual = zoomable.reverseMouseWheelScale
            )
            assertEquals(
                expected = 0.99f,
                actual = mouseZoomNode.convertAddScale(3f).format(2)
            )
            assertEquals(
                expected = -0.99f,
                actual = mouseZoomNode.convertAddScale(-3f).format(2)
            )

            zoomable.mouseWheelScaleScrollDeltaConverter = { it * 0.5f }
            assertEquals(
                expected = true,
                actual = zoomable.reverseMouseWheelScale
            )
            assertEquals(
                expected = 1.5f,
                actual = mouseZoomNode.convertAddScale(3f).format(2)
            )
            assertEquals(
                expected = -1.5f,
                actual = mouseZoomNode.convertAddScale(-3f).format(2)
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testContentPoint() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 6.0f,
                actual = zoomable.transform.scaleX.format(2)
            )

            val mouseZoomNode = MouseZoomNode(zoomable)
            assertEquals(
                expected = "Offset(33.3, 768.0)",
                actual = mouseZoomNode.contentPoint(Offset(200f, 300f)).toString()
            )
            assertEquals(
                expected = "Offset(16.6, 784.7)",
                actual = mouseZoomNode.contentPoint(Offset(100f, 400f)).toString()
            )
        }
    }
}