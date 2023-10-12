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

import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.checkSupportSubsamplingByMimeType
import com.github.panpf.zoomimage.createTileDecoder
import com.github.panpf.zoomimage.decodeExifOrientation
import com.github.panpf.zoomimage.decodeImageInfo
import com.github.panpf.zoomimage.subsampling.internal.format
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import kotlin.math.abs


/**
 * Create [TileDecoder]. If the image type is not supported or the thumbnail size is larger than the original image or the aspect ratio of the thumbnail and the original image is inconsistent, the creation will fail.
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.CoreSubsamplingUtilsTest.testCreateTileDecoder]
 */
@WorkerThread
fun decodeAndCreateTileDecoder(
    logger: Logger,
    imageSource: ImageSource,
    thumbnailSize: IntSizeCompat,
    ignoreExifOrientation: Boolean,
    tileBitmapReuseHelper: TileBitmapReuseHelper?,
): Result<TileDecoder> {
    val exifOrientation = if (!ignoreExifOrientation) {
        decodeExifOrientation(imageSource = imageSource).getOrNull()
    } else {
        null
    }

    val imageInfoResult = decodeImageInfo(imageSource = imageSource)
    val imageInfo = imageInfoResult.getOrNull()?.let {
        exifOrientation?.applyToImageInfo(it) ?: it
    }

    if (imageInfo == null) {
        val message = imageInfoResult.exceptionOrNull()!!.message.orEmpty()
        return Result.failure(CreateTileDecoderException(-1, false, message, null))
    }
    if (!checkSupportSubsamplingByMimeType(imageInfo.mimeType)) {
        val message = "Image type not support subsampling"
        return Result.failure(CreateTileDecoderException(-2, true, message, imageInfo))
    }
    if (thumbnailSize.width >= imageInfo.width && thumbnailSize.height >= imageInfo.height) {
        val message = "The thumbnail size is greater than or equal to the original image"
        return Result.failure(CreateTileDecoderException(-3, true, message, imageInfo))
    }
    if (!canUseSubsamplingByAspectRatio(imageInfo.size, thumbnailSize = thumbnailSize)) {
        val message = "The thumbnail aspect ratio is different with the original image"
        return Result.failure(CreateTileDecoderException(-4, false, message, imageInfo))
    }

    val result = createTileDecoder(
        logger = logger,
        imageSource = imageSource,
        imageInfo = imageInfo,
        tileBitmapReuseHelper = tileBitmapReuseHelper,
        exifOrientation = exifOrientation
    )
    val createDecoderException = result.exceptionOrNull()
    if (createDecoderException != null) {
        val message = createDecoderException.message.orEmpty()
        return Result.failure(CreateTileDecoderException(-5, false, message, imageInfo))
    }
    return Result.success(result.getOrThrow())
}

/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileDecodeUtilsTest.testCanUseSubsamplingByAspectRatio]
 */
internal fun canUseSubsamplingByAspectRatio(
    imageSize: IntSizeCompat,
    thumbnailSize: IntSizeCompat,
    minDifference: Float = 0.5f
): Boolean {
    if (imageSize.isEmpty() || thumbnailSize.isEmpty()) return false
    val widthScale = imageSize.width / thumbnailSize.width.toFloat()
    val heightScale = imageSize.height / thumbnailSize.height.toFloat()
    return abs(widthScale - heightScale).format(2) <= minDifference.format(2)
}

class CreateTileDecoderException(
    val code: Int, val skipped: Boolean, message: String, val imageInfo: ImageInfo?
) : Exception(message)

/**
 * Returns a string consisting of sample size, number of tiles, and grid size
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.CoreSubsamplingUtilsTest.testToIntroString]
 */
fun Map<Int, List<Tile>>.toIntroString(): String {
    return entries.joinToString(
        prefix = "[",
        postfix = "]",
        separator = ","
    ) { (sampleSize, tiles) ->
        val gridSize = tiles.last().coordinate.let { IntOffsetCompat(it.x + 1, it.y + 1) }
        "${sampleSize}:${tiles.size}:${gridSize.toShortString()}"
    }
}

fun exifOrientationName(exifOrientation: Int): String =
    when (exifOrientation) {
        6 -> "ROTATE_90" //        ExifInterface.ORIENTATION_ROTATE_90
        5 -> "TRANSPOSE" //        ExifInterface.ORIENTATION_TRANSPOSE
        3 -> "ROTATE_180" //        ExifInterface.ORIENTATION_ROTATE_180
        4 -> "FLIP_VERTICAL" //        ExifInterface.ORIENTATION_FLIP_VERTICAL
        8 -> "ROTATE_270" //        ExifInterface.ORIENTATION_ROTATE_270
        7 -> "TRANSVERSE" //        ExifInterface.ORIENTATION_TRANSVERSE
        2 -> "FLIP_HORIZONTAL" //        ExifInterface.ORIENTATION_FLIP_HORIZONTAL
        0 -> "UNDEFINED" //        ExifInterface.ORIENTATION_UNDEFINED
        1 -> "NORMAL" //        ExifInterface.ORIENTATION_NORMAL
        else -> exifOrientation.toString()
    }