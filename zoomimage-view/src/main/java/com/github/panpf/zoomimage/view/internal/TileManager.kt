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
package com.github.panpf.zoomimage.view.internal

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import androidx.annotation.MainThread
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withSave
import com.github.panpf.zoomimage.DefaultTileBitmap
import com.github.panpf.zoomimage.imagesource.ImageSource
import com.github.panpf.zoomimage.core.SizeCompat
import com.github.panpf.zoomimage.core.internal.freeBitmap
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

internal class TileManager constructor(
    private val engine: SubsamplingEngine,
    private val decoder: TileDecoder,
    private val imageSource: ImageSource,
    viewSize: SizeCompat,
) {

    private val tileBoundsPaint: Paint by lazy {
        Paint().apply {
            style = STROKE
            strokeWidth = 1f * Resources.getSystem().displayMetrics.density
        }
    }
    private val strokeHalfWidth by lazy { (tileBoundsPaint.strokeWidth) / 2 }

    private val tileMaxSize = viewSize.let {
        SizeCompat(it.width / 2, it.height / 2)
    }
    private val tileMap: Map<Int, List<Tile>> = initializeTileMap(decoder.imageSize, tileMaxSize)
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)
    private var lastTileList: List<Tile>? = null
    private var lastSampleSize: Int? = null
    private val _imageVisibleRect = Rect()
    private val _imageLoadRect = Rect()
    private val _tileDrawRect = Rect()

    val tileList: List<Tile>?
        get() = lastTileList?.toList()
    val imageVisibleRect: Rect
        get() = Rect().apply { set(_imageVisibleRect) }
    val imageLoadRect: Rect
        get() = Rect().apply { set(_imageLoadRect) }

    init {
        engine.logger.d(SubsamplingEngine.MODULE) {
            val tileMapInfoList = tileMap.keys.sortedDescending().map {
                "${it}:${tileMap[it]?.size}"
            }
            "tileMap. $tileMapInfoList. '${imageSource.key}'"
        }
    }

    @MainThread
    fun refreshTiles(drawableSize: SizeCompat, drawableVisibleRect: Rect, displayMatrix: Matrix) {
        requiredMainThread()

        val zoomScale = displayMatrix.getScale().scaleX.format(2)
        val sampleSize = findSampleSize(
            imageWidth = decoder.imageSize.width,
            imageHeight = decoder.imageSize.height,
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
            engine.logger.d(SubsamplingEngine.MODULE) {
                "refreshTiles. no tileList. " +
                        "imageSize=${decoder.imageSize}, " +
                        "drawableSize=$drawableSize, " +
                        "drawableVisibleRect=${drawableVisibleRect}, " +
                        "zoomScale=$zoomScale, " +
                        "sampleSize=$lastSampleSize. " +
                        "'${imageSource.key}"
            }
            return
        }
        resetVisibleAndLoadRect(drawableSize, drawableVisibleRect)

        engine.logger.d(SubsamplingEngine.MODULE) {
            "refreshTiles. started. " +
                    "imageSize=${decoder.imageSize}, " +
                    "imageVisibleRect=$_imageVisibleRect, " +
                    "imageLoadRect=$_imageLoadRect, " +
                    "drawableSize=$drawableSize, " +
                    "drawableVisibleRect=${drawableVisibleRect}, " +
                    "zoomScale=$zoomScale, " +
                    "sampleSize=$lastSampleSize. " +
                    "'${imageSource.key}"
        }
        tileList.forEach { tile ->
            if (tile.srcRect.crossWith(_imageLoadRect)) {
                loadTile(tile)
            } else {
                freeTile(tile)
            }
        }
        engine.invalidateView()
    }

    @MainThread
    fun onDraw(canvas: Canvas, drawableSize: SizeCompat, drawableVisibleRect: Rect, displayMatrix: Matrix) {
        requiredMainThread()

        val tileList = lastTileList
        if (tileList == null) {
            if (lastSampleSize != null) {
                engine.logger.d(SubsamplingEngine.MODULE) {
                    "onDraw. no tileList sampleSize is $lastSampleSize. '${imageSource.key}'"
                }
            }
            return
        }
        resetVisibleAndLoadRect(drawableSize, drawableVisibleRect)
        val widthScale = decoder.imageSize.width / drawableSize.width.toFloat()
        val heightScale = decoder.imageSize.height / drawableSize.height.toFloat()
        canvas.withSave {
            canvas.concat(displayMatrix)
            tileList.forEach { tile ->
                if (tile.srcRect.crossWith(_imageLoadRect)) {
                    val tileBitmap = tile.bitmap
                    val tileSrcRect = tile.srcRect
                    val tileDrawRect = _tileDrawRect.apply {
                        set(
                            floor(tileSrcRect.left / widthScale).toInt(),
                            floor(tileSrcRect.top / heightScale).toInt(),
                            floor(tileSrcRect.right / widthScale).toInt(),
                            floor(tileSrcRect.bottom / heightScale).toInt()
                        )
                    }
                    if (tileBitmap != null) {
                        canvas.drawBitmap(
                            tileBitmap,
                            Rect(0, 0, tileBitmap.width, tileBitmap.height),
                            tileDrawRect,
                            null
                        )
                    }

                    if (engine.showTileBounds) {
                        val boundsColor = when {
                            tileBitmap != null -> Color.GREEN
                            tile.loadJob?.isActive == true -> Color.YELLOW
                            else -> Color.RED
                        }
                        tileBoundsPaint.color = ColorUtils.setAlphaComponent(boundsColor, 100)
                        tileDrawRect.set(
                            floor(tileDrawRect.left + strokeHalfWidth).toInt(),
                            floor(tileDrawRect.top + strokeHalfWidth).toInt(),
                            ceil(tileDrawRect.right - strokeHalfWidth).toInt(),
                            ceil(tileDrawRect.bottom - strokeHalfWidth).toInt()
                        )
                        canvas.drawRect(tileDrawRect, tileBoundsPaint)
                    }
                }
            }
        }
    }

    @MainThread
    private fun notifyTileChanged() {
        requiredMainThread()

        engine.onTileChangedListenerList?.forEach {
            it.onTileChanged()
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

        val memoryCacheKey = "${imageSource.key}_tile_${tile.srcRect.toVeryShortString()}_${tile.inSampleSize}"
        val cachedValue = if (!engine.disableMemoryCache) {
            engine.tileMemoryCache?.get(memoryCacheKey)
        } else {
            null
        }
        if (cachedValue != null) {
            tile.countBitmap = cachedValue
            engine.logger.d(SubsamplingEngine.MODULE) {
                "loadTile. successful. fromMemory. $tile. '${imageSource.key}'"
            }
            engine.invalidateView()
            notifyTileChanged()
            return
        }

        tile.loadJob = scope.async(decodeDispatcher) {
            val bitmap = decoder.decode(tile)
            when {
                bitmap == null -> {
                    engine.logger.e(SubsamplingEngine.MODULE) {
                        "loadTile. null. $tile. '${imageSource.key}'"
                    }
                }

                isActive -> {
                    withContext(Dispatchers.Main) {
                        val newCountBitmap = if (!engine.disableMemoryCache) {
                            engine.tileMemoryCache?.put(
                                key = memoryCacheKey,
                                bitmap = bitmap,
                                imageKey = imageSource.key,
                                imageSize = decoder.imageSize,
                                imageMimeType = decoder.imageMimeType,
                                imageExifOrientation = decoder.imageExifOrientation,
                                disallowReuseBitmap = engine.disallowReuseBitmap
                            ) ?: DefaultTileBitmap(memoryCacheKey, bitmap)
                        } else {
                            DefaultTileBitmap(memoryCacheKey, bitmap)
                        }
                        tile.countBitmap = newCountBitmap
                        engine.logger.d(SubsamplingEngine.MODULE) {
                            "loadTile. successful. $tile. '${imageSource.key}'"
                        }
                        engine.invalidateView()
                        notifyTileChanged()
                    }
                }

                else -> {
                    engine.logger.d(SubsamplingEngine.MODULE) {
                        "loadTile. canceled. $tile. '${imageSource.key}'"
                    }
                    val bitmapPool = engine.tileBitmapPool
                    if (bitmapPool != null) {
                        bitmapPool.freeBitmap(
                            logger = engine.logger,
                            bitmap = bitmap,
                            disallowReuseBitmap = engine.disallowReuseBitmap,
                            caller = "tile:jobCanceled"
                        )
                    } else {
                        bitmap.recycle()
                    }
                    engine.logger.d(SubsamplingEngine.MODULE) {
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
            engine.logger.d(SubsamplingEngine.MODULE) {
                "freeTile. $tile. '${imageSource.key}'"
            }
            tile.countBitmap = null
            notifyTileChanged()
        }
    }

    private fun resetVisibleAndLoadRect(drawableSize: SizeCompat, drawableVisibleRect: Rect) {
        val drawableScaled = decoder.imageSize.width / drawableSize.width.toFloat()
        _imageVisibleRect.apply {
            set(
                floor(drawableVisibleRect.left * drawableScaled).toInt(),
                floor(drawableVisibleRect.top * drawableScaled).toInt(),
                ceil(drawableVisibleRect.right * drawableScaled).toInt(),
                ceil(drawableVisibleRect.bottom * drawableScaled).toInt()
            )
        }
        // Increase the visible area as the loading area,
        // this preloads tiles around the visible area,
        // the user will no longer feel the loading process while sliding slowly
        _imageLoadRect.apply {
            set(
                _imageVisibleRect.left - tileMaxSize.width / 2,
                _imageVisibleRect.top - tileMaxSize.height / 2,
                _imageVisibleRect.right + tileMaxSize.width / 2,
                _imageVisibleRect.bottom + tileMaxSize.height / 2
            )
        }
    }

    @MainThread
    private fun freeAllTile() {
        tileMap.values.forEach { tileList ->
            tileList.forEach { tile ->
                freeTile(tile)
            }
        }
        engine.invalidateView()
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