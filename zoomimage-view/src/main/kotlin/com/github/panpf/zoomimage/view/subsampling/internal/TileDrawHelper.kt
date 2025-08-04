/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import android.view.View
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.tileColor
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class TileDrawHelper(
    private val logger: Logger,
    private val view: View,
    private val zoomableEngine: ZoomableEngine,
    private val subsamplingEngine: SubsamplingEngine,
) {

    private val cacheRect1 = Rect()
    private val cacheRect2 = Rect()
    private val cacheMatrix: Matrix = Matrix()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var tilePaint: Paint = Paint()
    private val boundsPaint: Paint by lazy {
        Paint().apply {
            style = STROKE
            strokeWidth = 0.5f * view.resources.displayMetrics.density
        }
    }

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
        if (zoomableEngine.containerSizeState.value.isEmpty()) return
        if (zoomableEngine.contentSizeState.value.isEmpty()) return
        val foregroundTiles: List<TileSnapshot> = subsamplingEngine.foregroundTilesState.value
            .takeIf { it.isNotEmpty() } ?: return
        val imageLoadRect: IntRectCompat = subsamplingEngine.imageLoadRectState.value
            .takeIf { !it.isEmpty } ?: return
        val backgroundTiles: List<TileSnapshot> = subsamplingEngine.backgroundTilesState.value
        val showTileBounds = subsamplingEngine.showTileBoundsState.value

        var backgroundCount = 0
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0

        canvas.withZooming(
            zoomableEngine = zoomableEngine,
            cacheMatrix = cacheMatrix,
            firstScaleByContentSize = true,
        ) {
            backgroundTiles.forEach { tileSnapshot ->
                if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                    if (drawTile(canvas, tileSnapshot, drawBounds = false)) {
                        backgroundCount++
                    }
                }
            }
            foregroundTiles.forEach { tileSnapshot ->
                if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                    insideLoadCount++
                    if (drawTile(canvas, tileSnapshot, drawBounds = showTileBounds)) {
                        realDrawCount++
                    }
                } else {
                    outsideLoadCount++
                }
            }
        }

        logger.v {
            "TileDrawHelper. drawTiles. tiles=${foregroundTiles.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}, " +
                    "backgroundCount=${backgroundCount}. " +
                    "'${subsamplingEngine.subsamplingImage?.key}'"
        }
    }

    private fun drawTile(
        canvas: Canvas,
        tileSnapshot: TileSnapshot,
        drawBounds: Boolean,
    ): Boolean {
        val tileImage = tileSnapshot.tileImage
            ?.takeIf { !it.isRecycled } ?: return false
        val bitmap = (tileImage as BitmapTileImage).bitmap
            .takeIf { !it.isRecycled } ?: return false

        val srcRect = cacheRect1.apply {
            set(
                /* left = */ 0,
                /* top = */ 0,
                /* right = */ bitmap.width,
                /* bottom = */ bitmap.height
            )
        }
        val dstRect = cacheRect2.apply {
            set(
                /* left = */ tileSnapshot.srcRect.left,
                /* top = */ tileSnapshot.srcRect.top,
                /* right = */ tileSnapshot.srcRect.right,
                /* bottom = */ tileSnapshot.srcRect.bottom
            )
        }
        tilePaint.alpha = tileSnapshot.alpha
        canvas.drawBitmap(
            /* bitmap = */ bitmap,
            /* src = */ srcRect,
            /* dst = */ dstRect,
            /* paint = */ tilePaint
        )

        if (drawBounds) {
            val boundsColor = tileColor(
                state = tileSnapshot.state,
                from = tileSnapshot.from,
                withinLoadArea = true,
            )
            boundsPaint.color = boundsColor
            canvas.drawRect(dstRect, boundsPaint)
        }

        return true
    }
}