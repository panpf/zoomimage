package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.copy
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.isSameAspectRatio
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.reverseRotate
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test

class IntSizeCompatTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10x9", IntSizeCompat(10, 9).toShortString())
        Assert.assertEquals("9x10", IntSizeCompat(9, 10).toShortString())
    }

    @Test
    fun testIsEmpty() {
        Assert.assertFalse(IntSizeCompat(10, 9).isEmpty())
        Assert.assertTrue(IntSizeCompat(0, 9).isEmpty())
        Assert.assertTrue(IntSizeCompat(10, 0).isEmpty())
        Assert.assertTrue(IntSizeCompat(0, 0).isEmpty())
        Assert.assertTrue(IntSizeCompat(-1, 9).isEmpty())
        Assert.assertTrue(IntSizeCompat(10, -1).isEmpty())
        Assert.assertTrue(IntSizeCompat(-1, -1).isEmpty())
    }

    @Test
    fun testIsNotEmpty() {
        Assert.assertTrue(IntSizeCompat(10, 9).isNotEmpty())
        Assert.assertFalse(IntSizeCompat(0, 9).isNotEmpty())
        Assert.assertFalse(IntSizeCompat(10, 0).isNotEmpty())
        Assert.assertFalse(IntSizeCompat(0, 0).isNotEmpty())
        Assert.assertFalse(IntSizeCompat(-1, 9).isNotEmpty())
        Assert.assertFalse(IntSizeCompat(10, -1).isNotEmpty())
        Assert.assertFalse(IntSizeCompat(-1, -1).isNotEmpty())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "43x37",
            (IntSizeCompat(13, 7) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "69x23",
            (IntSizeCompat(13, 7) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )

        Assert.assertEquals(
            "43x23",
            (IntSizeCompat(13, 7) * 3.3f).toShortString()
        )
        Assert.assertEquals(
            "69x37",
            (IntSizeCompat(13, 7) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "13x7",
            (IntSizeCompat(43, 37) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "8x11",
            (IntSizeCompat(43, 37) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
        Assert.assertEquals(
            "13x11",
            (IntSizeCompat(43, 37) / 3.3f).toShortString()
        )
        Assert.assertEquals(
            "8x7",
            (IntSizeCompat(43, 37) / 5.3f).toShortString()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSizeCompat(600, 200),
                IntSizeCompat(600, 200).rotate(rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSizeCompat(200, 600),
                IntSizeCompat(600, 200).rotate(rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSizeCompat(600, 200),
                IntSizeCompat(600, 200).rotate(rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSizeCompat(200, 600),
                IntSizeCompat(600, 200).rotate(rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSizeCompat(600, 200),
                IntSizeCompat(600, 200).rotate(rotation)
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = IntSizeCompat(600, 200)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                Assert.assertEquals("rotation: $rotation", size, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    size,
                    rotatedSize.reverseRotate(rotation)
                )
            }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                Assert.assertNotEquals("rotation: $rotation", size, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    size,
                    rotatedSize.reverseRotate(rotation)
                )
            }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                Assert.assertEquals("rotation: $rotation", size, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    size,
                    rotatedSize.reverseRotate(rotation)
                )
            }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                Assert.assertNotEquals("rotation: $rotation", size, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    size,
                    rotatedSize.reverseRotate(rotation)
                )
            }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = size.rotate(rotation)
                Assert.assertEquals("rotation: $rotation", size, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    size,
                    rotatedSize.reverseRotate(rotation)
                )
            }
    }

    @Test
    fun testIsSameAspectRatio() {
        val size = IntSizeCompat(600, 200)
        Assert.assertTrue(size.isSameAspectRatio(size * 2))
        Assert.assertTrue(size.isSameAspectRatio(size / 2))
        Assert.assertTrue(size.isSameAspectRatio(size * 3))

        Assert.assertFalse(size.isSameAspectRatio(size / 3))
        Assert.assertFalse(size.isSameAspectRatio((size * 2).copy(height = size.height * 2 + 1)))
        Assert.assertFalse(size.isSameAspectRatio((size / 2).copy(height = size.height * 2 + 1)))
        Assert.assertFalse(size.isSameAspectRatio((size * 3).copy(height = size.height * 2 + 1)))
        Assert.assertFalse(size.isSameAspectRatio((size / 3).copy(height = size.height * 2 + 1)))

        Assert.assertTrue(size.isSameAspectRatio(size / 3, delta = 0.1f))
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size * 2).copy(height = size.height * 2 + 1),
                delta = 0.1f
            )
        )
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size / 2).copy(height = size.height / 2 + 1),
                delta = 0.1f
            )
        )
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size * 3).copy(height = size.height * 3 + 1),
                delta = 0.1f
            )
        )
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size / 3).copy(height = size.height / 3 + 1),
                delta = 0.1f
            )
        )
    }

    @Test
    fun testLerp() {
        val size1 = IntSizeCompat(600, 200)
        val size2 = IntSizeCompat(400, 800)
        Assert.assertEquals(size1, lerp(size1, size2, 0f))
        Assert.assertEquals(IntSizeCompat(500, 500), lerp(size1, size2, 0.5f))
        Assert.assertEquals(size2, lerp(size1, size2, 1f))
    }

    @Test
    fun testCopy() {
        val size = IntSizeCompat(600, 200)
        Assert.assertEquals(size, size.copy())
        Assert.assertEquals(IntSizeCompat(100, 200), size.copy(width = 100))
        Assert.assertEquals(IntSizeCompat(600, 100), size.copy(height = 100))
        Assert.assertEquals(IntSizeCompat(500, 700), size.copy(width = 500, height = 700))
    }
}