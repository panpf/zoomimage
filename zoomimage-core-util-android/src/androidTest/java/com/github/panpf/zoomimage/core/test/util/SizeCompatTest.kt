package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.isSameAspectRatio
import com.github.panpf.zoomimage.util.reverseRotate
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test

class SizeCompatTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10.34x9.57", SizeCompat(10.342f, 9.567f).toShortString())
        Assert.assertEquals("9.57x10.34", SizeCompat(9.567f, 10.342f).toShortString())
        Assert.assertEquals("Unspecified", SizeCompat.Unspecified.toShortString())
    }

    @Test
    fun testIsNotEmpty() {
        Assert.assertTrue(SizeCompat(10f, 9f).isNotEmpty())
        Assert.assertFalse(SizeCompat(0f, 9f).isNotEmpty())
        Assert.assertFalse(SizeCompat(10f, 0f).isNotEmpty())
        Assert.assertFalse(SizeCompat(0f, 0f).isNotEmpty())
        Assert.assertFalse(SizeCompat(-1f, 9f).isNotEmpty())
        Assert.assertFalse(SizeCompat(10f, -1f).isNotEmpty())
        Assert.assertFalse(SizeCompat(-1f, -1f).isNotEmpty())
    }

    @Test
    fun testRound() {
        Assert.assertEquals(
            IntSizeCompat(13, 8),
            SizeCompat(13.345f, 7.567f).round()
        )
        Assert.assertEquals(
            IntSizeCompat(8, 13),
            SizeCompat(7.567f, 13.345f).round()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                SizeCompat(600.4f, 200.7f),
                SizeCompat(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                SizeCompat(200.7f, 600.4f),
                SizeCompat(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                SizeCompat(600.4f, 200.7f),
                SizeCompat(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                SizeCompat(200.7f, 600.4f),
                SizeCompat(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                SizeCompat(600.4f, 200.7f),
                SizeCompat(600.4f, 200.7f).rotate(rotation)
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = SizeCompat(600.4f, 200.7f)

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
        val size = SizeCompat(600.4f, 200.7f)
        Assert.assertTrue(size.isSameAspectRatio(size * 2f))
        Assert.assertTrue(size.isSameAspectRatio(size / 2f))
        Assert.assertFalse(size.isSameAspectRatio(size * 3f))
        Assert.assertTrue(size.isSameAspectRatio(size / 3f))

        Assert.assertFalse(size.isSameAspectRatio((size * 2f).copy(height = size.height * 2 + 1)))
        Assert.assertFalse(size.isSameAspectRatio((size / 2f).copy(height = size.height * 2 + 1)))
        Assert.assertFalse(size.isSameAspectRatio((size * 3f).copy(height = size.height * 2 + 1)))
        Assert.assertFalse(size.isSameAspectRatio((size / 3f).copy(height = size.height * 2 + 1)))

        Assert.assertTrue(
            size.isSameAspectRatio(
                (size * 2f).copy(height = size.height * 2 + 1),
                delta = 0.1f
            )
        )
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size / 2f).copy(height = size.height / 2 + 1),
                delta = 0.1f
            )
        )
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size * 3f).copy(height = size.height * 3 + 1),
                delta = 0.1f
            )
        )
        Assert.assertTrue(
            size.isSameAspectRatio(
                (size / 3f).copy(height = size.height / 3 + 1),
                delta = 0.1f
            )
        )
    }
}