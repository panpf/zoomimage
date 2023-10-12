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
import com.github.panpf.zoomimage.subsampling.exifOrientationName
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

        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerA, cornerB, cornerC) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally and apply ORIENTATION_ROTATE_90
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerB, cornerA, cornerD) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerD, cornerA, cornerB) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally and apply ORIENTATION_ROTATE_180
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerC, cornerB, cornerA) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerC, cornerD, cornerA) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally and apply ORIENTATION_ROTATE_270
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerA, cornerD, cornerC, cornerB) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerA, cornerD, cornerC) }.toString(),
                )
            }
        Assert.assertNull(
            AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED).applyToBitmap(inBitmap)
        )
        Assert.assertNull(
            AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL).applyToBitmap(inBitmap)
        )
        Assert.assertNull(
            AndroidExifOrientation(-1).applyToBitmap(inBitmap)
        )
        Assert.assertNull(
            AndroidExifOrientation(100).applyToBitmap(inBitmap)
        )
    }

    @Test
    fun testAddToBitmap() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inBitmap = context.assets.open("sample_dog.jpg").use {
            BitmapFactory.decodeStream(it)
        }
        Assert.assertTrue(
            inBitmap.cornerA != inBitmap.cornerB
                    && inBitmap.cornerA != inBitmap.cornerC
                    && inBitmap.cornerA != inBitmap.cornerD
        )

        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerC, cornerD, cornerA) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally based on ORIENTATION_ROTATE_90
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerB, cornerA, cornerD) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerD, cornerA, cornerB) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally based on ORIENTATION_ROTATE_180
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerC, cornerB, cornerA) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerA, cornerB, cornerC) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally based on ORIENTATION_ROTATE_270
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerA, cornerD, cornerC, cornerB) }.toString(),
                )
            }
        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerA, cornerD, cornerC) }.toString(),
                )
            }
        Assert.assertNull(
            AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED)
                .addToBitmap(inBitmap)
        )
        Assert.assertNull(
            AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL)
                .addToBitmap(inBitmap)
        )
        Assert.assertNull(
            AndroidExifOrientation(-1).addToBitmap(inBitmap)
        )
        Assert.assertNull(
            AndroidExifOrientation(100).addToBitmap(inBitmap)
        )
    }

    @Test
    fun testAddAndApplyToBitmap() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inBitmap = context.assets.open("sample_dog.jpg").use {
            BitmapFactory.decodeStream(it)
        }
        Assert.assertTrue(
            inBitmap.cornerA != inBitmap.cornerB
                    && inBitmap.cornerA != inBitmap.cornerC
                    && inBitmap.cornerA != inBitmap.cornerD
        )

        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)
                .addToBitmap(inBitmap)!!,
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).applyToBitmap(
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }
    }

    @Test
    fun testApplyToSize() {
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(-1).applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            AndroidExifOrientation(100).applyToSize(IntSizeCompat(100, 50))
        )
    }

    @Test
    fun testAddToSize() {
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90).apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE).apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE).apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(-1).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        AndroidExifOrientation(100).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
    }

    @Test
    fun testAddToRect() {
        Assert.assertEquals(
            IntRectCompat(10, 50, 30, 60),
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(20, 50, 40, 60),
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(50, 20, 60, 40),
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_180)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 20, 50, 40),
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(20, 40, 40, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_ROTATE_270)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(10, 40, 30, 50),
            AndroidExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(50, 10, 60, 30),
            AndroidExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            AndroidExifOrientation(ExifInterface.ORIENTATION_UNDEFINED)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            AndroidExifOrientation(ExifInterface.ORIENTATION_NORMAL)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            AndroidExifOrientation(-1)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            AndroidExifOrientation(100)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
    }

    @Test
    fun testExifOrientationName() {
        Assert.assertEquals("ROTATE_90", exifOrientationName(ExifInterface.ORIENTATION_ROTATE_90))
        Assert.assertEquals("TRANSPOSE", exifOrientationName(ExifInterface.ORIENTATION_TRANSPOSE))
        Assert.assertEquals("ROTATE_180", exifOrientationName(ExifInterface.ORIENTATION_ROTATE_180))
        Assert.assertEquals(
            "FLIP_VERTICAL",
            exifOrientationName(ExifInterface.ORIENTATION_FLIP_VERTICAL)
        )
        Assert.assertEquals("ROTATE_270", exifOrientationName(ExifInterface.ORIENTATION_ROTATE_270))
        Assert.assertEquals("TRANSVERSE", exifOrientationName(ExifInterface.ORIENTATION_TRANSVERSE))
        Assert.assertEquals(
            "FLIP_HORIZONTAL",
            exifOrientationName(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
        )
        Assert.assertEquals("UNDEFINED", exifOrientationName(ExifInterface.ORIENTATION_UNDEFINED))
        Assert.assertEquals("NORMAL", exifOrientationName(ExifInterface.ORIENTATION_NORMAL))
        Assert.assertEquals("-1", exifOrientationName(-1))
        Assert.assertEquals("100", exifOrientationName(100))
    }
}