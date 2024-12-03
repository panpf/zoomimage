package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.compose.util.format
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
            assertEquals(expected = 2f, actual = shortPressStepScaleFactor)
            assertEquals(expected = 0.25f, actual = longPressStep)
            assertEquals(expected = 0.5f, actual = longPressAccelerateBase)
            assertEquals(expected = 500, actual = longPressAccelerateInterval)
        }
        ScaleKeyHandler(
            keyMatchers = DefaultScaleOutKeyMatchers,
            scaleIn = false,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        ).apply {
            assertEquals(expected = DefaultScaleOutKeyMatchers, actual = keyMatchers)
            assertEquals(expected = false, actual = scaleIn)
            assertEquals(expected = 3f, actual = shortPressStepScaleFactor)
            assertEquals(expected = 0.5f, actual = longPressStep)
            assertEquals(expected = 1f, actual = longPressAccelerateBase)
            assertEquals(expected = 1000, actual = longPressAccelerateInterval)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testGetShortPressStep() {
        val scaleInKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true)
        val scaleOutKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = false)
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 0.34f,
                actual = zoomable.transform.scaleX.format(2)
            )
            assertEquals(
                expected = true,
                actual = scaleInKeyHandler.scaleIn
            )
            assertEquals(
                expected = 0.34f,
                actual = scaleInKeyHandler.getShortPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = false,
                actual = scaleOutKeyHandler.scaleIn
            )
            assertEquals(
                expected = -0.17f,
                actual = scaleOutKeyHandler.getShortPressStep(zoomable).format(2)
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testGetLongPressStep() {
        val scaleInKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true)
        val scaleOutKeyHandler =
            ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = false)
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 0.34f,
                actual = zoomable.transform.scaleX.format(2)
            )
            assertEquals(
                expected = true,
                actual = scaleInKeyHandler.scaleIn
            )
            assertEquals(
                expected = 0.25f,
                actual = scaleInKeyHandler.getLongPressStep(zoomable).format(2)
            )
            assertEquals(
                expected = false,
                actual = scaleOutKeyHandler.scaleIn
            )
            assertEquals(
                expected = -0.25f,
                actual = scaleOutKeyHandler.getLongPressStep(zoomable).format(2)
            )
        }
    }

    @Test
    fun testHandle() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        val zoomableState = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
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
                actual = zoomable.transform.scaleX.format(2)
            )
        }
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    scaleInKeyHandler.updateValue(zoomable, animationSpec = null, add = -0.5f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 5.5f,
                actual = zoomable.transform.scaleX.format(2)
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
                expected = 6.5f,
                actual = zoomable.transform.scaleX.format(2)
            )
        }
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    scaleOutKeyHandler.updateValue(zoomable, animationSpec = null, add = -0.5f)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = 5.5f,
                actual = zoomable.transform.scaleX.format(2)
            )
        }
    }

    @Test
    fun testEqualsAndCode() {
        val scaleKeyHandler1 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler12 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler2 = ScaleKeyHandler(
            keyMatchers = DefaultScaleOutKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler3 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = false,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler4 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 4f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler5 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 1f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler6 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 2f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler7 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 2f,
            longPressAccelerateInterval = 2000,
        )

        assertEquals(expected = scaleKeyHandler1, actual = scaleKeyHandler12)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler2)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler3)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler4)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler1, actual = scaleKeyHandler7)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler3)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler4)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler2, actual = scaleKeyHandler7)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler4)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler3, actual = scaleKeyHandler7)
        assertNotEquals(illegal = scaleKeyHandler4, actual = scaleKeyHandler5)
        assertNotEquals(illegal = scaleKeyHandler4, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler4, actual = scaleKeyHandler7)
        assertNotEquals(illegal = scaleKeyHandler5, actual = scaleKeyHandler6)
        assertNotEquals(illegal = scaleKeyHandler5, actual = scaleKeyHandler7)
        assertNotEquals(illegal = scaleKeyHandler6, actual = scaleKeyHandler7)

        assertEquals(expected = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler12.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler2.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler3.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler4.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler1.hashCode(), actual = scaleKeyHandler7.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler3.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler4.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler2.hashCode(), actual = scaleKeyHandler7.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler4.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler3.hashCode(), actual = scaleKeyHandler7.hashCode())
        assertNotEquals(illegal = scaleKeyHandler4.hashCode(), actual = scaleKeyHandler5.hashCode())
        assertNotEquals(illegal = scaleKeyHandler4.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler4.hashCode(), actual = scaleKeyHandler7.hashCode())
        assertNotEquals(illegal = scaleKeyHandler5.hashCode(), actual = scaleKeyHandler6.hashCode())
        assertNotEquals(illegal = scaleKeyHandler5.hashCode(), actual = scaleKeyHandler7.hashCode())
        assertNotEquals(illegal = scaleKeyHandler6.hashCode(), actual = scaleKeyHandler7.hashCode())
    }

    @Test
    fun testToString() {
        val scaleKeyHandler1 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = true,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        val scaleKeyHandler2 = ScaleKeyHandler(
            keyMatchers = DefaultScaleInKeyMatchers,
            scaleIn = false,
            shortPressStepScaleFactor = 3f,
            longPressStep = 0.5f,
            longPressAccelerateBase = 1f,
            longPressAccelerateInterval = 1000,
        )
        assertEquals(
            expected = "ScaleKeyHandler(" +
                    "keyMatchers=${DefaultScaleInKeyMatchers}, " +
                    "scaleIn=true, " +
                    "shortPressStepScaleFactor=3.0, " +
                    "longPressStep=0.5, " +
                    "longPressAccelerateBase=1.0, " +
                    "longPressAccelerateInterval=1000)",
            actual = scaleKeyHandler1.toString()
        )
        assertEquals(
            expected = "ScaleKeyHandler(" +
                    "keyMatchers=${DefaultScaleInKeyMatchers}, " +
                    "scaleIn=false, " +
                    "shortPressStepScaleFactor=3.0, " +
                    "longPressStep=0.5, " +
                    "longPressAccelerateBase=1.0, " +
                    "longPressAccelerateInterval=1000)",
            actual = scaleKeyHandler2.toString()
        )
    }
}