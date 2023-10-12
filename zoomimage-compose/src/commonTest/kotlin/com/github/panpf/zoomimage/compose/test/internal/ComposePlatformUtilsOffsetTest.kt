package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.limitTo
import com.github.panpf.zoomimage.compose.internal.reverseRotateInSpace
import com.github.panpf.zoomimage.compose.internal.rotateInSpace
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsOffsetTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("10.34x9.57", Offset(10.342f, 9.567f).toShortString())
        Assert.assertEquals("9.57x10.34", Offset(9.567f, 10.342f).toShortString())
        Assert.assertEquals("Unspecified", Offset.Unspecified.toShortString())
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "42.9x37.1",
            (Offset(13f, 7f) * ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "68.9x23.1",
            (Offset(13f, 7f) * ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "13.03x6.98",
            (Offset(43f, 37f) / ScaleFactor(3.3f, 5.3f)).toShortString()
        )
        Assert.assertEquals(
            "8.11x11.21",
            (Offset(43f, 37f) / ScaleFactor(5.3f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = Size(1000f, 500f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Offset(600.6f, 200.4f).toShortString(),
                Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Offset(299.6f, 600.6f).toShortString(),
                Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Offset(399.4f, 299.6f).toShortString(),
                Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Offset(200.4f, 399.4f).toShortString(),
                Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            Assert.assertEquals(
                "rotation: $rotation",
                Offset(600.6f, 200.4f).toShortString(),
                Offset(600.6f, 200.4f).rotateInSpace(spaceSize, rotation).toShortString()
            )
        }

        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, -1)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 1)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 89)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 91)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 179)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 191)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 269)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 271)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 359)
        }
        assertThrow(IllegalArgumentException::class) {
            Offset(600.6f, 200.4f).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = Size(1000f, 500f)
        val offset = Offset(600.6f, 200.4f)

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
            Offset(600.4f, 200.7f),
            Offset(600.4f, 200.7f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )

        Assert.assertEquals(
            Offset(200.4f, 200.7f),
            Offset(100f, 200.7f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )
        Assert.assertEquals(
            Offset(700.9f, 200.7f),
            Offset(800.4f, 200.7f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )

        Assert.assertEquals(
            Offset(600.4f, 100.3f),
            Offset(600.4f, 50.6f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )
        Assert.assertEquals(
            Offset(600.4f, 300.5f),
            Offset(600.4f, 400.9f).limitTo(Rect(200.4f, 100.3f, 700.9f, 300.5f))
        )
    }

    @Test
    fun testLimitToSize() {
        Assert.assertEquals(
            Offset(600.4f, 200.7f),
            Offset(600.4f, 200.7f).limitTo(Size(700.9f, 300.5f))
        )

        Assert.assertEquals(
            Offset(0f, 200.7f),
            Offset(-100.2f, 200.7f).limitTo(Size(700.9f, 300.5f))
        )
        Assert.assertEquals(
            Offset(700.9f, 200.7f),
            Offset(800.4f, 200.7f).limitTo(Size(700.9f, 300.5f))
        )

        Assert.assertEquals(
            Offset(600.4f, 0f),
            Offset(600.4f, -100.2f).limitTo(Size(700.9f, 300.5f))
        )
        Assert.assertEquals(
            Offset(600.4f, 300.5f),
            Offset(600.4f, 400.9f).limitTo(Size(700.9f, 300.5f))
        )
    }
}