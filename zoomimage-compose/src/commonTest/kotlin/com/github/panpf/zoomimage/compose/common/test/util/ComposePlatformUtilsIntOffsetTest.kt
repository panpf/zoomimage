package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.div
import com.github.panpf.zoomimage.compose.util.limitTo
import com.github.panpf.zoomimage.compose.util.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.util.rotateInSpace
import com.github.panpf.zoomimage.compose.util.times
import com.github.panpf.zoomimage.compose.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ComposePlatformUtilsIntOffsetTest {

    @Test
    fun testToShortString() {
        assertEquals("10x9", IntOffset(10, 9).toShortString())
        assertEquals("9x10", IntOffset(9, 10).toShortString())
    }

    @Test
    fun testTimes() {
        assertEquals(
            "43x37",
            (IntOffset(13, 7) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "69x23",
            (IntOffset(13, 7) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "13x7",
            (IntOffset(43, 37) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "8x11",
            (IntOffset(43, 37) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSize(1000, 500)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffset(x = 600, y = 200),
                actual = IntOffset(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffset(x = 300, y = 600),
                actual = IntOffset(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffset(x = 400, y = 300),
                actual = IntOffset(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffset(x = 200, y = 400),
                actual = IntOffset(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = IntOffset(x = 600, y = 200),
                actual = IntOffset(600, 200).rotateInSpace(spaceSize, rotation),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = IntSize(1000, 500)
        val offset = IntOffset(600, 200)

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
            IntOffset(600, 200),
            IntOffset(600, 200).limitTo(IntRect(200, 100, 700, 300))
        )

        assertEquals(
            IntOffset(200, 200),
            IntOffset(100, 200).limitTo(IntRect(200, 100, 700, 300))
        )
        assertEquals(
            IntOffset(700, 200),
            IntOffset(800, 200).limitTo(IntRect(200, 100, 700, 300))
        )

        assertEquals(
            IntOffset(600, 100),
            IntOffset(600, 50).limitTo(IntRect(200, 100, 700, 300))
        )
        assertEquals(
            IntOffset(600, 300),
            IntOffset(600, 400).limitTo(IntRect(200, 100, 700, 300))
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            IntOffset(600, 200),
            IntOffset(600, 200).limitTo(IntSize(700, 300))
        )

        assertEquals(
            IntOffset(0, 200),
            IntOffset(-100, 200).limitTo(IntSize(700, 300))
        )
        assertEquals(
            IntOffset(700, 200),
            IntOffset(800, 200).limitTo(IntSize(700, 300))
        )

        assertEquals(
            IntOffset(600, 0),
            IntOffset(600, -100).limitTo(IntSize(700, 300))
        )
        assertEquals(
            IntOffset(600, 300),
            IntOffset(600, 400).limitTo(IntSize(700, 300))
        )
    }
}