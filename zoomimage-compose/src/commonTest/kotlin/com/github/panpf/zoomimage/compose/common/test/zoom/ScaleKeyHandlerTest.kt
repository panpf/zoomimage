package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleInKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleOutKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.ScaleKeyHandler
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

class ScaleKeyHandlerTest {

    @Test
    fun testConstructor() {
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
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testGetValueGetValueRangeGetShortStepMinValue() {
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
    }

    @Test
    fun testHandle() {
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
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testUpdateValue() {
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
                actual = scaleInKeyHandler.getValue(zoomable).format(2)
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
                actual = scaleOutKeyHandler.getValue(zoomable).format(2)
            )
        }
    }

    @Test
    fun testEqualsAndCode() {
        val scaleKeyHandler1 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler12 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler2 = ScaleKeyHandler(
            keyMatchers = DefaultScaleOutKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler3 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = false,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler4 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 10,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler5 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 5000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler6 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = false,
        )

        assertEquals(expected = scaleKeyHandler1, actual = scaleKeyHandler12)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler2)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler3)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler4)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler3)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler4)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler4)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler4, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler4, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler5, actual = scaleKeyHandler6)

        assertEquals(expected = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler12.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler2.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler3.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler4.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler3.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler4.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler4.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler4.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler4.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler5.hashCode(), actual = scaleKeyHandler6.hashCode())
    }

    @Test
    fun testToString() {
        val scaleKeyHandler1 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        val scaleKeyHandler2 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = false,
            shortPressReachedMaxValueNumber = 5,
            longPressReachedMaxValueDuration = 3000,
            longPressAccelerate = true,
        )
        assertEquals(
            expected = "ScaleKeyHandler(keyMatchers=${DefaultScaleInKeyMatchers}, scaleIn=true, shortPressReachedMaxValueNumber=5, longPressReachedMaxValueDuration=3000, longPressAccelerate=true)",
            actual = scaleKeyHandler1.toString()
        )
        assertEquals(
            expected = "ScaleKeyHandler(keyMatchers=${DefaultScaleInKeyMatchers}, scaleIn=false, shortPressReachedMaxValueNumber=5, longPressReachedMaxValueDuration=3000, longPressAccelerate=true)",
            actual = scaleKeyHandler2.toString()
        )
    }
}