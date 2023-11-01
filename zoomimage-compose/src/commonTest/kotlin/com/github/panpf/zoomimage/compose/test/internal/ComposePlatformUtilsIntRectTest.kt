package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.flip
import com.github.panpf.zoomimage.compose.internal.limitTo
import com.github.panpf.zoomimage.compose.internal.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.internal.rotateInSpace
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsIntRectTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("[10x9,600x500]", IntRect(10, 9, 600, 500).toShortString())
        Assert.assertEquals("[9x10,500x600]", IntRect(9, 10, 500, 600).toShortString())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "[43x37,1980x2650]",
            (IntRect(13, 7, 600, 500) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "[69x23,3180x1650]",
            (IntRect(13, 7, 600, 500) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )

        Assert.assertEquals(
            "[43x23,1980x1650]",
            (IntRect(13, 7, 600, 500) * 3.3f).toShortString()
        )
        Assert.assertEquals(
            "[69x37,3180x2650]",
            (IntRect(13, 7, 600, 500) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "[13x7,600x500]",
            (IntRect(43, 37, 1980, 2650) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "[8x11,374x803]",
            (IntRect(43, 37, 1980, 2650) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
        Assert.assertEquals(
            "[13x11,600x803]",
            (IntRect(43, 37, 1980, 2650) / 3.3f).toShortString()
        )
        Assert.assertEquals(
            "[8x7,374x500]",
            (IntRect(43, 37, 1980, 2650) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        Assert.assertEquals(
            IntRect(600, 200, 1000, 800),
            IntRect(600, 200, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRect(300, 200, 1000, 800),
            IntRect(200, 200, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRect(1200, 200, 1000, 800),
            IntRect(1300, 200, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRect(600, 100, 1000, 800),
            IntRect(600, 50, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRect(600, 900, 1000, 800),
            IntRect(600, 1000, 1000, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRect(600, 200, 300, 800),
            IntRect(600, 200, 200, 800).limitTo(IntRect(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRect(600, 200, 1200, 800),
            IntRect(600, 200, 1300, 800).limitTo(IntRect(300, 100, 1200, 900))
        )

        Assert.assertEquals(
            IntRect(600, 200, 1000, 100),
            IntRect(600, 200, 1000, 50).limitTo(IntRect(300, 100, 1200, 900))
        )
        Assert.assertEquals(
            IntRect(600, 200, 1000, 900),
            IntRect(600, 200, 1000, 1000).limitTo(IntRect(300, 100, 1200, 900))
        )
    }

    @Test
    fun testLimitToSize() {
        Assert.assertEquals(
            IntRect(600, 200, 1000, 800),
            IntRect(600, 200, 1000, 800).limitTo(IntSize(1200, 900))
        )

        Assert.assertEquals(
            IntRect(0, 200, 1000, 800),
            IntRect(-600, 200, 1000, 800).limitTo(IntSize(1200, 900))
        )
        Assert.assertEquals(
            IntRect(1200, 200, 1000, 800),
            IntRect(1300, 200, 1000, 800).limitTo(IntSize(1200, 900))
        )

        Assert.assertEquals(
            IntRect(600, 0, 1000, 800),
            IntRect(600, -200, 1000, 800).limitTo(IntSize(1200, 900))
        )
        Assert.assertEquals(
            IntRect(600, 900, 1000, 800),
            IntRect(600, 1000, 1000, 800).limitTo(IntSize(1200, 900))
        )

        Assert.assertEquals(
            IntRect(600, 200, 0, 800),
            IntRect(600, 200, -1000, 800).limitTo(IntSize(1200, 900))
        )
        Assert.assertEquals(
            IntRect(600, 200, 1200, 800),
            IntRect(600, 200, 1300, 800).limitTo(IntSize(1200, 900))
        )

        Assert.assertEquals(
            IntRect(600, 200, 1000, 0),
            IntRect(600, 200, 1000, -800).limitTo(IntSize(1200, 900))
        )
        Assert.assertEquals(
            IntRect(600, 200, 1000, 900),
            IntRect(600, 200, 1000, 1000).limitTo(IntSize(1200, 900))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = IntSize(1000, 700)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRect(100, 200, 600, 500),
                IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRect(200, 100, 500, 600),
                IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRect(400, 200, 900, 500),
                IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRect(200, 400, 500, 900),
                IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                IntRect(100, 200, 600, 500),
                IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, rotation)
            )
        }

        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
            IntRect(100, 200, 600, 500).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotate() {
        val spaceSize = IntSize(1000, 700)
        val rect = IntRect(100, 200, 600, 500)

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

    @Test
    fun testFlip() {
        val spaceSize = IntSize(1000, 700)
        val rect = IntRect(100, 200, 600, 400)

        Assert.assertEquals(
            IntRect(400, 200, 900, 400),
            rect.flip(spaceSize, vertical = false)
        )
        Assert.assertEquals(
            IntRect(100, 300, 600, 500),
            rect.flip(spaceSize, vertical = true)
        )
    }
}