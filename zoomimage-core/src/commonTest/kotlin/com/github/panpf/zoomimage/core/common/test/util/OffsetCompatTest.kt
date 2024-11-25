package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class OffsetCompatTest {

    @Test
    fun testIsEmpty() {
        assertEquals(expected = true, actual = OffsetCompat(0f, 0f).isEmpty())
        assertEquals(expected = true, actual = OffsetCompat(-0f, -0f).isEmpty())
        assertEquals(expected = true, actual = OffsetCompat(0.004f, 0.004f).isEmpty())
        assertEquals(expected = false, actual = OffsetCompat(0.006f, 0.006f).isEmpty())
    }

    @Test
    fun testToShortString() {
        assertEquals("10.34x9.57", OffsetCompat(10.342f, 9.567f).toShortString())
        assertEquals("9.57x10.34", OffsetCompat(9.567f, 10.342f).toShortString())
        assertEquals("Unspecified", OffsetCompat.Unspecified.toShortString())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "42.9x37.1",
            (OffsetCompat(13f, 7f) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "68.9x23.1",
            (OffsetCompat(13f, 7f) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "13.03x6.98",
            (OffsetCompat(43f, 37f) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "8.11x11.21",
            (OffsetCompat(43f, 37f) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 500f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = OffsetCompat(x = 600.6f, y = 200.4f).toShortString(),
                actual = OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = OffsetCompat(x = 299.6f, y = 600.6f).toShortString(),
                actual = OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = OffsetCompat(x = 399.4f, y = 299.6f).toShortString(),
                actual = OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = OffsetCompat(x = 200.4f, y = 399.4f).toShortString(),
                actual = OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = OffsetCompat(x = 600.6f, y = 200.4f).toShortString(),
                actual = OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 500f)
        val offset = OffsetCompat(600.6f, 200.4f)

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
            OffsetCompat(600.4f, 200.7f),
            OffsetCompat(600.4f, 200.7f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )

        assertEquals(
            OffsetCompat(200.4f, 200.7f),
            OffsetCompat(100f, 200.7f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )
        assertEquals(
            OffsetCompat(700.9f, 200.7f),
            OffsetCompat(800.4f, 200.7f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )

        assertEquals(
            OffsetCompat(600.4f, 100.3f),
            OffsetCompat(600.4f, 50.6f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )
        assertEquals(
            OffsetCompat(600.4f, 300.5f),
            OffsetCompat(600.4f, 400.9f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            OffsetCompat(600.4f, 200.7f),
            OffsetCompat(600.4f, 200.7f).limitTo(SizeCompat(700.9f, 300.5f))
        )

        assertEquals(
            OffsetCompat(0f, 200.7f),
            OffsetCompat(-100.2f, 200.7f).limitTo(SizeCompat(700.9f, 300.5f))
        )
        assertEquals(
            OffsetCompat(700.9f, 200.7f),
            OffsetCompat(800.4f, 200.7f).limitTo(SizeCompat(700.9f, 300.5f))
        )

        assertEquals(
            OffsetCompat(600.4f, 0f),
            OffsetCompat(600.4f, -100.2f).limitTo(SizeCompat(700.9f, 300.5f))
        )
        assertEquals(
            OffsetCompat(600.4f, 300.5f),
            OffsetCompat(600.4f, 400.9f).limitTo(SizeCompat(700.9f, 300.5f))
        )
    }
}