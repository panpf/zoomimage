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

import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.limitTo
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Calculates the maximum size of the tile based on the container size, typically half the container size
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileManageUtilsTest.testCalculateTileMaxSize]
 */
internal fun calculateTileMaxSize(containerSize: IntSizeCompat): IntSizeCompat {
    return containerSize / 2
}

/**
 * Calculates the sample size of the subsampling when the specified scaling factor is calculated
 *
 * If the size of the thumbnail is the original image divided by 16, then when the scaling factor is from 1.0 to 17.9, the node that changes the sample size is [[1.0:16, 1.5:8, 2.9:4, 5.7:2, 11.1:1]]
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileManageUtilsTest.testFindSampleSize]
 */
internal fun findSampleSize(
    imageSize: IntSizeCompat,
    thumbnailSize: IntSizeCompat,
    scale: Float
): Int {
    // A scale less than 1f indicates that the thumbnail is not enlarged, so subsampling is not required
    if (imageSize.isEmpty() || thumbnailSize.isEmpty() || scale < 1f) {
        return 0
    }
    val scaledFactor = (imageSize.width / (thumbnailSize.width * scale)).format(1)
    val sampleSize = closestPowerOfTwo(scaledFactor)
    @Suppress("UnnecessaryVariable") val limitedSampleSize = sampleSize.coerceAtLeast(1)
    return limitedSampleSize
}

/**
 * Find the closest power of two to the given number, rounding up.
 *
 * Results from 1.0 to 17.9 are [[1.0:1, 1.5:2, 2.9:4, 5.7:8, 11.4:16]]
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileManageUtilsTest.testClosestPowerOfTwo]
 */
internal fun closestPowerOfTwo(number: Float): Int {
    val logValue = log2(number) // Takes the logarithm of the input number
    val closestInteger = logValue.roundToInt()  // Take the nearest integer
    val powerOfTwo = 2.0.pow(closestInteger)    // Calculate the nearest power of 2
    return powerOfTwo.toInt()
}

/**
 * Calculates a list of tiles with different sample sizes, and [thumbnailSize] is used to limit the maximum sample size, the result is a Map sorted by sample size from largest to smallest
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileManageUtilsTest.testCalculateTileGridMap]
 */
internal fun calculateTileGridMap(
    imageSize: IntSizeCompat,
    tileMaxSize: IntSizeCompat,
    thumbnailSize: IntSizeCompat,
): Map<Int, List<Tile>> {
    /* The core rules are: The size of each tile does not exceed tileMaxSize */
    val tileMaxWith = tileMaxSize.width
    val tileMaxHeight = tileMaxSize.height
    val tileMap = HashMap<Int, List<Tile>>()

    val maxSampleSize =
        findSampleSize(imageSize = imageSize, thumbnailSize = thumbnailSize, scale = 1f)

    var sampleSize = 1
    while (true) {
        var xTiles = 0
        var sourceTileWidth: Int
        var sampleTileWidth: Int
        do {
            xTiles += 1
            sourceTileWidth = ceil(imageSize.width / xTiles.toFloat()).toInt()
            sampleTileWidth = ceil(sourceTileWidth / sampleSize.toFloat()).toInt()
        } while (sampleTileWidth > tileMaxWith)

        var yTiles = 0
        var sourceTileHeight: Int
        var sampleTileHeight: Int
        do {
            yTiles += 1
            sourceTileHeight = ceil(imageSize.height / yTiles.toFloat()).toInt()
            sampleTileHeight = ceil(sourceTileHeight / sampleSize.toFloat()).toInt()
        } while (sampleTileHeight > tileMaxHeight)

        val tileList = ArrayList<Tile>(xTiles * yTiles)
        var left = 0
        var top = 0
        var xCoordinate = 0
        var yCoordinate = 0
        while (true) {
            val right = (left + sourceTileWidth).coerceAtMost(imageSize.width)
            val bottom = (top + sourceTileHeight).coerceAtMost(imageSize.height)
            tileList.add(
                Tile(
                    coordinate = IntOffsetCompat(xCoordinate, yCoordinate),
                    srcRect = IntRectCompat(left, top, right, bottom),
                    sampleSize = sampleSize
                )
            )
            if (right >= imageSize.width && bottom >= imageSize.height) {
                break
            } else if (right >= imageSize.width) {
                left = 0
                top += sourceTileHeight
                xCoordinate = 0
                yCoordinate++
            } else {
                left += sourceTileWidth
                xCoordinate++
            }
        }
        tileMap[sampleSize] = tileList

        if (tileList.size == 1 || sampleSize >= maxSampleSize) {
            break
        } else {
            sampleSize *= 2
        }
    }
    return tileMap.toSortedMap { o1, o2 -> (o1 - o2) * -1 }
}

/**
 * The area that needs to be loaded on the original image is calculated from the area currently visible to the thumbnail, which is usually larger than the visible area, usually half the [tileMaxSize].
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileManageUtilsTest.testCalculateImageLoadRect]
 */
internal fun calculateImageLoadRect(
    imageSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    tileMaxSize: IntSizeCompat,
    contentVisibleRect: IntRectCompat
): IntRectCompat {
    if (imageSize.isEmpty() || contentSize.isEmpty() || contentVisibleRect.isEmpty) {
        return IntRectCompat.Zero
    }
    val widthScale = imageSize.width / contentSize.width.toFloat()
    val heightScale = imageSize.height / contentSize.height.toFloat()
    val imageVisibleRect = IntRectCompat(
        left = floor(contentVisibleRect.left * widthScale).toInt(),
        top = floor(contentVisibleRect.top * heightScale).toInt(),
        right = ceil(contentVisibleRect.right * widthScale).toInt(),
        bottom = ceil(contentVisibleRect.bottom * heightScale).toInt()
    )
    /*
     * Increase the visible area as the loading area,
     * this preloads tiles around the visible area,
     * the user will no longer feel the loading process while sliding slowly
     */
    val horExtend = tileMaxSize.width / 2f
    val verExtend = tileMaxSize.height / 2f
    val imageLoadRect = IntRectCompat(
        left = floor(imageVisibleRect.left - horExtend).toInt(),
        top = floor(imageVisibleRect.top - verExtend).toInt(),
        right = ceil(imageVisibleRect.right + horExtend).toInt(),
        bottom = ceil(imageVisibleRect.bottom + verExtend).toInt(),
    )
    @Suppress("UnnecessaryVariable") val limitedImageLoadRect = imageLoadRect.limitTo(imageSize)
    return limitedImageLoadRect
}