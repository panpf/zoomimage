package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
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
    fun testRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 500f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(600.6f, 200.4f).toShortString(),
                OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(299.6f, 600.6f).toShortString(),
                OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(399.4f, 299.6f).toShortString(),
                OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(200.4f, 399.4f).toShortString(),
                OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                OffsetCompat(600.6f, 200.4f).toShortString(),
                OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            OffsetCompat(600.6f, 200.4f).rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
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
            OffsetCompat(600.4f, 200.7f),
            OffsetCompat(600.4f, 200.7f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )

        Assert.assertEquals(
            OffsetCompat(200.4f, 200.7f),
            OffsetCompat(100f, 200.7f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )
        Assert.assertEquals(
            OffsetCompat(700.9f, 200.7f),
            OffsetCompat(800.4f, 200.7f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )

        Assert.assertEquals(
            OffsetCompat(600.4f, 100.3f),
            OffsetCompat(600.4f, 50.6f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )
        Assert.assertEquals(
            OffsetCompat(600.4f, 300.5f),
            OffsetCompat(600.4f, 400.9f).limitTo(RectCompat(200.4f, 100.3f, 700.9f, 300.5f))
        )
    }

    @Test
    fun testLimitToSize() {
        Assert.assertEquals(
            OffsetCompat(600.4f, 200.7f),
            OffsetCompat(600.4f, 200.7f).limitTo(SizeCompat(700.9f, 300.5f))
        )

        Assert.assertEquals(
            OffsetCompat(0f, 200.7f),
            OffsetCompat(-100.2f, 200.7f).limitTo(SizeCompat(700.9f, 300.5f))
        )
        Assert.assertEquals(
            OffsetCompat(700.9f, 200.7f),
            OffsetCompat(800.4f, 200.7f).limitTo(SizeCompat(700.9f, 300.5f))
        )

        Assert.assertEquals(
            OffsetCompat(600.4f, 0f),
            OffsetCompat(600.4f, -100.2f).limitTo(SizeCompat(700.9f, 300.5f))
        )
        Assert.assertEquals(
            OffsetCompat(600.4f, 300.5f),
            OffsetCompat(600.4f, 400.9f).limitTo(SizeCompat(700.9f, 300.5f))
        )
    }
}