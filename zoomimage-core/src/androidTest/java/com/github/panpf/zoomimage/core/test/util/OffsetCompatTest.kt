package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.roundToSize
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import org.junit.Assert
import org.junit.Test

class OffsetCompatTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10.34x9.57", OffsetCompat(10.342f, 9.567f).toShortString())
        Assert.assertEquals("9.57x10.34", OffsetCompat(9.567f, 10.342f).toShortString())
        Assert.assertEquals("Unspecified", OffsetCompat.Unspecified.toShortString())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "42.9x37.1",
            (OffsetCompat(13f, 7f) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "68.9x23.1",
            (OffsetCompat(13f, 7f) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "13.03x6.98",
            (OffsetCompat(43f, 37f) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "8.11x11.21",
            (OffsetCompat(43f, 37f) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testToSize() {
        Assert.assertEquals(
            SizeCompat(13.453f, 7.789f),
            OffsetCompat(13.453f, 7.789f).toSize()
        )
        Assert.assertEquals(
            SizeCompat(7.789f, 13.453f),
            OffsetCompat(7.789f, 13.453f).toSize()
        )
    }

    @Test
    fun testRoundToSize() {
        Assert.assertEquals(
            IntSizeCompat(13, 8),
            OffsetCompat(13.453f, 7.789f).roundToSize()
        )
        Assert.assertEquals(
            IntSizeCompat(8, 13),
            OffsetCompat(7.789f, 13.453f).roundToSize()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 500f)
        val offset = OffsetCompat(600.6f, 200.4f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(600.6f, 200.4f).toShortString(),
                offset.rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(299.6f, 600.6f).toShortString(),
                offset.rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(399.4f, 299.6f).toShortString(),
                offset.rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(200.4f, 399.4f).toShortString(),
                offset.rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(600.6f, 200.4f).toShortString(),
                offset.rotateInSpace(spaceSize, rotation).toShortString()
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
        val spaceSize = SizeCompat(1000f, 500f)
        val offset = OffsetCompat(600.6f, 200.4f)

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
        val offset = OffsetCompat(600.6f, 200.4f)

        Assert.assertEquals(
            offset,
            offset.limitTo(SizeCompat(700.7f, 300.3f))
        )
        Assert.assertEquals(
            OffsetCompat(500f, 200.4f),
            offset.limitTo(SizeCompat(500f, 300f))
        )
        Assert.assertEquals(
            OffsetCompat(600.6f, 100f),
            offset.limitTo(SizeCompat(700f, 100f))
        )

        Assert.assertEquals(
            offset,
            offset.limitTo(RectCompat(200f, 100f, 700f, 300f))
        )

        Assert.assertEquals(
            OffsetCompat(650f, 200.4f),
            offset.limitTo(RectCompat(650f, 100f, 700f, 300f))
        )
        Assert.assertEquals(
            OffsetCompat(600.6f, 250f),
            offset.limitTo(RectCompat(200f, 250f, 700f, 300f))
        )

        Assert.assertEquals(
            OffsetCompat(550f, 200.4f),
            offset.limitTo(RectCompat(200f, 100f, 550f, 300f))
        )
        Assert.assertEquals(
            OffsetCompat(600.6f, 150f),
            offset.limitTo(RectCompat(200f, 100f, 700f, 150f))
        )
    }
}