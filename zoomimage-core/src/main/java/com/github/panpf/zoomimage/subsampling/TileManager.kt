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

import androidx.annotation.MainThread
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.internal.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.findSampleSize
import com.github.panpf.zoomimage.subsampling.internal.initializeTileMap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.internal.requiredMainThread
import com.github.panpf.zoomimage.util.internal.toHexString
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

// todo 优化 tile 替换逻辑，现在是切换到新的 scale 后，会先清空所有 tile，然后再加载新的 tile，
//  这样会导致重新显示最模糊的图片，再到清晰的图片，体验不好
//  可以考虑在切换到新的 scale 后，先加载新的 tile，旧的 tile 被完全覆盖以后再清空旧的 tile，这样就不会出现重新显示最模糊的图片的情况了
class TileManager constructor(
    logger: Logger,
    private val tileDecoder: TileDecoder,
    private val tileMemoryCacheHelper: TileMemoryCacheHelper,
    private val tileBitmapPoolHelper: TileBitmapPoolHelper,
    private val imageSource: ImageSource,
    private val imageInfo: ImageInfo,
    containerSize: IntSizeCompat,
    private val contentSize: IntSizeCompat,
    private val onTileChanged: (tileManager: TileManager) -> Unit,
    private val onImageLoadRectChanged: (tileManager: TileManager) -> Unit,
) {
    private val logger: Logger = logger.newLogger(module = "SubsamplingTileManager")
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Suppress("OPT_IN_USAGE")
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)
    private var lastTileList: List<Tile>? = null
    private var lastSampleSize: Int? = null
    private var lastScale: Float? = null
    private var lastContentSize: IntSizeCompat? = null
    private var lastContentVisibleRect: IntRectCompat? = null

    /**
     * Whether to pause loading tiles when transforming
     */
    var pauseWhenTransforming: Boolean = false

    val tileMaxSize: IntSizeCompat
    val tileMap: Map<Int, List<Tile>>
    val tileList: List<Tile>
        get() = lastTileList ?: emptyList()
    var imageLoadRect = IntRectCompat.Zero
        private set

    init {
        tileMaxSize = IntSizeCompat(containerSize.width / 2, containerSize.height / 2)
        tileMap = initializeTileMap(imageInfo.size, tileMaxSize)
    }

    @MainThread
    fun refreshTiles(
        contentVisibleRect: IntRectCompat,
        scale: Float,
        rotation: Int,
        transforming: Boolean,
        caller: String,
    ) {
        requiredMainThread()

        if (!canUseSubsamplingByAspectRatio(
                imageWidth = imageInfo.width,
                imageHeight = imageInfo.height,
                drawableWidth = contentSize.width,
                drawableHeight = contentSize.height
            )
        ) {
            logger.d {
                "refreshTiles:$caller, interrupted, the aspect ratio is different. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                        "scale=$scale, " +
                        "imageInfo=${imageInfo.toShortString()}, " +
                        "'${imageSource.key}"
            }
            return
        }

        resetVisibleAndLoadRect(contentVisibleRect)

        if (contentVisibleRect.isEmpty) {
            logger.d {
                "refreshTiles:$caller. interrupted, contentVisibleRect is empty. " +
                        "contentVisibleRect=${contentVisibleRect}. '${imageSource.key}'"
            }
            clean("refreshTiles:contentVisibleRectEmpty")
            return
        }

        if (scale.format(2) <= 1.0f) {
            logger.d { "refreshTiles:$caller. interrupted, zoom is less than or equal to 1f. '${imageSource.key}'" }
            clean("refreshTiles:scale1f")
            return
        }

        if (pauseWhenTransforming && transforming) {
            logger.d { "refreshTiles:$caller. interrupted, transforming. '${imageSource.key}'" }
            return
        }

        if (rotation % 90 != 0) {
            logger.d { "refreshTiles:$caller. interrupted, rotation is not a multiple of 90: $rotation. '${imageSource.key}'" }
            return
        }

        val tiles = tryResetTiles(scale)
        if (tiles.isNullOrEmpty()) {
            logger.d {
                "refreshTiles:$caller, interrupted, tiles size is ${tiles?.size ?: 0}. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                        "scale=$scale, " +
                        "imageInfo=${imageInfo.toShortString()}, " +
                        "sampleSize=$lastSampleSize. " +
                        "'${imageSource.key}"
            }
            return
        }

        var loadCount = 0
        var freeCount = 0
        var realLoadCount = 0
        var realFreeCount = 0
        tiles.forEach { tile ->
            if (tile.srcRect.overlaps(imageLoadRect)) {
                loadCount++
                if (loadTile(tile)) {
                    realLoadCount++
                }
            } else {
                freeCount++
                if (freeTile(tile, nowNotifyTileChanged = true)) {
                    realFreeCount++
                }
            }
        }

        logger.d {
            "refreshTiles:$caller. " +
                    "tiles=${tiles.size}, " +
                    "loadCount=${realLoadCount}/${loadCount}, " +
                    "freeCount=${realFreeCount}/${freeCount}. " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                    "scale=$scale, " +
                    "imageInfo=${imageInfo.toShortString()}, " +
                    "sampleSize=$lastSampleSize, " +
                    "imageLoadRect=${imageLoadRect.toShortString()}. " +
                    "'${imageSource.key}"
        }
    }

    private fun resetVisibleAndLoadRect(contentVisibleRect: IntRectCompat) {
        if (lastContentVisibleRect == contentVisibleRect) {
            return
        }
        lastContentVisibleRect = contentVisibleRect

        val contentScale = imageInfo.width / contentSize.width.toFloat()
        val imageVisibleRect = IntRectCompat(
            left = floor(contentVisibleRect.left * contentScale).toInt(),
            top = floor(contentVisibleRect.top * contentScale).toInt(),
            right = ceil(contentVisibleRect.right * contentScale).toInt(),
            bottom = ceil(contentVisibleRect.bottom * contentScale).toInt()
        )
        // Increase the visible area as the loading area,
        // this preloads tiles around the visible area,
        // the user will no longer feel the loading process while sliding slowly
        imageLoadRect = IntRectCompat(
            left = imageVisibleRect.left - tileMaxSize.width / 2,
            top = imageVisibleRect.top - tileMaxSize.height / 2,
            right = imageVisibleRect.right + tileMaxSize.width / 2,
            bottom = imageVisibleRect.bottom + tileMaxSize.height / 2
        )

        onImageLoadRectChanged(this)
    }

    private fun tryResetTiles(scale: Float): List<Tile>? {
        val lastScale = lastScale
        val lastSampleSize = lastSampleSize
        val lastTileList = lastTileList
        val lastContentSize = lastContentSize
        if (scale == lastScale && contentSize == lastContentSize && lastSampleSize != null && lastTileList != null) {
            return lastTileList
        }
        val sampleSize = findSampleSize(
            imageWidth = imageInfo.width,
            imageHeight = imageInfo.height,
            drawableWidth = contentSize.width,
            drawableHeight = contentSize.height,
            scale = scale
        )
        if (sampleSize == lastSampleSize && contentSize == lastContentSize && lastTileList != null) {
            return lastTileList
        }

        lastTileList?.forEach { tile ->
            freeTile(tile, nowNotifyTileChanged = false)
        }

        val tiles = tileMap[sampleSize]
        this.lastScale = scale
        this.lastContentSize = contentSize
        this.lastSampleSize = sampleSize
        this.lastTileList = tiles

        notifyTileChanged()
        return tiles
    }

    @MainThread
    fun clean(caller: String) {
        requiredMainThread()
        val lastTileList = lastTileList
        if (lastTileList != null) {
            var freeCount = 0
            tileMap.values.forEach { tileList ->
                tileList.forEach { tile ->
                    if (freeTile(tile, nowNotifyTileChanged = false)) {
                        freeCount++
                    }
                }
            }
            this@TileManager.lastSampleSize = null
            this@TileManager.lastTileList = null
            logger.d { "clean:$caller. freeCount=$freeCount. '${imageSource.key}" }
            if (freeCount > 0) {
                notifyTileChanged()
            }
        }
    }

    @MainThread
    private fun loadTile(tile: Tile): Boolean {
        requiredMainThread()

        if (tile.bitmap != null) {
            logger.d {
                "loadTile. skipped, loaded. $tile. '${imageSource.key}'"
            }
            return false
        }

        val job = tile.loadJob
        if (job?.isActive == true) {
            logger.d {
                "loadTile. skipped, loading. $tile. '${imageSource.key}'"
            }
            return false
        }

        val memoryCacheKey =
            "${imageSource.key}_tile_${tile.srcRect.toShortString()}_${imageInfo.exifOrientation}_${tile.inSampleSize}"
        val cachedValue = tileMemoryCacheHelper.get(memoryCacheKey)
        if (cachedValue != null) {
            tile.setTileBitmap(cachedValue, fromCache = true)
            tile.state = Tile.STATE_LOADED
            logger.d { "loadTile. successful, fromMemory. $tile. '${imageSource.key}'" }
            notifyTileChanged()
            return true
        }

        tile.loadJob = scope.async {
            tile.state = Tile.STATE_LOADING
            notifyTileChanged()

            val bitmap = withContext(decodeDispatcher) {
                tileDecoder.decode(tile)
            }
            when {
                bitmap == null -> {
                    tile.state = Tile.STATE_ERROR
                    logger.e("loadTile. failed, bitmap null. $tile. '${imageSource.key}'")
                }

                isActive -> {
                    val newCountBitmap = tileMemoryCacheHelper.put(
                        key = memoryCacheKey,
                        bitmap = bitmap,
                        imageKey = imageSource.key,
                        imageInfo = imageInfo,
                        tileBitmapPoolHelper = tileBitmapPoolHelper,
                    )
                    tile.setTileBitmap(newCountBitmap, fromCache = false)
                    tile.state = Tile.STATE_LOADED
                    logger.d { "loadTile. successful. $tile. '${imageSource.key}'" }
                }

                else -> {
                    logger.d {
                        "loadTile. canceled. bitmap=${bitmap.toHexString()}, $tile. '${imageSource.key}'"
                    }
                    tile.state = Tile.STATE_ERROR
                    tileBitmapPoolHelper.freeBitmap(bitmap, "loadTile:jobCanceled")
                }
            }
            notifyTileChanged()
        }

        return true
    }

    @MainThread
    private fun freeTile(tile: Tile, nowNotifyTileChanged: Boolean): Boolean {
        if (tile.state == Tile.STATE_NONE) {
            return false
        }

        tile.state = Tile.STATE_NONE

        val loadJob = tile.loadJob
        if (loadJob != null && loadJob.isActive) {
            loadJob.cancel()
            tile.loadJob = null
        }

        val bitmap = tile.bitmap
        if (bitmap != null) {
            logger.d { "freeTile. $tile. '${imageSource.key}'" }
            tile.setTileBitmap(null, fromCache = false)
        }

        if (nowNotifyTileChanged) {
            notifyTileChanged()
        }
        return true
    }

    private fun notifyTileChanged() {
        onTileChanged(this)
    }
}