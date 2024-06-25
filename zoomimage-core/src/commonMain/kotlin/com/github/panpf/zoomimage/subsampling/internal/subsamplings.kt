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

import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SamplingTiles
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import kotlin.math.abs


/**
 * Create [TileDecoder]. If the image type is not supported or the thumbnail size is larger than the original image or the aspect ratio of the thumbnail and the original image is inconsistent, the creation will fail.
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.SubsamplingUtilsTest2.testDecodeAndCreateTileDecoder]
 */
@WorkerThread
suspend fun decodeAndCreateTileDecoder(
    logger: Logger,
    imageSource: ImageSource,
    thumbnailSize: IntSizeCompat,
): Result<TileDecoder> {
    val decodeHelper = createDecodeHelper(imageSource)
    if (decodeHelper == null) {
        val message = "Unsupported image source: ${imageSource::class}"
        return Result.failure(CreateTileDecoderException(-1, false, message, null))
    }
    val imageInfo = try {
        decodeHelper.getImageInfo()
    } catch (e: Exception) {
        decodeHelper.close()
        return Result.failure(CreateTileDecoderException(-1, false, e.toString(), null))
    }
    if (imageInfo.width <= 0 || imageInfo.height <= 0) {
        decodeHelper.close()
        val message = "image width or height is error: ${imageInfo.width}x${imageInfo.height}"
        return Result.failure(CreateTileDecoderException(-2, true, message, imageInfo))
    }
    if (!decodeHelper.supportRegion()) {
        decodeHelper.close()
        val message = "Image type not support subsampling"
        return Result.failure(CreateTileDecoderException(-3, true, message, imageInfo))
    }
    if (thumbnailSize.width >= imageInfo.width && thumbnailSize.height >= imageInfo.height) {
        decodeHelper.close()
        val message = "The thumbnail size is greater than or equal to the original image"
        return Result.failure(CreateTileDecoderException(-4, true, message, imageInfo))
    }
    if (!canUseSubsamplingByAspectRatio(imageInfo.size, thumbnailSize = thumbnailSize)) {
        decodeHelper.close()
        val message = "The thumbnail aspect ratio is different with the original image"
        return Result.failure(CreateTileDecoderException(-5, false, message, imageInfo))
    }
    return Result.success(TileDecoder(logger, imageSource, decodeHelper))
}

/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.SubsamplingUtilsTest.testCanUseSubsamplingByAspectRatio]
 */
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

class CreateTileDecoderException(
    val code: Int, val skipped: Boolean, message: String, val imageInfo: ImageInfo?
) : Exception(message)

///**
// * Returns a string consisting of sample size, number of tiles, and grid size
// *
// * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.SubsamplingUtilsTest.testToIntroString]
// */
//fun Map<Int, List<Tile>>.toIntroString(): String {
//    return entries.joinToString(
//        prefix = "[",
//        postfix = "]",
//        separator = ","
//    ) { (sampleSize, tiles) ->
//        val gridSize = tiles.last().coordinate.let { IntOffsetCompat(it.x + 1, it.y + 1) }
//        "${sampleSize}:${tiles.size}:${gridSize.toShortString()}"
//    }
//}

/**
 * Returns a string consisting of sample size, number of tiles, and grid size
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.SubsamplingUtilsTest.testToIntroString]
 */
fun List<SamplingTiles>.toIntroString(): String {
    return joinToString(
        prefix = "[",
        postfix = "]",
        separator = ","
    ) {
        val gridSize = it.tiles.last().coordinate.let { IntOffsetCompat(it.x + 1, it.y + 1) }
        "${it.sampleSize}:${it.tiles.size}:${gridSize.toShortString()}"
    }
}

/**
 * Calculates the preferred size of the tile based on the container size, typically half the container size
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.SubsamplingUtilsTest.testCalculatePreferredTileSize]
 */
fun calculatePreferredTileSize(containerSize: IntSizeCompat): IntSizeCompat {
    return containerSize / 2
}