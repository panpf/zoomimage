package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveLeftKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.MoveArrow
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler
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

class StepMatcherZoomKeyHandler {

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
                keyMatchers = DefaultMoveLeftKeyMatchers,
                arrow = MoveArrow.Left,
            )
            assertEquals(0, keyHandler.values.size)
            assertEquals(0, keyHandler.durations.size)

            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyDown)
            )
            waitMillis(30)
            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyUp)
            )
            assertEquals(listOf(170.28f), keyHandler.values.map { it.format(2) })
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
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyDown)
            )
            waitMillis(30)
            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyUp)
            )
            assertContentEquals(
                expected = listOf(170.28f, 170.28f),
                actual = keyHandler.values.map { it.format(2) }
            )
            assertTrue(
                actual = keyHandler.durations.last() in 0..zoomable.animationSpec.durationMillis,
                message = "durations=${keyHandler.durations}"
            )

            waitMillis(zoomable.animationSpec.durationMillis.toLong() + 100)

            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyDown)
            )
            waitMillis(30)
            keyHandler.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyUp)
            )
            assertContentEquals(
                expected = listOf(170.28f, 170.28f, 170.28f),
                actual = keyHandler.values.map { it.format(2) }
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
            keyMatchers = DefaultMoveLeftKeyMatchers,
            arrow = MoveArrow.Left,
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
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyDown)
            )

            // If the interval between pressing and lifting is greater than 200 milliseconds, it will be treated as a long press.
            waitMillis(2000)

            keyHandler1.handle(
                coroutineScope = coroutineScope,
                zoomableState = zoomable,
                event = KeyEvent(Key.DirectionLeft, KeyEventType.KeyUp)
            )
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
    }

    private class TestMoveKeyHandler(
        keyMatchers: ImmutableList<KeyMatcher>,
        arrow: MoveArrow,
    ) : MoveKeyHandler(keyMatchers, arrow) {

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