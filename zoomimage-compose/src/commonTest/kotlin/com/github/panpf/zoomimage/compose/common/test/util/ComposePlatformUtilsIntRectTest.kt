package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.div
import com.github.panpf.zoomimage.compose.util.flip
import com.github.panpf.zoomimage.compose.util.limitTo
import com.github.panpf.zoomimage.compose.util.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.util.rotateInSpace
import com.github.panpf.zoomimage.compose.util.times
import com.github.panpf.zoomimage.compose.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ComposePlatformUtilsIntRectTest {

    @Test
    fun testToShortString() {
        assertEquals("[10x9,600x500]", IntRect(10, 9, 600, 500).toShortString())
        assertEquals("[9x10,500x600]", IntRect(9, 10, 500, 600).toShortString())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "[43x37,1980x2650]",
            (IntRect(13, 7, 600, 500) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[69x23,3180x1650]",
            (IntRect(13, 7, 600, 500) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )

        assertEquals(
            "[43x23,1980x1650]",
            (IntRect(13, 7, 600, 500) * 3.3f).toShortString()
        )
        assertEquals(
            "[69x37,3180x2650]",
            (IntRect(13, 7, 600, 500) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "[13x7,600x500]",
            (IntRect(43, 37, 1980, 2650) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[8x11,374x803]",
            (IntRect(43, 37, 1980, 2650) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
        assertEquals(
            "[13x11,600x803]",
            (IntRect(43, 37, 1980, 2650) / 3.3f).toShortString()
        )
        assertEquals(
            "[8x7,374x500]",
            (IntRect(43, 37, 1980, 2650) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        assertEquals(
            IntRect(600, 200, 1000, 800),
            IntRect(600, 200, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        assertEquals(
            IntRect(300, 200, 1000, 800),
            IntRect(200, 200, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )
        assertEquals(
            IntRect(1200, 200, 1000, 800),
            IntRect(1300, 200, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        assertEquals(
            IntRect(600, 100, 1000, 800),
            IntRect(600, 50, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )
        assertEquals(
            IntRect(600, 900, 1000, 800),
            IntRect(600, 1000, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        assertEquals(
            IntRect(600, 200, 300, 800),
            IntRect(600, 200, 200, 800).limitTo(IntRect(300, 100, 1200, 900))
        )
        assertEquals(
            IntRect(600, 200, 1200, 800),
            IntRect(600, 200, 1300, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        assertEquals(
            IntRect(600, 200, 1000, 100),
            IntRect(600, 200, 1000, 50).limitTo(IntRect(300, 100, 1200, 900))
        )
        assertEquals(
            IntRect(600, 200, 1000, 900),
            IntRect(600, 200, 1000, 1000).limitTo(IntRect(300, 100, 1200, 900))
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            IntRect(600, 200, 1000, 800),
            IntRect(600, 200, 1000, 800).limitTo(IntSize(1200, 900))
        )

        assertEquals(
            IntRect(0, 200, 1000, 800),
            IntRect(-600, 200, 1000, 800).limitTo(IntSize(1200, 900))
        )
        assertEquals(
            IntRect(1200, 200, 1000, 800),
            IntRect(1300, 200, 1000, 800).limitTo(IntSize(1200, 900))
        )

        assertEquals(
            IntRect(600, 0, 1000, 800),
            IntRect(600, -200, 1000, 800).limitTo(IntSize(1200, 900))
        )
        assertEquals(
            IntRect(600, 900, 1000, 800),
            IntRect(600, 1000, 1000, 800).limitTo(IntSize(1200, 900))
        )

        assertEquals(
            IntRect(600, 200, 0, 800),
            IntRect(600, 200, -1000, 800).limitTo(IntSize(1200, 900))
        )
        assertEquals(
            IntRect(600, 200, 1200, 800),
            IntRect(600, 200, 1300, 800).limitTo(IntSize(1200, 900))
        )

        assertEquals(
            IntRect(600, 200, 1000, 0),
            IntRect(600, 200, 1000, -800).limitTo(IntSize(1200, 900))
        )
        assertEquals(
            IntRect(600, 200, 1000, 900),
            IntRect(600, 200, 1000, 1000).limitTo(IntSize(1200, 900))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSize(1000, 700)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRect(left = 100, top = 200, right = 600, bottom = 500),
                actual = IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRect(left = 200, top = 100, right = 500, bottom = 600),
                actual = IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRect(left = 400, top = 200, right = 900, bottom = 500),
                actual = IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRect(left = 200, top = 400, right = 500, bottom = 900),
                actual = IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntRect(left = 100, top = 200, right = 600, bottom = 500),
                actual = IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = IntSize(1000, 700)
        val rect = IntRect(100, 200, 600, 500)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360)
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

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360)
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
        val spaceSize = IntSize(1000, 700)
        val rect = IntRect(100, 200, 600, 400)

        assertEquals(
            IntRect(400, 200, 900, 400),
            rect.flip(spaceSize, vertical = false)
        )
        assertEquals(
            IntRect(100, 300, 600, 500),
            rect.flip(spaceSize, vertical = true)
        )
    }
}