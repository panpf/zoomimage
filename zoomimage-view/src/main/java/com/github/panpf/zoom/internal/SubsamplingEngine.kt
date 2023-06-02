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
package com.github.panpf.zoom.internal

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import androidx.annotation.MainThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoom.ImageSource
import com.github.panpf.zoom.OnTileChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class SubsamplingEngine constructor(
    private val context: Context,
    private val logger: Logger,
    private val zoomEngine: ZoomEngine,
) {

    companion object {
        internal const val MODULE = "SubsamplingEngine"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var _destroyed: Boolean = false
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private val tempDrawMatrix = Matrix()
    private val tempDrawableVisibleRect = Rect()
    internal var onTileChangedListenerList: MutableSet<OnTileChangedListener>? = null

    var disallowMemoryCache: Boolean = false
    var disallowReuseBitmap: Boolean = false
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

    val destroyed: Boolean
        get() = _destroyed
    val tileList: List<Tile>?
        get() = tileManager?.tileList

    init {
        zoomEngine.addOnMatrixChangeListener {
            refreshTiles()
        }
    }

    fun setImageSource(imageSource: ImageSource) {
        val viewContentSize = zoomEngine.viewSize
        if (viewContentSize.isEmpty) {
            logger.d(MODULE) { "Can't use Subsampling. View size error" }
            return
        }
        scope.cancel("setImageSource")
        scope.launch(Dispatchers.Main) {
            val optionsJob= async(Dispatchers.IO) {
                kotlin.runCatching {
                    imageSource.openInputStream().getOrNull()?.use { inputStream ->
                        BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                            BitmapFactory.decodeStream(inputStream, null, this)
                        }
                    }?.takeIf { it.outWidth > 0 && it.outHeight > 0 }
                }
            }
            val exifOrientationJob = async(Dispatchers.IO) {
                kotlin.runCatching {
                    imageSource.openInputStream().getOrNull()?.use { inputStream ->
                        ExifInterface(inputStream)
                            .getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED
                            )
                    }
                }
            }
            val options = optionsJob.await().getOrNull() ?: return@launch
            val exifOrientation = exifOrientationJob.await().getOrNull() ?: return@launch
            // todo 继续
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight
            val imageType = options.outMimeType
            this@SubsamplingEngine.imageSource = imageSource
            val tileDecoder = TileDecoder(
                logger = logger,
                sketch = sketch,
                imageUri = imageUri,
                imageInfo = imageInfo,
                disallowReuseBitmap = disallowReuseBitmap,
                dataSource = dataSource
            )
            tileManager = TileManager(
                sketch = sketch,
                imageUri = imageUri,
                imageSize = Size(imageInfo.width, imageInfo.height),
                memoryCachePolicy = memoryCachePolicy,
                disallowReuseBitmap = disallowReuseBitmap,
                viewSize = viewSize,
                decoder = tileDecoder,
                engine = this@SubsamplingEngine
            )
            refreshTiles()
        }
    }

    @MainThread
    private fun refreshTiles() {
        requiredMainThread()
        val imageSource = imageSource ?: return

        if (destroyed) {
            logger.d(MODULE) { "refreshTiles. interrupted. destroyed. '$imageUri'" }
            return
        }
        if (paused) {
            logger.d(MODULE) { "refreshTiles. interrupted. paused. '$imageUri'" }
            return
        }
        val manager = tileManager
        if (manager == null) {
            logger.d(MODULE) { "refreshTiles. interrupted. initializing. '$imageUri'" }
            return
        }
        if (zoomEngine.rotateDegrees % 90 != 0) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. rotate degrees must be in multiples of 90. '$imageUri'"
            }
            return
        }

        val drawableSize = zoomEngine.drawableSize
        val scaling = zoomEngine.isScaling
        val drawMatrix = tempDrawMatrix.apply {
            zoomEngine.getDrawMatrix(this)
        }
        val drawableVisibleRect = tempDrawableVisibleRect.apply {
            zoomEngine.getVisibleRect(this)
        }

        if (drawableVisibleRect.isEmpty) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. drawableVisibleRect is empty. drawableVisibleRect=${drawableVisibleRect}. '$imageUri'"
            }
            tileManager?.clean()
            return
        }

        if (scaling) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. scaling. '$imageUri'"
            }
            return
        }

        if (zoomEngine.scale.format(2) <= zoomEngine.minScale.format(2)) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. minScale. '$imageUri'"
            }
            tileManager?.clean()
            return
        }

        tileManager?.refreshTiles(drawableSize, drawableVisibleRect, drawMatrix)
    }

    @MainThread
    fun onDraw(canvas: Canvas) {
        requiredMainThread()

        if (destroyed) return
        val drawableSize = zoomEngine.drawableSize
        val drawMatrix = tempDrawMatrix
        val drawableVisibleRect = tempDrawableVisibleRect
        tileManager?.onDraw(canvas, drawableSize, drawableVisibleRect, drawMatrix)
    }

    @MainThread
    internal fun invalidateView() {
        requiredMainThread()
        zoomEngine.view.invalidate()
    }

    fun addOnTileChangedListener(listener: OnTileChangedListener) {
        this.onTileChangedListenerList = (onTileChangedListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnTileChangedListener(listener: OnTileChangedListener): Boolean {
        return onTileChangedListenerList?.remove(listener) == true
    }

    fun eachTileList(action: (tile: Tile, load: Boolean) -> Unit) {
        val drawableSize = zoomEngine.drawableSize.takeIf { !it.isEmpty } ?: return
        val drawableVisibleRect = tempDrawableVisibleRect.apply {
            zoomEngine.getVisibleRect(this)
        }.takeIf { !it.isEmpty } ?: return
        tileManager?.eachTileList(drawableSize, drawableVisibleRect, action)
    }

    @MainThread
    fun destroy() {
        requiredMainThread()
        imageSource = null
        if (_destroyed) return
        logger.d(MODULE) {
            "destroy"
        }
        _destroyed = true
        scope.cancel()
        tileManager?.destroy()
        tileManager = null
    }
}