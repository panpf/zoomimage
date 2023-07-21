/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.internal.requiredMainThread
import com.github.panpf.zoomimage.core.toShortString
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.internal.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.toHexString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

class TileManager constructor(
    logger: Logger,
    private val tileDecoder: TileDecoder,
    private val tileMemoryCacheHelper: TileMemoryCacheHelper,
    private val tileBitmapPoolHelper: TileBitmapPoolHelper,
    private val imageSource: ImageSource,
    private val imageInfo: ImageInfo,
    containerSize: IntSizeCompat,
    private val onTileChanged: () -> Unit,
) {
    private val logger: Logger = logger.newLogger(module = "SubsamplingTileManager")
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)
    private var lastTileList: List<Tile>? = null
    private var lastSampleSize: Int? = null
    private var lastScale: Float? = null
    private var lastContentSize: IntSizeCompat? = null

    val tileMaxSize: IntSizeCompat
    val tileMap: Map<Int, List<Tile>>

    var imageVisibleRect = IntRectCompat.Zero
        private set
    var imageLoadRect = IntRectCompat.Zero
        private set
    val tileList: List<Tile>?
        get() = lastTileList

    init {
        tileMaxSize = IntSizeCompat(containerSize.width / 2, containerSize.height / 2)
        tileMap = initializeTileMap(imageInfo.size, tileMaxSize)
    }

    @MainThread
    fun refreshTiles(
        contentSize: IntSizeCompat,
        contentVisibleRect: IntRectCompat,
        scale: Float,
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

        val tileList = findTiles(scale, contentSize)
        if (tileList.isNullOrEmpty()) {
            logger.d {
                "refreshTiles:$caller, interrupted, tiles size is ${tileList?.size ?: 0}. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                        "scale=$scale, " +
                        "imageInfo=${imageInfo.toShortString()}, " +
                        "sampleSize=$lastSampleSize. " +
                        "'${imageSource.key}"
            }
            return
        }

        resetVisibleAndLoadRect(contentSize, contentVisibleRect)

        var loadCount = 0
        var freeCount = 0
        var realLoadCount = 0
        var realFreeCount = 0
        tileList.forEach { tile ->
            if (tile.srcRect.overlaps(imageLoadRect)) {
                loadCount++
                if (loadTile(tile)) {
                    realLoadCount++
                }
            } else {
                freeCount++
                if (freeTile(tile, notifyTileChanged = true)) {
                    realFreeCount++
                }
            }
        }

        logger.d {
            "refreshTiles:$caller. " +
                    "tiles=${tileList.size}, " +
                    "loadCount=${realLoadCount}/${loadCount}, " +
                    "freeCount=${realFreeCount}/${freeCount}. " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                    "scale=$scale, " +
                    "imageInfo=${imageInfo.toShortString()}, " +
                    "sampleSize=$lastSampleSize, " +
                    "imageVisibleRect=${imageVisibleRect.toShortString()}, " +
                    "imageLoadRect=${imageLoadRect.toShortString()}. " +
                    "'${imageSource.key}"
        }
    }

    private fun findTiles(scale: Float, contentSize: IntSizeCompat): List<Tile>? {
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
        val tiles = tileMap[sampleSize]
        this.lastScale = scale
        this.lastContentSize = contentSize
        this.lastSampleSize = sampleSize
        this.lastTileList = tiles
        return tiles
    }

    @MainThread
    fun clean(caller: String) {
        requiredMainThread()
        val lastTileList = lastTileList
        if (lastTileList != null) {
            val freeCount = freeAllTile()
            logger.d { "clean:$caller. freeCount=$freeCount. '${imageSource.key}" }
            this@TileManager.lastSampleSize = null
            this@TileManager.lastTileList = null
        }
    }

    @MainThread
    private fun loadTile(tile: Tile): Boolean {
        requiredMainThread()

        if (tile.tileBitmap != null) {
            return false
        }

        val job = tile.loadJob
        if (job?.isActive == true) {
            return false
        }

        val memoryCacheKey =
            "${imageSource.key}_tile_${tile.srcRect.toShortString()}_${imageInfo.exifOrientation}_${tile.inSampleSize}"
        val cachedValue = tileMemoryCacheHelper.get(memoryCacheKey)
        if (cachedValue != null) {
            tile.tileBitmap = cachedValue
            logger.d {
                "loadTile. successful, fromMemory. $tile. '${imageSource.key}'"
            }
            notifyTileChanged()
            return true
        }

        tile.loadJob = scope.async(decodeDispatcher) {
            val bitmap = tileDecoder.decode(tile)
            when {
                bitmap == null -> {
                    logger.e("loadTile. failed, bitmap null. $tile. '${imageSource.key}'")
                }

                isActive -> {
                    withContext(Dispatchers.Main) {
                        val newCountBitmap = tileMemoryCacheHelper.put(
                            key = memoryCacheKey,
                            bitmap = bitmap,
                            imageKey = imageSource.key,
                            imageInfo = imageInfo,
                            tileBitmapPoolHelper = tileBitmapPoolHelper,
                        )
                        tile.tileBitmap = newCountBitmap
                        logger.d { "loadTile. successful. $tile. '${imageSource.key}'" }
                        notifyTileChanged()
                    }
                }

                else -> {
                    logger.d {
                        "loadTile. canceled. bitmap=${bitmap.toHexString()}, $tile. '${imageSource.key}'"
                    }
                    tileBitmapPoolHelper.freeBitmap(
                        bitmap = bitmap,
                        caller = "loadTile:jobCanceled"
                    )
                }
            }
        }

        return true
    }

    @MainThread
    private fun freeTile(tile: Tile, notifyTileChanged: Boolean): Boolean {
        tile.loadJob?.run {
            if (isActive) {
                cancel()
            }
            tile.loadJob = null
        }

        val bitmap = tile.tileBitmap
        val recyclable = bitmap != null
        if (recyclable) {
            logger.d {
                "freeTile. $tile. '${imageSource.key}'"
            }
            tile.tileBitmap = null
            if (notifyTileChanged) {
                notifyTileChanged()
            }
        }
        return recyclable
    }

    private fun resetVisibleAndLoadRect(
        drawableSize: IntSizeCompat,
        drawableVisibleRect: IntRectCompat
    ) {
        val drawableScaled = imageInfo.width / drawableSize.width.toFloat()
        imageVisibleRect = IntRectCompat(
            left = floor(drawableVisibleRect.left * drawableScaled).toInt(),
            top = floor(drawableVisibleRect.top * drawableScaled).toInt(),
            right = ceil(drawableVisibleRect.right * drawableScaled).toInt(),
            bottom = ceil(drawableVisibleRect.bottom * drawableScaled).toInt()
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
    }

    @MainThread
    private fun freeAllTile(): Int {
        var freeCount = 0
        tileMap.values.forEach { tileList ->
            tileList.forEach { tile ->
                freeTile(tile, notifyTileChanged = false)
                freeCount++
            }
        }
        if (freeCount > 0) {
            notifyTileChanged()
        }
        return freeCount
    }

    private fun notifyTileChanged() {
        onTileChanged()
    }
}