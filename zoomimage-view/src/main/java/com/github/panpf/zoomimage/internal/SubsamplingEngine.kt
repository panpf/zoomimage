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
package com.github.panpf.zoomimage.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import androidx.annotation.MainThread
import com.github.panpf.zoomimage.core.Logger
import com.github.panpf.zoomimage.OnTileChangedListener
import com.github.panpf.zoomimage.core.TileBitmapPool
import com.github.panpf.zoomimage.core.TileMemoryCache
import com.github.panpf.zoomimage.core.imagesource.ImageSource
import com.github.panpf.zoomimage.core.internal.ExifOrientationHelper
import com.github.panpf.zoomimage.core.SizeCompat
import com.github.panpf.zoomimage.core.internal.exifOrientationName
import com.github.panpf.zoomimage.core.internal.readExifOrientation
import com.github.panpf.zoomimage.core.internal.readImageBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class SubsamplingEngine constructor(
    val context: Context,
    val logger: Logger,
    private val zoomEngine: ZoomEngine,
) {

    companion object {
        internal const val MODULE = "SubsamplingEngine"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var initJob: Job? = null
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private val tempDisplayMatrix = Matrix()
    private val tempDrawableVisibleRect = Rect()
    internal var onTileChangedListenerList: MutableSet<OnTileChangedListener>? = null

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
                    imageSource?.run { logger.d(MODULE) { "pause. '$key'" } }
                    tileManager?.clean()
                } else {
                    imageSource?.run { logger.d(MODULE) { "resume. '$key'" } }
                    refreshTiles()
                }
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    val destroyed: Boolean
        get() = imageSource == null
    val tileList: List<Tile>?
        get() = tileManager?.tileList
    val imageVisibleRect: Rect
        get() = tileManager?.imageVisibleRect ?: Rect()
    val imageLoadRect: Rect
        get() = tileManager?.imageLoadRect ?: Rect()
    var imageSize: SizeCompat? = null
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

        val viewSize = zoomEngine.viewSize
        if (viewSize.isEmpty) {
            logger.d(MODULE) { "setImageSource failed. View size error. '${imageSource.key}'" }
            return
        }
        val drawableSize = zoomEngine.drawableSize
        if (drawableSize.isEmpty) {
            logger.d(MODULE) { "setImageSource failed. Drawable size error. '${imageSource.key}'" }
            return
        }
        val (drawableWidth, drawableHeight) = drawableSize
        initJob = scope.launch(Dispatchers.Main) {
            val optionsJob = async { imageSource.readImageBounds() }
            val exifOrientationJob =
                async { imageSource.readExifOrientation(ignoreExifOrientation) }
            val options = optionsJob.await()
                .apply { exceptionOrNull()?.printStackTrace() }
                .getOrNull()
            val exifOrientation = exifOrientationJob.await()
                .apply { exceptionOrNull()?.printStackTrace() }
                .getOrNull()
            if (options == null) {
                logger.w(MODULE) { "setImageSource failed. Can't decode image bounds. '${imageSource.key}'" }
                initJob = null
                return@launch
            }
            if (exifOrientation == null) {
                logger.w(MODULE) { "setImageSource failed. Can't decode image exifOrientation. '${imageSource.key}'" }
                initJob = null
                return@launch
            }
            val imageSize = ExifOrientationHelper(exifOrientation)
                .applyToSize(SizeCompat(options.outWidth, options.outHeight))
            val imageWidth = imageSize.width
            val imageHeight = imageSize.height
            val mimeType = options.outMimeType
            this@SubsamplingEngine.imageSource = imageSource
            this@SubsamplingEngine.imageSize = imageSize
            this@SubsamplingEngine.imageMimeType = mimeType
            this@SubsamplingEngine.imageExifOrientation = exifOrientation

            if (drawableWidth >= imageWidth && drawableHeight >= imageHeight) {
                logger.d(MODULE) {
                    "setImageSource failed. The Drawable size is greater than or equal to the original image. viewSize=$viewSize, drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: $mimeType. '${imageSource.key}'"
                }
                initJob = null
                return@launch
            }
            if (!canUseSubsampling(imageWidth, imageHeight, drawableWidth, drawableHeight)) {
                logger.w(MODULE) {
                    "setImageSource failed. The drawable aspect ratio is inconsistent with the original image. viewSize=$viewSize, drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: $mimeType. '${imageSource.key}'"
                }
                initJob = null
                return@launch
            }
            if (!isSupportBitmapRegionDecoder(mimeType)) {
                logger.d(MODULE) {
                    "setImageSource failed. Image type not support subsampling. viewSize=$viewSize, drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: $mimeType. '${imageSource.key}'"
                }
                initJob = null
                return@launch
            }

            logger.d(MODULE) {
                val exifOrientationName = exifOrientationName(exifOrientation)
                "setImageSource success. viewSize=$viewSize, drawableSize: ${drawableWidth}x${drawableHeight}, imageSize=$imageSize, mimeType=$mimeType, exifOrientation=$exifOrientationName. '${imageSource.key}'"
            }
            zoomEngine.imageSize = imageSize
            val tileDecoder = TileDecoder(
                engine = this@SubsamplingEngine,
                imageSource = imageSource,
                imageSize = imageSize,
                imageMimeType = mimeType,
                imageExifOrientation = exifOrientation,
            )
            tileManager = TileManager(
                engine = this@SubsamplingEngine,
                decoder = tileDecoder,
                imageSource = imageSource,
                viewSize = viewSize,
            )
            refreshTiles()
            initJob = null
        }
    }

    @MainThread
    private fun refreshTiles() {
        requiredMainThread()
        val imageSource = imageSource ?: return
        if (destroyed) {
            logger.d(MODULE) { "refreshTiles. interrupted. destroyed. '${imageSource.key}'" }
            return
        }
        if (paused) {
            logger.d(MODULE) { "refreshTiles. interrupted. paused. '${imageSource.key}'" }
            return
        }
        val manager = tileManager
        if (manager == null) {
            logger.d(MODULE) { "refreshTiles. interrupted. initializing. '${imageSource.key}'" }
            return
        }
        if (zoomEngine.rotateDegrees % 90 != 0) {
            logger.d(MODULE) { "refreshTiles. interrupted. rotate degrees must be in multiples of 90. '${imageSource.key}'" }
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
            logger.d(MODULE) {
                "refreshTiles. interrupted. drawableVisibleRect is empty. drawableVisibleRect=${drawableVisibleRect}. '${imageSource.key}'"
            }
            tileManager?.clean()
            return
        }

        if (scaling) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. scaling. '${imageSource.key}'"
            }
            return
        }

        if (zoomEngine.scale.format(2) <= zoomEngine.minScale.format(2)) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. minScale. '${imageSource.key}'"
            }
            tileManager?.clean()
            return
        }

        tileManager?.refreshTiles(drawableSize, drawableVisibleRect, displayMatrix)
    }

    @MainThread
    fun onDraw(canvas: Canvas) {
        if (destroyed) return
        val drawableSize = zoomEngine.drawableSize
        val displayMatrix = tempDisplayMatrix
        val drawableVisibleRect = tempDrawableVisibleRect
        tileManager?.onDraw(canvas, drawableSize, drawableVisibleRect, displayMatrix)
    }

    @MainThread
    internal fun invalidateView() {
        zoomEngine.view.invalidate()
    }

    @MainThread
    fun destroy() {
        requiredMainThread()
        if (destroyed) return
        logger.d(MODULE) { "destroy. '${imageSource?.key}'" }
        initJob?.cancel("destroy")
        tileManager?.destroy()
        tileManager = null
        imageSource = null
        imageSize = null
        imageMimeType = null
        imageExifOrientation = null
        zoomEngine.imageSize = SizeCompat.Empty
    }

    fun addOnTileChangedListener(listener: OnTileChangedListener) {
        this.onTileChangedListenerList = (onTileChangedListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnTileChangedListener(listener: OnTileChangedListener): Boolean {
        return onTileChangedListenerList?.remove(listener) == true
    }
}