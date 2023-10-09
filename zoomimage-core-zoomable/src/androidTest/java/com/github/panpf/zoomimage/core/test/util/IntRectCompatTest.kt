package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test

class IntRectCompatTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("[10x9,600x500]", IntRectCompat(10, 9, 600, 500).toShortString())
        Assert.assertEquals("[9x10,500x600]", IntRectCompat(9, 10, 500, 600).toShortString())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "[43x37,1980x2650]",
            (IntRectCompat(13, 7, 600, 500) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "[69x23,3180x1650]",
            (IntRectCompat(13, 7, 600, 500) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )

        Assert.assertEquals(
            "[43x23,1980x1650]",
            (IntRectCompat(13, 7, 600, 500) * 3.3f).toShortString()
        )
        Assert.assertEquals(
            "[69x37,3180x2650]",
            (IntRectCompat(13, 7, 600, 500) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "[13x7,600x500]",
            (IntRectCompat(43, 37, 1980, 2650) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "[8x11,374x803]",
            (IntRectCompat(43, 37, 1980, 2650) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
        Assert.assertEquals(
            "[13x11,600x803]",
            (IntRectCompat(43, 37, 1980, 2650) / 3.3f).toShortString()
        )
        Assert.assertEquals(
            "[8x7,374x500]",
            (IntRectCompat(43, 37, 1980, 2650) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        Assert.assertEquals(
            IntRectCompat(600, 200, 1000, 800),
            IntRectCompat(600, 200, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(300, 200, 1000, 800),
            IntRectCompat(200, 200, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(1200, 200, 1000, 800),
            IntRectCompat(1300, 200, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(600, 100, 1000, 800),
            IntRectCompat(600, 50, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(600, 900, 1000, 800),
            IntRectCompat(600, 1000, 1000, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(600, 200, 300, 800),
            IntRectCompat(600, 200, 200, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(600, 200, 1200, 800),
            IntRectCompat(600, 200, 1300, 800).limitTo(IntRectCompat(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(600, 200, 1000, 100),
            IntRectCompat(600, 200, 1000, 50).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(600, 200, 1000, 900),
            IntRectCompat(600, 200, 1000, 1000).limitTo(IntRectCompat(300, 100, 1200, 900))
        )
    }

    @Test
    fun testLimitToSize() {
        Assert.assertEquals(
            IntRectCompat(600, 200, 1000, 800),
            IntRectCompat(600, 200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(0, 200, 1000, 800),
            IntRectCompat(-600, 200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(1200, 200, 1000, 800),
            IntRectCompat(1300, 200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(600, 0, 1000, 800),
            IntRectCompat(600, -200, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(600, 900, 1000, 800),
            IntRectCompat(600, 1000, 1000, 800).limitTo(IntSizeCompat(1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(600, 200, 0, 800),
            IntRectCompat(600, 200, -1000, 800).limitTo(IntSizeCompat(1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(600, 200, 1200, 800),
            IntRectCompat(600, 200, 1300, 800).limitTo(IntSizeCompat(1200, 900))
        )

        Assert.assertEquals(
            IntRectCompat(600, 200, 1000, 0),
            IntRectCompat(600, 200, 1000, -800).limitTo(IntSizeCompat(1200, 900))
        )
        Assert.assertEquals(
            IntRectCompat(600, 200, 1000, 900),
            IntRectCompat(600, 200, 1000, 1000).limitTo(IntSizeCompat(1200, 900))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 700)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRectCompat(100, 200, 600, 500),
                IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRectCompat(200, 100, 500, 600),
                IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRectCompat(400, 200, 900, 500),
                IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRectCompat(200, 400, 500, 900),
                IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRectCompat(100, 200, 600, 500),
                IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRectCompat(100, 200, 600, 500).rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
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
                Assert.assertEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect,
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation)
                )
            }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertNotEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect,
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation)
                )
            }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertNotEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect,
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation)
                )
            }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertNotEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect,
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation)
                )
            }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect,
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation)
                )
            }
    }
}