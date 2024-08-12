package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveDownKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveLeftKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveRightKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveUpKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleInKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleOutKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultZoomKeyHandlers
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler
import com.github.panpf.zoomimage.compose.zoom.ScaleKeyHandler
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.test.KeyEvent
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyZoomTest {
    // TODO test

    @Test
    fun testDefaultScaleInKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.ZoomIn),
                KeyMatcher(key = Key.Equals, assistKey = platformAssistKey()),
                KeyMatcher(key = Key.Equals, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionUp, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionUp, assistKey = platformAssistKey()),
            ).toImmutableList(),
            actual = DefaultScaleInKeyMatchers
        )
    }

    @Test
    fun testDefaultScaleOutKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.ZoomOut),
                KeyMatcher(key = Key.Minus, assistKey = platformAssistKey()),
                KeyMatcher(key = Key.Minus, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionDown, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionDown, assistKey = platformAssistKey()),
            ).toImmutableList(),
            actual = DefaultScaleOutKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveUpKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionUp),
            ).toImmutableList(),
            actual = DefaultMoveUpKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveDownKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionDown),
            ).toImmutableList(),
            actual = DefaultMoveDownKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveLeftKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionLeft),
            ).toImmutableList(),
            actual = DefaultMoveLeftKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveRightKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionRight),
            ).toImmutableList(),
            actual = DefaultMoveRightKeyMatchers
        )
    }

    @Test
    fun testDefaultZoomKeyHandlers() {
        assertEquals(
            expected = listOf(
                ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true),
                ScaleKeyHandler(keyMatchers = DefaultScaleOutKeyMatchers, scaleIn = false),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveUpKeyMatchers,
                    arrow = MoveKeyHandler.Arrow.Up
                ),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveDownKeyMatchers,
                    arrow = MoveKeyHandler.Arrow.Down
                ),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveLeftKeyMatchers,
                    arrow = MoveKeyHandler.Arrow.Left
                ),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveRightKeyMatchers,
                    arrow = MoveKeyHandler.Arrow.Right
                ),
            ).toImmutableList(),
            actual = DefaultZoomKeyHandlers
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testScaleKeyHandler() {
        // constructor
        ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true).apply {
            assertEquals(expected = DefaultScaleInKeyMatchers, actual = keyMatchers)
            assertEquals(expected = true, actual = scaleIn)
            assertEquals(expected = 5, actual = shortPressReachedMaxValueNumber)
            assertEquals(expected = 3000, actual = longPressReachedMaxValueDuration)
            assertEquals(expected = true, actual = longPressAccelerate)
        }
        ScaleKeyHandler(
            keyMatchers = DefaultScaleOutKeyMatchers,
            scaleIn = false,
            shortPressReachedMaxValueNumber = 10,
            longPressReachedMaxValueDuration = 6000,
            longPressAccelerate = false
        ).apply {
            assertEquals(expected = DefaultScaleOutKeyMatchers, actual = keyMatchers)
            assertEquals(expected = false, actual = scaleIn)
            assertEquals(expected = 10, actual = shortPressReachedMaxValueNumber)
            assertEquals(expected = 6000, actual = longPressReachedMaxValueDuration)
            assertEquals(expected = false, actual = longPressAccelerate)
        }

        // getValue, getValueRange, getShortStepMinValue
        val scaleKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true)
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
                actual = scaleKeyHandler.getValue(zoomable).format(2)
            )
            assertEquals(
                expected = "0.34 .. 18.0",
                actual = scaleKeyHandler.getValueRange(zoomable)
                    .let { "${it.start.format(2)} .. ${it.endInclusive.format(2)}" }
            )
            assertEquals(
                expected = null,
                actual = scaleKeyHandler.getShortStepMinValue(zoomable)?.format(2)
            )
        }

        // handle
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        val zoomableState = ZoomableState(Logger("Test"))
        val scaleKeyHandler2 =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true)
        assertEquals(
            expected = true,
            actual = zoomableState.checkSupportGestureType(GestureType.KEYBOARD_SCALE)
        )
        assertEquals(
            expected = true,
            actual = scaleKeyHandler2.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomableState,
                event = KeyEvent(Key.ZoomIn, type = KeyEventType.KeyDown)
            )
        )

        zoomableState.disabledGestureTypes =
            zoomableState.disabledGestureTypes or GestureType.KEYBOARD_SCALE
        assertEquals(
            expected = false,
            actual = zoomableState.checkSupportGestureType(GestureType.KEYBOARD_SCALE)
        )
        assertEquals(
            expected = false,
            actual = scaleKeyHandler2.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomableState,
                event = KeyEvent(Key.ZoomIn, type = KeyEventType.KeyDown)
            )
        )

        // updateValue
        val scaleInKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true)
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    scaleInKeyHandler.updateValue(zoomable, animationSpec = null, add = 0.5f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 6.5f,
                actual = scaleKeyHandler.getValue(zoomable).format(2)
            )
        }

        val scaleOutKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = false)
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    scaleOutKeyHandler.updateValue(zoomable, animationSpec = null, add = 0.5f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 5.5f,
                actual = scaleKeyHandler.getValue(zoomable).format(2)
            )
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testMoveKeyHandler() {
        // constructor
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

        // getValue, getValueRange, getShortStepMinValue
        val upMoveKeyHandler =
            MoveKeyHandler(keyMatchers = DefaultMoveUpKeyMatchers, arrow = MoveKeyHandler.Arrow.Up)
        val downMoveKeyHandler =
            MoveKeyHandler(
                keyMatchers = DefaultMoveDownKeyMatchers,
                arrow = MoveKeyHandler.Arrow.Down
            )
        val leftMoveKeyHandler =
            MoveKeyHandler(
                keyMatchers = DefaultMoveLeftKeyMatchers,
                arrow = MoveKeyHandler.Arrow.Left
            )
        val rightMoveKeyHandler =
            MoveKeyHandler(
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

        // handle
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

        // updateValue
        var currentScaleKeyHandler = upMoveKeyHandler
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    currentScaleKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13340.0f,
                actual = currentScaleKeyHandler.getValue(zoomable).format(2)
            )
        }

        currentScaleKeyHandler = downMoveKeyHandler
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    currentScaleKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -13540.0f,
                actual = currentScaleKeyHandler.getValue(zoomable).format(2)
            )
        }

        currentScaleKeyHandler = leftMoveKeyHandler
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    currentScaleKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -415.42f,
                actual = currentScaleKeyHandler.getValue(zoomable).format(2)
            )
        }

        currentScaleKeyHandler = rightMoveKeyHandler
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.maxScale, animated = false)
                    currentScaleKeyHandler.updateValue(zoomable, animationSpec = null, add = 100f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = -615.42f,
                actual = currentScaleKeyHandler.getValue(zoomable).format(2)
            )
        }
    }

    @Test
    fun testZoomMatcherKeyHandler() {
        // TODO test
    }
}