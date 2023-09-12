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
import com.github.panpf.zoomimage.subsampling.internal.calculateSampleSize
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
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
    private val contentSize: IntSizeCompat,
    private val onTileChanged: (tileManager: TileManager) -> Unit,
    private val onSampleSizeChanged: (tileManager: TileManager) -> Unit,
    private val onImageLoadRectChanged: (tileManager: TileManager) -> Unit,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val logger: Logger = logger.newLogger(module = "TileManager")
    private var lastScale: Float? = null
    private var lastSampleSize: Int = 0
    private var lastContentVisibleRect: IntRectCompat? = null
    private var notifyTileSnapshotListJob: Job? = null

    /**
     * Whether to pause loading tiles when transforming, which improves performance,
     * but delays the loading of tiles, allowing users to perceive the loading process more,
     * and the user experience will be reduced
     */
    var pauseWhenTransforming: Boolean = false

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    var disabledBackgroundTiles: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateTileSnapshotList()
            }
        }

    /**
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec = TileAnimationSpec.Default

    /**
     * The maximum size of the tile
     */
    val tileMaxSize: IntSizeCompat =
        IntSizeCompat(containerSize.width / 2, containerSize.height / 2)

    /**
     * Tile Map with sample size from largest to smallest
     */
    val sortedTileMap: Map<Int, List<Tile>> =
        initializeTileMap(imageInfo.size, tileMaxSize).toSortedMap { o1, o2 -> (o1 - o2) * -1 }

    /**
     * The sample size of the image
     */
    var sampleSize: Int = 0
        private set(value) {
            if (field != value) {
                field = value
                notifySampleSizeChanged()
            }
        }

    /**
     * The image load rect
     */
    var imageLoadRect = IntRectCompat.Zero
        private set(value) {
            if (field != value) {
                field = value
                notifyImageLoadRectChanged()
            }
        }

    /**
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    var foregroundTiles: List<TileSnapshot> = emptyList()
        private set

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    var backgroundTiles: List<TileSnapshot> = emptyList()
        private set

    @MainThread
    fun refreshTiles(
        contentVisibleRect: IntRectCompat,
        scale: Float,
        rotation: Int,
        transforming: Boolean,  // todo 除手势操作外的如缩放动画，fling 都应该暂停，分为以下几种手势、缩放动画、位移动画、fling，这样的话 pauseWhenTransforming 也要改为 pauseWhenGestureTransforming，也可以搞成 pauseWhenTransforming 用位运算来判断，这样就可以同时支持多种暂停了
        caller: String,
    ) {
        requiredMainThread()
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
        if (rotation % 90 != 0) {
            logger.d { "refreshTiles:$caller. interrupted, rotation is not a multiple of 90: $rotation. '${imageSource.key}'" }
            return
        }
        if (pauseWhenTransforming && transforming) {
            logger.d { "refreshTiles:$caller. interrupted, transforming. '${imageSource.key}'" }
            return
        }

        resetImageLoadRect(contentVisibleRect)
        resetSampleSize(scale)

        val sampleSize = sampleSize
        val foregroundTiles = sortedTileMap[sampleSize]
        if (foregroundTiles == null) {
            logger.d {
                val tileMapInfoList =
                    sortedTileMap.entries.map { "${it.key}:${it.value.size}" }
                "refreshTiles:$caller. " +
                        "interrupted, foregroundTilesEmpty. " +
                        "scale=${scale.format(4)}, " +
                        "sampleSize=$sampleSize, " +
                        "tileMaxSize=${tileMaxSize.toShortString()}, " +
                        "tileMapInfoList=${tileMapInfoList}, " +
                        "'${imageSource.key}'"
            }
            clean("refreshTiles:foregroundTilesEmpty")
            return
        }
        if (foregroundTiles.size == 1) {
            logger.d {
                "refreshTiles:$caller. interrupted, foregroundTilesOnlyOne. sampleSize=$sampleSize, '${imageSource.key}'"
            }
            clean("refreshTiles:foregroundTilesOnlyOne")
            return
        }

        var expectLoadCount = 0
        var actualLoadCount = 0
        var expectFreeCount = 0
        var actualFreeCount = 0
        val imageLoadRect = imageLoadRect
        val currentSampleSize = sampleSize
        val lastSampleSize = lastSampleSize
        sortedTileMap.entries.forEach { (eachSampleSize, eachTiles) ->
            if (eachSampleSize == currentSampleSize) {
                eachTiles.forEach { foregroundTile ->
                    if (foregroundTile.srcRect.overlaps(imageLoadRect)) {
                        expectLoadCount++
                        if (loadTile(foregroundTile)) {
                            actualLoadCount++
                        }
                    } else {
                        expectFreeCount++
                        if (freeTile(foregroundTile, skipNotify = true)) {
                            actualFreeCount++
                        }
                    }
                }
            } else if (!disabledBackgroundTiles
                && isBackground(lastSampleSize, currentSampleSize, eachSampleSize)
            ) {
                eachTiles.forEach { backgroundTile ->
                    if (!backgroundTile.srcRect.overlaps(imageLoadRect)) {
                        expectFreeCount++
                        if (freeTile(backgroundTile, skipNotify = true)) {
                            actualFreeCount++
                        }
                    }
                }
            } else {
                expectFreeCount += eachTiles.size
                actualFreeCount += freeTiles(eachTiles, skipNotify = true)
            }
        }
        logger.d {
            "refreshTiles:$caller. " +
                    "loadCount=${actualLoadCount}/${expectLoadCount}, " +
                    "freeCount=${actualFreeCount}/${expectFreeCount}. " +
                    "sampleSize=${sampleSize}, " +
                    "foregroundTiles=${foregroundTiles.size}, " +
                    "imageLoadRect=${imageLoadRect.toShortString()}. " +
                    "scale=$scale, " +
                    "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}, " +
                    "'${imageSource.key}"
        }
        if (actualFreeCount > 0) {
            updateTileSnapshotList()
        }
    }

    private fun isBackground(
        lastSampleSize: Int,
        currentSampleSize: Int,
        eachSampleSize: Int
    ): Boolean {
        if (lastSampleSize == 0) return false
        if (lastSampleSize > currentSampleSize && eachSampleSize > currentSampleSize) return true
        return lastSampleSize < currentSampleSize && eachSampleSize < currentSampleSize
    }

    private fun resetImageLoadRect(contentVisibleRect: IntRectCompat): Boolean {
        if (lastContentVisibleRect == contentVisibleRect) {
            return false
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
        return true
    }

    private fun resetSampleSize(scale: Float): Boolean {
        val lastScale = lastScale
        val currentSampleSize = sampleSize
        if (currentSampleSize != 0 && scale == lastScale) {
            return false
        }
        this.lastScale = scale

        val newSampleSize = calculateSampleSize(
            imageSize = imageInfo.size,
            drawableSize = contentSize,
            scale = scale
        )
        if (newSampleSize == currentSampleSize) {
            return false
        }

        this.lastSampleSize = currentSampleSize
        this.sampleSize = newSampleSize
        return true
    }

    @MainThread
    fun clean(caller: String) {
        requiredMainThread()

        val notifyTileSnapshotListJob = notifyTileSnapshotListJob
        if (notifyTileSnapshotListJob != null) {
            notifyTileSnapshotListJob.cancel("clean:$caller")
            this.notifyTileSnapshotListJob = null
        }

        val sampleSize = sampleSize
        if (sampleSize != 0) {
            var freeCount = 0
            sortedTileMap.values.forEach { tileList ->
                freeCount += freeTiles(tileList, skipNotify = true)
            }
            this.sampleSize = 0
            logger.d { "clean:$caller. freeCount=$freeCount. '${imageSource.key}" }

            if (freeCount > 0) {
                updateTileSnapshotList()
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
            "${imageSource.key}_tile_${tile.srcRect.toShortString()}_${imageInfo.exifOrientation}_${tile.sampleSize}"
        val cachedValue = tileMemoryCacheHelper.get(memoryCacheKey)
        if (cachedValue != null) {
            tile.setTileBitmap(cachedValue, fromCache = true)
            tile.state = Tile.STATE_LOADED
            logger.d { "loadTile. successful, fromMemory. $tile. '${imageSource.key}'" }
            updateTileSnapshotList()
            return true
        }

        logger.d("loadTile. started. $tile. '${imageSource.key}'")
        tile.loadJob = coroutineScope.async {
            tile.state = Tile.STATE_LOADING
            updateTileSnapshotList()

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
            updateTileSnapshotList()
        }

        return true
    }

    @MainThread
    private fun freeTile(tile: Tile, skipNotify: Boolean = false): Boolean {
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

        if (!skipNotify) {
            updateTileSnapshotList()
        }
        return true
    }

    @MainThread
    private fun freeTiles(
        tiles: List<Tile>,
        @Suppress("SameParameterValue") skipNotify: Boolean = false
    ): Int {
        var freeCount = 0
        tiles.forEach { tile ->
            if (freeTile(tile, skipNotify)) {
                freeCount++
            }
        }
        if (!skipNotify && freeCount > 0) {
            updateTileSnapshotList()
        }
        return freeCount
    }

    private fun updateTileSnapshotList() {
        if (notifyTileSnapshotListJob?.isActive == true) {
            return
        }

        notifyTileSnapshotListJob = coroutineScope.launch {
            var running = true
            while (running && isActive) {
                val sampleSize = sampleSize
                val imageLoadRect = imageLoadRect
                val foregroundTileSnapshots = LinkedList<TileSnapshot>()
                var foregroundLoadAllCompleted = true
                var foregroundAnimationAllFinished = true
                var foregroundInsideCount = 0
                var foregroundOutsideCount = 0
                var foregroundLoadedCount = 0
                var foregroundLoadingCount = 0
                var foregroundAnimatingCount = 0
                val foregroundTiles = sortedTileMap[sampleSize]
                val foregroundTileCount = foregroundTiles?.size ?: 0
                foregroundTiles?.forEach { foregroundTile ->
                    val animationState = foregroundTile.animationState
                    animationState.calculate(tileAnimationSpec.duration)
                    if (animationState.running) {
                        foregroundAnimatingCount++
                    }
                    foregroundAnimationAllFinished =
                        foregroundAnimationAllFinished && !animationState.running

                    if (foregroundTile.state == Tile.STATE_LOADED) {
                        foregroundLoadedCount++
                    } else if (foregroundTile.state == Tile.STATE_LOADING) {
                        foregroundLoadingCount++
                    }

                    if (foregroundTile.srcRect.overlaps(imageLoadRect)) {
                        foregroundLoadAllCompleted =
                            foregroundLoadAllCompleted && foregroundTile.state == Tile.STATE_LOADED
                        foregroundInsideCount++
                    } else {
                        foregroundOutsideCount++
                    }

                    foregroundTileSnapshots.add(foregroundTile.toSnapshot())
                }

                /*
                 * If the foreground tile is all loaded and the animation is all over, release the background tile,
                 * otherwise continue to display the background tile
                 */
                var backgroundTileCount = 0
                var backgroundFreeCount = 0
                val backgroundTileSnapshots = LinkedList<TileSnapshot>()
                val currentSampleSize = sampleSize
                val lastSampleSize = lastSampleSize
                val disabledBackgroundTiles = disabledBackgroundTiles
                sortedTileMap.entries.forEach { (eachSampleSize, eachTiles) ->
                    if (eachSampleSize != currentSampleSize) {
                        if (!disabledBackgroundTiles
                            && isBackground(lastSampleSize, currentSampleSize, eachSampleSize)
                        ) {
                            if (foregroundLoadAllCompleted && foregroundAnimationAllFinished) {
                                val freeCount = freeTiles(eachTiles, skipNotify = true)
                                backgroundFreeCount += freeCount
                            } else {
                                eachTiles.forEach { backgroundTile ->
                                    if (backgroundTile.srcRect.overlaps(imageLoadRect) && backgroundTile.state == Tile.STATE_LOADED) {
                                        backgroundTileCount++
                                        if (backgroundTile.animationState.running) {
                                            backgroundTile.animationState.stop()
                                        }
                                        backgroundTileSnapshots.add(backgroundTile.toSnapshot())
                                    }
                                }
                            }
                        } else {
                            val freeCount = freeTiles(eachTiles, skipNotify = true)
                            backgroundFreeCount += freeCount
                        }
                    }
                }

                logger.d {
                    "updateTileSnapshotList. " +
                            "sampleSize=$sampleSize, " +
                            "foregroundTileCount=$foregroundTileCount," +
                            "foregroundInsideCount=$foregroundInsideCount, " +
                            "foregroundOutsideCount=$foregroundOutsideCount, " +
                            "foregroundLoadedCount=$foregroundLoadedCount, " +
                            "foregroundLoadingCount=$foregroundLoadingCount, " +
                            "foregroundAnimatingCount=$foregroundAnimatingCount, " +
                            "backgroundTileCount=$backgroundTileCount, " +
                            "backgroundFreeCount=$backgroundFreeCount, "
                }

                this@TileManager.foregroundTiles = foregroundTileSnapshots
                this@TileManager.backgroundTiles = backgroundTileSnapshots
                notifyTileChanged()

                running = !foregroundAnimationAllFinished
                if (running) {
                    delay(tileAnimationSpec.interval)
                }
            }
        }
    }

    private fun notifyTileChanged() {
        onTileChanged(this)
    }

    private fun notifySampleSizeChanged() {
        onSampleSizeChanged(this)
    }

    private fun notifyImageLoadRectChanged() {
        onImageLoadRectChanged(this)
    }
}