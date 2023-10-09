package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntRect
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.limitTo
import com.github.panpf.zoomimage.compose.internal.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.internal.rotateInSpace
import com.github.panpf.zoomimage.compose.internal.round
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsRectTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals(
            "[10.34x9.77,600.32x500.91]",
            Rect(10.342f, 9.765f, 600.321f, 500.906f).toShortString()
        )
        Assert.assertEquals(
            "[9.77x10.34,500.91x600.32]",
            Rect(9.765f, 10.342f, 500.906f, 600.321f).toShortString()
        )
    }

    @Test
    fun testRound() {
        Assert.assertEquals(
            IntRect(10, 9, 600, 501),
            Rect(10.342f, 8.765f, 600.321f, 500.906f).round()
        )
        Assert.assertEquals(
            IntRect(9, 10, 501, 600),
            Rect(8.765f, 10.342f, 500.906f, 600.321f).round()
        )
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "[42.9x37.1,1989.9x2687.1]",
            (Rect(13f, 7f, 603f, 507f) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "[68.9x23.1,3195.9x1673.1]",
            (Rect(13f, 7f, 603f, 507f) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )

        Assert.assertEquals(
            "[42.9x23.1,1989.9x1673.1]",
            (Rect(13f, 7f, 603f, 507f) * 3.3f).toShortString()
        )
        Assert.assertEquals(
            "[68.9x37.1,3195.9x2687.1]",
            (Rect(13f, 7f, 603f, 507f) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "[17.27x14.91,182.73x95.66]",
            (Rect(57f, 79f, 603f, 507f) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "[10.75x23.94,113.77x153.64]",
            (Rect(57f, 79f, 603f, 507f) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
        Assert.assertEquals(
            "[17.27x23.94,182.73x153.64]",
            (Rect(57f, 79f, 603f, 507f) / 3.3f).toShortString()
        )
        Assert.assertEquals(
            "[10.75x14.91,113.77x95.66]",
            (Rect(57f, 79f, 603f, 507f) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 800.4f),
            Rect(600.5f, 200.2f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        Assert.assertEquals(
            Rect(300.6f, 200.2f, 1000.9f, 800.4f),
            Rect(200.2f, 200.2f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        Assert.assertEquals(
            Rect(1200.5f, 200.2f, 1000.9f, 800.4f),
            Rect(1300.1f, 200.2f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        Assert.assertEquals(
            Rect(600.5f, 100.3f, 1000.9f, 800.4f),
            Rect(600.5f, 50.4f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        Assert.assertEquals(
            Rect(600.5f, 900.6f, 1000.9f, 800.4f),
            Rect(600.5f, 1000.9f, 1000.9f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        Assert.assertEquals(
            Rect(600.5f, 200.2f, 300.6f, 800.4f),
            Rect(600.5f, 200.2f, 200.2f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1200.5f, 800.4f),
            Rect(600.5f, 200.2f, 1300.1f, 800.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 100.3f),
            Rect(600.5f, 200.2f, 1000.9f, 50.4f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 900.6f),
            Rect(600.5f, 200.2f, 1000.9f, 1000.9f).limitTo(
                Rect(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
    }

    @Test
    fun testLimitToSize() {
        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 800.4f),
            Rect(600.5f, 200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        Assert.assertEquals(
            Rect(0f, 200.2f, 1000.9f, 800.4f),
            Rect(-600.5f, 200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        Assert.assertEquals(
            Rect(1200.5f, 200.2f, 1000.9f, 800.4f),
            Rect(1300.1f, 200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        Assert.assertEquals(
            Rect(600.5f, 0f, 1000.9f, 800.4f),
            Rect(600.5f, -200.2f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        Assert.assertEquals(
            Rect(600.5f, 900.6f, 1000.9f, 800.4f),
            Rect(600.5f, 1000.9f, 1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        Assert.assertEquals(
            Rect(600.5f, 200.2f, 0f, 800.4f),
            Rect(600.5f, 200.2f, -1000.9f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1200.5f, 800.4f),
            Rect(600.5f, 200.2f, 1300.1f, 800.4f).limitTo(Size(1200.5f, 900.6f))
        )

        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 0f),
            Rect(600.5f, 200.2f, 1000.9f, -800.4f).limitTo(Size(1200.5f, 900.6f))
        )
        Assert.assertEquals(
            Rect(600.5f, 200.2f, 1000.9f, 900.6f),
            Rect(600.5f, 200.2f, 1000.9f, 1000.9f).limitTo(Size(1200.5f, 900.6f))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = Size(1000f, 700f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Rect(100.2f, 200.7f, 600.9f, 500.4f).toShortString(),
                Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString()
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Rect(199.6f, 100.2f, 499.3f, 600.9f).toShortString(),
                Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString()
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Rect(399.1f, 199.6f, 899.8f, 499.3f).toShortString(),
                Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString()
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Rect(200.7f, 399.1f, 500.4f, 899.8f).toShortString(),
                Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString()
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Rect(100.2f, 200.7f, 600.9f, 500.4f).toShortString(),
                Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, rotation)
                    .toShortString()
            )
        }

        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
            Rect(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotate() {
        val spaceSize = Size(1000f, 700f)
        val rect = Rect(100.2f, 200.7f, 600.9f, 500.4f)

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
                    rect.toShortString(),
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString()
                )
            }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertNotEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect.toShortString(),
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString()
                )
            }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertNotEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect.toShortString(),
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString()
                )
            }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                Assert.assertEquals("rotation: $rotation", rect, rotatedSize)
                Assert.assertEquals(
                    "rotation: $rotation",
                    rect.toShortString(),
                    rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString()
                )
            }
    }
}