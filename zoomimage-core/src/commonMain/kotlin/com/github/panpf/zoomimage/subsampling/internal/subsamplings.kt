/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

import androidx.annotation.MainThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SamplingTiles
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Create [TileDecoder]. If the image type is not supported or the thumbnail size is larger than the
 * original image or the aspect ratio of the thumbnail and the original image is inconsistent, the creation will fail.
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.internal.SubsamplingDesktopTest.testCreateTileDecoder
 */
@MainThread
suspend fun createTileDecoder(
    logger: Logger,
    subsamplingImage: SubsamplingImage,
    contentSize: IntSizeCompat,
    regionDecoders: List<RegionDecoder.Factory>,
    onImageInfoPassed: (ImageInfo) -> Unit,
): Result<TileDecoder> = runCatching {
    val regionDecoderFactory = regionDecoders
        .plus(defaultRegionDecoder())
        .find { it.accept(subsamplingImage) }!!

    // The contentOriginSize of Zoomable can be set in advance through the external ImageInfo.
    // Avoid setting contentOriginSize after initialization to reset user transformations generated during initialization
    val externalImageInfo = subsamplingImage.imageInfo
    if (externalImageInfo != null) {
        checkImageInfo(externalImageInfo, regionDecoderFactory, contentSize)
        onImageInfoPassed(externalImageInfo)
    }

    val regionDecoder = withContext(ioCoroutineDispatcher()) {
        runCatching {
            val imageSource = subsamplingImage.imageSource.create()
            val regionDecoder = regionDecoderFactory.create(subsamplingImage, imageSource)

            try {
                if (externalImageInfo == null) {
                    val imageInfo = regionDecoder.imageInfo
                    checkImageInfo(imageInfo, regionDecoderFactory, contentSize)
                }

                regionDecoder.prepare()
            } catch (e: Exception) {
                regionDecoder.close()
                throw e
            }
            regionDecoder
        }
    }.apply {
        if (isFailure) throw exceptionOrNull()!!
    }.getOrThrow()

    // Although the checkImageInfo check passes, ready may fail, so call onImageInfoPassed after ready
    if (externalImageInfo == null) {
        onImageInfoPassed(regionDecoder.imageInfo)
    }

    return Result.success(TileDecoder(logger, regionDecoder))
}

/**
 * Check the image information, including the image size, aspect ratio, and whether the image type is supported
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.SubsamplingsCommonTest.testCheckImageInfo
 */
fun checkImageInfo(
    imageInfo: ImageInfo,
    factory: RegionDecoder.Factory,
    contentSize: IntSizeCompat
) {
    // Check image size
    val imageSize = imageInfo.size
    if (imageSize.isEmpty()) {
        throw Exception("image size invalid: ${imageInfo.width}x${imageInfo.height}")
    }
    if (contentSize.width >= imageSize.width || contentSize.height >= imageSize.height) {
        throw Exception(
            "the thumbnail size is greater than or equal to the original image. " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "imageSize=${imageSize.toShortString()}"
        )
    }

    // Check aspect ratio
    if (!canUseSubsamplingByAspectRatio(imageSize, thumbnailSize = contentSize)) {
        throw Exception(
            "The aspect ratio of the thumbnail is too different from that of the original image. " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "imageSize=${imageSize.toShortString()}"
        )
    }

    // Check image mimeType
    val supportRegion = imageInfo.mimeType.let { factory.checkSupport(it) }
    if (supportRegion == false) {
        throw Exception("Image type not support subsampling. mimeType=${imageInfo.mimeType}")
    }
}

/**
 * Determine whether Subsampling can be used based on the difference between the width and height
 * scaling factors of the original image and the thumbnail. The difference cannot exceed [maxDifference]
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.SubsamplingsCommonTest.testCanUseSubsamplingByAspectRatio
 */
fun canUseSubsamplingByAspectRatio(
    imageSize: IntSizeCompat,
    thumbnailSize: IntSizeCompat,
    maxDifference: Float = 1f
): Boolean {
    if (imageSize.isEmpty() || thumbnailSize.isEmpty()) return false
    if (imageSize.width < thumbnailSize.width || imageSize.height < thumbnailSize.height) return false
    val widthScale = imageSize.width / thumbnailSize.width.toFloat()
    val heightScale = imageSize.height / thumbnailSize.height.toFloat()
    val diff = abs(widthScale - heightScale)
    val diffFormatted = diff.format(1)
    val maxDiffFormatted = maxDifference.format(1)
    return diffFormatted <= maxDiffFormatted
}

/**
 * Returns a string consisting of sample size, number of tiles, and grid size
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.SubsamplingsCommonTest.testToIntroString
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
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.SubsamplingsCommonTest.testCalculatePreferredTileSize
 */
fun calculatePreferredTileSize(containerSize: IntSizeCompat): IntSizeCompat {
    return containerSize / 2
}

/**
 * Returns true if the new preferred tile size is doubled in width or height or reduced by half,
 * which can significantly reduce the number of times the TileManager is reset when the container size changes frequently (window resizing)
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.SubsamplingsCommonTest.testCheckNewPreferredTileSize
 */
fun checkNewPreferredTileSize(
    oldPreferredTileSize: IntSizeCompat,
    newPreferredTileSize: IntSizeCompat
): Boolean {
    if (newPreferredTileSize.isEmpty()) {
        return false
    }
    if (oldPreferredTileSize.isEmpty()) {
        return true
    }

    val widthDifferent = abs(newPreferredTileSize.width - oldPreferredTileSize.width)
    val widthTargetMultiple =
        if (newPreferredTileSize.width > oldPreferredTileSize.width) 1f else 0.5f
    val widthTarget = oldPreferredTileSize.width * widthTargetMultiple
    if (widthDifferent >= widthTarget) {
        return true
    }

    val heightDifferent = abs(newPreferredTileSize.height - oldPreferredTileSize.height)
    val heightTargetMultiple =
        if (newPreferredTileSize.height > oldPreferredTileSize.height) 1f else 0.5f
    val heightTarget = oldPreferredTileSize.height * heightTargetMultiple
    if (heightDifferent >= heightTarget) {
        return true
    }

    return false
}