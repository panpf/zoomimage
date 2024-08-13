package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.isNotEmpty
import com.github.panpf.zoomimage.compose.util.isSameAspectRatio
import com.github.panpf.zoomimage.compose.util.reverseRotate
import com.github.panpf.zoomimage.compose.util.rotate
import com.github.panpf.zoomimage.compose.util.round
import com.github.panpf.zoomimage.compose.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ComposePlatformUtilsSizeTest {

    @Test
    fun testToShortString() {
        assertEquals("10.34x9.57", Size(10.342f, 9.567f).toShortString())
        assertEquals("9.57x10.34", Size(9.567f, 10.342f).toShortString())
        assertEquals("Unspecified", Size.Unspecified.toShortString())
    }

    @Test
    fun testIsNotEmpty() {
        assertTrue(Size(10f, 9f).isNotEmpty())
        assertFalse(Size(0f, 9f).isNotEmpty())
        assertFalse(Size(10f, 0f).isNotEmpty())
        assertFalse(Size(0f, 0f).isNotEmpty())
        assertFalse(Size(-1f, 9f).isNotEmpty())
        assertFalse(Size(10f, -1f).isNotEmpty())
        assertFalse(Size(-1f, -1f).isNotEmpty())
    }

    @Test
    fun testRound() {
        assertEquals(
            IntSize(13, 8),
            Size(13.345f, 7.567f).round()
        )
        assertEquals(
            IntSize(8, 13),
            Size(7.567f, 13.345f).round()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Size(width = 600.4f, height = 200.7f),
                actual = Size(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Size(width = 200.7f, height = 600.4f),
                actual = Size(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Size(width = 600.4f, height = 200.7f),
                actual = Size(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Size(width = 200.7f, height = 600.4f),
                actual = Size(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Size(width = 600.4f, height = 200.7f),
                actual = Size(600.4f, 200.7f).rotate(rotation),
                message = "rotation: $rotation",
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = Size(600.4f, 200.7f)

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
        val size = Size(600.4f, 200.7f)
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