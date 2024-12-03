package com.github.panpf.zoomimage.compose.common.test.zoom.internal

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.compose.util.AssistKey.Shift
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.internal.MatcherZoomKeyHandler
import com.github.panpf.zoomimage.test.eventADown
import com.github.panpf.zoomimage.test.eventAUp
import com.github.panpf.zoomimage.test.eventBUp
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZoomKeyHandlerTest {

    @Test
    fun testMatcherZoomKeyHandler() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext)
        val zoomableState = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        val callbacks = mutableListOf<String>()
        val keyHandler = object : MatcherZoomKeyHandler(
            keyMatchers = listOf(
                KeyMatcher(Key.A, Shift, KeyEventType.KeyUp),
                KeyMatcher(Key.B, Shift, KeyEventType.KeyUp)
            ),
        ) {
            override fun onKey(
                coroutineScope: CoroutineScope,
                zoomableState: ZoomableState,
                event: KeyEvent
            ) {
                callbacks.add("onKey")
            }

            override fun onCanceled(
                coroutineScope: CoroutineScope,
                zoomableState: ZoomableState,
                event: KeyEvent
            ) {
                callbacks.add("onCanceled")
            }
        }
        assertEquals(expected = listOf(), actual = callbacks)

        assertFalse(keyHandler.handle(coroutineScope, zoomableState, eventADown()))
        assertEquals(expected = listOf(), actual = callbacks)

        assertFalse(keyHandler.handle(coroutineScope, zoomableState, eventAUp()))
        assertEquals(expected = listOf(), actual = callbacks)

        assertTrue(keyHandler.handle(coroutineScope, zoomableState, eventAUp(shift = true)))
        assertEquals(expected = listOf("onKey"), actual = callbacks)

        assertTrue(keyHandler.handle(coroutineScope, zoomableState, eventBUp(shift = true)))
        assertEquals(expected = listOf("onKey", "onKey"), actual = callbacks)

        assertFalse(
            keyHandler.handle(
                coroutineScope,
                zoomableState,
                eventBUp(shift = true, alt = true)
            )
        )
        assertEquals(expected = listOf("onKey", "onKey", "onCanceled"), actual = callbacks)

        assertFalse(
            keyHandler.handle(
                coroutineScope,
                zoomableState,
                eventAUp(shift = true, alt = true)
            )
        )
        assertEquals(expected = listOf("onKey", "onKey", "onCanceled"), actual = callbacks)
    }
}