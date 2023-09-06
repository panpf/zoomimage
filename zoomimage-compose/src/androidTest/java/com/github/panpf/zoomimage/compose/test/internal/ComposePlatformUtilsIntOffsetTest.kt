package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.limitTo
import com.github.panpf.zoomimage.compose.internal.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.internal.rotateInSpace
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsIntOffsetTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10x9", IntOffset(10, 9).toShortString())
        Assert.assertEquals("9x10", IntOffset(9, 10).toShortString())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "43x37",
            (IntOffset(13, 7) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "69x23",
            (IntOffset(13, 7) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "13x7",
            (IntOffset(43, 37) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "8x11",
            (IntOffset(43, 37) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSize(1000, 500)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffset(600, 200),
                IntOffset(600, 200).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffset(300, 600),
                IntOffset(600, 200).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffset(400, 300),
                IntOffset(600, 200).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffset(200, 400),
                IntOffset(600, 200).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffset(600, 200),
                IntOffset(600, 200).rotateInSpace(spaceSize, rotation)
            )
        }

        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            IntOffset(600, 200).rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
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
                Assert.assertNotEquals("rotation: $rotation", offset, rotatedOffset)
                Assert.assertEquals(
                    "rotation: $rotation",
                    offset,
                    rotatedOffset.reverseRotateInSpace(spaceSize, rotation)
                )
            }

        listOf(90, 180, 270, -90, -180, -270)
            .map { if (it > 0) it + 360 else it - 360 }
            .forEach { rotation ->
                val rotatedOffset = offset.rotateInSpace(spaceSize, rotation)
                Assert.assertNotEquals("rotation: $rotation", offset, rotatedOffset)
                Assert.assertEquals(
                    "rotation: $rotation",
                    offset,
                    rotatedOffset.reverseRotateInSpace(spaceSize, rotation)
                )
            }
    }

    @Test
    fun testLimitToRect() {
        Assert.assertEquals(
            IntOffset(600, 200),
            IntOffset(600, 200).limitTo(IntRect(200, 100, 700, 300))
        )

        Assert.assertEquals(
            IntOffset(200, 200),
            IntOffset(100, 200).limitTo(IntRect(200, 100, 700, 300))
        )
        Assert.assertEquals(
            IntOffset(700, 200),
            IntOffset(800, 200).limitTo(IntRect(200, 100, 700, 300))
        )

        Assert.assertEquals(
            IntOffset(600, 100),
            IntOffset(600, 50).limitTo(IntRect(200, 100, 700, 300))
        )
        Assert.assertEquals(
            IntOffset(600, 300),
            IntOffset(600, 400).limitTo(IntRect(200, 100, 700, 300))
        )
    }

    @Test
    fun testLimitToSize() {
        Assert.assertEquals(
            IntOffset(600, 200),
            IntOffset(600, 200).limitTo(IntSize(700, 300))
        )

        Assert.assertEquals(
            IntOffset(0, 200),
            IntOffset(-100, 200).limitTo(IntSize(700, 300))
        )
        Assert.assertEquals(
            IntOffset(700, 200),
            IntOffset(800, 200).limitTo(IntSize(700, 300))
        )

        Assert.assertEquals(
            IntOffset(600, 0),
            IntOffset(600, -100).limitTo(IntSize(700, 300))
        )
        Assert.assertEquals(
            IntOffset(600, 300),
            IntOffset(600, 400).limitTo(IntSize(700, 300))
        )
    }
}