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
import com.github.panpf.zoomimage.util.isEmpty
import kotlin.math.ceil
import kotlin.math.floor

internal fun calculateTileMaxSize(containerSize: IntSizeCompat): IntSizeCompat {
    return containerSize / 2
}

internal fun findSampleSize(
    imageSize: IntSizeCompat,
    thumbnailSize: IntSizeCompat,
    scale: Float
): Int {
    if (imageSize.isEmpty() || thumbnailSize.isEmpty() || scale <= 0) {
        return 0
    }
//    val scaledWidthRatio = (imageSize.width / (thumbnailSize.width * scale)).roundToInt()
//    var sampleSize = 1
//    while (scaledWidthRatio >= sampleSize * 2) {
//        sampleSize *= 2
//    }
//    return sampleSize
    // todo 优化不精准的问题，例如 scale 为 1f， scaledWidthRatio 为 3.98，但是 sampleSize 为 2
    val scaledWidthRatio = (imageSize.width / (thumbnailSize.width * scale))
    var sampleSize = 1
    while (scaledWidthRatio >= sampleSize * 2) {
        sampleSize *= 2
    }
    return sampleSize
}

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

internal fun calculateImageLoadRect(
    imageSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    tileMaxSize: IntSizeCompat,
    contentVisibleRect: IntRectCompat
): IntRectCompat {
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
    return IntRectCompat(
        left = imageVisibleRect.left - tileMaxSize.width / 2,
        top = imageVisibleRect.top - tileMaxSize.height / 2,
        right = imageVisibleRect.right + tileMaxSize.width / 2,
        bottom = imageVisibleRect.bottom + tileMaxSize.height / 2
    )
}