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
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import kotlin.math.ceil
import kotlin.math.floor

internal actual fun createDecodeHelper(imageSource: ImageSource): DecodeHelper? {
    return BitmapFactoryDecodeHelper(imageSource)
}

/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testReadExifOrientation]
 */
@WorkerThread
internal fun ImageSource.decodeExifOrientationValue(): Result<Int> {
    val inputStreamResult = openInputStream()
    if (inputStreamResult.isFailure) {
        return Result.failure(inputStreamResult.exceptionOrNull()!!)
    }
    val inputStream = inputStreamResult.getOrNull()!!
    val exifOrientation = try {
        inputStream.use {
            ExifInterface(it).getAttributeInt(
                /* tag = */ ExifInterface.TAG_ORIENTATION,
                /* defaultValue = */ ExifInterface.ORIENTATION_UNDEFINED
            )
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }
    return Result.success(exifOrientation)
}

/**
 * If true, indicates that the given mimeType can be using 'inBitmap' in BitmapRegionDecoder
 *
 * Test results based on the BitmapRegionDecoderTest.testInBitmapAndInSampleSize() method
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testIsSupportInBitmapForRegion]
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
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testCalculateSampledBitmapSizeForRegion]
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
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testIsInBitmapError]
 */
internal fun isInBitmapError(throwable: Throwable): Boolean =
    if (throwable is IllegalArgumentException) {
        val message = throwable.message.orEmpty()
        (message == "Problem decoding into existing bitmap" || message.contains("bitmap"))
    } else {
        false
    }

/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testIsSrcRectError]
 */
internal fun isSrcRectError(throwable: Throwable): Boolean =
    if (throwable is IllegalArgumentException) {
        val message = throwable.message.orEmpty()
        message == "rectangle is outside the image srcRect" || message.contains("srcRect")
    } else {
        false
    }

/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testIsSupportBitmapRegionDecoder]
 */
@SuppressLint("ObsoleteSdkInt")
internal fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean =
    "image/jpeg".equals(mimeType, true)
            || "image/png".equals(mimeType, true)
            || "image/webp".equals(mimeType, true)
            || ("image/heic".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)
            || ("image/heif".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)

actual fun createTileBitmapReuseHelper(
    logger: Logger,
    tileBitmapReuseSpec: TileBitmapReuseSpec,
): TileBitmapReuseHelper? = AndroidTileBitmapReuseHelper(logger, tileBitmapReuseSpec)