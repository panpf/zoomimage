package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import com.github.panpf.zoomimage.compose.util.AssistKey.Shift
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.matcherKeyHandler
import com.github.panpf.zoomimage.test.eventADown
import com.github.panpf.zoomimage.test.eventAUp
import com.github.panpf.zoomimage.test.eventBUp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MatcherKeyHandlerTest {

    @Test
    fun test() {
        val callbacks = mutableListOf<String>()
        val keyHandler = matcherKeyHandler(
            keyMatchers = listOf(
                KeyMatcher(Key.A, Shift, KeyEventType.KeyUp),
                KeyMatcher(Key.B, Shift, KeyEventType.KeyUp)
            ),
            onCanceled = { callbacks.add("onCanceled") },
            onKey = { callbacks.add("onKey") }
        )
        assertEquals(expected = listOf(), actual = callbacks)

        assertFalse(keyHandler.handle(eventADown()))
        assertEquals(expected = listOf(), actual = callbacks)

        assertFalse(keyHandler.handle(eventAUp()))
        assertEquals(expected = listOf(), actual = callbacks)

        assertTrue(keyHandler.handle(eventAUp(shift = true)))
        assertEquals(expected = listOf("onKey"), actual = callbacks)

        assertTrue(keyHandler.handle(eventBUp(shift = true)))
        assertEquals(expected = listOf("onKey", "onKey"), actual = callbacks)

        assertFalse(keyHandler.handle(eventBUp(shift = true, alt = true)))
        assertEquals(expected = listOf("onKey", "onKey", "onCanceled"), actual = callbacks)

        assertFalse(keyHandler.handle(eventAUp(shift = true, alt = true)))
        assertEquals(expected = listOf("onKey", "onKey", "onCanceled"), actual = callbacks)
    }
}