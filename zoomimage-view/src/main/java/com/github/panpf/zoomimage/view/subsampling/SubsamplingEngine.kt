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
import com.github.panpf.zoomimage.subsampling.canUseSubsampling
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
    private val reuseRect1 = Rect()
    private val reuseRect2 = Rect()
    private var tileBoundsPaint: Paint? = null
    private var lastResetTileDecoderJob: Job? = null
    private var lastDisplayScale: Float? = null
    private var lastDisplayMinScale: Float? = null
    private var lastDrawableVisibleRect: Rect? = null
    private var onTileChangeListenerList: MutableSet<OnTileChangeListener>? = null
    private var onReadyChangeListenerList: MutableSet<OnReadyChangeListener>? = null
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null

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

    var ignoreExifOrientation: Boolean =
        false  // ignoreExifOrientation 的改变的同时 drawableSize 也会改变，所以这里不必重置

    //        set(value) {
//            if (field != value) {
//                field = value
//                resetTileDecoder("ignoreExifOrientationChanged")
//            }
//        }
    var tileMemoryCache: TileMemoryCache? = null
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("tileMemoryCacheChanged")   // todo 代价太大了
            }
        }
    var disableMemoryCache: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("disableMemoryCacheChanged")   // todo 代价太大了
            }
        }
    var tileBitmapPool: TileBitmapPool? = null
        set(value) {
            if (field != value) {
                field = value
                resetTileDecoder("tileBitmapPoolChanged")   // todo 代价太大了
            }
        }
    var disallowReuseBitmap: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resetTileDecoder("disallowReuseBitmapChanged")   // todo 代价太大了
            }
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

    fun setImageSource(imageSource: ImageSource?) {
        if (this.imageSource == imageSource) return
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        resetTileDecoder("setImageSource")
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("$caller:resetTileDecoder")
        cleanTileDecoder("$caller:resetTileDecoder")

        val imageSource = imageSource ?: return
        val drawableSize = contentSize.takeIf { !it.isEmpty() } ?: return

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
            val imageInfo = imageSource.readImageInfo(ignoreExifOrientation)
            this@SubsamplingEngine.imageInfo = imageInfo
            val result =
                imageInfo?.let { canUseSubsampling(it, drawableSize) } ?: -10
            if (imageInfo != null && result >= 0) {
                logger.d {
                    "resetTileDecoder success. $caller. " +
                            "drawableSize=${drawableSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageSource.key}'"
                }
                this@SubsamplingEngine.tileDecoder = TileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    tileBitmapPool = if (disallowReuseBitmap) null else tileBitmapPool,
                    imageInfo = imageInfo,
                )
                resetTileManager(caller)
            } else {
                val cause = when (result) {
                    -1 -> "The Drawable size is greater than or equal to the original image"
                    -2 -> "The drawable aspect ratio is inconsistent with the original image"
                    -3 -> "Image type not support subsampling"
                    -10 -> "Can't decode image bounds or exif orientation"
                    else -> "Unknown"
                }
                logger.d {
                    "resetTileDecoder failed. $caller. $cause. " +
                            "drawableSize: ${drawableSize.toShortString()}, " +
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
        val viewSize = containerSize.takeIf { !it.isEmpty() } ?: return

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            imageSource = imageSource,
            viewSize = viewSize,
            tileBitmapPool = if (disallowReuseBitmap) null else tileBitmapPool,
            tileMemoryCache = if (disableMemoryCache) null else tileMemoryCache,
            imageInfo = imageInfo,
            onTileChanged = { notifyTileChange() }
        )
        logger.d {
            val tileMaxSize = tileManager.tileMaxSize
            val tileMap = tileManager.tileMap
            val tileMapInfoList = tileMap.keys.sortedDescending()
                .map { "${it}:${tileMap[it]?.size}" }
            "resetTileManager success. $caller. " +
                    "viewSize=${viewSize.toShortString()}, " +
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
        this.lastDrawableVisibleRect = contentVisibleRect
        val imageSource = imageSource ?: return
        val tileManager = tileManager ?: return
        val drawableSize = contentSize.takeIf { !it.isEmpty() } ?: return
        if (paused) {
            logger.d { "refreshTiles. $caller. interrupted. paused. '${imageSource.key}'" }
            return
        }
        if (contentVisibleRect.isEmpty) {
            logger.d {
                "refreshTiles. $caller. interrupted. contentVisibleRect is empty. " +
                        "contentVisibleRect=${contentVisibleRect}. '${imageSource.key}'"
            }
            tileManager.clean("refreshTiles:contentVisibleRectEmpty")
            return
        }
        if (displayScale.format(2) <= displayMinScale.format(2)) {
            logger.d { "refreshTiles. $caller. interrupted. Reach minScale. '${imageSource.key}'" }
            tileManager.clean("refreshTiles:minScale")
            return
        }
        tileManager.refreshTiles(
            contentSize = drawableSize,
            contentVisibleRect = contentVisibleRect.toIntRectCompat(),
            scale = displayScale,
            caller = caller
        )
    }

    fun drawTiles(canvas: Canvas, displayMatrix: Matrix) {
        val imageSource = imageSource ?: return
        val tileManager = tileManager ?: return
        val drawableSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val imageInfo = imageInfo ?: return
        val tileList = tileManager.tileList?.takeIf { it.isNotEmpty() } ?: return
        val widthScale = imageInfo.width / drawableSize.width.toFloat()
        val heightScale = imageInfo.height / drawableSize.height.toFloat()
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0
        canvas.withSave {
            canvas.concat(displayMatrix)
            tileList.forEach { tile ->
                if (tile.srcRect.overlaps(tileManager.imageLoadRect)) {
                    insideLoadCount++
                    val tileBitmap = tile.bitmap
                    val tileDrawDstRect = reuseRect1.apply {
                        set(
                            /* left = */ floor(tile.srcRect.left / widthScale).toInt(),
                            /* top = */ floor(tile.srcRect.top / heightScale).toInt(),
                            /* right = */ floor(tile.srcRect.right / widthScale).toInt(),
                            /* bottom = */ floor(tile.srcRect.bottom / heightScale).toInt()
                        )
                    }
                    if (tileBitmap != null) {
                        realDrawCount++
                        val tileDrawSrcRect = reuseRect2.apply {
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
                        val strokeHalfWidth by lazy { (tileBoundsPaint.strokeWidth) / 2 }
                        val tileBoundsRect = reuseRect2.apply {
                            set(
                                /* left = */ floor(tileDrawDstRect.left + strokeHalfWidth).toInt(),
                                /* top = */
                                floor(tileDrawDstRect.top + strokeHalfWidth).toInt(),
                                /* right = */
                                ceil(tileDrawDstRect.right - strokeHalfWidth).toInt(),
                                /* bottom = */
                                ceil(tileDrawDstRect.bottom - strokeHalfWidth).toInt()
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
            lastResetTileDecoderJob.cancel("$caller:cleanTileDecoder")
            this@SubsamplingEngine.lastResetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingEngine.tileDecoder
        if (tileDecoder != null) {
            tileDecoder.destroy("$caller:cleanTileDecoder")
            this@SubsamplingEngine.tileDecoder = null
            logger.d { "cleanTileDecoder. $caller. '${imageSource?.key}'" }
            notifyReadyChange()
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingEngine.tileManager
        if (tileManager != null) {
            tileManager.clean("$caller:cleanTileManager")
            this@SubsamplingEngine.tileManager = null
            logger.d { "cleanTileManager. $caller. '${imageSource?.key}'" }
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
        return tileBoundsPaint ?: Paint()
            .apply {
                style = STROKE
                strokeWidth = 1f * Resources.getSystem().displayMetrics.density
            }
            .apply {
                this@SubsamplingEngine.tileBoundsPaint = this
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
        val drawableVisibleRect = lastDrawableVisibleRect
        if (displayScale != null && lastDisplayMinScale != null && drawableVisibleRect != null) {
            refreshTiles(
                displayScale = displayScale,
                displayMinScale = lastDisplayMinScale,
                contentVisibleRect = drawableVisibleRect,
                caller = caller
            )
        }
    }
}