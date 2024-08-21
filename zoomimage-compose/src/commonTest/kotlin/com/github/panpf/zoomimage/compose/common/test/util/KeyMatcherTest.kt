package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import com.github.panpf.zoomimage.compose.util.AssistKey.Alt
import com.github.panpf.zoomimage.compose.util.AssistKey.Ctrl
import com.github.panpf.zoomimage.compose.util.AssistKey.Meta
import com.github.panpf.zoomimage.compose.util.AssistKey.Shift
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.test.eventADown
import com.github.panpf.zoomimage.test.eventAUp
import com.github.panpf.zoomimage.test.eventBDown
import com.github.panpf.zoomimage.test.eventBUp
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class KeyMatcherTest {

    @Test
    fun testConstructor() {
        KeyMatcher(key = Key.A).apply {
            assertEquals(expected = Key.A, key)
            assertEquals(expected = null, assistKeys)
            assertEquals(expected = null, type)
        }
        KeyMatcher(key = Key.A, assistKey = Shift).apply {
            assertEquals(expected = Key.A, key)
            assertContentEquals(expected = arrayOf(Shift), assistKeys)
            assertEquals(expected = null, type)
        }
        KeyMatcher(key = Key.A, assistKeys = arrayOf(Ctrl, Alt), type = KeyEventType.KeyUp).apply {
            assertEquals(expected = Key.A, key)
            assertContentEquals(expected = arrayOf(Ctrl, Alt), assistKeys)
            assertEquals(expected = KeyEventType.KeyUp, type)
        }
    }

    @Test
    fun testMatch() {
        KeyMatcher(key = Key.A, assistKeys = null, type = null).apply {
            assertTrue(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertTrue(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKey = Ctrl, type = null).apply {
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertTrue(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertFalse(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKeys = Ctrl + Alt, type = null).apply {
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertTrue(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertFalse(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKeys = Ctrl + Alt + Shift, type = null).apply {
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertTrue(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertFalse(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKeys = Ctrl + Alt + Shift + Meta, type = null).apply {
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertTrue(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertFalse(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKeys = null, type = KeyEventType.KeyDown).apply {
            assertTrue(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertFalse(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKeys = null, type = KeyEventType.KeyUp).apply {
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertTrue(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
        KeyMatcher(key = Key.A, assistKeys = Ctrl + Shift, type = KeyEventType.KeyDown).apply {
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBDown(ctrl = false, meta = false, alt = false, shift = false)))

            assertFalse(match(eventADown(ctrl = true, meta = false, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = false, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = false)))
            assertTrue(match(eventADown(ctrl = true, meta = false, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = false, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = false)))
            assertFalse(match(eventADown(ctrl = true, meta = true, alt = false, shift = true)))
            assertFalse(match(eventADown(ctrl = true, meta = false, alt = true, shift = true)))
            assertFalse(match(eventADown(ctrl = false, meta = true, alt = true, shift = true)))

            assertFalse(match(eventADown(ctrl = true, meta = true, alt = true, shift = true)))

            assertFalse(match(eventAUp(ctrl = false, meta = false, alt = false, shift = false)))
            assertFalse(match(eventBUp(ctrl = false, meta = false, alt = false, shift = false)))
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val matcher1 = KeyMatcher(key = Key.A, assistKey = Shift, type = KeyEventType.KeyDown)
        val matcher12 = KeyMatcher(key = Key.A, assistKey = Shift, type = KeyEventType.KeyDown)
        val matcher2 = KeyMatcher(key = Key.B, assistKey = Shift, type = KeyEventType.KeyDown)
        val matcher3 = KeyMatcher(key = Key.A, assistKey = Alt, type = KeyEventType.KeyDown)
        val matcher4 = KeyMatcher(key = Key.A, assistKey = Shift, type = KeyEventType.KeyUp)
        val matcher5 = KeyMatcher(key = Key.B, assistKey = Alt, type = KeyEventType.KeyUp)

        assertEquals(expected = matcher1, actual = matcher1)
        assertEquals(expected = matcher1, actual = matcher12)
        assertNotEquals(illegal = matcher1, actual = null as Any?)
        assertNotEquals(illegal = matcher1, actual = Any())
        assertNotEquals(illegal = matcher1, actual = matcher2)
        assertNotEquals(illegal = matcher1, actual = matcher3)
        assertNotEquals(illegal = matcher1, actual = matcher4)
        assertNotEquals(illegal = matcher1, actual = matcher5)
        assertNotEquals(illegal = matcher2, actual = matcher3)
        assertNotEquals(illegal = matcher2, actual = matcher4)
        assertNotEquals(illegal = matcher2, actual = matcher5)
        assertNotEquals(illegal = matcher3, actual = matcher4)
        assertNotEquals(illegal = matcher3, actual = matcher5)
        assertNotEquals(illegal = matcher4, actual = matcher5)

        assertEquals(expected = matcher1.hashCode(), actual = matcher12.hashCode())
        assertNotEquals(illegal = matcher1.hashCode(), actual = matcher2.hashCode())
        assertNotEquals(illegal = matcher1.hashCode(), actual = matcher3.hashCode())
        assertNotEquals(illegal = matcher1.hashCode(), actual = matcher4.hashCode())
        assertNotEquals(illegal = matcher1.hashCode(), actual = matcher5.hashCode())
        assertNotEquals(illegal = matcher2.hashCode(), actual = matcher3.hashCode())
        assertNotEquals(illegal = matcher2.hashCode(), actual = matcher4.hashCode())
        assertNotEquals(illegal = matcher2.hashCode(), actual = matcher5.hashCode())
        assertNotEquals(illegal = matcher3.hashCode(), actual = matcher4.hashCode())
        assertNotEquals(illegal = matcher3.hashCode(), actual = matcher5.hashCode())
        assertNotEquals(illegal = matcher4.hashCode(), actual = matcher5.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "KeyMatcher(key=${Key.A}, assistKeys=[Shift], type=KeyDown)",
            actual = KeyMatcher(
                key = Key.A,
                assistKey = Shift,
                type = KeyEventType.KeyDown
            ).toString()
        )
        assertEquals(
            expected = "KeyMatcher(key=${Key.B}, assistKeys=[Ctrl, Alt], type=KeyDown)",
            actual = KeyMatcher(
                key = Key.B,
                assistKeys = arrayOf(Ctrl, Alt),
                type = KeyEventType.KeyDown
            ).toString()
        )
    }
}