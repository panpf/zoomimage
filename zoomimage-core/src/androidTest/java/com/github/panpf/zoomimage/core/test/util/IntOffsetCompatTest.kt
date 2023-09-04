package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toIntSize
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import org.junit.Assert
import org.junit.Test

class IntOffsetCompatTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10x9", IntOffsetCompat(10, 9).toShortString())
        Assert.assertEquals("9x10", IntOffsetCompat(9, 10).toShortString())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "43x37",
            (IntOffsetCompat(13, 7) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "69x23",
            (IntOffsetCompat(13, 7) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "13x7",
            (IntOffsetCompat(43, 37) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "8x11",
            (IntOffsetCompat(43, 37) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testToSize() {
        Assert.assertEquals(
            SizeCompat(13f, 7f),
            IntOffsetCompat(13, 7).toSize()
        )
        Assert.assertEquals(
            SizeCompat(7f, 13f),
            IntOffsetCompat(7, 13).toSize()
        )
    }

    @Test
    fun testToIntSize() {
        Assert.assertEquals(
            IntSizeCompat(13, 7),
            IntOffsetCompat(13, 7).toIntSize()
        )
        Assert.assertEquals(
            IntSizeCompat(7, 13),
            IntOffsetCompat(7, 13).toIntSize()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 500)
        val offset = IntOffsetCompat(600, 200)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffsetCompat(600, 200),
                offset.rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffsetCompat(300, 600),
                offset.rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffsetCompat(400, 300),
                offset.rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffsetCompat(200, 400),
                offset.rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntOffsetCompat(600, 200),
                offset.rotateInSpace(spaceSize, rotation)
            )
        }

        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
            offset.rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 500)
        val offset = IntOffsetCompat(600, 200)

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
    fun testLimitTo() {
        val offset = IntOffsetCompat(600, 200)

        Assert.assertEquals(
            offset,
            offset.limitTo(IntSizeCompat(700, 300))
        )
        Assert.assertEquals(
            IntOffsetCompat(500, 200),
            offset.limitTo(IntSizeCompat(500, 300))
        )
        Assert.assertEquals(
            IntOffsetCompat(600, 100),
            offset.limitTo(IntSizeCompat(600, 100))
        )

        Assert.assertEquals(
            offset,
            offset.limitTo(IntRectCompat(200, 100, 700, 300))
        )

        Assert.assertEquals(
            IntOffsetCompat(650, 200),
            offset.limitTo(IntRectCompat(650, 100, 700, 300))
        )
        Assert.assertEquals(
            IntOffsetCompat(600, 250),
            offset.limitTo(IntRectCompat(200, 250, 700, 300))
        )

        Assert.assertEquals(
            IntOffsetCompat(550, 200),
            offset.limitTo(IntRectCompat(200, 100, 550, 300))
        )
        Assert.assertEquals(
            IntOffsetCompat(600, 150),
            offset.limitTo(IntRectCompat(200, 100, 700, 150))
        )
    }
}