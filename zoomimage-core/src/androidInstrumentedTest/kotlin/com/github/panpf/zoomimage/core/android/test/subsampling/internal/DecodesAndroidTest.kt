package com.github.panpf.zoomimage.core.android.test.subsampling.internal

import android.os.Build
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.internal.BitmapRegionDecoderDecodeHelper
import com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper
import com.github.panpf.zoomimage.subsampling.internal.applyToImageInfo
import com.github.panpf.zoomimage.subsampling.internal.checkSupportSubsamplingByMimeType
import com.github.panpf.zoomimage.subsampling.internal.createDecodeHelper
import com.github.panpf.zoomimage.subsampling.internal.decodeExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.decodeImageInfo
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
        val decodeHelper = createDecodeHelper(ResourceImages.dog.toImageSource())
        assertTrue(decodeHelper is BitmapRegionDecoderDecodeHelper)
    }

    @Test
    fun testCheckSupportSubsamplingByMimeType() {
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

    @Test
    fun testDecodeExifOrientation() = runTest {
        ResourceImages.values.forEach {
            assertEquals(
                expected = it.exifOrientation,
                actual = it.toImageSource().decodeExifOrientation(),
                message = it.toString(),
            )
        }
    }

    @Test
    fun testDecodeImageInfo() = runTest {
        ResourceImages.values.forEach { imageFile ->
            val mimeType = MimeTypeMap.getMimeTypeFromUrl(imageFile.uri)!!
            assertEquals(
                expected = ImageInfo(imageFile.size.toIntSizeCompat(), mimeType),
                actual = imageFile.toImageSource().decodeImageInfo().let {
                    ExifOrientationHelper(imageFile.exifOrientation).applyToImageInfo(it)
                },
                message = imageFile.toString(),
            )
        }
    }

    private operator fun IntSizeCompat.minus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width - other.width, this.height - other.height)
    }

    private operator fun IntSizeCompat.plus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width + other.width, this.height + other.height)
    }
}