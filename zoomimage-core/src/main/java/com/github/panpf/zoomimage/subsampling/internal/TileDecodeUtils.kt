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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

internal fun Bitmap.Config.isAndSupportHardware(): Boolean =
    VERSION.SDK_INT >= VERSION_CODES.O && this == Bitmap.Config.HARDWARE

///**
// * If true, indicates that the given mimeType and sampleSize combination can be using 'inBitmap' in BitmapFactory
// *
// * Test results based on the BitmapFactoryTest.testInBitmapAndInSampleSize() method
// */
//@SuppressLint("ObsoleteSdkInt")
//internal fun isSupportInBitmap(mimeType: String?, sampleSize: Int): Boolean =
//    when {
//        "image/jpeg".equals(mimeType, true) ->
//            if (sampleSize == 1) VERSION.SDK_INT >= 16 else VERSION.SDK_INT >= 19
//
//        "image/png".equals(mimeType, true) ->
//            if (sampleSize == 1) VERSION.SDK_INT >= 16 else VERSION.SDK_INT >= 19
//
//        "image/gif".equals(mimeType, true) ->
//            if (sampleSize == 1) VERSION.SDK_INT >= 19 else VERSION.SDK_INT >= 21
//
//        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 19
////        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 26 animated
//        "image/bmp".equals(mimeType, true) -> VERSION.SDK_INT >= 19
//        "image/heic".equals(mimeType, true) -> false
//        "image/heif".equals(mimeType, true) -> VERSION.SDK_INT >= 28
//        else -> VERSION.SDK_INT >= 32   // Compatible with new image types supported in the future
//    }

/**
 * If true, indicates that the given mimeType can be using 'inBitmap' in BitmapRegionDecoder
 *
 * Test results based on the BitmapRegionDecoderTest.testInBitmapAndInSampleSize() method
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

///**
// * Calculate the size of the sampled Bitmap, support for BitmapFactory or ImageDecoder
// */
//internal fun calculateSampledBitmapSize(
//    imageSize: IntSizeCompat, sampleSize: Int, mimeType: String? = null
//): IntSizeCompat {
//    val widthValue = imageSize.width / sampleSize.toDouble()
//    val heightValue = imageSize.height / sampleSize.toDouble()
//    val isPNGFormat = "image/png".equals(mimeType, true)
//    val width: Int
//    val height: Int
//    if (isPNGFormat) {
//        width = floor(widthValue).toInt()
//        height = floor(heightValue).toInt()
//    } else {
//        width = ceil(widthValue).toInt()
//        height = ceil(heightValue).toInt()
//    }
//    return IntSizeCompat(width, height)
//}


/**
 * Calculate the size of the sampled Bitmap, support for BitmapRegionDecoder
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

suspend fun ImageSource.readImageBounds(): Result<BitmapFactory.Options> {
    return withContext(Dispatchers.IO) {
        openInputStream()
            .let { it.getOrNull() ?: return@withContext Result.failure(it.exceptionOrNull()!!) }
            .use { inputStream ->
                kotlin.runCatching {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, options)
                    require(options.outWidth > 0 && options.outHeight > 0) {
                        "image width or height is error: ${options.outWidth}x${options.outHeight}"
                    }
                    options
                }
            }
    }
}

suspend fun ImageSource.readExifOrientation(): Result<Int> {
    val orientationUndefined = ExifInterface.ORIENTATION_UNDEFINED
    return withContext(Dispatchers.IO) {
        openInputStream()
            .let { it.getOrNull() ?: return@withContext Result.failure(it.exceptionOrNull()!!) }
            .use { inputStream ->
                kotlin.runCatching {
                    ExifInterface(inputStream)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, orientationUndefined)
                }
            }
    }
}

suspend fun ImageSource.readImageInfo(ignoreExifOrientation: Boolean): Result<ImageInfo> {
    val options = readImageBounds()
        .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
    val exifOrientation = if (ignoreExifOrientation) {
        ExifInterface.ORIENTATION_UNDEFINED
    } else {
        readExifOrientation()
            .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
    }
    val imageInfo = ImageInfo(
        size = IntSizeCompat(options.outWidth, options.outHeight),
        mimeType = options.outMimeType,
        exifOrientation = exifOrientation,
    ).applyExifOrientation()
    return Result.success(imageInfo)
}

internal fun isInBitmapError(throwable: Throwable): Boolean =
    if (throwable is IllegalArgumentException) {
        val message = throwable.message.orEmpty()
        (message == "Problem decoding into existing bitmap" || message.contains("bitmap"))
    } else {
        false
    }

internal fun isSrcRectError(throwable: Throwable): Boolean =
    if (throwable is IllegalArgumentException) {
        val message = throwable.message.orEmpty()
        message == "rectangle is outside the image srcRect" || message.contains("srcRect")
    } else {
        false
    }

@SuppressLint("ObsoleteSdkInt")
internal fun isSupportBitmapRegionDecoder(mimeType: String): Boolean =
    "image/jpeg".equals(mimeType, true)
            || "image/png".equals(mimeType, true)
            || "image/webp".equals(mimeType, true)
            || ("image/heic".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)
            || ("image/heif".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)