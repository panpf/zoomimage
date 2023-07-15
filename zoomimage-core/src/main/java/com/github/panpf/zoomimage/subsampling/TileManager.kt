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
import com.github.panpf.zoomimage.subsampling.internal.freeBitmap
import com.github.panpf.zoomimage.subsampling.internal.logString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

class TileManager constructor(
    private val logger: Logger,
    private val tileBitmapPool: TileBitmapPool?,
    private val tileMemoryCache: TileMemoryCache?,
    private val imageSource: ImageSource,
    val imageInfo: ImageInfo,
    viewSize: IntSizeCompat,
    private val onTileChanged: () -> Unit,
) {
    val decoder: TileDecoder = TileDecoder(
        imageSource = imageSource,
        tileBitmapPool = tileBitmapPool,
        logger = Logger(),
        imageInfo = imageInfo,
    )

    private val tileMaxSize = viewSize.let {
        IntSizeCompat(it.width / 2, it.height / 2)
    }
    private val tileMap: Map<Int, List<Tile>> = initializeTileMap(imageInfo.size, tileMaxSize)
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)
    private var lastTileList: List<Tile>? = null
    private var lastSampleSize: Int? = null
    var imageVisibleRect = IntRectCompat.Zero
        private set
    var imageLoadRect = IntRectCompat.Zero
        private set
//    private val _tileDrawRect = Rect()

    val rowTileList: List<Tile>?
        get() = lastTileList
    val tileList: List<Tile>?
        get() = lastTileList?.toList()

    //    val imageVisibleRect: IntRectCompat
//        get() = IntRectCompat().apply { set(_imageVisibleRect) }
//    val imageLoadRect: IntRectCompat
//        get() = IntRectCompat().apply { set(_imageLoadRect) }
    val sampleSize: Int?
        get() = lastSampleSize

    init {
        logger.d(SUBSAMPLING_MODULE) {
            val tileMapInfoList = tileMap.keys.sortedDescending().map {
                "${it}:${tileMap[it]?.size}"
            }
            "tileMap. $tileMapInfoList. '${imageSource.key}'"
        }
    }

    @MainThread
    fun refreshTiles(drawableSize: IntSizeCompat, drawableVisibleRect: IntRectCompat, displayScale: Float) {
        requiredMainThread()

        val zoomScale = displayScale
        val sampleSize = findSampleSize(
            imageWidth = imageInfo.width,
            imageHeight = imageInfo.height,
            drawableWidth = drawableSize.width,
            drawableHeight = drawableSize.height,
            scale = zoomScale
        )
        if (sampleSize != lastSampleSize) {
            lastTileList?.forEach { freeTile(it) }
            lastTileList = tileMap[sampleSize]
            lastSampleSize = sampleSize
            if (lastTileList?.size == 1) {
                // Tiles are not required when the current is a minimal preview
                lastTileList = null
                lastSampleSize = null
            }
        }
        val tileList = lastTileList
        if (tileList == null) {
            logger.d(SUBSAMPLING_MODULE) {
                "refreshTiles. no tileList. " +
                        "imageInfo=${imageInfo.toShortString()}, " +
                        "drawableSize=$drawableSize, " +
                        "drawableVisibleRect=${drawableVisibleRect}, " +
                        "zoomScale=$zoomScale, " +
                        "sampleSize=$lastSampleSize. " +
                        "'${imageSource.key}"
            }
            return
        }
        resetVisibleAndLoadRect(drawableSize, drawableVisibleRect)

        logger.d(SUBSAMPLING_MODULE) {
            "refreshTiles. started. " +
                    "imageInfo=${imageInfo.toShortString()}, " +
                    "imageVisibleRect=$imageVisibleRect, " +
                    "imageLoadRect=$imageLoadRect, " +
                    "drawableSize=$drawableSize, " +
                    "drawableVisibleRect=${drawableVisibleRect}, " +
                    "zoomScale=$zoomScale, " +
                    "sampleSize=$lastSampleSize. " +
                    "'${imageSource.key}"
        }
        tileList.forEach { tile ->
            if (tile.srcRect.overlaps(imageLoadRect)) {
                loadTile(tile)
            } else {
                freeTile(tile)
            }
        }
    }

    @MainThread
    private fun loadTile(tile: Tile) {
        requiredMainThread()

        if (tile.countBitmap != null) {
            return
        }

        val job = tile.loadJob
        if (job?.isActive == true) {
            return
        }

        val memoryCacheKey =
            "${imageSource.key}_tile_${tile.srcRect.toShortString()}_${tile.inSampleSize}"
        val cachedValue = tileMemoryCache?.get(memoryCacheKey)
        if (cachedValue != null) {
            tile.countBitmap = cachedValue
            logger.d(SUBSAMPLING_MODULE) {
                "loadTile. successful. fromMemory. $tile. '${imageSource.key}'"
            }
            onTileChanged()
            return
        }

        tile.loadJob = scope.async(decodeDispatcher) {
            val bitmap = decoder.decode(tile)
            when {
                bitmap == null -> {
                    logger.e(SUBSAMPLING_MODULE) {
                        "loadTile. null. $tile. '${imageSource.key}'"
                    }
                }

                isActive -> {
                    withContext(Dispatchers.Main) {
                        val newCountBitmap = tileMemoryCache?.put(
                            key = memoryCacheKey,
                            bitmap = bitmap,
                            imageKey = imageSource.key,
                            imageInfo = imageInfo,
                            tileBitmapPool = tileBitmapPool,
                        ) ?: DefaultTileBitmap(memoryCacheKey, bitmap)
                        tile.countBitmap = newCountBitmap
                        logger.d(SUBSAMPLING_MODULE) {
                            "loadTile. successful. $tile. '${imageSource.key}'"
                        }
                        onTileChanged()
                    }
                }

                else -> {
                    logger.d(SUBSAMPLING_MODULE) {
                        "loadTile. canceled. $tile. '${imageSource.key}'"
                    }
                    val bitmapPool = tileBitmapPool
                    if (bitmapPool != null) {
                        bitmapPool.freeBitmap(
                            logger = logger,
                            bitmap = bitmap,
                            caller = "tile:jobCanceled"
                        )
                    } else {
                        bitmap.recycle()
                    }
                    logger.d(SUBSAMPLING_MODULE) {
                        "loadTile. freeBitmap. tile job canceled. bitmap=${bitmap.logString}. '${imageSource.key}'"
                    }
                }
            }
        }
    }

    @MainThread
    private fun freeTile(tile: Tile) {
        tile.loadJob?.run {
            if (isActive) {
                cancel()
            }
            tile.loadJob = null
        }

        tile.countBitmap?.run {
            logger.d(SUBSAMPLING_MODULE) {
                "freeTile. $tile. '${imageSource.key}'"
            }
            tile.countBitmap = null
            onTileChanged()
        }
    }

    private fun resetVisibleAndLoadRect(drawableSize: IntSizeCompat, drawableVisibleRect: IntRectCompat) {
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
    private fun freeAllTile() {
        tileMap.values.forEach { tileList ->
            tileList.forEach { tile ->
                freeTile(tile)
            }
        }
        onTileChanged()
    }

    @MainThread
    fun destroy() {
        requiredMainThread()
        clean()
        decoder.destroy()
    }

    @MainThread
    fun clean() {
        requiredMainThread()
        freeAllTile()
        lastSampleSize = null
        lastTileList = null
    }
}