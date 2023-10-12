package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.isSameAspectRatio
import com.github.panpf.zoomimage.compose.internal.reverseRotate
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.round
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsSizeTest {
    
    @Test
    fun testToShortString() {
        Assert.assertEquals("10.34x9.57", Size(10.342f, 9.567f).toShortString())
        Assert.assertEquals("9.57x10.34", Size(9.567f, 10.342f).toShortString())
        Assert.assertEquals("Unspecified", Size.Unspecified.toShortString())
    }

    @Test
    fun testIsNotEmpty() {
        Assert.assertTrue(Size(10f, 9f).isNotEmpty())
        Assert.assertFalse(Size(0f, 9f).isNotEmpty())
        Assert.assertFalse(Size(10f, 0f).isNotEmpty())
        Assert.assertFalse(Size(0f, 0f).isNotEmpty())
        Assert.assertFalse(Size(-1f, 9f).isNotEmpty())
        Assert.assertFalse(Size(10f, -1f).isNotEmpty())
        Assert.assertFalse(Size(-1f, -1f).isNotEmpty())
    }

    @Test
    fun testRound() {
        Assert.assertEquals(
            IntSize(13, 8),
            Size(13.345f, 7.567f).round()
        )
        Assert.assertEquals(
            IntSize(8, 13),
            Size(7.567f, 13.345f).round()
        )
    }

    @Test
    fun testRotate() {
        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Size(600.4f, 200.7f),
                Size(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Size(200.7f, 600.4f),
                Size(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Size(600.4f, 200.7f),
                Size(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Size(200.7f, 600.4f),
                Size(600.4f, 200.7f).rotate(rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Size(600.4f, 200.7f),
                Size(600.4f, 200.7f).rotate(rotation)
            )
        }
    }

    @Test
    fun testReverseRotate() {
        val size = Size(600.4f, 200.7f)

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
        val size = Size(600.4f, 200.7f)
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