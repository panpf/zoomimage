package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.isSameAspectRatio
import com.github.panpf.zoomimage.util.reverseRotate
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SizeCompatTest {

    @Test
    fun testToShortString() {
        assertEquals("10.34x9.57", SizeCompat(10.342f, 9.567f).toShortString())
        assertEquals("9.57x10.34", SizeCompat(9.567f, 10.342f).toShortString())
        assertEquals("Unspecified", SizeCompat.Unspecified.toShortString())
    }

    @Test
    fun testIsNotEmpty() {
        assertTrue(SizeCompat(10f, 9f).isNotEmpty())
        assertFalse(SizeCompat(0f, 9f).isNotEmpty())
        assertFalse(SizeCompat(10f, 0f).isNotEmpty())
        assertFalse(SizeCompat(0f, 0f).isNotEmpty())
        assertFalse(SizeCompat(-1f, 9f).isNotEmpty())
        assertFalse(SizeCompat(10f, -1f).isNotEmpty())
        assertFalse(SizeCompat(-1f, -1f).isNotEmpty())
    }

    @Test
    fun testRound() {
        assertEquals(
            IntSizeCompat(13, 8),
            SizeCompat(13.345f, 7.567f).round()
        )
        assertEquals(
            IntSizeCompat(8, 13),
            SizeCompat(7.567f, 13.345f).round()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = SizeCompat(width = 600.4f, height = 200.7f),
                actual = SizeCompat(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = SizeCompat(width = 200.7f, height = 600.4f),
                actual = SizeCompat(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = SizeCompat(width = 600.4f, height = 200.7f),
                actual = SizeCompat(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = SizeCompat(width = 200.7f, height = 600.4f),
                actual = SizeCompat(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = SizeCompat(width = 600.4f, height = 200.7f),
                actual = SizeCompat(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = SizeCompat(600.4f, 200.7f)

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
        val size = SizeCompat(600.4f, 200.7f)
        assertTrue(size.isSameAspectRatio(size * 2f))
        assertTrue(size.isSameAspectRatio(size / 2f))
        assertFalse(size.isSameAspectRatio(size * 3f))
        assertTrue(size.isSameAspectRatio(size / 3f))

        assertFalse(size.isSameAspectRatio((size * 2f).copy(height = size.height * 2 + 1)))
        assertFalse(size.isSameAspectRatio((size / 2f).copy(height = size.height * 2 + 1)))
        assertFalse(size.isSameAspectRatio((size * 3f).copy(height = size.height * 2 + 1)))
        assertFalse(size.isSameAspectRatio((size / 3f).copy(height = size.height * 2 + 1)))

        assertTrue(
            size.isSameAspectRatio(
                (size * 2f).copy(height = size.height * 2 + 1),
                delta = 0.1f
            )
        )
        assertTrue(
            size.isSameAspectRatio(
                (size / 2f).copy(height = size.height / 2 + 1),
                delta = 0.1f
            )
        )
        assertTrue(
            size.isSameAspectRatio(
                (size * 3f).copy(height = size.height * 3 + 1),
                delta = 0.1f
            )
        )
        assertTrue(
            size.isSameAspectRatio(
                (size / 3f).copy(height = size.height / 3 + 1),
                delta = 0.1f
            )
        )
    }
}