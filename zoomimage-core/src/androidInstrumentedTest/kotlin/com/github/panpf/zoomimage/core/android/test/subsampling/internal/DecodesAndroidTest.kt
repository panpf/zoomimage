package com.github.panpf.zoomimage.core.android.test.subsampling.internal

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.internal.BitmapRegionDecoderDecodeHelper
import com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper
import com.github.panpf.zoomimage.subsampling.internal.applyToImageInfo
import com.github.panpf.zoomimage.subsampling.internal.calculateSampledBitmapSizeForRegion
import com.github.panpf.zoomimage.subsampling.internal.checkSupportSubsamplingByMimeType
import com.github.panpf.zoomimage.subsampling.internal.createDecodeHelper
import com.github.panpf.zoomimage.subsampling.internal.decodeExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.decodeImageInfo
import com.github.panpf.zoomimage.subsampling.internal.isSupportInBitmapForRegion
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.toIntSizeCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecodesAndroidTest {

    @Test
    fun testCreateDecodeHelper() {
        val context = InstrumentationRegistry.getInstrumentation().context

        val decodeHelper = createDecodeHelper(ResourceImages.dog.toImageSource(context))
        assertTrue(decodeHelper is BitmapRegionDecoderDecodeHelper)
    }

    @Test
    fun testDecodeExifOrientation() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        ResourceImages.values.forEach {
            assertEquals(
                expected = it.exifOrientation,
                actual = it.toImageSource(context).decodeExifOrientation(),
                message = it.toString(),
            )
        }
    }

    @Test
    fun testDecodeImageInfo() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        ResourceImages.values.forEach { imageFile ->
            val mimeType = MimeTypeMap.getMimeTypeFromUrl(imageFile.uri)!!
            assertEquals(
                expected = ImageInfo(imageFile.size.toIntSizeCompat(), mimeType),
                actual = imageFile.toImageSource(context).decodeImageInfo().let {
                    ExifOrientationHelper(imageFile.exifOrientation).applyToImageInfo(it)
                },
                message = imageFile.toString(),
            )
        }
    }

    @Test
    fun testIsSupportInBitmapForRegion() {
        assertEquals(Build.VERSION.SDK_INT >= 16, isSupportInBitmapForRegion("image/jpeg"))
        assertEquals(Build.VERSION.SDK_INT >= 16, isSupportInBitmapForRegion("image/png"))
        assertEquals(false, isSupportInBitmapForRegion("image/gif"))
        assertEquals(Build.VERSION.SDK_INT >= 16, isSupportInBitmapForRegion("image/webp"))
        assertEquals(false, isSupportInBitmapForRegion("image/bmp"))
        assertEquals(Build.VERSION.SDK_INT >= 28, isSupportInBitmapForRegion("image/heic"))
        assertEquals(Build.VERSION.SDK_INT >= 28, isSupportInBitmapForRegion("image/heif"))
        assertEquals(Build.VERSION.SDK_INT >= 32, isSupportInBitmapForRegion("image/svg"))
    }

    @Test
    fun testCalculateSampledBitmapSizeForRegion() {
        assertEquals(
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
        assertEquals(
            IntSizeCompat(502, 100),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(1005, 201),
                sampleSize = 2,
                mimeType = "image/png",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        assertEquals(
            IntSizeCompat(288, 100),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(577, 201),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        assertEquals(
            IntSizeCompat(502, 55),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(1005, 111),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        assertEquals(
            IntSizeCompat(288, 55),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(577, 111),
                sampleSize = 2,
                mimeType = "image/jpeg",
                imageSize = IntSizeCompat(1005, 201)
            )
        )
        assertEquals(
            IntSizeCompat(288, 55),
            calculateSampledBitmapSizeForRegion(
                regionSize = IntSizeCompat(577, 111),
                sampleSize = 2,
                mimeType = "image/jpeg",
            )
        )
    }

    @Test
    fun testIsSupportBitmapRegionDecoder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            assertTrue(checkSupportSubsamplingByMimeType("image/heic"))
        } else {
            assertFalse(checkSupportSubsamplingByMimeType("image/heic"))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            assertTrue(checkSupportSubsamplingByMimeType("image/heif"))
        } else {
            assertFalse(checkSupportSubsamplingByMimeType("image/heif"))
        }
        assertFalse(checkSupportSubsamplingByMimeType("image/bmp"))
        assertFalse(checkSupportSubsamplingByMimeType("image/gif"))
        assertTrue(checkSupportSubsamplingByMimeType("image/jpeg"))
        assertTrue(checkSupportSubsamplingByMimeType("image/png"))
        assertTrue(checkSupportSubsamplingByMimeType("image/webp"))
    }

    private operator fun IntSizeCompat.minus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width - other.width, this.height - other.height)
    }

    private operator fun IntSizeCompat.plus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width + other.width, this.height + other.height)
    }
}