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

import android.content.Context
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
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.OnTileChangedListener
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.core.toShortString
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.canUseSubsampling
import com.github.panpf.zoomimage.subsampling.internal.applyExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal class SubsamplingEngine constructor(
    val context: Context,
    logger: Logger,
    private val zoomEngine: ZoomEngine, // todo 避免直接依赖 ZoomEngine
) {

    val logger: Logger = logger.newLogger(module = "Subsampling-Engine")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var initJob: Job? = null
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private val tempDisplayMatrix = Matrix()
    private val tempDrawableVisibleRect = Rect()
    internal var onTileChangedListenerList: MutableSet<OnTileChangedListener>? = null
    private val tileBoundsPaint: Paint by lazy {
        Paint().apply {
            style = STROKE
            strokeWidth = 1f * Resources.getSystem().displayMetrics.density
        }
    }
    private val strokeHalfWidth by lazy { (tileBoundsPaint.strokeWidth) / 2 }

    var disableMemoryCache: Boolean = false
    var disallowReuseBitmap: Boolean = false
    var ignoreExifOrientation: Boolean = false
    var tileBitmapPool: TileBitmapPool? = null
    var tileMemoryCache: TileMemoryCache? = null
    var showTileBounds = false
        set(value) {
            if (field != value) {
                field = value
                invalidateView()
            }
        }
    var paused = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    imageSource?.run { logger.d { "pause. '$key'" } }
                    tileManager?.clean()
                } else {
                    imageSource?.run { logger.d { "resume. '$key'" } }
                    refreshTiles()
                }
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    val destroyed: Boolean
        get() = imageSource == null
    val tileList: List<Tile>?
        get() = tileManager?.tileList
    val imageVisibleRect: IntRectCompat
        get() = tileManager?.imageVisibleRect ?: IntRectCompat.Zero
    val imageLoadRect: IntRectCompat
        get() = tileManager?.imageLoadRect ?: IntRectCompat.Zero
    var imageSize: IntSizeCompat? = null
        private set
    var imageMimeType: String? = null
        private set
    var imageExifOrientation: Int? = null
        private set

    init {
        zoomEngine.addOnMatrixChangeListener {
            refreshTiles()
        }
    }

    fun setImageSource(imageSource: ImageSource) {
        initJob?.cancel("setImageSource")
        initJob = null
        tileManager?.destroy()
        tileManager = null

        // todo 抽离后续的重置逻辑为一个单独的方法，并在这里保存 imageSource，drawableSiz 额和 viewsize 满足条件重新初始化

        val viewSize = zoomEngine.viewSize
        if (viewSize.isEmpty()) {
            logger.d { "setImageSource failed. View size error. '${imageSource.key}'" }
            return
        }
        val drawableSize = zoomEngine.drawableSize
        if (drawableSize.isEmpty()) {
            logger.d { "setImageSource failed. Drawable size error. '${imageSource.key}'" }
            return
        }
        initJob = scope.launch(Dispatchers.Main) {
            val imageInfo = imageSource.readImageInfo()
                ?.let { if (!ignoreExifOrientation) it.applyExifOrientation() else it }
            val result =
                imageInfo?.let { canUseSubsampling(it, drawableSize) } ?: -10
            if (imageInfo != null && result >= 0) {
                logger.d {
                    "setImageSource success. " +
                            "viewSize=$viewSize, " +
                            "drawableSize: ${drawableSize.toShortString()}, " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageSource.key}'"
                }
                tileManager = TileManager(
                    logger = logger,
                    imageSource = imageSource,
                    viewSize = viewSize,
                    tileBitmapPool = if (disallowReuseBitmap) null else tileBitmapPool,
                    tileMemoryCache = if (disableMemoryCache) null else tileMemoryCache,
                    imageInfo = imageInfo,
                    onTileChanged = {
                        this@SubsamplingEngine.invalidateView()
                    }
                )
                this@SubsamplingEngine.imageSource = imageSource
                zoomEngine.imageSize = imageInfo.size   // todo 改成回调的方式，提供成功初始化监听
                refreshTiles()
            } else {
                val cause = when (result) {
                    -1 -> "The Drawable size is greater than or equal to the original image"
                    -2 -> "The drawable aspect ratio is inconsistent with the original image"
                    -3 -> "Image type not support subsampling"
                    -10 -> "Can't decode image bounds or exif orientation"
                    else -> "Unknown"
                }
                logger.d {
                    "setImageSource failed. $cause. " +
                            "viewSize=$viewSize, " +
                            "drawableSize: ${drawableSize.toShortString()}, " +
                            "imageInfo: ${imageInfo?.toShortString()}. " +
                            "'${imageSource.key}'"
                }
            }
            initJob = null
        }
    }

    @MainThread
    private fun refreshTiles() {
        requiredMainThread()
        val imageSource = imageSource ?: return
        if (destroyed) {
            logger.d { "refreshTiles. interrupted. destroyed. '${imageSource.key}'" }
            return
        }
        if (paused) {
            logger.d { "refreshTiles. interrupted. paused. '${imageSource.key}'" }
            return
        }
        val manager = tileManager
        if (manager == null) {
            logger.d { "refreshTiles. interrupted. initializing. '${imageSource.key}'" }
            return
        }
        if (zoomEngine.rotateDegrees % 90 != 0) {
            logger.d { "refreshTiles. interrupted. rotate degrees must be in multiples of 90. '${imageSource.key}'" }
            return
        }

        val drawableSize = zoomEngine.drawableSize
        val scaling = zoomEngine.isScaling
        val displayMatrix = tempDisplayMatrix.apply {
            zoomEngine.getDisplayMatrix(this)
        }
        val drawableVisibleRect = tempDrawableVisibleRect.apply {
            zoomEngine.getVisibleRect(this)
        }

        if (drawableVisibleRect.isEmpty) {
            logger.d {
                "refreshTiles. interrupted. drawableVisibleRect is empty. drawableVisibleRect=${drawableVisibleRect}. '${imageSource.key}'"
            }
            tileManager?.clean()
            return
        }

        if (scaling) {
            logger.d {
                "refreshTiles. interrupted. scaling. '${imageSource.key}'"
            }
            return
        }

        if (zoomEngine.scale.format(2) <= zoomEngine.minScale.format(2)) {
            logger.d {
                "refreshTiles. interrupted. minScale. '${imageSource.key}'"
            }
            tileManager?.clean()
            return
        }

        tileManager?.refreshTiles(
            drawableSize,
            drawableVisibleRect.toIntRectCompat(),
            displayMatrix.getScale().scaleX.format(2)
        )
    }

    @MainThread
    fun onDraw(canvas: Canvas) {
        if (destroyed) return
        val drawableSize = zoomEngine.drawableSize
        val displayMatrix = tempDisplayMatrix
        val drawableVisibleRect = tempDrawableVisibleRect
        drawTiles(canvas, drawableSize, drawableVisibleRect, displayMatrix)
    }

    @MainThread
    internal fun invalidateView() {
        zoomEngine.view.invalidate()
    }

    @MainThread
    fun destroy() {
        requiredMainThread()
        if (destroyed) return
        logger.d { "destroy. '${imageSource?.key}'" }
        initJob?.cancel("destroy")
        tileManager?.destroy()
        tileManager = null
        imageSource = null
        imageSize = null
        imageMimeType = null
        imageExifOrientation = null
        zoomEngine.imageSize = IntSizeCompat.Zero
    }

    fun addOnTileChangedListener(listener: OnTileChangedListener) {
        this.onTileChangedListenerList = (onTileChangedListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnTileChangedListener(listener: OnTileChangedListener): Boolean {
        return onTileChangedListenerList?.remove(listener) == true
    }

    @MainThread
    private fun drawTiles(
        canvas: Canvas,
        drawableSize: IntSizeCompat,
        drawableVisibleRect: Rect,
        displayMatrix: Matrix
    ) {
        requiredMainThread()
        val tileManager = tileManager ?: return
        val tileList = tileManager.rowTileList ?: return
        val widthScale = tileManager.imageInfo.width / drawableSize.width.toFloat()
        val heightScale = tileManager.imageInfo.height / drawableSize.height.toFloat()
        canvas.withSave {
            canvas.concat(displayMatrix)
            tileList.forEach { tile ->
                if (tile.srcRect.overlaps(tileManager.imageLoadRect)) {
                    val tileBitmap = tile.bitmap
                    val tileSrcRect = tile.srcRect
                    val tileDrawRect = Rect(
                        left = floor(tileSrcRect.left / widthScale).toInt(),
                        top = floor(tileSrcRect.top / heightScale).toInt(),
                        right = floor(tileSrcRect.right / widthScale).toInt(),
                        bottom = floor(tileSrcRect.bottom / heightScale).toInt()
                    )
                    if (tileBitmap != null) {
                        canvas.drawBitmap(
                            /* bitmap = */ tileBitmap,
                            /* src = */ Rect(0, 0, tileBitmap.width, tileBitmap.height),
                            /* dst = */ tileDrawRect,
                            /* paint = */ null
                        )
                    }

                    if (showTileBounds) {
                        val boundsColor = when {
                            tileBitmap != null -> Color.GREEN
                            tile.loadJob?.isActive == true -> Color.YELLOW
                            else -> Color.RED
                        }
                        tileBoundsPaint.color = ColorUtils.setAlphaComponent(boundsColor, 100)
                        val tileBoundsRect = Rect(
                            left = floor(tileDrawRect.left + strokeHalfWidth).toInt(),
                            top = floor(tileDrawRect.top + strokeHalfWidth).toInt(),
                            right = ceil(tileDrawRect.right - strokeHalfWidth).toInt(),
                            bottom = ceil(tileDrawRect.bottom - strokeHalfWidth).toInt()
                        )
                        canvas.drawRect(/* r = */ tileBoundsRect, /* paint = */ tileBoundsPaint)
                    }
                }
            }
        }
    }
}