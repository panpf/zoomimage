package com.github.panpf.zoomimage.core.test.subsampling.internal

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.internal.calculateSampledBitmapSizeForRegion
import com.github.panpf.zoomimage.subsampling.internal.isInBitmapError
import com.github.panpf.zoomimage.subsampling.internal.isSrcRectError
import com.github.panpf.zoomimage.subsampling.internal.isSupportBitmapRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.isSupportInBitmapForRegion
import com.github.panpf.zoomimage.subsampling.internal.readExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert
import org.junit.Test

class AndroidTileDecodeUtilsTest {

    @Test
    fun testReadExifOrientation() {
        val context = InstrumentationRegistry.getInstrumentation().context

        listOf(
            "sample_dog.jpg" to ExifOrientation.ORIENTATION_NORMAL,
            "sample_exif_girl_rotate_90.jpg" to ExifOrientation.ORIENTATION_ROTATE_90,
        ).forEach { (assetPath, excepted) ->
            Assert.assertEquals(
                "assetPath=$assetPath, excepted=$excepted",
                excepted,
                ImageSource.fromAsset(context, assetPath).readExifOrientation()
                    .getOrThrow().exifOrientation
            )
        }
    }

    @Test
    fun testReadImageInfo() {
        val context = InstrumentationRegistry.getInstrumentation().context

        listOf(
            "sample_dog.jpg" to ImageInfo(575, 427, "image/jpeg"),
            "sample_exif_girl_rotate_90.jpg" to ImageInfo(6400, 1080, "image/jpeg"),
        ).forEach { (assetPath, excepted) ->
            Assert.assertEquals(
                "assetPath=$assetPath, excepted=$excepted",
                excepted,
                ImageSource.fromAsset(context, assetPath).readImageInfo().getOrThrow()
            )
        }
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