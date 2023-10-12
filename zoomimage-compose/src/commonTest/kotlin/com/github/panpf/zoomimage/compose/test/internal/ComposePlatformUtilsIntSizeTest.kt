package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.copy
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.isSameAspectRatio
import com.github.panpf.zoomimage.compose.internal.lerp
import com.github.panpf.zoomimage.compose.internal.reverseRotate
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsIntSizeTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10x9", IntSize(10, 9).toShortString())
        Assert.assertEquals("9x10", IntSize(9, 10).toShortString())
    }

    @Test
    fun testIsEmpty() {
        Assert.assertFalse(IntSize(10, 9).isEmpty())
        Assert.assertTrue(IntSize(0, 9).isEmpty())
        Assert.assertTrue(IntSize(10, 0).isEmpty())
        Assert.assertTrue(IntSize(0, 0).isEmpty())
        Assert.assertTrue(IntSize(-1, 9).isEmpty())
        Assert.assertTrue(IntSize(10, -1).isEmpty())
        Assert.assertTrue(IntSize(-1, -1).isEmpty())
    }

    @Test
    fun testIsNotEmpty() {
        Assert.assertTrue(IntSize(10, 9).isNotEmpty())
        Assert.assertFalse(IntSize(0, 9).isNotEmpty())
        Assert.assertFalse(IntSize(10, 0).isNotEmpty())
        Assert.assertFalse(IntSize(0, 0).isNotEmpty())
        Assert.assertFalse(IntSize(-1, 9).isNotEmpty())
        Assert.assertFalse(IntSize(10, -1).isNotEmpty())
        Assert.assertFalse(IntSize(-1, -1).isNotEmpty())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "43x37",
            (IntSize(13, 7) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "69x23",
            (IntSize(13, 7) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )

        Assert.assertEquals(
            "43x23",
            (IntSize(13, 7) * 3.3f).toShortString()
        )
        Assert.assertEquals(
            "69x37",
            (IntSize(13, 7) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "13x7",
            (IntSize(43, 37) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "8x11",
            (IntSize(43, 37) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
        Assert.assertEquals(
            "13x11",
            (IntSize(43, 37) / 3.3f).toShortString()
        )
        Assert.assertEquals(
            "8x7",
            (IntSize(43, 37) / 5.3f).toShortString()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSize(600, 200),
                IntSize(600, 200).rotate(rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSize(200, 600),
                IntSize(600, 200).rotate(rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSize(600, 200),
                IntSize(600, 200).rotate(rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSize(200, 600),
                IntSize(600, 200).rotate(rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntSize(600, 200),
                IntSize(600, 200).rotate(rotation)
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = IntSize(600, 200)

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
        val size = IntSize(600, 200)
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
        val size1 = IntSize(600, 200)
        val size2 = IntSize(400, 800)
        Assert.assertEquals(size1, lerp(size1, size2, 0f))
        Assert.assertEquals(IntSize(500, 500), lerp(size1, size2, 0.5f))
        Assert.assertEquals(size2, lerp(size1, size2, 1f))
    }

    @Test
    fun testCopy() {
        val size = IntSize(600, 200)
        Assert.assertEquals(size, size.copy())
        Assert.assertEquals(IntSize(100, 200), size.copy(width = 100))
        Assert.assertEquals(IntSize(600, 100), size.copy(height = 100))
        Assert.assertEquals(IntSize(500, 700), size.copy(width = 500, height = 700))
    }
}