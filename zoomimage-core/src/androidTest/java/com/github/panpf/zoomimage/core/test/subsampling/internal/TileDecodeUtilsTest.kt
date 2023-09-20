package com.github.panpf.zoomimage.core.test.subsampling.internal

import android.os.Build
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.ExifOrientationTestFileHelper
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.internal.calculateSampledBitmapSizeForRegion
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.isInBitmapError
import com.github.panpf.zoomimage.subsampling.internal.isSrcRectError
import com.github.panpf.zoomimage.subsampling.internal.isSupportBitmapRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.isSupportInBitmapForRegion
import com.github.panpf.zoomimage.subsampling.internal.readExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.readImageBounds
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import org.junit.Assert
import org.junit.Test

class TileDecodeUtilsTest {

    @Test
    fun testReadImageBounds() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromAsset(context, "sample_dog.jpg").readImageBounds().getOrThrow().apply {
            Assert.assertEquals(575, outWidth)
            Assert.assertEquals(427, outHeight)
            Assert.assertEquals("image/jpeg", outMimeType)
        }

        ImageSource.fromAsset(context, "sample_cat.jpg").readImageBounds().getOrThrow().apply {
            Assert.assertEquals(551, outWidth)
            Assert.assertEquals(1038, outHeight)
            Assert.assertEquals("image/jpeg", outMimeType)
        }
    }

    @Test
    fun testReadExifOrientation() {
        val context = InstrumentationRegistry.getInstrumentation().context

        Assert.assertEquals(
            ExifInterface.ORIENTATION_NORMAL,
            ImageSource.fromAsset(context, "sample_dog.jpg").readExifOrientation().getOrThrow()
        )

        ExifOrientationTestFileHelper(context, "sample_dog.jpg").files().forEach {
            Assert.assertEquals(
                it.exifOrientation,
                ImageSource.fromFile(it.file).readExifOrientation().getOrThrow()
            )
        }
    }

    @Test
    fun testReadImageInfo() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromAsset(context, "sample_dog.jpg").readImageInfo(false).getOrThrow().apply {
            Assert.assertEquals(IntSizeCompat(575, 427), size)
            Assert.assertEquals("image/jpeg", mimeType)
            Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, exifOrientation)
        }

        ImageSource.fromAsset(context, "sample_cat.jpg").readImageInfo(false).getOrThrow().apply {
            Assert.assertEquals(IntSizeCompat(551, 1038), size)
            Assert.assertEquals("image/jpeg", mimeType)
            Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, exifOrientation)
        }

        ExifOrientationTestFileHelper(context, "sample_dog.jpg")
            .files()
            .find { it.exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 }!!
            .also {
                ImageSource.fromFile(it.file).readImageInfo(false).getOrThrow().apply {
                    Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exifOrientation)
                    Assert.assertEquals(IntSizeCompat(575, 427), size)
                    Assert.assertEquals("image/jpeg", mimeType)
                }

                ImageSource.fromFile(it.file).readImageInfo(true).getOrThrow().apply {
                    Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exifOrientation)
                    Assert.assertEquals(IntSizeCompat(427, 575), size)
                    Assert.assertEquals("image/jpeg", mimeType)
                }
            }
    }

    @Test
    fun testCanUseSubsamplingByAspectRatio() {
        val imageSize = IntSizeCompat(1000, 2000)

        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 17f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.5f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.3f, 17f))
        )
        Assert.assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.4f))
        )
        Assert.assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.6f, 17f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17f, 16.4f),
                minDifference = 0.8f
            )
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17.6f, 17f),
                minDifference = 0.8f
            )
        )
    }

    @Test
    fun testIsSupportInBitmapForRegion() {
        Assert.assertEquals(Build.VERSION.SDK_INT >= 16, isSupportInBitmapForRegion("image/jpeg"))
        Assert.assertEquals(Build.VERSION.SDK_INT >= 16, isSupportInBitmapForRegion("image/png"))
        Assert.assertEquals(false, isSupportInBitmapForRegion("image/gif"))
        Assert.assertEquals(Build.VERSION.SDK_INT >= 16, isSupportInBitmapForRegion("image/webp"))
        Assert.assertEquals(false, isSupportInBitmapForRegion("image/bmp"))
        Assert.assertEquals(Build.VERSION.SDK_INT >= 28, isSupportInBitmapForRegion("image/heic"))
        Assert.assertEquals(Build.VERSION.SDK_INT >= 28, isSupportInBitmapForRegion("image/heif"))
        Assert.assertEquals(Build.VERSION.SDK_INT >= 32, isSupportInBitmapForRegion("image/svg"))
    }

    @Test
    fun testCalculateSampledBitmapSizeForRegion() {
        Assert.assertEquals(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) IntSizeCompat(
                503,
                101
            ) else IntSizeCompat(502, 100),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(1005, 201),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        Assert.assertEquals(
            IntSizeCompat(502, 100),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(1005, 201),
                sampleSize = 2,
                mimeType = "image/png",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        Assert.assertEquals(
            IntSizeCompat(288, 100),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(577, 201),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        Assert.assertEquals(
            IntSizeCompat(502, 55),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(1005, 111),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        Assert.assertEquals(
            IntSizeCompat(288, 55),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(577, 111),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        Assert.assertEquals(
            IntSizeCompat(288, 55),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(577, 111),
                sampleSize = 2,
                mimeType = "image/jpeg",
            )
        )
    }

    @Test
    fun testIsInBitmapError() {
        Assert.assertTrue(
            isInBitmapError(IllegalArgumentException("Problem decoding into existing bitmap"))
        )
        Assert.assertTrue(
            isInBitmapError(IllegalArgumentException("bitmap"))
        )

        Assert.assertFalse(
            isInBitmapError(IllegalArgumentException("Problem decoding"))
        )
        Assert.assertFalse(
            isInBitmapError(IllegalStateException("Problem decoding into existing bitmap"))
        )
    }

    @Test
    fun testIsSrcRectError() {
        Assert.assertTrue(
            isSrcRectError(IllegalArgumentException("rectangle is outside the image srcRect"))
        )
        Assert.assertTrue(
            isSrcRectError(IllegalArgumentException("srcRect"))
        )

        Assert.assertFalse(
            isSrcRectError(IllegalStateException("rectangle is outside the image srcRect"))
        )
        Assert.assertFalse(
            isSrcRectError(IllegalArgumentException(""))
        )
    }

    @Test
    fun testIsSupportBitmapRegionDecoder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Assert.assertTrue(isSupportBitmapRegionDecoder("image/heic"))
        } else {
            Assert.assertFalse(isSupportBitmapRegionDecoder("image/heic"))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Assert.assertTrue(isSupportBitmapRegionDecoder("image/heif"))
        } else {
            Assert.assertFalse(isSupportBitmapRegionDecoder("image/heif"))
        }
        Assert.assertFalse(isSupportBitmapRegionDecoder("image/bmp"))
        Assert.assertFalse(isSupportBitmapRegionDecoder("image/gif"))
        Assert.assertTrue(isSupportBitmapRegionDecoder("image/jpeg"))
        Assert.assertTrue(isSupportBitmapRegionDecoder("image/png"))
        Assert.assertTrue(isSupportBitmapRegionDecoder("image/webp"))
    }

    private operator fun IntSizeCompat.minus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width - other.width, this.height - other.height)
    }

    private operator fun IntSizeCompat.plus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width + other.width, this.height + other.height)
    }
}