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

package com.github.panpf.zoomimage.subsampling

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.internal.applyExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.isSupportBitmapRegionDecoder
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.isEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

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

fun canUseSubsampling(imageInfo: ImageInfo, drawableSize: IntSizeCompat): Int {
    if (drawableSize.width >= imageInfo.width && drawableSize.height >= imageInfo.height) {
        return -1
    }
    if (!canUseSubsamplingByAspectRatio(imageSize = imageInfo.size, drawableSize = drawableSize)) {
        return -2
    }
    if (!isSupportBitmapRegionDecoder(imageInfo.mimeType)) {
        return -3
    }
    return 0
}

fun canUseSubsamplingByAspectRatio(
    imageSize: IntSizeCompat,
    drawableSize: IntSizeCompat,
    minDifference: Float = 0.5f
): Boolean {
    if (imageSize.isEmpty() || drawableSize.isEmpty()) return false
    val imageAspectRatio = imageSize.width / imageSize.height.toFloat()
    val drawableAspectRatio = drawableSize.width / drawableSize.height.toFloat()
    return abs(imageAspectRatio - drawableAspectRatio).format(2) <= minDifference.format(2)
}