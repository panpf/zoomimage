package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.GestureType
import kotlin.test.Test
import kotlin.test.assertEquals

class GestureTypeTest {

    @Test
    fun testValue() {
        assertEquals(1, GestureType.DRAG)
        assertEquals(2, GestureType.TWO_FINGER_SCALE)
        assertEquals(4, GestureType.ONE_FINGER_SCALE)
        assertEquals(8, GestureType.DOUBLE_TAP_SCALE)
        assertEquals(16, GestureType.MOUSE_SCROLL_SCALE)
    }

    @Test
    fun testValues() {
        assertEquals(listOf(1, 2, 4, 8, 16), GestureType.values)
    }

    @Test
    fun testName() {
        assertEquals("DRAG", GestureType.name(GestureType.DRAG))
        assertEquals("TWO_FINGER_SCALE", GestureType.name(GestureType.TWO_FINGER_SCALE))
        assertEquals("ONE_FINGER_SCALE", GestureType.name(GestureType.ONE_FINGER_SCALE))
        assertEquals("DOUBLE_TAP_SCALE", GestureType.name(GestureType.DOUBLE_TAP_SCALE))
        assertEquals("MOUSE_SCROLL_SCALE", GestureType.name(GestureType.MOUSE_SCROLL_SCALE))
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
            expected = listOf("DRAG"),
            actual = GestureType.parse(GestureType.DRAG)
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
            expected = listOf("MOUSE_SCROLL_SCALE"),
            actual = GestureType.parse(GestureType.MOUSE_SCROLL_SCALE)
                .map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("DRAG", "TWO_FINGER_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DRAG", "ONE_FINGER_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.ONE_FINGER_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DRAG", "DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.DOUBLE_TAP_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DRAG", "MOUSE_SCROLL_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.MOUSE_SCROLL_SCALE
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DRAG", "TWO_FINGER_SCALE", "DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DRAG", "TWO_FINGER_SCALE", "MOUSE_SCROLL_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.MOUSE_SCROLL_SCALE
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf("DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE", "DOUBLE_TAP_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
            ).map { GestureType.name(it) }
        )
        assertEquals(
            expected = listOf("DRAG", "TWO_FINGER_SCALE", "ONE_FINGER_SCALE", "MOUSE_SCROLL_SCALE"),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.MOUSE_SCROLL_SCALE
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_SCROLL_SCALE"
            ),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_SCROLL_SCALE
            ).map { GestureType.name(it) }
        )

        assertEquals(
            expected = listOf(
                "DRAG",
                "TWO_FINGER_SCALE",
                "ONE_FINGER_SCALE",
                "DOUBLE_TAP_SCALE",
                "MOUSE_SCROLL_SCALE"
            ),
            actual = GestureType.parse(
                GestureType.DRAG
                        or GestureType.TWO_FINGER_SCALE
                        or GestureType.ONE_FINGER_SCALE
                        or GestureType.DOUBLE_TAP_SCALE
                        or GestureType.MOUSE_SCROLL_SCALE
                        or GestureType.DOUBLE_TAP_SCALE * 2
            ).map { GestureType.name(it) }
        )
    }
}