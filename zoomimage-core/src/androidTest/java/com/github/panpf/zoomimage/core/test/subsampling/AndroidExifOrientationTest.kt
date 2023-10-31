package com.github.panpf.zoomimage.core.test.subsampling

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.cornerA
import com.github.panpf.zoomimage.core.test.internal.cornerB
import com.github.panpf.zoomimage.core.test.internal.cornerC
import com.github.panpf.zoomimage.core.test.internal.cornerD
import com.github.panpf.zoomimage.core.test.internal.corners
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.DefaultAndroidTileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert
import org.junit.Test

class AndroidExifOrientationTest {

    @Test
    fun testConstructor() {
        AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED).apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exifOrientation)
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_270, exifOrientation)
        }
    }

    @Test
    fun testName() {
        Assert.assertEquals(
            "ROTATE_90",
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90).name()
        )
        Assert.assertEquals(
            "TRANSPOSE",
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE).name()
        )
        Assert.assertEquals(
            "ROTATE_180",
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180).name()
        )
        Assert.assertEquals(
            "FLIP_VERTICAL",
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL).name()
        )
        Assert.assertEquals(
            "ROTATE_270",
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).name()
        )
        Assert.assertEquals(
            "TRANSVERSE",
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE).name()
        )
        Assert.assertEquals(
            "FLIP_HORIZONTAL",
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).name()
        )
        Assert.assertEquals(
            "UNDEFINED",
            AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED).name()
        )
        Assert.assertEquals(
            "NORMAL",
            AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL).name()
        )
        Assert.assertEquals("-1", AndroidExifOrientation(-1).name())
        Assert.assertEquals("100", AndroidExifOrientation(100).name())
    }

    @Test
    fun testIsFlipped() {
        Assert.assertFalse(AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90).isFlipped)
        Assert.assertTrue(AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE).isFlipped)
        Assert.assertFalse(AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180).isFlipped)
        Assert.assertTrue(AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL).isFlipped)
        Assert.assertFalse(AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).isFlipped)
        Assert.assertTrue(AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE).isFlipped)
        Assert.assertTrue(AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).isFlipped)
        Assert.assertFalse(AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED).isFlipped)
        Assert.assertFalse(AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL).isFlipped)
        Assert.assertFalse(AndroidExifOrientation(-1).isFlipped)
        Assert.assertFalse(AndroidExifOrientation(100).isFlipped)
    }

    @Test
    fun testRotationDegrees() {
        Assert.assertEquals(
            90,
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90).rotationDegrees
        )
        Assert.assertEquals(
            270,
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE).rotationDegrees
        )
        Assert.assertEquals(
            180,
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180).rotationDegrees
        )
        Assert.assertEquals(
            180,
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL).rotationDegrees
        )
        Assert.assertEquals(
            270,
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).rotationDegrees
        )
        Assert.assertEquals(
            90,
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE).rotationDegrees
        )
        Assert.assertEquals(
            0,
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).rotationDegrees
        )
        Assert.assertEquals(
            0,
            AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED).rotationDegrees
        )
        Assert.assertEquals(
            0,
            AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL).rotationDegrees
        )
        Assert.assertEquals(0, AndroidExifOrientation(-1).rotationDegrees)
        Assert.assertEquals(0, AndroidExifOrientation(100).rotationDegrees)
    }

    @Test
    fun testApplyToSize() {
        val size = IntSizeCompat(100, 50)
        listOf(
            ExifInterface.ORIENTATION_ROTATE_90 to IntSizeCompat(50, 100),
            ExifInterface.ORIENTATION_TRANSVERSE to IntSizeCompat(50, 100),
            ExifInterface.ORIENTATION_ROTATE_180 to IntSizeCompat(100, 50),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to IntSizeCompat(100, 50),
            ExifInterface.ORIENTATION_ROTATE_270 to IntSizeCompat(50, 100),
            ExifInterface.ORIENTATION_TRANSPOSE to IntSizeCompat(50, 100),
            ExifInterface.ORIENTATION_UNDEFINED to IntSizeCompat(100, 50),
            ExifInterface.ORIENTATION_NORMAL to IntSizeCompat(100, 50),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to IntSizeCompat(100, 50),
            -1 to IntSizeCompat(100, 50),
            100 to IntSizeCompat(100, 50),
        ).forEach { (exifOrientationInt, exceptedSize) ->
            val exifOrientation = AndroidExifOrientation(exifOrientationInt)
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
            ExifInterface.ORIENTATION_ROTATE_90 to IntRectCompat(20, 40, 40, 50),
            ExifInterface.ORIENTATION_TRANSVERSE to IntRectCompat(20, 50, 40, 60),
            ExifInterface.ORIENTATION_ROTATE_180 to IntRectCompat(50, 20, 60, 40),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to IntRectCompat(40, 20, 50, 40),
            ExifInterface.ORIENTATION_ROTATE_270 to IntRectCompat(10, 50, 30, 60),
            ExifInterface.ORIENTATION_TRANSPOSE to IntRectCompat(10, 40, 30, 50),
            ExifInterface.ORIENTATION_UNDEFINED to IntRectCompat(40, 10, 50, 30),
            ExifInterface.ORIENTATION_NORMAL to IntRectCompat(40, 10, 50, 30),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to IntRectCompat(50, 10, 60, 30),
            -1 to IntRectCompat(40, 10, 50, 30),
            100 to IntRectCompat(40, 10, 50, 30),
        ).forEach { (exifOrientationInt, exceptedRect) ->
            val exifOrientation = AndroidExifOrientation(exifOrientationInt)
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
        val context = InstrumentationRegistry.getInstrumentation().context
        val inBitmap = context.assets.open("sample_dog.jpg").use {
            BitmapFactory.decodeStream(it)
        }
        Assert.assertTrue(
            inBitmap.cornerA != inBitmap.cornerB
                    && inBitmap.cornerA != inBitmap.cornerC
                    && inBitmap.cornerA != inBitmap.cornerD
        )

        listOf(
            ExifInterface.ORIENTATION_ROTATE_90 to true,
            ExifInterface.ORIENTATION_TRANSVERSE to true,
            ExifInterface.ORIENTATION_ROTATE_180 to true,
            ExifInterface.ORIENTATION_FLIP_VERTICAL to true,
            ExifInterface.ORIENTATION_ROTATE_270 to true,
            ExifInterface.ORIENTATION_TRANSPOSE to true,
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to true,
            ExifInterface.ORIENTATION_NORMAL to false,
            ExifInterface.ORIENTATION_UNDEFINED to false,
            -1 to false,
            100 to false,
        ).forEach { (exifOrientationInt, change) ->
            val exifOrientation = AndroidExifOrientation(exifOrientationInt)
            val message = "exifOrientationInt=${exifOrientation.name()}"

            val appliedBitmap = exifOrientation.applyToTileBitmap(
                DefaultAndroidTileBitmap(inBitmap),
                reverse = false
            ).let { it as AndroidTileBitmap }.bitmap!!
            if (change) {
                Assert.assertNotEquals(
                    message,
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    appliedBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                )

                val reversedBitmap = exifOrientation.applyToTileBitmap(
                    DefaultAndroidTileBitmap(appliedBitmap),
                    reverse = true
                ).let { it as AndroidTileBitmap }.bitmap!!
                Assert.assertEquals(
                    message,
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    reversedBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }
                        .toString(),
                )
            } else {
                Assert.assertEquals(
                    message,
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    appliedBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                )
            }
        }
    }
}