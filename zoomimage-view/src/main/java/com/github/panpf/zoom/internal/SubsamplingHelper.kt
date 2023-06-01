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
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import androidx.annotation.MainThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.Size
import com.github.panpf.zoom.OnMatrixChangeListener
import com.github.panpf.zoom.OnTileChangedListener
import com.github.panpf.zoom.Tile
import com.github.panpf.zoom.ZoomAbility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SubsamplingHelper constructor(
    private val context: Context,
    private val sketch: Sketch,
    private val zoomAbility: ZoomAbility,
    private val imageUri: String,
    private val imageInfo: ImageInfo,
    private val memoryCachePolicy: CachePolicy,
    private val disallowReuseBitmap: Boolean,
    viewSize: Size,
) {

    companion object {
        internal const val MODULE = "SubsamplingHelper"
    }

    private val tempDrawMatrix = Matrix()
    private val tempDrawableVisibleRect = Rect()
    private val logger: Logger = sketch.logger
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
    private val onMatrixChangeListener = OnMatrixChangeListener {
        refreshTiles()
    }

    private var _destroyed: Boolean = false
    private var tileManager: TileManager? = null
    internal var onTileChangedListenerList: MutableSet<OnTileChangedListener>? = null

    @Suppress("MemberVisibilityCanBePrivate")
    val destroyed: Boolean
        get() = _destroyed
    val tileList: List<Tile>?
        get() = tileManager?.tileList

    var showTileBounds = false
        set(value) {
            field = value
            invalidateView()
        }
    var paused = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    logger.d(MODULE) { "pause. '$imageUri'" }
                    tileManager?.clean()
                } else {
                    logger.d(MODULE) { "resume. '$imageUri'" }
                    refreshTiles()
                }
            }
        }

    init {
        scope.launch(Dispatchers.Main) {
            val dataSource = withContext(Dispatchers.IO) {
                sketch.components.newFetcherOrThrow(LoadRequest(context, imageUri)).fetch()
            }.getOrThrow().dataSource
            val tileDecoder = TileDecoder(
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
                subsamplingHelper = this@SubsamplingHelper
            )
            refreshTiles()
        }

        zoomAbility.addOnMatrixChangeListener(onMatrixChangeListener)
    }

    @MainThread
    private fun refreshTiles() {
        requiredMainThread()

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
        if (zoomAbility.rotateDegrees % 90 != 0) {
            logger.d(MODULE) {
                "refreshTiles. interrupted. rotate degrees must be in multiples of 90. '$imageUri'"
            }
            return
        }

        val drawableSize = zoomAbility.drawableSize
        val scaling = zoomAbility.isScaling
        val drawMatrix = tempDrawMatrix.apply {
            zoomAbility.getDrawMatrix(this)
        }
        val drawableVisibleRect = tempDrawableVisibleRect.apply {
            zoomAbility.getVisibleRect(this)
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

        if (zoomAbility.scale.format(2) <= zoomAbility.minScale.format(2)) {
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
        val drawableSize = zoomAbility.drawableSize
        val drawMatrix = tempDrawMatrix
        val drawableVisibleRect = tempDrawableVisibleRect
        tileManager?.onDraw(canvas, drawableSize, drawableVisibleRect, drawMatrix)
    }

    @MainThread
    internal fun invalidateView() {
        requiredMainThread()
        zoomAbility.view?.invalidate()
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
        val drawableSize = zoomAbility.drawableSize.takeIf { !it.isEmpty } ?: return
        val drawableVisibleRect = tempDrawableVisibleRect.apply {
            zoomAbility.getVisibleRect(this)
        }.takeIf { !it.isEmpty } ?: return
        tileManager?.eachTileList(drawableSize, drawableVisibleRect, action)
    }

    @MainThread
    fun destroy() {
        requiredMainThread()

        if (_destroyed) return
        logger.d(MODULE) {
            "destroy"
        }
        _destroyed = true
        zoomAbility.removeOnMatrixChangeListener(onMatrixChangeListener)
        scope.cancel()
        tileManager?.destroy()
        tileManager = null
    }
}