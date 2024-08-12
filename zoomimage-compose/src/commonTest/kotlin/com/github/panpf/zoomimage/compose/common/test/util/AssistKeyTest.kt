package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.AssistKey.Alt
import com.github.panpf.zoomimage.compose.util.AssistKey.Ctrl
import com.github.panpf.zoomimage.compose.util.AssistKey.Meta
import com.github.panpf.zoomimage.compose.util.AssistKey.Shift
import com.github.panpf.zoomimage.test.KeyEvent
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AssistKeyTest {

    @Test
    fun testCheck() {
        listOf(
            Item(Ctrl, expected = false, ctrl = false, meta = false, alt = false, shift = false),
            Item(Ctrl, expected = true, ctrl = true, meta = false, alt = false, shift = false),
            Item(Ctrl, expected = false, ctrl = false, meta = true, alt = false, shift = false),
            Item(Ctrl, expected = false, ctrl = false, meta = false, alt = true, shift = false),
            Item(Ctrl, expected = false, ctrl = false, meta = false, alt = false, shift = true),
            Item(Ctrl, expected = true, ctrl = true, meta = true, alt = false, shift = false),
            Item(Ctrl, expected = false, ctrl = false, meta = true, alt = true, shift = false),
            Item(Ctrl, expected = false, ctrl = false, meta = false, alt = true, shift = true),
            Item(Ctrl, expected = true, ctrl = true, meta = false, alt = true, shift = false),
            Item(Ctrl, expected = false, ctrl = false, meta = true, alt = false, shift = true),
            Item(Ctrl, expected = true, ctrl = true, meta = false, alt = false, shift = true),
            Item(Ctrl, expected = true, ctrl = true, meta = true, alt = true, shift = false),
            Item(Ctrl, expected = true, ctrl = true, meta = true, alt = false, shift = true),
            Item(Ctrl, expected = true, ctrl = true, meta = false, alt = true, shift = true),
            Item(Ctrl, expected = false, ctrl = false, meta = true, alt = true, shift = true),
            Item(Ctrl, expected = true, ctrl = true, meta = true, alt = true, shift = true),

            Item(Meta, expected = false, ctrl = false, meta = false, alt = false, shift = false),
            Item(Meta, expected = false, ctrl = true, meta = false, alt = false, shift = false),
            Item(Meta, expected = true, ctrl = false, meta = true, alt = false, shift = false),
            Item(Meta, expected = false, ctrl = false, meta = false, alt = true, shift = false),
            Item(Meta, expected = false, ctrl = false, meta = false, alt = false, shift = true),
            Item(Meta, expected = true, ctrl = true, meta = true, alt = false, shift = false),
            Item(Meta, expected = true, ctrl = false, meta = true, alt = true, shift = false),
            Item(Meta, expected = false, ctrl = false, meta = false, alt = true, shift = true),
            Item(Meta, expected = false, ctrl = true, meta = false, alt = true, shift = false),
            Item(Meta, expected = true, ctrl = false, meta = true, alt = false, shift = true),
            Item(Meta, expected = false, ctrl = true, meta = false, alt = false, shift = true),
            Item(Meta, expected = true, ctrl = true, meta = true, alt = true, shift = false),
            Item(Meta, expected = true, ctrl = true, meta = true, alt = false, shift = true),
            Item(Meta, expected = false, ctrl = true, meta = false, alt = true, shift = true),
            Item(Meta, expected = true, ctrl = false, meta = true, alt = true, shift = true),
            Item(Meta, expected = true, ctrl = true, meta = true, alt = true, shift = true),

            Item(Alt, expected = false, ctrl = false, meta = false, alt = false, shift = false),
            Item(Alt, expected = false, ctrl = true, meta = false, alt = false, shift = false),
            Item(Alt, expected = false, ctrl = false, meta = true, alt = false, shift = false),
            Item(Alt, expected = true, ctrl = false, meta = false, alt = true, shift = false),
            Item(Alt, expected = false, ctrl = false, meta = false, alt = false, shift = true),
            Item(Alt, expected = false, ctrl = true, meta = true, alt = false, shift = false),
            Item(Alt, expected = true, ctrl = false, meta = true, alt = true, shift = false),
            Item(Alt, expected = true, ctrl = false, meta = false, alt = true, shift = true),
            Item(Alt, expected = true, ctrl = true, meta = false, alt = true, shift = false),
            Item(Alt, expected = false, ctrl = false, meta = true, alt = false, shift = true),
            Item(Alt, expected = false, ctrl = true, meta = false, alt = false, shift = true),
            Item(Alt, expected = true, ctrl = true, meta = true, alt = true, shift = false),
            Item(Alt, expected = false, ctrl = true, meta = true, alt = false, shift = true),
            Item(Alt, expected = true, ctrl = true, meta = false, alt = true, shift = true),
            Item(Alt, expected = true, ctrl = false, meta = true, alt = true, shift = true),
            Item(Alt, expected = true, ctrl = true, meta = true, alt = true, shift = true),

            Item(Shift, expected = false, ctrl = false, meta = false, alt = false, shift = false),
            Item(Shift, expected = false, ctrl = true, meta = false, alt = false, shift = false),
            Item(Shift, expected = false, ctrl = false, meta = true, alt = false, shift = false),
            Item(Shift, expected = false, ctrl = false, meta = false, alt = true, shift = false),
            Item(Shift, expected = true, ctrl = false, meta = false, alt = false, shift = true),
            Item(Shift, expected = false, ctrl = true, meta = true, alt = false, shift = false),
            Item(Shift, expected = false, ctrl = false, meta = true, alt = true, shift = false),
            Item(Shift, expected = true, ctrl = false, meta = false, alt = true, shift = true),
            Item(Shift, expected = false, ctrl = true, meta = false, alt = true, shift = false),
            Item(Shift, expected = true, ctrl = false, meta = true, alt = false, shift = true),
            Item(Shift, expected = true, ctrl = true, meta = false, alt = false, shift = true),
            Item(Shift, expected = false, ctrl = true, meta = true, alt = true, shift = false),
            Item(Shift, expected = true, ctrl = true, meta = true, alt = false, shift = true),
            Item(Shift, expected = true, ctrl = true, meta = false, alt = true, shift = true),
            Item(Shift, expected = true, ctrl = false, meta = true, alt = true, shift = true),
            Item(Shift, expected = true, ctrl = true, meta = true, alt = true, shift = true),
        ).forEach { (assistKey, expected, ctrl, meta, alt, shift) ->
            val keyEvent1 = KeyEvent(Key.A, KeyEventType.KeyUp, 0, ctrl, meta, alt, shift)
            assertEquals(
                expected = expected,
                actual = assistKey.check(keyEvent1),
                message = "assistKey=$assistKey, ctrl=$ctrl, meta=$meta, alt=$alt, shift=$shift"
            )

            val keyEvent2 = KeyEvent(Key.A, KeyEventType.KeyDown, 0, ctrl, meta, alt, shift)
            assertEquals(
                expected = expected,
                actual = assistKey.check(keyEvent2),
                message = "assistKey=$assistKey, ctrl=$ctrl, meta=$meta, alt=$alt, shift=$shift"
            )

            val keyEvent3 = KeyEvent(Key.B, KeyEventType.KeyUp, 0, ctrl, meta, alt, shift)
            assertEquals(
                expected = expected,
                actual = assistKey.check(keyEvent3),
                message = "assistKey=$assistKey, ctrl=$ctrl, meta=$meta, alt=$alt, shift=$shift"
            )
        }
    }

    @Test
    fun testPlus() {
        assertContentEquals(expected = arrayOf(Ctrl, Meta), actual = Ctrl + Meta)
        assertContentEquals(expected = arrayOf(Ctrl, Meta, Alt), actual = Ctrl + Meta + Alt)
        assertContentEquals(
            expected = arrayOf(Ctrl, Meta, Alt, Shift),
            actual = Ctrl + Meta + Alt + Shift
        )
    }

    private data class Item(
        val assistKey: AssistKey,
        val expected: Boolean,
        val ctrl: Boolean = false,
        val meta: Boolean = false,
        val alt: Boolean = false,
        val shift: Boolean = false,
    )
}