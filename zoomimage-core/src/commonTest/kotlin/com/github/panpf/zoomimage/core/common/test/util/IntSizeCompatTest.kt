package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.copy
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.isSameAspectRatio
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.reverseRotate
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IntSizeCompatTest {

    @Test
    fun testToShortString() {
        assertEquals("10x9", IntSizeCompat(10, 9).toShortString())
        assertEquals("9x10", IntSizeCompat(9, 10).toShortString())
    }

    @Test
    fun testIsEmpty() {
        assertFalse(IntSizeCompat(10, 9).isEmpty())
        assertTrue(IntSizeCompat(0, 9).isEmpty())
        assertTrue(IntSizeCompat(10, 0).isEmpty())
        assertTrue(IntSizeCompat(0, 0).isEmpty())
        assertTrue(IntSizeCompat(-1, 9).isEmpty())
        assertTrue(IntSizeCompat(10, -1).isEmpty())
        assertTrue(IntSizeCompat(-1, -1).isEmpty())
    }

    @Test
    fun testIsNotEmpty() {
        assertTrue(IntSizeCompat(10, 9).isNotEmpty())
        assertFalse(IntSizeCompat(0, 9).isNotEmpty())
        assertFalse(IntSizeCompat(10, 0).isNotEmpty())
        assertFalse(IntSizeCompat(0, 0).isNotEmpty())
        assertFalse(IntSizeCompat(-1, 9).isNotEmpty())
        assertFalse(IntSizeCompat(10, -1).isNotEmpty())
        assertFalse(IntSizeCompat(-1, -1).isNotEmpty())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "43x37",
            (IntSizeCompat(13, 7) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "69x23",
            (IntSizeCompat(13, 7) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )

        assertEquals(
            "43x23",
            (IntSizeCompat(13, 7) * 3.3f).toShortString()
        )
        assertEquals(
            "69x37",
            (IntSizeCompat(13, 7) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "13x7",
            (IntSizeCompat(43, 37) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "8x11",
            (IntSizeCompat(43, 37) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
        assertEquals(
            "13x11",
            (IntSizeCompat(43, 37) / 3.3f).toShortString()
        )
        assertEquals(
            "8x7",
            (IntSizeCompat(43, 37) / 5.3f).toShortString()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntSizeCompat(width = 600, height = 200),
                actual = IntSizeCompat(600, 200).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntSizeCompat(width = 200, height = 600),
                actual = IntSizeCompat(600, 200).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntSizeCompat(width = 600, height = 200),
                actual = IntSizeCompat(600, 200).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntSizeCompat(200, 600),
                actual = IntSizeCompat(600, 200).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntSizeCompat(width = 600, height = 200),
                actual = IntSizeCompat(600, 200).rotate(rotation),
                message = "rotation: $rotation",
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = IntSizeCompat(600, 200)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                assertEquals(
                    expected = size,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = size,
                    actual = rotatedSize.reverseRotate(rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                assertNotEquals(
                    illegal = size,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = size,
                    actual = rotatedSize.reverseRotate(rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                assertEquals(
                    expected = size,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = size,
                    actual = rotatedSize.reverseRotate(rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                assertNotEquals(
                    illegal = size,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = size,
                    actual = rotatedSize.reverseRotate(rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                assertEquals(
                    expected = size,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = size,
                    actual = rotatedSize.reverseRotate(rotation),
                    message = "rotation: $rotation",
                )
            }
    }

    @Test
    fun testIsSameAspectRatio() {
        val size = IntSizeCompat(600, 200)
        assertTrue(size.isSameAspectRatio(size * 2))
        assertTrue(size.isSameAspectRatio(size / 2))
        assertTrue(size.isSameAspectRatio(size * 3))

        assertFalse(size.isSameAspectRatio(size / 3))
        assertFalse(size.isSameAspectRatio((size * 2).copy(height = size.height * 2 + 1)))
        assertFalse(size.isSameAspectRatio((size / 2).copy(height = size.height * 2 + 1)))
        assertFalse(size.isSameAspectRatio((size * 3).copy(height = size.height * 2 + 1)))
        assertFalse(size.isSameAspectRatio((size / 3).copy(height = size.height * 2 + 1)))

        assertTrue(size.isSameAspectRatio(size / 3, delta = 0.1f))
        assertTrue(
            size.isSameAspectRatio(
                (size * 2).copy(height = size.height * 2 + 1),
                delta = 0.1f
            )
        )
        assertTrue(
            size.isSameAspectRatio(
                (size / 2).copy(height = size.height / 2 + 1),
                delta = 0.1f
            )
        )
        assertTrue(
            size.isSameAspectRatio(
                (size * 3).copy(height = size.height * 3 + 1),
                delta = 0.1f
            )
        )
        assertTrue(
            size.isSameAspectRatio(
                (size / 3).copy(height = size.height / 3 + 1),
                delta = 0.1f
            )
        )
    }

    @Test
    fun testLerp() {
        val size1 = IntSizeCompat(600, 200)
        val size2 = IntSizeCompat(400, 800)
        assertEquals(size1, lerp(size1, size2, 0f))
        assertEquals(IntSizeCompat(500, 500), lerp(size1, size2, 0.5f))
        assertEquals(size2, lerp(size1, size2, 1f))
    }

    @Test
    fun testCopy() {
        val size = IntSizeCompat(600, 200)
        assertEquals(size, size.copy())
        assertEquals(IntSizeCompat(100, 200), size.copy(width = 100))
        assertEquals(IntSizeCompat(600, 100), size.copy(height = 100))
        assertEquals(IntSizeCompat(500, 700), size.copy(width = 500, height = 700))
    }
}