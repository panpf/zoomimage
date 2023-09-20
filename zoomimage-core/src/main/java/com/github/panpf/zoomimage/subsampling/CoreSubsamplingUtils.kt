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
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.isSupportBitmapRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.requiredWorkThread
import com.github.panpf.zoomimage.util.toShortString

/**
 * Create [TileDecoder]. If the image type is not supported or the thumbnail size is larger than the original image or the aspect ratio of the thumbnail and the original image is inconsistent, the creation will fail.
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.CoreSubsamplingUtilsTest.testCreateTileDecoder]
 */
@WorkerThread
fun createTileDecoder(
    logger: Logger,
    tileBitmapPoolHelper: TileBitmapPoolHelper,
    imageSource: ImageSource,
    thumbnailSize: IntSizeCompat,
    ignoreExifOrientation: Boolean
): Result<TileDecoder> {
    requiredWorkThread()
    val imageInfoResult = imageSource.readImageInfo(ignoreExifOrientation)
    val imageInfo = imageInfoResult.getOrNull()
    if (imageInfo == null) {
        val message = imageInfoResult.exceptionOrNull()!!.message.orEmpty()
        return Result.failure(CreateTileDecoderException(-1, false, message, null))
    }
    if (!isSupportBitmapRegionDecoder(imageInfo.mimeType)) {
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
    val tileDecoder = TileDecoder(
        logger = logger,
        imageSource = imageSource,
        tileBitmapPoolHelper = tileBitmapPoolHelper,
        imageInfo = imageInfo,
    )
    return Result.success(tileDecoder)
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