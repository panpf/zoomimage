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
import com.github.panpf.zoomimage.test.waitMillis
import com.github.panpf.zoomimage.zoom.DefaultMouseWheelScaleCalculator
import com.github.panpf.zoomimage.zoom.MouseWheelScaleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@Suppress("OPT_IN_USAGE")
class MouseZoomTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testCalculateScale() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.setContainerSize(IntSize(516, 516))
                zoomable.setContentSize(IntSize(86, 1522))
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = 6.0f, actual = zoomable.transform.scaleX.format(2))

            val mouseZoomNode = MouseZoomNode(zoomable)

            assertEquals(expected = false, actual = zoomable.reverseMouseWheelScale)
            assertNull(zoomable.mouseWheelScaleScrollDeltaConverter)
            assertEquals(MouseWheelScaleCalculator.Default, zoomable.mouseWheelScaleCalculator)
            val newScale1 = mouseZoomNode.calculateScale(3f).format(2)
            assertEquals(
                expected = MouseWheelScaleCalculator.Default.calculateScale(
                    currentScale = zoomable.transform.scaleX,
                    scrollDelta = 3f
                ).format(2),
                actual = newScale1
            )

            zoomable.setReverseMouseWheelScale(true)
            assertEquals(expected = true, actual = zoomable.reverseMouseWheelScale)
            assertNull(zoomable.mouseWheelScaleScrollDeltaConverter)
            assertEquals(MouseWheelScaleCalculator.Default, zoomable.mouseWheelScaleCalculator)
            val newScale11 = mouseZoomNode.calculateScale(3f).format(2)
            assertEquals(
                expected = MouseWheelScaleCalculator.Default.calculateScale(
                    currentScale = zoomable.transform.scaleX,
                    scrollDelta = -3f
                ).format(2),
                actual = newScale11
            )

            val myMouseWheelScaleScrollDeltaConverter: (Float) -> Float = { it * 0.33f }
            zoomable.setMouseWheelScaleScrollDeltaConverter(myMouseWheelScaleScrollDeltaConverter)

            zoomable.setReverseMouseWheelScale(false)
            assertEquals(expected = false, actual = zoomable.reverseMouseWheelScale)
            assertEquals(
                myMouseWheelScaleScrollDeltaConverter,
                zoomable.mouseWheelScaleScrollDeltaConverter
            )
            assertEquals(MouseWheelScaleCalculator.Default, zoomable.mouseWheelScaleCalculator)
            val newScale2 = mouseZoomNode.calculateScale(3f).format(2)
            assertEquals(
                expected = (zoomable.transform.scaleX - myMouseWheelScaleScrollDeltaConverter(3f)).format(
                    2
                ),
                actual = newScale2
            )

            zoomable.setReverseMouseWheelScale(true)
            assertEquals(expected = true, actual = zoomable.reverseMouseWheelScale)
            assertEquals(
                myMouseWheelScaleScrollDeltaConverter,
                zoomable.mouseWheelScaleScrollDeltaConverter
            )
            assertEquals(MouseWheelScaleCalculator.Default, zoomable.mouseWheelScaleCalculator)
            val newScale21 = mouseZoomNode.calculateScale(3f).format(2)
            assertEquals(
                expected = (zoomable.transform.scaleX - myMouseWheelScaleScrollDeltaConverter(-3f)).format(
                    2
                ),
                actual = newScale21
            )

            val myMouseWheelScaleCalculator = DefaultMouseWheelScaleCalculator(
                stepScrollDelta = 0.5f,
                stepScaleFactor = 0.5f
            )
            zoomable.setMouseWheelScaleCalculator(myMouseWheelScaleCalculator)

            zoomable.setReverseMouseWheelScale(false)
            assertEquals(expected = false, actual = zoomable.reverseMouseWheelScale)
            assertEquals(
                myMouseWheelScaleScrollDeltaConverter,
                zoomable.mouseWheelScaleScrollDeltaConverter
            )
            assertEquals(myMouseWheelScaleCalculator, zoomable.mouseWheelScaleCalculator)
            val newScale3 = mouseZoomNode.calculateScale(3f).format(2)
            assertEquals(
                expected = myMouseWheelScaleCalculator.calculateScale(
                    currentScale = zoomable.transform.scaleX,
                    scrollDelta = 3f
                ).format(2).format(2),
                actual = newScale3
            )

            zoomable.setReverseMouseWheelScale(true)
            assertEquals(expected = true, actual = zoomable.reverseMouseWheelScale)
            assertEquals(
                myMouseWheelScaleScrollDeltaConverter,
                zoomable.mouseWheelScaleScrollDeltaConverter
            )
            assertEquals(myMouseWheelScaleCalculator, zoomable.mouseWheelScaleCalculator)
            val newScale31 = mouseZoomNode.calculateScale(3f).format(2)
            assertEquals(
                expected = myMouseWheelScaleCalculator.calculateScale(
                    currentScale = zoomable.transform.scaleX,
                    scrollDelta = -3f
                ).format(2).format(2),
                actual = newScale31
            )

            assertNotEquals(newScale1, newScale2)
            assertNotEquals(newScale1, newScale3)
            assertNotEquals(newScale1, newScale11)
            assertNotEquals(newScale1, newScale21)
            assertNotEquals(newScale1, newScale31)
            assertNotEquals(newScale2, newScale3)
            assertNotEquals(newScale2, newScale11)
            assertNotEquals(newScale2, newScale21)
            assertNotEquals(newScale2, newScale31)
            assertNotEquals(newScale3, newScale11)
            assertNotEquals(newScale3, newScale21)
            assertNotEquals(newScale3, newScale31)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testContentPoint() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.setContainerSize(IntSize(516, 516))
                zoomable.setContentSize(IntSize(86, 1522))
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
            GlobalScope.launch(Dispatchers.Main) {
                assertEquals(
                    expected = "Offset(33.3, 768.0)",
                    actual = mouseZoomNode.contentPoint(Offset(200f, 300f)).toString()
                )
                assertEquals(
                    expected = "Offset(16.6, 784.7)",
                    actual = mouseZoomNode.contentPoint(Offset(100f, 400f)).toString()
                )
            }
            waitMillis(100)
        }
    }
}