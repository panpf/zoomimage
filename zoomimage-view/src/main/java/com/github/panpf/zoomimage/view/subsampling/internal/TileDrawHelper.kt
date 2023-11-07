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

package com.github.panpf.zoomimage.view.subsampling.internal

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withSave
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.view.internal.applyTransform
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

class TileDrawHelper(
    private val logger: Logger,
    private val view: View,
    private val zoomableEngine: ZoomableEngine,
    private val subsamplingEngine: SubsamplingEngine,
) {

    private val cacheRect1 = Rect()
    private val cacheRect2 = Rect()
    private val cacheDisplayMatrix: Matrix = Matrix()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var tilePaint: Paint = Paint()
    private var cacheTileBoundsPaint: Paint? = null

    init {
        coroutineScope.launch {
            listOf(
                subsamplingEngine.readyState,
                subsamplingEngine.foregroundTilesState,
                subsamplingEngine.backgroundTilesState,
                subsamplingEngine.showTileBoundsState,
            ).merge().collect {
                view.invalidate()
            }
        }
    }

    fun drawTiles(canvas: Canvas) {
        val containerSize =
            zoomableEngine.containerSizeState.value.takeIf { !it.isEmpty() } ?: return
        val contentSize = zoomableEngine.contentSizeState.value.takeIf { !it.isEmpty() } ?: return
        val transform = zoomableEngine.transformState.value
        val imageInfo = subsamplingEngine.imageInfoState.value ?: return
        val backgroundTiles = subsamplingEngine.backgroundTilesState.value
        val foregroundTiles =
            subsamplingEngine.foregroundTilesState.value.takeIf { it.isNotEmpty() } ?: return
        val imageLoadRect =
            subsamplingEngine.imageLoadRectState.value.takeIf { !it.isEmpty } ?: return

        var backgroundCount = 0
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0
        canvas.withSave {
            canvas.concat(cacheDisplayMatrix.applyTransform(transform, containerSize))

            backgroundTiles.forEach { tileSnapshot ->
                if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                    if (drawTile(canvas, imageInfo, contentSize, tileSnapshot)) {
                        backgroundCount++
                    }
                }
            }
            foregroundTiles.forEach { tileSnapshot ->
                if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                    insideLoadCount++
                    if (drawTile(canvas, imageInfo, contentSize, tileSnapshot)) {
                        realDrawCount++
                    }
                    if (subsamplingEngine.showTileBoundsState.value) {
                        drawTileBounds(canvas, imageInfo, contentSize, tileSnapshot)
                    }
                } else {
                    outsideLoadCount++
                }
            }
        }

        logger.d {
            "drawTiles. tiles=${foregroundTiles.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}, " +
                    "backgroundCount=${backgroundCount}. " +
                    "'${subsamplingEngine.imageKey}'"
        }
    }

    private fun drawTile(
        canvas: Canvas,
        imageInfo: ImageInfo,
        contentSize: IntSizeCompat,
        tileSnapshot: TileSnapshot
    ): Boolean {
        val tileBitmap = tileSnapshot.bitmap ?: return false
        val bitmap = (tileBitmap as AndroidTileBitmap).bitmap ?: return false

        val widthScale = imageInfo.width / contentSize.width.toFloat()
        val heightScale = imageInfo.height / contentSize.height.toFloat()
        val tileDrawDstRect = cacheRect1.apply {
            set(
                /* left = */ floor(tileSnapshot.srcRect.left / widthScale).toInt(),
                /* top = */ floor(tileSnapshot.srcRect.top / heightScale).toInt(),
                /* right = */ floor(tileSnapshot.srcRect.right / widthScale).toInt(),
                /* bottom = */ floor(tileSnapshot.srcRect.bottom / heightScale).toInt()
            )
        }
        val tileDrawSrcRect = cacheRect2.apply {
            set(0, 0, bitmap.width, bitmap.height)
        }
        tilePaint.alpha = tileSnapshot.alpha
        canvas.drawBitmap(
            /* bitmap = */ bitmap,
            /* src = */ tileDrawSrcRect,
            /* dst = */ tileDrawDstRect,
            /* paint = */ tilePaint
        )
        return true
    }

    private fun drawTileBounds(
        canvas: Canvas,
        imageInfo: ImageInfo,
        contentSize: IntSizeCompat,
        tileSnapshot: TileSnapshot
    ) {
        val widthScale = imageInfo.width / contentSize.width.toFloat()
        val heightScale = imageInfo.height / contentSize.height.toFloat()
        val tileDrawDstRect = cacheRect1.apply {
            set(
                /* left = */ floor(tileSnapshot.srcRect.left / widthScale).toInt(),
                /* top = */ floor(tileSnapshot.srcRect.top / heightScale).toInt(),
                /* right = */ floor(tileSnapshot.srcRect.right / widthScale).toInt(),
                /* bottom = */ floor(tileSnapshot.srcRect.bottom / heightScale).toInt()
            )
        }
        val bitmapNoRecycled = tileSnapshot.bitmap?.isRecycled == false
        val boundsColor = when {
            bitmapNoRecycled && tileSnapshot.state == TileState.STATE_LOADED -> Color.GREEN
            tileSnapshot.state == TileState.STATE_LOADING -> Color.YELLOW
            tileSnapshot.state == TileState.STATE_NONE -> Color.GRAY
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

    private fun getTileBoundsPaint(): Paint {
        return cacheTileBoundsPaint ?: Paint()
            .apply {
                style = STROKE
                strokeWidth = 1f * Resources.getSystem().displayMetrics.density
            }
            .apply {
                this@TileDrawHelper.cacheTileBoundsPaint = this
            }
    }
}