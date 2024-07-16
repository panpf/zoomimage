package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.flip
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class IntRectCompatTest {

    @Test
    fun testToShortString() {
        assertEquals("[10x9,600x500]", IntRectCompat(10, 9, 600, 500).toShortString())
        assertEquals("[9x10,500x600]", IntRectCompat(9, 10, 500, 600).toShortString())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "[43x37,1980x2650]",
            (IntRectCompat(13, 7, 600, 500) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[69x23,3180x1650]",
            (IntRectCompat(13, 7, 600, 500) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )

        assertEquals(
            "[43x23,1980x1650]",
            (IntRectCompat(13, 7, 600, 500) * 3.3f).toShortString()
        )
        assertEquals(
            "[69x37,3180x2650]",
            (IntRectCompat(13, 7, 600, 500) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "[13x7,600x500]",
            (IntRectCompat(43, 37, 1980, 2650) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[8x11,374x803]",
            (IntRectCompat(43, 37, 1980, 2650) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
        assertEquals(
            "[13x11,600x803]",
            (IntRectCompat(43, 37, 1980, 2650) / 3.3f).toShortString()
        )
        assertEquals(
            "[8x7,374x500]",
            (IntRectCompat(43, 37, 1980, 2650) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        assertEquals(
            IntRectCompat(600, 200, 1000, 800),
            IntRectCompat(600, 200, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        assertEquals(
            IntRectCompat(300, 200, 1000, 800),
            IntRectCompat(200, 200, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        assertEquals(
            IntRectCompat(1200, 200, 1000, 800),
            IntRectCompat(1300, 200, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        assertEquals(
            IntRectCompat(600, 100, 1000, 800),
            IntRectCompat(600, 50, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        assertEquals(
            IntRectCompat(600, 900, 1000, 800),
            IntRectCompat(600, 1000, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        assertEquals(
            IntRectCompat(600, 200, 300, 800),
            IntRectCompat(600, 200, 200, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        assertEquals(
            IntRectCompat(600, 200, 1200, 800),
            IntRectCompat(600, 200, 1300, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        assertEquals(
            IntRectCompat(600, 200, 1000, 100),
            IntRectCompat(600, 200, 1000, 50).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        assertEquals(
            IntRectCompat(600, 200, 1000, 900),
            IntRectCompat(600, 200, 1000, 1000).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            IntRectCompat(600, 200, 1000, 800),
            IntRectCompat(600, 200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )

        assertEquals(
            IntRectCompat(0, 200, 1000, 800),
            IntRectCompat(-600, 200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )
        assertEquals(
            IntRectCompat(1200, 200, 1000, 800),
            IntRectCompat(1300, 200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )

        assertEquals(
            IntRectCompat(600, 0, 1000, 800),
            IntRectCompat(600, -200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )
        assertEquals(
            IntRectCompat(600, 900, 1000, 800),
            IntRectCompat(600, 1000, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )

        assertEquals(
            IntRectCompat(600, 200, 0, 800),
            IntRectCompat(600, 200, -1000, 800).limitTo(IntSizeCompat(1200, 900))
        )
        assertEquals(
            IntRectCompat(600, 200, 1200, 800),
            IntRectCompat(600, 200, 1300, 800).limitTo(IntSizeCompat(1200, 900))
        )

        assertEquals(
            IntRectCompat(600, 200, 1000, 0),
            IntRectCompat(600, 200, 1000, -800).limitTo(IntSizeCompat(1200, 900))
        )
        assertEquals(
            IntRectCompat(600, 200, 1000, 900),
            IntRectCompat(600, 200, 1000, 1000).limitTo(IntSizeCompat(1200, 900))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 700)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRectCompat(100, 200, 600, 500),
                actual = IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRectCompat(200, 100, 500, 600),
                actual = IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRectCompat(400, 200, 900, 500),
                actual = IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRectCompat(200, 400, 500, 900),
                actual = IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRectCompat(100, 200, 600, 500),
                actual = IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotate() {
        val spaceSize = IntSizeCompat(1000, 700)
        val rect = IntRectCompat(100, 200, 600, 500)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertEquals(
                    expected = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation"
                )
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
                    expected = rect,
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation),
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
                    expected = rect,
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation),
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
                    expected = rect,
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation),
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
                    expected = rect,
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation),
                    message = "rotation: $rotation",
                )
            }
    }

    @Test
    fun testFlip() {
        val spaceSize = IntSizeCompat(1000, 700)
        val rect = IntRectCompat(100, 200, 600, 400)

        assertEquals(
            IntRectCompat(400, 200, 900, 400),
            rect.flip(spaceSize, vertical = false)
        )
        assertEquals(
            IntRectCompat(100, 300, 600, 500),
            rect.flip(spaceSize, vertical = true)
        )
    }
}