package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntRect
import com.github.panpf.zoomimage.compose.util.div
import com.github.panpf.zoomimage.compose.util.flip
import com.github.panpf.zoomimage.compose.util.limitTo
import com.github.panpf.zoomimage.compose.util.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.util.rotateInSpace
import com.github.panpf.zoomimage.compose.util.round
import com.github.panpf.zoomimage.compose.util.times
import com.github.panpf.zoomimage.compose.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ComposePlatformUtilsRectTest {

    @Test
    fun testToShortString() {
        assertEquals(
            "[10.34x9.77,600.32x500.91]",
            Rect(10.342f, 9.765f, 600.321f, 500.906f).toShortString()
        )
        assertEquals(
            "[9.77x10.34,500.91x600.32]",
            Rect(9.765f, 10.342f, 500.906f, 600.321f).toShortString()
        )
    }

    @Test
    fun testRound() {
        assertEquals(
            IntRect(10, 9, 600, 501),
            Rect(10.342f, 8.765f, 600.321f, 500.906f).round()
        )
        assertEquals(
            IntRect(9, 10, 501, 600),
            Rect(8.765f, 10.342f, 500.906f, 600.321f).round()
        )
    }

    @Test
    fun testTimes() {
        assertEquals(
            "[42.9x37.1,1989.9x2687.1]",
            (Rect(13f, 7f, 603f, 507f) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[68.9x23.1,3195.9x1673.1]",
            (Rect(13f, 7f, 603f, 507f) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )

        assertEquals(
            "[42.9x23.1,1989.9x1673.1]",
            (Rect(13f, 7f, 603f, 507f) * 3.3f).toShortString()
        )
        assertEquals(
            "[68.9x37.1,3195.9x2687.1]",
            (Rect(13f, 7f, 603f, 507f) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "[17.27x14.91,182.73x95.66]",
            (Rect(57f, 79f, 603f, 507f) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[10.75x23.94,113.77x153.64]",
            (Rect(57f, 79f, 603f, 507f) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
        assertEquals(
            "[17.27x23.94,182.73x153.64]",
            (Rect(57f, 79f, 603f, 507f) / 3.3f).toShortString()
        )
        assertEquals(
            "[10.75x14.91,113.77x95.66]",
            (Rect(57f, 79f, 603f, 507f) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 800.4f),
            Rect(600.5f, 200.2f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            Rect(300.6f, 200.2f, 1000.9f, 800.4f),
            Rect(200.2f, 200.2f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            Rect(1200.5f, 200.2f, 1000.9f, 800.4f),
            Rect(1300.1f, 200.2f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            Rect(600.5f, 100.3f, 1000.9f, 800.4f),
            Rect(600.5f, 50.4f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            Rect(600.5f, 900.6f, 1000.9f, 800.4f),
            Rect(600.5f, 1000.9f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            Rect(600.5f, 200.2f, 300.6f, 800.4f),
            Rect(600.5f, 200.2f, 200.2f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            Rect(600.5f, 200.2f, 1200.5f, 800.4f),
            Rect(600.5f, 200.2f, 1300.1f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 100.3f),
            Rect(600.5f, 200.2f, 1000.9f, 50.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 900.6f),
            Rect(600.5f, 200.2f, 1000.9f, 1000.9f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 800.4f),
            Rect(600.5f, 200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        assertEquals(
            Rect(0f, 200.2f, 1000.9f, 800.4f),
            Rect(-600.5f, 200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        assertEquals(
            Rect(1200.5f, 200.2f, 1000.9f, 800.4f),
            Rect(1300.1f, 200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        assertEquals(
            Rect(600.5f, 0f, 1000.9f, 800.4f),
            Rect(600.5f, -200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        assertEquals(
            Rect(600.5f, 900.6f, 1000.9f, 800.4f),
            Rect(600.5f, 1000.9f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        assertEquals(
            Rect(600.5f, 200.2f, 0f, 800.4f),
            Rect(600.5f, 200.2f, -1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        assertEquals(
            Rect(600.5f, 200.2f, 1200.5f, 800.4f),
            Rect(600.5f, 200.2f, 1300.1f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 0f),
            Rect(600.5f, 200.2f, 1000.9f, -800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 900.6f),
            Rect(600.5f, 200.2f, 1000.9f, 1000.9f).limitTo(Size(1200.5f, 900.6f))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = Size(1000f, 700f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Rect(left = 100.2f, top = 200.7f, right = 600.9f, bottom = 500.4f).toShortString(),
                actual = Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Rect(left = 199.6f, top = 100.2f, right = 499.3f, bottom = 600.9f).toShortString(),
                actual = Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Rect(left = 399.1f, top = 199.6f, right = 899.8f, bottom = 499.3f).toShortString(),
                actual = Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Rect(left = 200.7f, top = 399.1f, right = 500.4f, bottom = 899.8f).toShortString(),
                actual = Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Rect(left = 100.2f, top = 200.7f, right = 600.9f, bottom = 500.4f).toShortString(),
                actual = Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = Size(1000f, 700f)
        val rect = Rect(100.2f, 200.7f, 600.9f, 500.4f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertEquals(expected = rect, actual = rotatedSize, message = "rotation: $rotation")
                assertEquals(
                    expected = rect,
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation"
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertEquals(
                    expected = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }
    }

    @Test
    fun testFlip() {
        val spaceSize = Size(1000f, 700f)
        val rect = Rect(100.2f, 200.7f, 600.9f, 400.4f)

        assertEquals(
            Rect(399.1f, 200.7f, 899.8f, 400.4f).toShortString(),
            rect.flip(spaceSize, vertical = false).toShortString()
        )
        assertEquals(
            Rect(100.2f, 299.6f, 600.9f, 499.3f),
            rect.flip(spaceSize, vertical = true)
        )
    }
}