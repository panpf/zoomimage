package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveDownKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveLeftKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveRightKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveUpKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.MoveArrow
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler
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
            moveArrow = MoveArrow.Up
        ).apply {
            assertEquals(expected = DefaultMoveUpKeyMatchers, actual = keyMatchers)
            assertEquals(expected = MoveArrow.Up, actual = moveArrow)
            assertEquals(expected = 0.33f, actual = shortPressStepWithContainerPercentage)
            assertEquals(expected = 0.075f, actual = longPressStepWithContainerPercentage)
            assertEquals(expected = 0.5f, actual = longPressAccelerateBase)
            assertEquals(expected = 500, actual = longPressAccelerateInterval)
        }
        MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            moveArrow = MoveArrow.Down,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        ).apply {
            assertEquals(expected = DefaultMoveDownKeyMatchers, actual = keyMatchers)
            assertEquals(expected = MoveArrow.Down, actual = moveArrow)
            assertEquals(expected = 0.66f, actual = shortPressStepWithContainerPercentage)
            assertEquals(expected = 0.15f, actual = longPressStepWithContainerPercentage)
            assertEquals(expected = 1f, actual = longPressAccelerateBase)
            assertEquals(expected = 1000, actual = longPressAccelerateInterval)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testGetShortPressStep() {
        val upMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up
        )
        val downMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            moveArrow = MoveArrow.Down
        )
        val leftMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveLeftKeyMatchers,
            moveArrow = MoveArrow.Left
        )
        val rightMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            moveArrow = MoveArrow.Right
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
                expected = "Offset(-515.4, -13440.0)",
                actual = zoomable.transform.offset.toString()
            )

            assertEquals(
                expected = MoveArrow.Up,
                actual = upMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = 170.28f,
                actual = upMoveKeyHandler.getShortPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = MoveArrow.Down,
                actual = downMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = -170.28f,
                actual = downMoveKeyHandler.getShortPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = MoveArrow.Left,
                actual = leftMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = 170.28f,
                actual = leftMoveKeyHandler.getShortPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = MoveArrow.Right,
                actual = rightMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = -170.28f,
                actual = rightMoveKeyHandler.getShortPressStep(zoomable).format(2)
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testGetLongPressStep() {
        val upMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up
        )
        val downMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            moveArrow = MoveArrow.Down
        )
        val leftMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveLeftKeyMatchers,
            moveArrow = MoveArrow.Left
        )
        val rightMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            moveArrow = MoveArrow.Right
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
                expected = "Offset(-515.4, -13440.0)",
                actual = zoomable.transform.offset.toString()
            )

            assertEquals(
                expected = MoveArrow.Up,
                actual = upMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = 38.7f,
                actual = upMoveKeyHandler.getLongPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = MoveArrow.Down,
                actual = downMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = -38.7f,
                actual = downMoveKeyHandler.getLongPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = MoveArrow.Left,
                actual = leftMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = 38.7f,
                actual = leftMoveKeyHandler.getLongPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = MoveArrow.Right,
                actual = rightMoveKeyHandler.moveArrow,
            )
            assertEquals(
                expected = -38.7f,
                actual = rightMoveKeyHandler.getLongPressStep(zoomable).format(2)
            )
        }
    }

    @Test
    fun testHandle() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        val zoomableState = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        val scaleKeyHandler2 =
            MoveKeyHandler(keyMatchers = DefaultMoveUpKeyMatchers, moveArrow = MoveArrow.Up)
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
            moveArrow = MoveArrow.Up
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
                actual = zoomable.transform.offset.y.format(2)
            )
        }
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    upMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = -100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13540.0f,
                actual = zoomable.transform.offset.y.format(2)
            )
        }

        val downMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            moveArrow = MoveArrow.Down
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
                expected = -13340.0f,
                actual = zoomable.transform.offset.y.format(2)
            )
        }
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    downMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = -100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13540.0f,
                actual = zoomable.transform.offset.y.format(2)
            )
        }

        val leftMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveLeftKeyMatchers,
            moveArrow = MoveArrow.Left
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
                actual = zoomable.transform.offset.x.format(2)
            )
        }
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    leftMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = -100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -615.42f,
                actual = zoomable.transform.offset.x.format(2)
            )
        }

        val rightMoveKeyHandler = MoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            moveArrow = MoveArrow.Right
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
                expected = -415.42f,
                actual = zoomable.transform.offset.x.format(2)
            )
        }
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    rightMoveKeyHandler.updateValue(zoomable, animationSpec = null, add = -100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -615.42f,
                actual = zoomable.transform.offset.x.format(2)
            )
        }
    }

    @Test
    fun testEqualsAndCode() {
        val moveKeyHandler1 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler12 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler2 = MoveKeyHandler(
            keyMatchers = DefaultMoveDownKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler3 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Down,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler4 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 2f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler5 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.8f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler6 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 2f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler7 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 2f,
            longPressAccelerateInterval = 20000
        )

        assertEquals(expected = moveKeyHandler1, actual = moveKeyHandler12)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler2)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler3)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler4)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler1, actual = moveKeyHandler7)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler3)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler4)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler2, actual = moveKeyHandler7)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler4)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler3, actual = moveKeyHandler7)
        assertNotEquals(illegal = moveKeyHandler4, actual = moveKeyHandler5)
        assertNotEquals(illegal = moveKeyHandler4, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler4, actual = moveKeyHandler7)
        assertNotEquals(illegal = moveKeyHandler5, actual = moveKeyHandler6)
        assertNotEquals(illegal = moveKeyHandler5, actual = moveKeyHandler7)
        assertNotEquals(illegal = moveKeyHandler6, actual = moveKeyHandler7)

        assertEquals(expected = moveKeyHandler1.hashCode(), actual = moveKeyHandler12.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler2.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler3.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler4.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler1.hashCode(), actual = moveKeyHandler7.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler3.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler4.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler2.hashCode(), actual = moveKeyHandler7.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler4.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler3.hashCode(), actual = moveKeyHandler7.hashCode())
        assertNotEquals(illegal = moveKeyHandler4.hashCode(), actual = moveKeyHandler5.hashCode())
        assertNotEquals(illegal = moveKeyHandler4.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler4.hashCode(), actual = moveKeyHandler7.hashCode())
        assertNotEquals(illegal = moveKeyHandler5.hashCode(), actual = moveKeyHandler6.hashCode())
        assertNotEquals(illegal = moveKeyHandler5.hashCode(), actual = moveKeyHandler7.hashCode())
        assertNotEquals(illegal = moveKeyHandler6.hashCode(), actual = moveKeyHandler7.hashCode())
    }

    @Test
    fun testToString() {
        val moveKeyHandler1 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Up,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        val moveKeyHandler2 = MoveKeyHandler(
            keyMatchers = DefaultMoveUpKeyMatchers,
            moveArrow = MoveArrow.Down,
            shortPressStepWithContainerPercentage = 0.66f,
            longPressStepWithContainerPercentage = 0.15f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000
        )
        assertEquals(
            expected = "MoveKeyHandler(" +
                    "keyMatchers=${DefaultMoveUpKeyMatchers}, " +
                    "moveArrow=Up, " +
                    "shortPressStepWithContainerPercentage=0.66, " +
                    "longPressStepWithContainerPercentage=0.15, " +
                    "longPressAccelerateBase=1.0, " +
                    "longPressAccelerateInterval=1000)",
            actual = moveKeyHandler1.toString()
        )
        assertEquals(
            expected = "MoveKeyHandler(" +
                    "keyMatchers=${DefaultMoveUpKeyMatchers}, " +
                    "moveArrow=Down, " +
                    "shortPressStepWithContainerPercentage=0.66, " +
                    "longPressStepWithContainerPercentage=0.15, " +
                    "longPressAccelerateBase=1.0, " +
                    "longPressAccelerateInterval=1000)",
            actual = moveKeyHandler2.toString()
        )
    }
}