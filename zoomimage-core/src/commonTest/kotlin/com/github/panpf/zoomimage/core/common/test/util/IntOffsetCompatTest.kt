package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class IntOffsetCompatTest {

    @Test
    fun testToShortString() {
        assertEquals("10x9", IntOffsetCompat(10, 9).toShortString())
        assertEquals("9x10", IntOffsetCompat(9, 10).toShortString())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "43x37",
            (IntOffsetCompat(13, 7) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "69x23",
            (IntOffsetCompat(13, 7) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "13x7",
            (IntOffsetCompat(43, 37) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "8x11",
            (IntOffsetCompat(43, 37) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 500)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffsetCompat(x = 600, y = 200),
                actual = IntOffsetCompat(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffsetCompat(x = 300, y = 600),
                actual = IntOffsetCompat(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffsetCompat(x = 400, y = 300),
                actual = IntOffsetCompat(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffsetCompat(x = 200, y = 400),
                actual = IntOffsetCompat(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffsetCompat(x = 600, y = 200),
                actual = IntOffsetCompat(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffsetCompat(600, 200).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 500)
        val offset = IntOffsetCompat(600, 200)

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
            IntOffsetCompat(600, 200),
            IntOffsetCompat(600, 200).limitTo(IntRectCompat(200, 100, 700, 300))
        )

        assertEquals(
            IntOffsetCompat(200, 200),
            IntOffsetCompat(100, 200).limitTo(IntRectCompat(200, 100, 700, 300))
        )
        assertEquals(
            IntOffsetCompat(700, 200),
            IntOffsetCompat(800, 200).limitTo(IntRectCompat(200, 100, 700, 300))
        )

        assertEquals(
            IntOffsetCompat(600, 100),
            IntOffsetCompat(600, 50).limitTo(IntRectCompat(200, 100, 700, 300))
        )
        assertEquals(
            IntOffsetCompat(600, 300),
            IntOffsetCompat(600, 400).limitTo(IntRectCompat(200, 100, 700, 300))
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            IntOffsetCompat(600, 200),
            IntOffsetCompat(600, 200).limitTo(IntSizeCompat(700, 300))
        )

        assertEquals(
            IntOffsetCompat(0, 200),
            IntOffsetCompat(-100, 200).limitTo(IntSizeCompat(700, 300))
        )
        assertEquals(
            IntOffsetCompat(700, 200),
            IntOffsetCompat(800, 200).limitTo(IntSizeCompat(700, 300))
        )

        assertEquals(
            IntOffsetCompat(600, 0),
            IntOffsetCompat(600, -100).limitTo(IntSizeCompat(700, 300))
        )
        assertEquals(
            IntOffsetCompat(600, 300),
            IntOffsetCompat(600, 400).limitTo(IntSizeCompat(700, 300))
        )
    }
}