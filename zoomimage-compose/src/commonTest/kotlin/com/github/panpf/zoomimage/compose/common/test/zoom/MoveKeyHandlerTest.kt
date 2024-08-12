package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveDownKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveLeftKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveRightKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveUpKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleOutKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler.Arrow.Down
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler.Arrow.Up
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.test.KeyEvent
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MoveKeyHandlerTest {

    @Test
    fun testConstructor() {
        MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Up
        ).apply {
            assertEquals(expected = DefaultMoveUpKeyMatchers, actual = keyMatchers)
            assertEquals(expected = MoveKeyHandler.Arrow.Up, actual = arrow)
            assertEquals(expected = 10, actual = shortPressReachedMaxValueNumber)
            assertEquals(expected = 0.25f, actual = shortPressMinStepWithContainerPercentage)
            assertEquals(expected = 3000, actual = longPressReachedMaxValueDuration)
            assertEquals(expected = true, actual = longPressAccelerate)
        }
        MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Down,
            shortPressReachedMaxValueNumber = 20,
            shortPressMinStepWithContainerPercentage = 0.1f,
            longPressReachedMaxValueDuration = 6000,
            longPressAccelerate = false
        ).apply {
            assertEquals(expected = DefaultMoveDownKeyMatchers, actual = keyMatchers)
            assertEquals(expected = MoveKeyHandler.Arrow.Down, actual = arrow)
            assertEquals(expected = 20, actual = shortPressReachedMaxValueNumber)
            assertEquals(expected = 0.1f, actual = shortPressMinStepWithContainerPercentage)
            assertEquals(expected = 6000, actual = longPressReachedMaxValueDuration)
            assertEquals(expected = false, actual = longPressAccelerate)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testGetValueGetValueRangeGetShortStepMinValue() {
        val upMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Up
        )
        val downMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Down
        )
        val leftMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveLeftKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Left
        )
        val rightMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Right
        )
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13440.0f,
                actual = upMoveKeyHandler.getValue(zoomable).format(2)
            )
            assertEquals(
                expected = -13440.0f,
                actual = downMoveKeyHandler.getValue(zoomable).format(2)
            )
            assertEquals(
                expected = -515.42f,
                actual = leftMoveKeyHandler.getValue(zoomable).format(2)
            )
            assertEquals(
                expected = -515.42f,
                actual = rightMoveKeyHandler.getValue(zoomable).format(2)
            )

            assertEquals(
                expected = "-26880.0 .. 0.0",
                actual = upMoveKeyHandler.getValueRange(zoomable)
                    .let { "${it.start.format(2)} .. ${it.endInclusive.format(2)}" }
            )
            assertEquals(
                expected = "-26880.0 .. 0.0",
                actual = downMoveKeyHandler.getValueRange(zoomable)
                    .let { "${it.start.format(2)} .. ${it.endInclusive.format(2)}" }
            )
            assertEquals(
                expected = "-13987.0 .. -12955.0",
                actual = leftMoveKeyHandler.getValueRange(zoomable)
                    .let { "${it.start.format(2)} .. ${it.endInclusive.format(2)}" }
            )
            assertEquals(
                expected = "-13987.0 .. -12955.0",
                actual = rightMoveKeyHandler.getValueRange(zoomable)
                    .let { "${it.start.format(2)} .. ${it.endInclusive.format(2)}" }
            )

            assertEquals(
                expected = 129.0f,
                actual = upMoveKeyHandler.getShortStepMinValue(zoomable).format(2)
            )
            assertEquals(
                expected = 129.0f,
                actual = downMoveKeyHandler.getShortStepMinValue(zoomable).format(2)
            )
            assertEquals(
                expected = 129.0f,
                actual = leftMoveKeyHandler.getShortStepMinValue(zoomable).format(2)
            )
            assertEquals(
                expected = 129.0f,
                actual = rightMoveKeyHandler.getShortStepMinValue(zoomable).format(2)
            )
        }
    }

    @Test
    fun testHandle() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        val zoomableState = ZoomableState(Logger("Test"))
        val scaleKeyHandler2 =
            MoveKeyHandler(keyMatchers = DefaultMoveUpKeyMatchers, arrow = MoveKeyHandler.Arrow.Up)
        assertEquals(
            expected = true,
            actual = zoomableState.checkSupportGestureType(GestureType.KEYBOARD_DRAG)
        )
        assertEquals(
            expected = true,
            actual = scaleKeyHandler2.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomableState,
                event = KeyEvent(Key.DirectionUp, type = KeyEventType.KeyDown)
            )
        )

        zoomableState.disabledGestureTypes =
            zoomableState.disabledGestureTypes or GestureType.KEYBOARD_DRAG
        assertEquals(
            expected = false,
            actual = zoomableState.checkSupportGestureType(GestureType.KEYBOARD_DRAG)
        )
        assertEquals(
            expected = false,
            actual = scaleKeyHandler2.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomableState,
                event = KeyEvent(Key.DirectionUp, type = KeyEventType.KeyDown)
            )
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUpdateValue() {
        // updateValue
        val upMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Up
        )
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    upMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13340.0f,
                actual = upMoveKeyHandler.getValue(zoomable).format(2)
            )
        }

        val downMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Down
        )
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    downMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13540.0f,
                actual = downMoveKeyHandler.getValue(zoomable).format(2)
            )
        }

        val leftMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveLeftKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Left
        )
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    leftMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -415.42f,
                actual = leftMoveKeyHandler.getValue(zoomable).format(2)
            )
        }

        val rightMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            arrow = MoveKeyHandler.Arrow.Right
        )
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    rightMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -615.42f,
                actual = rightMoveKeyHandler.getValue(zoomable).format(2)
            )
        }
    }

    @Test
    fun testEqualsAndCode() {
        val moveKeyHandler1 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val moveKeyHandler12 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val moveKeyHandler2 = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val moveKeyHandler3 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Down,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val moveKeyHandler4 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 10,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val moveKeyHandler5 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 5000,
            longPressAccelerate = true,
        )
        val moveKeyHandler6 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = false,
        )

        assertEquals(expected = moveKeyHandler1, actual = moveKeyHandler12)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler2)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler3)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler4)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler3)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler4)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler4)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler4, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler4, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler5, actual = moveKeyHandler6)

        assertEquals(expected = moveKeyHandler1.hashCode(), actual = moveKeyHandler12.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler2.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler3.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler4.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler3.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler4.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler4.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler4.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler4.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler5.hashCode(), actual = moveKeyHandler6.hashCode())
    }

    @Test
    fun testToString() {
        val moveKeyHandler1 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Up,
            shortPressReachedMaxValueNumber = 5,
            shortPressMinStepWithContainerPercentage = 0.25f,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val moveKeyHandler2 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            arrow = Down,
            shortPressReachedMaxValueNumber = 5,
            shortPressMinStepWithContainerPercentage = 0.25f,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        assertEquals(
            expected = "MoveKeyHandler(keyMatchers=${DefaultMoveUpKeyMatchers}, arrow=Up, shortPressReachedMaxValueNumber=5, shortPressMinStepWithContainerPercentage=0.25, longPressReachedMaxValueDuration=3000, longPressAccelerate=true)",
            actual = moveKeyHandler1.toString()
        )
        assertEquals(
            expected = "MoveKeyHandler(keyMatchers=${DefaultMoveUpKeyMatchers}, arrow=Down, shortPressReachedMaxValueNumber=5, shortPressMinStepWithContainerPercentage=0.25, longPressReachedMaxValueDuration=3000, longPressAccelerate=true)",
            actual = moveKeyHandler2.toString()
        )
    }
}