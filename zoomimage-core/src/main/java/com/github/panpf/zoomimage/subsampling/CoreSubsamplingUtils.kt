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
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.internal.applyExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.isSupportBitmapRegionDecoder
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.internal.requiredWorkThread
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import kotlin.math.abs

@WorkerThread
fun ImageSource.readImageBounds(): Result<BitmapFactory.Options> {
    requiredWorkThread()
    return openInputStream()
        .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
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

@WorkerThread
fun ImageSource.readExifOrientation(): Result<Int> {
    requiredWorkThread()
    val orientationUndefined = ExifInterface.ORIENTATION_UNDEFINED
    return openInputStream()
        .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
        .use { inputStream ->
            kotlin.runCatching {
                ExifInterface(inputStream)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, orientationUndefined)
            }
        }
}

@WorkerThread
fun ImageSource.readExifOrientationWithMimeType(mimeType: String): Result<Int> {
    requiredWorkThread()
    return if (ExifInterface.isSupportedMimeType(mimeType)) {
        readExifOrientation()
    } else {
        Result.success(ExifInterface.ORIENTATION_UNDEFINED)
    }
}

@WorkerThread
fun ImageSource.readImageInfo(ignoreExifOrientation: Boolean): Result<ImageInfo> {
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

fun checkUseSubsampling(imageInfo: ImageInfo, thumbnailSize: IntSizeCompat): Int {
    if (thumbnailSize.width >= imageInfo.width && thumbnailSize.height >= imageInfo.height) {
        return -1
    }
    if (!canUseSubsamplingByAspectRatio(imageInfo.size, thumbnailSize = thumbnailSize)) {
        return -2
    }
    if (!isSupportBitmapRegionDecoder(imageInfo.mimeType)) {
        return -3
    }
    return 0
}

fun canUseSubsamplingByAspectRatio(
    imageSize: IntSizeCompat,
    thumbnailSize: IntSizeCompat,
    minDifference: Float = 0.5f
): Boolean {
    if (imageSize.isEmpty() || thumbnailSize.isEmpty()) return false
    val widthScale = imageSize.width / thumbnailSize.width.toFloat()
    val heightScale = imageSize.height / thumbnailSize.height.toFloat()
    return abs(widthScale - heightScale).format(2) <= minDifference.format(2)
}

fun Map<Int, List<Tile>>.toIntroString(): String {
    return entries.joinToString(
        prefix = "[",
        postfix = "]",
        separator = ","
    ) { (sampleSize, tiles) ->
        val tableSize = tiles.last().coordinate.let { IntOffsetCompat(it.x + 1, it.y + 1) }
        "${sampleSize}:${tiles.size}:${tableSize.toShortString()}"
    }
}