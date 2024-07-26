package com.github.panpf.zoomimage.core.android.test.subsampling.internal

import android.graphics.BitmapFactory
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper
import com.github.panpf.zoomimage.test.produceFingerPrint
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import okio.buffer
import org.junit.Assert
import org.junit.Test

class ExifOrientationHelperTest {

    @Test
    fun testConstructor() {
        ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_UNDEFINED).apply {
            Assert.assertEquals(ExifOrientationHelper.ORIENTATION_UNDEFINED, exifOrientation)
        }

        ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_270).apply {
            Assert.assertEquals(ExifOrientationHelper.ORIENTATION_ROTATE_270, exifOrientation)
        }
    }

    @Test
    fun testName() {
        Assert.assertEquals(
            "ROTATE_90",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_ROTATE_90)
        )
        Assert.assertEquals(
            "TRANSPOSE",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_TRANSPOSE)
        )
        Assert.assertEquals(
            "ROTATE_180",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_ROTATE_180)
        )
        Assert.assertEquals(
            "FLIP_VERTICAL",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_FLIP_VERTICAL)
        )
        Assert.assertEquals(
            "ROTATE_270",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_ROTATE_270)
        )
        Assert.assertEquals(
            "TRANSVERSE",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_TRANSVERSE)
        )
        Assert.assertEquals(
            "FLIP_HORIZONTAL",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_FLIP_HORIZONTAL)
        )
        Assert.assertEquals(
            "UNDEFINED",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_UNDEFINED)
        )
        Assert.assertEquals(
            "NORMAL",
            ExifOrientationHelper.name(ExifOrientationHelper.ORIENTATION_NORMAL)
        )
        Assert.assertEquals("-1", ExifOrientationHelper.name(-1))
        Assert.assertEquals("100", ExifOrientationHelper.name(100))
    }

    @Test
    fun testIsFlipped() {
        Assert.assertFalse(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_90).isFlipped)
        Assert.assertTrue(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_TRANSPOSE).isFlipped)
        Assert.assertFalse(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_180).isFlipped)
        Assert.assertTrue(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_FLIP_VERTICAL).isFlipped)
        Assert.assertFalse(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_270).isFlipped)
        Assert.assertTrue(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_TRANSVERSE).isFlipped)
        Assert.assertTrue(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_FLIP_HORIZONTAL).isFlipped)
        Assert.assertFalse(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_UNDEFINED).isFlipped)
        Assert.assertFalse(ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_NORMAL).isFlipped)
        Assert.assertFalse(ExifOrientationHelper(-1).isFlipped)
        Assert.assertFalse(ExifOrientationHelper(100).isFlipped)
    }

    @Test
    fun testRotationDegrees() {
        Assert.assertEquals(
            90,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_90).rotationDegrees
        )
        Assert.assertEquals(
            270,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_TRANSPOSE).rotationDegrees
        )
        Assert.assertEquals(
            180,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_180).rotationDegrees
        )
        Assert.assertEquals(
            180,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_FLIP_VERTICAL).rotationDegrees
        )
        Assert.assertEquals(
            270,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_ROTATE_270).rotationDegrees
        )
        Assert.assertEquals(
            90,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_TRANSVERSE).rotationDegrees
        )
        Assert.assertEquals(
            0,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_FLIP_HORIZONTAL).rotationDegrees
        )
        Assert.assertEquals(
            0,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_UNDEFINED).rotationDegrees
        )
        Assert.assertEquals(
            0,
            ExifOrientationHelper(ExifOrientationHelper.ORIENTATION_NORMAL).rotationDegrees
        )
        Assert.assertEquals(0, ExifOrientationHelper(-1).rotationDegrees)
        Assert.assertEquals(0, ExifOrientationHelper(100).rotationDegrees)
    }

    @Test
    fun testApplyToSize() {
        val size = IntSizeCompat(100, 50)
        listOf(
            ExifOrientationHelper.ORIENTATION_ROTATE_90 to IntSizeCompat(50, 100),
            ExifOrientationHelper.ORIENTATION_TRANSVERSE to IntSizeCompat(50, 100),
            ExifOrientationHelper.ORIENTATION_ROTATE_180 to IntSizeCompat(100, 50),
            ExifOrientationHelper.ORIENTATION_FLIP_VERTICAL to IntSizeCompat(100, 50),
            ExifOrientationHelper.ORIENTATION_ROTATE_270 to IntSizeCompat(50, 100),
            ExifOrientationHelper.ORIENTATION_TRANSPOSE to IntSizeCompat(50, 100),
            ExifOrientationHelper.ORIENTATION_UNDEFINED to IntSizeCompat(100, 50),
            ExifOrientationHelper.ORIENTATION_NORMAL to IntSizeCompat(100, 50),
            ExifOrientationHelper.ORIENTATION_FLIP_HORIZONTAL to IntSizeCompat(100, 50),
            -1 to IntSizeCompat(100, 50),
            100 to IntSizeCompat(100, 50),
        ).forEach { (exifOrientationInt, exceptedSize) ->
            val exifOrientation = ExifOrientationHelper(exifOrientationInt)
            val message =
                "exifOrientationInt=${ExifOrientationHelper.name(exifOrientationInt)}, exceptedSize=$exceptedSize"
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
            ExifOrientationHelper.ORIENTATION_ROTATE_90 to IntRectCompat(20, 40, 40, 50),
            ExifOrientationHelper.ORIENTATION_TRANSVERSE to IntRectCompat(20, 50, 40, 60),
            ExifOrientationHelper.ORIENTATION_ROTATE_180 to IntRectCompat(50, 20, 60, 40),
            ExifOrientationHelper.ORIENTATION_FLIP_VERTICAL to IntRectCompat(40, 20, 50, 40),
            ExifOrientationHelper.ORIENTATION_ROTATE_270 to IntRectCompat(10, 50, 30, 60),
            ExifOrientationHelper.ORIENTATION_TRANSPOSE to IntRectCompat(10, 40, 30, 50),
            ExifOrientationHelper.ORIENTATION_UNDEFINED to IntRectCompat(40, 10, 50, 30),
            ExifOrientationHelper.ORIENTATION_NORMAL to IntRectCompat(40, 10, 50, 30),
            ExifOrientationHelper.ORIENTATION_FLIP_HORIZONTAL to IntRectCompat(50, 10, 60, 30),
            -1 to IntRectCompat(40, 10, 50, 30),
            100 to IntRectCompat(40, 10, 50, 30),
        ).forEach { (exifOrientationInt, exceptedRect) ->
            val exifOrientation = ExifOrientationHelper(exifOrientationInt)
            val message =
                "exifOrientationInt=${ExifOrientationHelper.name(exifOrientationInt)}, exceptedRect=$exceptedRect"
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
        val inBitmap = ResourceImages.dog.toImageSource()
            .openSource().buffer().inputStream().use {
                BitmapFactory.decodeStream(it)
            }
        listOf(
            ExifOrientationHelper.ORIENTATION_ROTATE_90 to true,
            ExifOrientationHelper.ORIENTATION_TRANSVERSE to true,
            ExifOrientationHelper.ORIENTATION_ROTATE_180 to true,
            ExifOrientationHelper.ORIENTATION_FLIP_VERTICAL to true,
            ExifOrientationHelper.ORIENTATION_ROTATE_270 to true,
            ExifOrientationHelper.ORIENTATION_TRANSPOSE to true,
            ExifOrientationHelper.ORIENTATION_FLIP_HORIZONTAL to true,
            ExifOrientationHelper.ORIENTATION_NORMAL to false,
            ExifOrientationHelper.ORIENTATION_UNDEFINED to false,
            -1 to false,
            100 to false,
        ).forEach { (exifOrientationInt, change) ->
            val exifOrientation = ExifOrientationHelper(exifOrientationInt)
            val message = "exifOrientationInt=${ExifOrientationHelper.name(exifOrientationInt)}"

            val appliedBitmap = exifOrientation.applyToTileBitmap(
                tileBitmap = AndroidTileBitmap(inBitmap, "", BitmapFrom.LOCAL),
                reverse = false
            ).bitmap!!
            if (change) {
                Assert.assertNotEquals(
                    message,
                    produceFingerPrint(inBitmap),
                    produceFingerPrint(appliedBitmap),
                )

                val reversedBitmap = exifOrientation.applyToTileBitmap(
                    tileBitmap = AndroidTileBitmap(appliedBitmap, "", BitmapFrom.LOCAL),
                    reverse = true
                ).bitmap
                Assert.assertEquals(
                    message,
                    produceFingerPrint(inBitmap),
                    produceFingerPrint(reversedBitmap),
                )
            } else {
                Assert.assertEquals(
                    message,
                    produceFingerPrint(inBitmap),
                    produceFingerPrint(appliedBitmap),
                )
            }
        }
    }
}