package com.github.panpf.zoomimage.core.test.subsampling

import com.github.panpf.zoomimage.core.test.internal.produceFingerPrint
import com.github.panpf.zoomimage.core.test.internal.readImage
import com.github.panpf.zoomimage.subsampling.DesktopExifOrientation
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert
import org.junit.Test

class DesktopExifOrientationTest {

    @Test
    fun testConstructor() {
        DesktopExifOrientation(ExifOrientation.ORIENTATION_UNDEFINED).apply {
            Assert.assertEquals(ExifOrientation.ORIENTATION_UNDEFINED, exifOrientation)
        }

        DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_270).apply {
            Assert.assertEquals(ExifOrientation.ORIENTATION_ROTATE_270, exifOrientation)
        }
    }

    @Test
    fun testName() {
        Assert.assertEquals(
            "ROTATE_90",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_90).name()
        )
        Assert.assertEquals(
            "TRANSPOSE",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_TRANSPOSE).name()
        )
        Assert.assertEquals(
            "ROTATE_180",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_180).name()
        )
        Assert.assertEquals(
            "FLIP_VERTICAL",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_FLIP_VERTICAL).name()
        )
        Assert.assertEquals(
            "ROTATE_270",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_270).name()
        )
        Assert.assertEquals(
            "TRANSVERSE",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_TRANSVERSE).name()
        )
        Assert.assertEquals(
            "FLIP_HORIZONTAL",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_FLIP_HORIZONTAL).name()
        )
        Assert.assertEquals(
            "UNDEFINED",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_UNDEFINED).name()
        )
        Assert.assertEquals(
            "NORMAL",
            DesktopExifOrientation(ExifOrientation.ORIENTATION_NORMAL).name()
        )
        Assert.assertEquals("-1", DesktopExifOrientation(-1).name())
        Assert.assertEquals("100", DesktopExifOrientation(100).name())
    }

    @Test
    fun testIsFlipped() {
        Assert.assertFalse(DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_90).isFlipped)
        Assert.assertTrue(DesktopExifOrientation(ExifOrientation.ORIENTATION_TRANSPOSE).isFlipped)
        Assert.assertFalse(DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_180).isFlipped)
        Assert.assertTrue(DesktopExifOrientation(ExifOrientation.ORIENTATION_FLIP_VERTICAL).isFlipped)
        Assert.assertFalse(DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_270).isFlipped)
        Assert.assertTrue(DesktopExifOrientation(ExifOrientation.ORIENTATION_TRANSVERSE).isFlipped)
        Assert.assertTrue(DesktopExifOrientation(ExifOrientation.ORIENTATION_FLIP_HORIZONTAL).isFlipped)
        Assert.assertFalse(DesktopExifOrientation(ExifOrientation.ORIENTATION_UNDEFINED).isFlipped)
        Assert.assertFalse(DesktopExifOrientation(ExifOrientation.ORIENTATION_NORMAL).isFlipped)
        Assert.assertFalse(DesktopExifOrientation(-1).isFlipped)
        Assert.assertFalse(DesktopExifOrientation(100).isFlipped)
    }

    @Test
    fun testRotationDegrees() {
        Assert.assertEquals(
            90,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_90).rotationDegrees
        )
        Assert.assertEquals(
            270,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_TRANSPOSE).rotationDegrees
        )
        Assert.assertEquals(
            180,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_180).rotationDegrees
        )
        Assert.assertEquals(
            180,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_FLIP_VERTICAL).rotationDegrees
        )
        Assert.assertEquals(
            270,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_ROTATE_270).rotationDegrees
        )
        Assert.assertEquals(
            90,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_TRANSVERSE).rotationDegrees
        )
        Assert.assertEquals(
            0,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_FLIP_HORIZONTAL).rotationDegrees
        )
        Assert.assertEquals(
            0,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_UNDEFINED).rotationDegrees
        )
        Assert.assertEquals(
            0,
            DesktopExifOrientation(ExifOrientation.ORIENTATION_NORMAL).rotationDegrees
        )
        Assert.assertEquals(0, DesktopExifOrientation(-1).rotationDegrees)
        Assert.assertEquals(0, DesktopExifOrientation(100).rotationDegrees)
    }

    @Test
    fun testApplyToSize() {
        val size = IntSizeCompat(100, 50)
        listOf(
            ExifOrientation.ORIENTATION_ROTATE_90 to IntSizeCompat(50, 100),
            ExifOrientation.ORIENTATION_TRANSVERSE to IntSizeCompat(50, 100),
            ExifOrientation.ORIENTATION_ROTATE_180 to IntSizeCompat(100, 50),
            ExifOrientation.ORIENTATION_FLIP_VERTICAL to IntSizeCompat(100, 50),
            ExifOrientation.ORIENTATION_ROTATE_270 to IntSizeCompat(50, 100),
            ExifOrientation.ORIENTATION_TRANSPOSE to IntSizeCompat(50, 100),
            ExifOrientation.ORIENTATION_UNDEFINED to IntSizeCompat(100, 50),
            ExifOrientation.ORIENTATION_NORMAL to IntSizeCompat(100, 50),
            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL to IntSizeCompat(100, 50),
            -1 to IntSizeCompat(100, 50),
            100 to IntSizeCompat(100, 50),
        ).forEach { (exifOrientationInt, exceptedSize) ->
            val exifOrientation = DesktopExifOrientation(exifOrientationInt)
            val message = "exifOrientationInt=${exifOrientation.name()}, exceptedSize=$exceptedSize"
            val appliedSize = exifOrientation.applyToSize(size)
            Assert.assertEquals(message, exceptedSize, appliedSize)
            val reverseAppliedSize = exifOrientation.applyToSize(appliedSize, reverse = true)
            Assert.assertEquals(message, size, reverseAppliedSize)
        }
    }

    @Test
    fun testApplyToRect() {
        val srcRect = IntRectCompat(40, 10, 50, 30)
        val imageSize = IntSizeCompat(100, 50)
        listOf(
            ExifOrientation.ORIENTATION_ROTATE_90 to IntRectCompat(20, 40, 40, 50),
            ExifOrientation.ORIENTATION_TRANSVERSE to IntRectCompat(20, 50, 40, 60),
            ExifOrientation.ORIENTATION_ROTATE_180 to IntRectCompat(50, 20, 60, 40),
            ExifOrientation.ORIENTATION_FLIP_VERTICAL to IntRectCompat(40, 20, 50, 40),
            ExifOrientation.ORIENTATION_ROTATE_270 to IntRectCompat(10, 50, 30, 60),
            ExifOrientation.ORIENTATION_TRANSPOSE to IntRectCompat(10, 40, 30, 50),
            ExifOrientation.ORIENTATION_UNDEFINED to IntRectCompat(40, 10, 50, 30),
            ExifOrientation.ORIENTATION_NORMAL to IntRectCompat(40, 10, 50, 30),
            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL to IntRectCompat(50, 10, 60, 30),
            -1 to IntRectCompat(40, 10, 50, 30),
            100 to IntRectCompat(40, 10, 50, 30),
        ).forEach { (exifOrientationInt, exceptedRect) ->
            val exifOrientation = DesktopExifOrientation(exifOrientationInt)
            val message = "exifOrientationInt=${exifOrientation.name()}, exceptedRect=$exceptedRect"
            val appliedRect = exifOrientation.applyToRect(srcRect, imageSize)
            val appliedImageSize = exifOrientation.applyToSize(imageSize)
            Assert.assertEquals(message, exceptedRect, appliedRect)
            val reverseAppliedRect =
                exifOrientation.applyToRect(appliedRect, appliedImageSize, reverse = true)
            Assert.assertEquals(message, srcRect, reverseAppliedRect)
        }
    }

    @Test
    fun testApplyToBitmap() {
        val image = readImage("sample_dog.jpg")
        listOf(
            ExifOrientation.ORIENTATION_ROTATE_90 to true,
            ExifOrientation.ORIENTATION_TRANSVERSE to true,
            ExifOrientation.ORIENTATION_ROTATE_180 to true,
            ExifOrientation.ORIENTATION_FLIP_VERTICAL to true,
            ExifOrientation.ORIENTATION_ROTATE_270 to true,
            ExifOrientation.ORIENTATION_TRANSPOSE to true,
            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL to true,
            ExifOrientation.ORIENTATION_NORMAL to false,
            ExifOrientation.ORIENTATION_UNDEFINED to false,
            -1 to false,
            100 to false,
        ).forEach { (exifOrientationInt, change) ->
            val exifOrientation = DesktopExifOrientation(exifOrientationInt)
            val message = "exifOrientationInt=${exifOrientation.name()}"

            val appliedBitmap = exifOrientation.applyToTileBitmap(
                DesktopTileBitmap(image),
                reverse = false
            ).let { it as DesktopTileBitmap }.bufferedImage
            if (change) {
                Assert.assertNotEquals(
                    message,
                    produceFingerPrint(image),
                    produceFingerPrint(appliedBitmap),
                )

                val reversedBitmap = exifOrientation.applyToTileBitmap(
                    DesktopTileBitmap(appliedBitmap),
                    reverse = true
                ).let { it as DesktopTileBitmap }.bufferedImage
                Assert.assertEquals(
                    message,
                    produceFingerPrint(image),
                    produceFingerPrint(reversedBitmap),
                )
            } else {
                Assert.assertEquals(
                    message,
                    produceFingerPrint(image),
                    produceFingerPrint(appliedBitmap),
                )
            }
        }
    }
}