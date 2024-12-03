package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.check
import com.github.panpf.zoomimage.zoom.isEmpty
import com.github.panpf.zoomimage.zoom.rtlFlipped
import com.github.panpf.zoomimage.zoom.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ContainerWhitespaceTest {

    @Test
    fun testConstructor() {
        assertEquals(
            expected = "ContainerWhitespace(left=1.0, top=1.0, right=1.0, bottom=1.0)",
            actual = ContainerWhitespace(size = 1f).toString()
        )

        assertEquals(
            expected = "ContainerWhitespace(left=1.0, top=2.0, right=1.0, bottom=2.0)",
            actual = ContainerWhitespace(horizontal = 1f, vertical = 2f).toString()
        )

        assertEquals(
            expected = "ContainerWhitespace(left=2.0, top=1.0, right=2.0, bottom=1.0)",
            actual = ContainerWhitespace(horizontal = 2f, vertical = 1f).toString()
        )

        assertEquals(
            expected = "ContainerWhitespace(left=4.0, top=3.0, right=2.0, bottom=1.0)",
            actual = ContainerWhitespace(left = 4f, top = 3f, right = 2f, bottom = 1f).toString()
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = ContainerWhitespace(1f)
        val element11 = element1.copy()
        val element2 = element1.copy(left = 2f)
        val element3 = element1.copy(top = 2f)
        val element4 = element1.copy(right = 2f)
        val element5 = element1.copy(bottom = 2f)

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = element3)
        assertNotEquals(illegal = element1, actual = element4)
        assertNotEquals(illegal = element1, actual = element5)
        assertNotEquals(illegal = element2, actual = element3)
        assertNotEquals(illegal = element2, actual = element4)
        assertNotEquals(illegal = element2, actual = element5)
        assertNotEquals(illegal = element3, actual = element4)
        assertNotEquals(illegal = element3, actual = element5)
        assertNotEquals(illegal = element4, actual = element5)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element3.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element4.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element3.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element4.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element3.hashCode(), actual = element4.hashCode())
        assertNotEquals(illegal = element3.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element4.hashCode(), actual = element5.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "ContainerWhitespace(left=4.0, top=3.0, right=2.0, bottom=1.0)",
            actual = ContainerWhitespace(left = 4f, top = 3f, right = 2f, bottom = 1f).toString()
        )
    }

    @Test
    fun testZero() {
        assertEquals(expected = 0f, actual = ContainerWhitespace.Zero.left)
        assertEquals(expected = 0f, actual = ContainerWhitespace.Zero.top)
        assertEquals(expected = 0f, actual = ContainerWhitespace.Zero.right)
        assertEquals(expected = 0f, actual = ContainerWhitespace.Zero.bottom)
    }

    @Test
    fun testCheck() {
        assertEquals(expected = true, actual = ContainerWhitespace.Zero.check())
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(left = -1f).check())
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(top = -1f).check())
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(right = -1f).check())
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(bottom = -1f).check())
    }

    @Test
    fun testIsEmpty() {
        assertEquals(expected = true, actual = ContainerWhitespace.Zero.isEmpty())
        assertEquals(expected = true, actual = ContainerWhitespace.Zero.copy(left = -1f).isEmpty())
        assertEquals(expected = true, actual = ContainerWhitespace.Zero.copy(top = -1f).isEmpty())
        assertEquals(expected = true, actual = ContainerWhitespace.Zero.copy(right = -1f).isEmpty())
        assertEquals(
            expected = true,
            actual = ContainerWhitespace.Zero.copy(bottom = -1f).isEmpty()
        )
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(left = 1f).isEmpty())
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(top = 1f).isEmpty())
        assertEquals(expected = false, actual = ContainerWhitespace.Zero.copy(right = 1f).isEmpty())
        assertEquals(
            expected = false,
            actual = ContainerWhitespace.Zero.copy(bottom = 1f).isEmpty()
        )
    }

    @Test
    fun testToShortString() {
        assertEquals(
            "[10.34x9.77,600.32x500.91]",
            ContainerWhitespace(10.342f, 9.765f, 600.321f, 500.906f).toShortString()
        )
        assertEquals(
            "[9.77x10.34,500.91x600.32]",
            ContainerWhitespace(9.765f, 10.342f, 500.906f, 600.321f).toShortString()
        )
    }

    @Test
    fun testRtlFlipped() {
        assertEquals(
            expected = ContainerWhitespace(left = 3f, top = 2f, right = 1f, bottom = 4f),
            actual = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f).rtlFlipped()
        )
    }
}