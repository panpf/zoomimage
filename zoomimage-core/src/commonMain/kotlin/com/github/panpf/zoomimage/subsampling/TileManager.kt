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
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.findSampleSize
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
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

/**
 * Manage the loading and release of tiles
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.TileManagerTest]
 */
class TileManager constructor(
    logger: Logger,
    private val tileDecoder: TileDecoder,
    private val tileBitmapConvertor: TileBitmapConvertor?,
    private val tileBitmapCacheHelper: TileBitmapCacheHelper,
    private val tileBitmapReuseHelper: TileBitmapReuseHelper?,
    private val imageSource: ImageSource,
    private val imageInfo: ImageInfo,
    private val contentSize: IntSizeCompat,
    private val preferredTileSize: IntSizeCompat,
    private val onTileChanged: (tileManager: TileManager) -> Unit,
    private val onSampleSizeChanged: (tileManager: TileManager) -> Unit,
    private val onImageLoadRectChanged: (tileManager: TileManager) -> Unit,
) {

    companion object {
        const val DefaultPausedContinuousTransformType =
            ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(2)
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val logger: Logger = logger.newLogger(module = "TileManager")
    private var lastScale: Float? = null
    private var lastSampleSize: Int = 0
    private var lastContentVisibleRect: IntRectCompat? = null
    private var updateTileSnapshotListJob: Job? = null

    /**
     * A continuous transform type that needs to pause loading
     */
    var pausedContinuousTransformType: Int = DefaultPausedContinuousTransformType

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    var disabledBackgroundTiles: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateTileSnapshotList("disabledBackgroundTilesChanged")
            }
        }

    /**
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec = TileAnimationSpec.Default

    /**
     * Tile Map with sample size from largest to smallest
     */
    val sortedTileGridMap: Map<Int, List<Tile>>

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

    init {
        val maxSampleSize =
            findSampleSize(imageSize = imageInfo.size, thumbnailSize = contentSize, scale = 1f)
        sortedTileGridMap = calculateTileGridMap(
            imageSize = imageInfo.size,
            preferredTileSize = preferredTileSize,
        ).filterKeys { it <= maxSampleSize }
    }

    /**
     * Refresh the tiles, [scale] is used to calculate sampleSize, [contentVisibleRect] is used to calculate imageLoadRect, and then decide which tiles need to be loaded and which need to be freed based on sampleSize and imageLoadRect.
     *
     * [rotation] and [continuousTransformType] are only used to filter cases where a refresh is not required and will not affect the loading and release of the tile
     *
     * @return 0: Success;
     * -1: scale is less than or equal to 1f;
     * -2: foregroundTiles is null or size is 1;
     * -3: imageLoadRect is empty;
     * -4: rotation is not a multiple of 90;
     * -5: continuousTransformType hits pausedContinuousTransformType;
     */
    @MainThread
    fun refreshTiles(
        scale: Float,
        contentVisibleRect: IntRectCompat,
        rotation: Int,
        @ContinuousTransformType continuousTransformType: Int,
        caller: String,
    ): Int {
        /*
         * If the following detections fail, simply skip the refresh and keep the current state
         */
        if (rotation % 90 != 0) {
            logger.d { "refreshTiles:$caller. interrupted, rotation is not a multiple of 90: $rotation. '${imageSource.key}'" }
            return -1
        }
        if (continuousTransformType and pausedContinuousTransformType != 0) {
            val continuousTransformTypeName = ContinuousTransformType.name(continuousTransformType)
            logger.d {
                "refreshTiles:$caller. interrupted, continuousTransformType is $continuousTransformTypeName. '${imageSource.key}'"
            }
            return -2
        }

        /* reset sampleSize and imageLoadRect */
        val oldSampleSize = sampleSize
        val oldImageLoadRect = imageLoadRect
        val sampleSizeChanged = resetSampleSize(scale)
        val imageLoadRectChanged = resetImageLoadRect(contentVisibleRect)
        val newSampleSize = sampleSize
        val newImageLoadRect = imageLoadRect

        /*
         * When the following detection fails, subsampling is no longer needed, so empty the existing tile
         */
        val foregroundTiles = sortedTileGridMap[newSampleSize]
        if (foregroundTiles == null || foregroundTiles.size == 1) {
            logger.d {
                "refreshTiles:$caller. interrupted, foregroundTiles is null or size is 1. " +
                        "foregroundTilesSize=${foregroundTiles?.size ?: 0}, " +
                        "sampleSizeChanged=${sampleSizeChanged}, " +
                        "sampleSize=$oldSampleSize -> $newSampleSize, " +
                        "imageSize=${imageInfo.size.toShortString()}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "scale=${scale.format(4)}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "tileGridMap=${sortedTileGridMap.toIntroString()}. " +
                        "'${imageSource.key}'"
            }
            if (sampleSizeChanged) {
                clean("refreshTiles:foregroundTilesEmptyOrOne")
                updateTileSnapshotList("refreshTiles:foregroundTilesEmptyOrOne")
            }
            return -3
        }
        if (newImageLoadRect.isEmpty) {
            logger.d {
                "refreshTiles:$caller. interrupted, imageLoadRect is empty. " +
                        "imageLoadRect=${oldImageLoadRect.toShortString()} -> ${newImageLoadRect.toShortString()}, " +
                        "imageSize=${imageInfo.size.toShortString()}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                        "'${imageSource.key}'"
            }
            if (imageLoadRectChanged) {
                clean("refreshTiles:contentVisibleRectEmpty")
                updateTileSnapshotList("refreshTiles:contentVisibleRectEmpty")
            }
            return -4
        }

        /*
         * free or load the tile
         */
        var expectLoadCount = 0
        var actualLoadCount = 0
        var expectFreeCount = 0
        var actualFreeCount = 0
        val lastSampleSize = lastSampleSize
        sortedTileGridMap.entries.forEach { (eachSampleSize, eachTiles) ->
            if (eachSampleSize == newSampleSize) {
                eachTiles.forEach { foregroundTile ->
                    if (foregroundTile.srcRect.overlaps(newImageLoadRect)) {
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
                && isBackground(lastSampleSize, newSampleSize, eachSampleSize)
            ) {
                eachTiles.forEach { backgroundTile ->
                    if (!backgroundTile.srcRect.overlaps(newImageLoadRect)) {
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
            val continuousTransformTypeName = ContinuousTransformType.name(continuousTransformType)
            "refreshTiles:$caller. " +
                    "loadCount=${actualLoadCount}/${expectLoadCount}, " +
                    "freeCount=${actualFreeCount}/${expectFreeCount}. " +
                    "sampleSize=$oldSampleSize -> $newSampleSize, " +
                    "foregroundTiles=${foregroundTiles.size}, " +
                    "imageLoadRect=${oldImageLoadRect.toShortString()} -> ${newImageLoadRect.toShortString()}. " +
                    "scale=$scale, " +
                    "contentVisibleRect=${contentVisibleRect.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "continuousTransformType=${continuousTransformTypeName}, " +
                    "imageInfo=${imageInfo.toShortString()}, " +
                    "'${imageSource.key}"
        }
        if (sampleSizeChanged || imageLoadRectChanged || actualFreeCount > 0) {
            updateTileSnapshotList("refreshTiles:loadOrFreeTile")
        }
        return 0
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

    private fun resetSampleSize(scale: Float): Boolean {
        val lastScale = lastScale
        val currentSampleSize = sampleSize
        if (currentSampleSize != 0 && scale == lastScale) {
            return false
        }
        this.lastScale = scale

        val newSampleSize = if (scale > 1f) {
            findSampleSize(
                imageSize = imageInfo.size,
                thumbnailSize = contentSize,
                scale = scale
            )
        } else {
            // When scale is less than or equal to 0, there is no enlargement of content, so tile is not needed
            0
        }
        if (newSampleSize == currentSampleSize) {
            return false
        }

        this.lastSampleSize = currentSampleSize
        this.sampleSize = newSampleSize
        return true
    }

    private fun resetImageLoadRect(contentVisibleRect: IntRectCompat): Boolean {
        if (lastContentVisibleRect == contentVisibleRect) {
            return false
        }
        lastContentVisibleRect = contentVisibleRect

        val newImageLoadRect = calculateImageLoadRect(
            imageSize = imageInfo.size,
            contentSize = contentSize,
            preferredTileSize = preferredTileSize,
            contentVisibleRect = contentVisibleRect
        )
        if (newImageLoadRect == imageLoadRect) {
            return false
        }

        this.imageLoadRect = newImageLoadRect
        return true
    }

    @MainThread
    fun clean(caller: String) {
        val updateTileSnapshotListJob = updateTileSnapshotListJob
        if (updateTileSnapshotListJob != null && updateTileSnapshotListJob.isActive) {
            logger.d { "clean:$caller. cancel updateTileSnapshotListJob. '${imageSource.key}" }
            updateTileSnapshotListJob.cancel("clean:$caller")
            this.updateTileSnapshotListJob = null
        }

        val sampleSize = sampleSize
        if (sampleSize != 0) {
            var freeCount = 0
            sortedTileGridMap.values.forEach { tileList ->
                freeCount += freeTiles(tileList, skipNotify = true)
            }
            logger.d { "clean:$caller. freeCount=$freeCount. '${imageSource.key}" }
            if (freeCount > 0) {
                updateTileSnapshotList("clean:$caller")
            }
        }
    }

    @MainThread
    private fun loadTile(tile: Tile): Boolean {
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
            "${imageSource.key}_tile_${tile.srcRect.toShortString()}_${tileDecoder.exifOrientation}_${tile.sampleSize}"
        val cachedValue = tileBitmapCacheHelper.get(memoryCacheKey)
        if (cachedValue != null) {
            tile.setTileBitmap(cachedValue, fromCache = true)
            tile.state = Tile.STATE_LOADED
            logger.d { "loadTile. successful, fromMemory. $tile. '${imageSource.key}'" }
            updateTileSnapshotList("loadTile:fromMemory")
            return true
        }

        logger.d("loadTile. started. $tile. '${imageSource.key}'")
        tile.loadJob = coroutineScope.async {
            tile.state = Tile.STATE_LOADING
            updateTileSnapshotList("loadTile:loading")

            val decodeResult = withContext(decodeDispatcher) {
                kotlin.runCatching {
                    tileDecoder.decode(tile.srcRect, tile.sampleSize)
                        ?.let { tileBitmapConvertor?.convert(it) ?: it }
                }
            }
            val tileBitmap = decodeResult.getOrNull()
            when {
                decodeResult.isFailure -> {
                    tile.state = Tile.STATE_ERROR
                    logger.e("loadTile. failed, ${decodeResult.exceptionOrNull()?.message}. $tile. '${imageSource.key}'")
                    updateTileSnapshotList("loadTile:failed")
                }

                tileBitmap == null -> {
                    tile.state = Tile.STATE_ERROR
                    logger.e("loadTile. failed, bitmap null. $tile. '${imageSource.key}'")
                    updateTileSnapshotList("loadTile:failed")
                }

                isActive -> {
                    val cacheTileBitmap = tileBitmapCacheHelper.put(
                        key = memoryCacheKey,
                        tileBitmap = tileBitmap,
                        imageUrl = imageSource.key,
                        imageInfo = imageInfo,
                        disableReuseBitmap = tileBitmapReuseHelper?.spec?.disabled ?: true,
                    )
                    tile.setTileBitmap(cacheTileBitmap ?: tileBitmap, fromCache = false)
                    tile.state = Tile.STATE_LOADED
                    logger.d { "loadTile. successful. $tile. '${imageSource.key}'" }
                    updateTileSnapshotList("loadTile:successful")
                }

                else -> {
                    logger.d {
                        "loadTile. canceled. bitmap=${tileBitmap}, $tile. '${imageSource.key}'"
                    }
                    tile.state = Tile.STATE_ERROR
                    if (tileBitmapReuseHelper != null) {
                        tileBitmapReuseHelper.freeTileBitmap(tileBitmap, "loadTile:jobCanceled")
                    } else {
                        tileBitmap.recycle()
                    }
                    updateTileSnapshotList("loadTile:canceled")
                }
            }
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
            updateTileSnapshotList("freeTile")
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
            updateTileSnapshotList("freeTiles")
        }
        return freeCount
    }

    private fun updateTileSnapshotList(caller: String) {
        if (updateTileSnapshotListJob?.isActive == true) {
            logger.d { "updateTileSnapshotList:$caller. skipped, notifyTileSnapshotListJob is running. '${imageSource.key}'" }
            return
        }

        logger.d { "updateTileSnapshotList:$caller. launched. '${imageSource.key}'" }
        updateTileSnapshotListJob = coroutineScope.launch {
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
                val foregroundTiles = sortedTileGridMap[sampleSize]
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
                sortedTileGridMap.entries.forEach { (eachSampleSize, eachTiles) ->
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
                            "foregroundTileCount=$foregroundTileCount, " +
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

            logger.d { "updateTileSnapshotList:$caller. end, running=$running, active=$isActive. '${imageSource.key}'" }
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