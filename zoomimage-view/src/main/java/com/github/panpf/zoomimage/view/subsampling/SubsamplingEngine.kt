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
package com.github.panpf.zoomimage.view.subsampling

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withSave
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.core.toShortString
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsampling
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.internal.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.view.zoom.internal.format
import com.github.panpf.zoomimage.view.zoom.internal.toIntRectCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

class SubsamplingEngine constructor(logger: Logger) {

    private val logger: Logger = logger.newLogger(module = "SubsamplingEngine")
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var tileMemoryCacheHelper = TileMemoryCacheHelper(logger)
    private var tileBitmapPoolHelper = TileBitmapPoolHelper(logger)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var onTileChangeListenerList: MutableSet<OnTileChangeListener>? = null
    private var onReadyChangeListenerList: MutableSet<OnReadyChangeListener>? = null
    private val cacheRect1 = Rect()
    private val cacheRect2 = Rect()
    private var cacheTileBoundsPaint: Paint? = null
    private var lastResetTileDecoderJob: Job? = null
    private var lastDisplayScale: Float? = null
    private var lastDisplayMinScale: Float? = null
    private var lastContentVisibleRect: Rect? = null

    var containerSize: IntSizeCompat = IntSizeCompat.Zero
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("containerSizeChanged")
            }
        }
    var contentSize: IntSizeCompat = IntSizeCompat.Zero
        set(value) {
            if (field != value) {
                field = value
                resetTileDecoder("contentSizeChanged")
            }
        }
    var ignoreExifOrientation: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("ignoreExifOrientationChanged")
            }
        }
    var tileMemoryCache: TileMemoryCache?
        get() = tileMemoryCacheHelper.tileMemoryCache
        set(value) {
            tileMemoryCacheHelper.tileMemoryCache = value
        }
    var disableMemoryCache: Boolean
        get() = tileMemoryCacheHelper.disableMemoryCache
        set(value) {
            tileMemoryCacheHelper.disableMemoryCache = value
        }
    var tileBitmapPool: TileBitmapPool?
        get() = tileBitmapPoolHelper.tileBitmapPool
        set(value) {
            tileBitmapPoolHelper.tileBitmapPool = value
        }
    var disallowReuseBitmap: Boolean
        get() = tileBitmapPoolHelper.disallowReuseBitmap
        set(value) {
            tileBitmapPoolHelper.disallowReuseBitmap = value
        }
    var showTileBounds = false
        set(value) {
            if (field != value) {
                field = value
                notifyTileChange()
            }
        }

    val ready: Boolean
        get() = imageInfo != null && tileDecoder != null && tileManager != null
    var imageInfo: ImageInfo? = null
        private set
    val tileList: List<Tile>?
        get() = tileManager?.tileList
    val imageVisibleRect: IntRectCompat
        get() = tileManager?.imageVisibleRect ?: IntRectCompat.Zero
    val imageLoadRect: IntRectCompat
        get() = tileManager?.imageLoadRect ?: IntRectCompat.Zero

    var paused = false
        set(value) {
            if (field != value) {
                field = value
                if (ready) {
                    if (value) {
                        imageSource?.run { logger.d { "pause. '$key'" } }
                        tileManager?.clean("paused")
                    } else {
                        imageSource?.run { logger.d { "resume. '$key'" } }
                        refreshTiles("resume")
                    }
                }
            }
        }

    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${imageSource?.key}'" }
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        resetTileDecoder("setImageSource")
        return true
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
            val imageInfo = imageSource.readImageInfo(ignoreExifOrientation)
            this@SubsamplingEngine.imageInfo = imageInfo
            val result =
                imageInfo?.let { canUseSubsampling(it, contentSize) } ?: -10
            if (imageInfo != null && result >= 0) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageSource.key}'"
                }
                this@SubsamplingEngine.tileDecoder = TileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    tileBitmapPoolHelper = tileBitmapPoolHelper,
                    imageInfo = imageInfo,
                )
                resetTileManager(caller)
            } else {
                val cause = when (result) {
                    -1 -> "The content size is greater than or equal to the original image"
                    -2 -> "The content aspect ratio is different with the original image"
                    -3 -> "Image type not support subsampling"
                    -10 -> "Can't decode image bounds or exif orientation"
                    else -> "Unknown"
                }
                logger.d {
                    "resetTileDecoder:$caller. failed, $cause. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "imageInfo: ${imageInfo?.toShortString()}. " +
                            "'${imageSource.key}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager(caller)

        val imageSource = imageSource ?: return
        val tileDecoder = tileDecoder ?: return
        val imageInfo = imageInfo ?: return
        val containerSize = containerSize.takeIf { !it.isEmpty() } ?: return

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            imageSource = imageSource,
            containerSize = containerSize,
            tileMemoryCacheHelper = tileMemoryCacheHelper,
            tileBitmapPoolHelper = tileBitmapPoolHelper,
            imageInfo = imageInfo,
            onTileChanged = { notifyTileChange() }
        )
        logger.d {
            val tileMaxSize = tileManager.tileMaxSize
            val tileMap = tileManager.tileMap
            val tileMapInfoList = tileMap.keys.sortedDescending()
                .map { "${it}:${tileMap[it]?.size}" }
            "resetTileManager:$caller. success. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "tileMaxSize=${tileMaxSize.toShortString()}, " +
                    "tileMap=$tileMapInfoList, " +
                    "'${imageSource.key}'"
        }
        this@SubsamplingEngine.tileManager = tileManager
        notifyReadyChange()
        notifyTileChange()
    }

    fun refreshTiles(
        displayScale: Float,
        displayMinScale: Float,
        contentVisibleRect: Rect,
        caller: String,
    ) {
        this.lastDisplayScale = displayScale
        this.lastDisplayMinScale = displayMinScale
        this.lastContentVisibleRect = contentVisibleRect
        val imageSource = imageSource ?: return
        val tileManager = tileManager ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        if (paused) {
            logger.d { "refreshTiles:$caller. interrupted, paused. '${imageSource.key}'" }
            return
        }
        if (contentVisibleRect.isEmpty) {
            logger.d {
                "refreshTiles:$caller. interrupted, contentVisibleRect is empty. " +
                        "contentVisibleRect=${contentVisibleRect}. '${imageSource.key}'"
            }
            tileManager.clean("refreshTiles:contentVisibleRectEmpty")
            return
        }
        if (displayScale.format(2) <= displayMinScale.format(2)) {
            logger.d { "refreshTiles:$caller. interrupted, reach minScale. '${imageSource.key}'" }
            tileManager.clean("refreshTiles:reachMinScale")
            return
        }
        tileManager.refreshTiles(
            contentSize = contentSize,
            contentVisibleRect = contentVisibleRect.toIntRectCompat(),
            scale = displayScale,
            caller = caller
        )
    }

    fun drawTiles(canvas: Canvas, displayMatrix: Matrix) {
        val imageSource = imageSource ?: return
        val imageInfo = imageInfo ?: return
        val tileList = tileList?.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val imageLoadRect = imageLoadRect.takeIf { !it.isEmpty } ?: return
        val widthScale = imageInfo.width / contentSize.width.toFloat()
        val heightScale = imageInfo.height / contentSize.height.toFloat()
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0
        canvas.withSave {
            canvas.concat(displayMatrix)
            tileList.forEach { tile ->
                if (tile.srcRect.overlaps(imageLoadRect)) {
                    insideLoadCount++
                    val tileBitmap = tile.bitmap
                    val tileDrawDstRect = cacheRect1.apply {
                        set(
                            /* left = */ floor(tile.srcRect.left / widthScale).toInt(),
                            /* top = */ floor(tile.srcRect.top / heightScale).toInt(),
                            /* right = */ floor(tile.srcRect.right / widthScale).toInt(),
                            /* bottom = */ floor(tile.srcRect.bottom / heightScale).toInt()
                        )
                    }
                    if (tileBitmap != null) {
                        realDrawCount++
                        val tileDrawSrcRect = cacheRect2.apply {
                            set(0, 0, tileBitmap.width, tileBitmap.height)
                        }
                        canvas.drawBitmap(
                            /* bitmap = */ tileBitmap,
                            /* src = */ tileDrawSrcRect,
                            /* dst = */ tileDrawDstRect,
                            /* paint = */ null
                        )
                    }

                    if (showTileBounds) {
                        val boundsColor = when {
                            tileBitmap != null -> Color.GREEN
                            tile.loadJob?.isActive == true -> Color.YELLOW
                            else -> Color.RED
                        }
                        val tileBoundsPaint = getTileBoundsPaint()
                        tileBoundsPaint.color = ColorUtils.setAlphaComponent(boundsColor, 100)
                        val boundsStrokeHalfWidth by lazy { (tileBoundsPaint.strokeWidth) / 2 }
                        val tileBoundsRect = cacheRect2.apply {
                            set(
                                /* left = */
                                floor(tileDrawDstRect.left + boundsStrokeHalfWidth).toInt(),
                                /* top = */
                                floor(tileDrawDstRect.top + boundsStrokeHalfWidth).toInt(),
                                /* right = */
                                ceil(tileDrawDstRect.right - boundsStrokeHalfWidth).toInt(),
                                /* bottom = */
                                ceil(tileDrawDstRect.bottom - boundsStrokeHalfWidth).toInt()
                            )
                        }
                        canvas.drawRect(/* r = */ tileBoundsRect, /* paint = */ tileBoundsPaint)
                    }
                } else {
                    outsideLoadCount++
                }
            }
        }
        logger.d {
            "drawTiles. tiles=${tileList.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}. " +
                    "'${imageSource.key}'"
        }
    }

    private fun cleanTileDecoder(caller: String) {
        val lastResetTileDecoderJob = this@SubsamplingEngine.lastResetTileDecoderJob
        if (lastResetTileDecoderJob != null) {
            lastResetTileDecoderJob.cancel("cleanTileDecoder:$caller")
            this@SubsamplingEngine.lastResetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingEngine.tileDecoder
        if (tileDecoder != null) {
            logger.d { "cleanTileDecoder:$caller. '${imageSource?.key}'" }
            tileDecoder.destroy("cleanTileDecoder:$caller")
            this@SubsamplingEngine.tileDecoder = null
            notifyReadyChange()
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingEngine.tileManager
        if (tileManager != null) {
            logger.d { "cleanTileManager:$caller. '${imageSource?.key}'" }
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingEngine.tileManager = null
            notifyReadyChange()
            notifyTileChange()
        }
    }

    fun addOnTileChangedListener(listener: OnTileChangeListener) {
        this.onTileChangeListenerList = (onTileChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnTileChangedListener(listener: OnTileChangeListener): Boolean {
        return onTileChangeListenerList?.remove(listener) == true
    }

    fun addOnReadyChangeListener(listener: OnReadyChangeListener) {
        this.onReadyChangeListenerList = (onReadyChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnReadyChangeListener(listener: OnReadyChangeListener): Boolean {
        return onReadyChangeListenerList?.remove(listener) == true
    }

    private fun getTileBoundsPaint(): Paint {
        return cacheTileBoundsPaint ?: Paint()
            .apply {
                style = STROKE
                strokeWidth = 1f * Resources.getSystem().displayMetrics.density
            }
            .apply {
                this@SubsamplingEngine.cacheTileBoundsPaint = this
            }
    }

    private fun notifyTileChange() {
        onTileChangeListenerList?.forEach {
            it.onTileChanged()
        }
    }

    private fun notifyReadyChange() {
        val ready = ready
        onReadyChangeListenerList?.forEach {
            it.onReadyChanged(ready)
        }
    }

    private fun refreshTiles(@Suppress("SameParameterValue") caller: String) {
        val displayScale = lastDisplayScale
        val lastDisplayMinScale = lastDisplayMinScale
        val contentVisibleRect = lastContentVisibleRect
        if (displayScale != null && lastDisplayMinScale != null && contentVisibleRect != null) {
            refreshTiles(
                displayScale = displayScale,
                displayMinScale = lastDisplayMinScale,
                contentVisibleRect = contentVisibleRect,
                caller = caller
            )
        }
    }
}