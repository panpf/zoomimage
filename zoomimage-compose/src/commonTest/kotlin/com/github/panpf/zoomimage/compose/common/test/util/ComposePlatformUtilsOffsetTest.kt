package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.zoomimage.compose.util.div
import com.github.panpf.zoomimage.compose.util.isEmpty
import com.github.panpf.zoomimage.compose.util.limitTo
import com.github.panpf.zoomimage.compose.util.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.util.rotateInSpace
import com.github.panpf.zoomimage.compose.util.times
import com.github.panpf.zoomimage.compose.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ComposePlatformUtilsOffsetTest {

    @Test
    fun testToShortString() {
        assertEquals("10.34x9.57", Offset(10.342f, 9.567f).toShortString())
        assertEquals("9.57x10.34", Offset(9.567f, 10.342f).toShortString())
        assertEquals("Unspecified", Offset.Unspecified.toShortString())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "42.9x37.1",
            (Offset(13f, 7f) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "68.9x23.1",
            (Offset(13f, 7f) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "13.03x6.98",
            (Offset(43f, 37f) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "8.11x11.21",
            (Offset(43f, 37f) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = Size(1000f, 500f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Offset(x = 600.6f, y = 200.4f).toShortString(),
                actual = Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Offset(x = 299.6f, y = 600.6f).toShortString(),
                actual = Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Offset(x = 399.4f, y = 299.6f).toShortString(),
                actual = Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Offset(x = 200.4f, y = 399.4f).toShortString(),
                actual = Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = Offset(x = 600.6f, y = 200.4f).toShortString(),
                actual = Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString(),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = Size(1000f, 500f)
        val offset = Offset(600.6f, 200.4f)

        listOf(90, 180, 270, -90, -180, -270)
            .forEach { rotation ->
                val rotatedOffset = offset.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = offset,
                    actual = rotatedOffset,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = offset,
                    actual = rotatedOffset.reverseRotateInSpace(spaceSize, rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(90, 180, 270, -90, -180, -270)
            .map { if (it > 0) it + 360 else it - 360 }
            .forEach { rotation ->
                val rotatedOffset = offset.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = offset,
                    actual = rotatedOffset,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = offset,
                    actual = rotatedOffset.reverseRotateInSpace(spaceSize, rotation),
                    message = "rotation: $rotation",
                )
            }
    }

    @Test
    fun testLimitToRect() {
        assertEquals(
            Offset(600.4f, 200.7f),
            Offset(600.4f, 200.7f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )

        assertEquals(
            Offset(200.4f, 200.7f),
            Offset(100f, 200.7f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )
        assertEquals(
            Offset(700.9f, 200.7f),
            Offset(800.4f, 200.7f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )

        assertEquals(
            Offset(600.4f, 100.3f),
            Offset(600.4f, 50.6f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )
        assertEquals(
            Offset(600.4f, 300.5f),
            Offset(600.4f, 400.9f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            Offset(600.4f, 200.7f),
            Offset(600.4f, 200.7f).limitTo(Size(700.9f, 300.5f))
        )

        assertEquals(
            Offset(0f, 200.7f),
            Offset(-100.2f, 200.7f).limitTo(Size(700.9f, 300.5f))
        )
        assertEquals(
            Offset(700.9f, 200.7f),
            Offset(800.4f, 200.7f).limitTo(Size(700.9f, 300.5f))
        )

        assertEquals(
            Offset(600.4f, 0f),
            Offset(600.4f, -100.2f).limitTo(Size(700.9f, 300.5f))
        )
        assertEquals(
            Offset(600.4f, 300.5f),
            Offset(600.4f, 400.9f).limitTo(Size(700.9f, 300.5f))
        )
    }

    @Test
    fun testIsEmpty() {
        assertEquals(expected = true, actual = Offset(0f, 0f).isEmpty())
        assertEquals(expected = true, actual = Offset(-0f, -0f).isEmpty())
        assertEquals(expected = true, actual = Offset(0.004f, 0.004f).isEmpty())
        assertEquals(expected = false, actual = Offset(0.006f, 0.006f).isEmpty())
    }
}