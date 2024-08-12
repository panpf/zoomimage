package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveRightKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler.Arrow
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.test.KeyEvent
import com.github.panpf.zoomimage.test.waitMillis
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoomMatcherKeyHandlerTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testShortPress() {
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
            assertEquals(18.0f, zoomable.transform.scaleX)

            val coroutineScope = CoroutineScope(Dispatchers.Main)
            val keyHandler = TestMoveKeyHandler(
                keyMatchers = DefaultMoveRightKeyMatchers,
                arrow = Arrow.Right,
                longPressAccelerate = true
            )
            assertEquals(0, keyHandler.values.size)
            assertEquals(0, keyHandler.durations.size)

            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyDown)
            )
            waitMillis(30)
            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyUp)
            )
            assertEquals(listOf(129.0f), keyHandler.values)
            assertEquals(
                expected = zoomable.animationSpec.durationMillis,
                actual = keyHandler.durations.last(),
                message = "durations=${keyHandler.durations}"
            )

            // When the time interval between two short presses is shorter than [zoomable.animationSpec.durationMillis],
            // the interval time is used as the animation duration.
            waitMillis(zoomable.animationSpec.durationMillis.toLong() - 100)

            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyDown)
            )
            waitMillis(30)
            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyUp)
            )
            assertContentEquals(
                expected = listOf(129.0f, 129.0f),
                actual = keyHandler.values
            )
            assertTrue(
                actual = keyHandler.durations.last() in 0..zoomable.animationSpec.durationMillis,
                message = "durations=${keyHandler.durations}"
            )

            waitMillis(zoomable.animationSpec.durationMillis.toLong() + 100)

            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyDown)
            )
            waitMillis(30)
            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyUp)
            )
            assertContentEquals(
                expected = listOf(129.0f, 129.0f, 129.0f),
                actual = keyHandler.values
            )
            assertEquals(
                expected = zoomable.animationSpec.durationMillis,
                actual = keyHandler.durations.last(),
                message = "durations=${keyHandler.durations}"
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testLongPress() {
        val keyHandler1 = TestMoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            arrow = Arrow.Right,
            longPressAccelerate = true
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
            assertEquals(18.0f, zoomable.transform.scaleX)

            val coroutineScope = CoroutineScope(Dispatchers.Main)
            assertEquals(0, keyHandler1.values.size)
            assertEquals(0, keyHandler1.durations.size)

            keyHandler1.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyDown)
            )

            // If the interval between pressing and lifting is greater than 200 milliseconds, it will be treated as a long press.
            waitMillis(2000)

            keyHandler1.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyUp)
            )
            assertEquals(true, keyHandler1.longPressAccelerate)
            assertTrue(
                actual = keyHandler1.values.size > 50,
                message = "values.size=${keyHandler1.values.size}"
            )
            assertTrue(
                actual = keyHandler1.values.last() > 8f,
                message = "values.last()=${keyHandler1.values.last()}"
            )
            assertEquals(
                expected = -1,
                actual = keyHandler1.durations.last(),
                message = "durations.last()=${keyHandler1.durations.last()}"
            )
        }

        val keyHandler2 = TestMoveKeyHandler(
            keyMatchers = DefaultMoveRightKeyMatchers,
            arrow = Arrow.Right,
            longPressAccelerate = false
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
            assertEquals(18.0f, zoomable.transform.scaleX)

            val coroutineScope = CoroutineScope(Dispatchers.Main)
            assertEquals(0, keyHandler2.values.size)
            assertEquals(0, keyHandler2.durations.size)

            keyHandler2.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyDown)
            )

            // If the interval between pressing and lifting is greater than 200 milliseconds, it will be treated as a long press.
            waitMillis(2000)

            keyHandler2.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionRight, KeyEventType.KeyUp)
            )
            assertEquals(false, keyHandler2.longPressAccelerate)
            assertTrue(
                actual = keyHandler2.values.size > 50,
                message = "values.size=${keyHandler2.values.size}"
            )
            assertTrue(
                actual = keyHandler2.values.last() < 6f,
                message = "values.last()=${keyHandler2.values.last()}"
            )
            assertEquals(
                expected = -1,
                actual = keyHandler2.durations.last(),
                message = "durations.last()=${keyHandler2.durations.last()}"
            )
        }
    }

    private class TestMoveKeyHandler(
        keyMatchers: ImmutableList<KeyMatcher>,
        arrow: Arrow,
        longPressAccelerate: Boolean,
    ) : MoveKeyHandler(keyMatchers, arrow, longPressAccelerate = longPressAccelerate) {

        val values = mutableListOf<Float>()
        val durations = mutableListOf<Int>()

        override suspend fun updateValue(
            zoomableState: ZoomableState,
            animationSpec: ZoomAnimationSpec?,
            add: Float
        ) {
            values.add(add)
            durations.add(animationSpec?.durationMillis ?: -1)
        }
    }
}