package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.GestureType
import kotlin.test.Test
import kotlin.test.assertEquals

class GestureTypeTest {

    @Test
    fun testValue() {
        assertEquals(1, GestureType.ONE_FINGER_DRAG)
        assertEquals(2, GestureType.TWO_FINGER_SCALE)
        assertEquals(4, GestureType.ONE_FINGER_SCALE)
        assertEquals(8, GestureType.DOUBLE_TAP_SCALE)
        assertEquals(16, GestureType.MOUSE_WHEEL_SCALE)
        assertEquals(32, GestureType.KEYBOARD_SCALE)
        assertEquals(64, GestureType.KEYBOARD_DRAG)
    }

    @Test
    fun testValues() {
        assertEquals(listOf(1, 2, 4, 8, 16, 32, 64), GestureType.values)
    }

    @Test
    fun testName() {
        assertEquals("ONE_FINGER_DRAG", GestureType.name(GestureType.ONE_FINGER_DRAG))
        assertEquals("TWO_FINGER_SCALE", GestureType.name(GestureType.TWO_FINGER_SCALE))
        assertEquals("ONE_FINGER_SCALE", GestureType.name(GestureType.ONE_FINGER_SCALE))
        assertEquals("DOUBLE_TAP_SCALE", GestureType.name(GestureType.DOUBLE_TAP_SCALE))
        assertEquals("MOUSE_WHEEL_SCALE", GestureType.name(GestureType.MOUSE_WHEEL_SCALE))
        assertEquals("KEYBOARD_SCALE", GestureType.name(GestureType.KEYBOARD_SCALE))
        assertEquals("KEYBOARD_DRAG", GestureType.name(GestureType.KEYBOARD_DRAG))
        assertEquals("UNKNOWN", GestureType.name(0))
        assertEquals("UNKNOWN", GestureType.name(-1))
        assertEquals("UNKNOWN", GestureType.name(GestureType.values.last() * 2))
    }

    @Test
    fun testParse() {
        assertEquals(
            expected = listOf(),
            actual = GestureType.parse(0)
                .map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("ONE_FINGER_DRAG"),
            actual = GestureType.parse(GestureType.ONE_FINGER_DRAG)
                .map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("TWO_FINGER_SCALE"),
            actual = GestureType.parse(GestureType.TWO_FINGER_SCALE)
                .map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_SCALE"),
            actual = GestureType.parse(GestureType.ONE_FINGER_SCALE)
                .map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(GestureType.DOUBLE_TAP_SCALE)
                .map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("MOUSE_WHEEL_SCALE"),
            actual = GestureType.parse(GestureType.MOUSE_WHEEL_SCALE)
                .map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("KEYBOARD_SCALE"),
            actual = GestureType.parse(GestureType.KEYBOARD_SCALE)
                .map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("KEYBOARD_DRAG"),
            actual = GestureType.parse(GestureType.KEYBOARD_DRAG)
                .map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "ONE_FINGER_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.ONE_FINGER_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.DOUBLE_TAP_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "MOUSE_WHEEL_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.MOUSE_WHEEL_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "KEYBOARD_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.KEYBOARD_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "KEYBOARD_DRAG"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "MOUSE_WHEEL_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "KEYBOARD_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.KEYBOARD_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "KEYBOARD_DRAG"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE", "DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE", "MOUSE_WHEEL_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE", "KEYBOARD_SCALE"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.KEYBOARD_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("ONE_FINGER_DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE", "KEYBOARD_DRAG"),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_WHEEL_SCALE"
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "KEYBOARD_SCALE"
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.KEYBOARD_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "KEYBOARD_DRAG"
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_WHEEL_SCALE",
                "KEYBOARD_SCALE"
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
                        or GestureType.KEYBOARD_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_WHEEL_SCALE",
                "KEYBOARD_DRAG"
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_WHEEL_SCALE",
                "KEYBOARD_SCALE",
                "KEYBOARD_DRAG",
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
                        or GestureType.KEYBOARD_SCALE
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_WHEEL_SCALE",
                "KEYBOARD_SCALE",
                "KEYBOARD_DRAG",
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
                        or GestureType.KEYBOARD_SCALE
                        or GestureType.KEYBOARD_DRAG
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "ONE_FINGER_DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_WHEEL_SCALE",
                "KEYBOARD_SCALE",
                "KEYBOARD_DRAG",
            ),
            actual = GestureType.parse(
                GestureType.ONE_FINGER_DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_WHEEL_SCALE
                        or GestureType.KEYBOARD_SCALE
                        or GestureType.KEYBOARD_DRAG
                        or GestureType.KEYBOARD_DRAG * 2
            ).map { GestureType.name(it) }
        )
    }
}