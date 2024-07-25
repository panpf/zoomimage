/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.subsampling.internal

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import okio.buffer
import okio.use
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Create a [DecodeHelper] instance using [ImageSource], on the Android platform, [BitmapRegionDecoderDecodeHelper] will be used
 *
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testCreateDecodeHelper]
 */
internal actual fun createDecodeHelper(imageSource: ImageSource): DecodeHelper {
    return BitmapRegionDecoderDecodeHelper.Factory().create(imageSource)
}

/**
 * Decode the Exif orientation of the image
 *
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDecodeExifOrientation]
 */
@WorkerThread
internal fun ImageSource.decodeExifOrientation(): Int {
    val exifOrientation = openSource().buffer().inputStream().use {
        ExifInterface(it).getAttributeInt(
            /* tag = */ ExifInterface.TAG_ORIENTATION,
            /* defaultValue = */ ExifInterface.ORIENTATION_UNDEFINED
        )
    }
    return exifOrientation
}


/**
 * Decode the image width and height and mimeType
 *
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDecodeImageInfo]
 */
internal fun ImageSource.decodeImageInfo(): ImageInfo {
    val boundOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    openSource().buffer().inputStream().use {
        BitmapFactory.decodeStream(it, null, boundOptions)
    }
    val mimeType = boundOptions.outMimeType.orEmpty()
    val imageSize = IntSizeCompat(
        width = boundOptions.outWidth,
        height = boundOptions.outHeight
    )
    return ImageInfo(size = imageSize, mimeType = mimeType)
}

/**
 * If true, indicates that the given mimeType can be using 'inBitmap' in BitmapRegionDecoder
 *
 * Test results based on the BitmapRegionDecoderTest.testInBitmapAndInSampleSize() method
 *
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testIsSupportInBitmapForRegion]
 */
@SuppressLint("ObsoleteSdkInt")
internal fun isSupportInBitmapForRegion(mimeType: String?): Boolean =
    when {
        "image/jpeg".equals(mimeType, true) -> VERSION.SDK_INT >= 16
        "image/png".equals(mimeType, true) -> VERSION.SDK_INT >= 16
        "image/gif".equals(mimeType, true) -> false
        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 16
//        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 26 animated
        "image/bmp".equals(mimeType, true) -> false
        "image/heic".equals(mimeType, true) -> VERSION.SDK_INT >= 28
        "image/heif".equals(mimeType, true) -> VERSION.SDK_INT >= 28
        else -> VERSION.SDK_INT >= 32   // Compatible with new image types supported in the future
    }

/**
 * Calculate the size of the sampled Bitmap, support for BitmapRegionDecoder
 *
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testCalculateSampledBitmapSizeForRegion]
 */
internal fun calculateSampledBitmapSizeForRegion(
    regionSize: IntSizeCompat,
    sampleSize: Int,
    mimeType: String? = null,
    imageSize: IntSizeCompat? = null
): IntSizeCompat {
    val widthValue = regionSize.width / sampleSize.toDouble()
    val heightValue = regionSize.height / sampleSize.toDouble()
    val width: Int
    val height: Int
    val isPNGFormat = "image/png".equals(mimeType, true)
    if (!isPNGFormat && VERSION.SDK_INT >= VERSION_CODES.N && regionSize == imageSize) {
        width = ceil(widthValue).toInt()
        height = ceil(heightValue).toInt()
    } else {
        width = floor(widthValue).toInt()
        height = floor(heightValue).toInt()
    }
    return IntSizeCompat(width, height)
}

/**
 * Checks whether the specified image type supports subsampling, on the Android platform, it mainly depends on the types supported by BitmapRegionDecoder.
 *
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testIsSupportBitmapRegionDecoder]
 */
@SuppressLint("ObsoleteSdkInt")
internal actual fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean =
    "image/jpeg".equals(mimeType, true)
            || "image/png".equals(mimeType, true)
            || "image/webp".equals(mimeType, true)
            || ("image/heic".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)
            || ("image/heif".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)