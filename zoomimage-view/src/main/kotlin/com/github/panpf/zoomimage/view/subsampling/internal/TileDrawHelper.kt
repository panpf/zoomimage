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
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.util.applyTransform
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.math.roundToInt

class TileDrawHelper(
    private val logger: Logger,
    private val view: View,
    private val zoomableEngine: ZoomableEngine,
    private val subsamplingEngine: SubsamplingEngine,
) {

    private val cacheRect1 = Rect()
    private val cacheRect2 = Rect()
    private val cacheRect3 = RectF()
    private val cacheDisplayMatrix: Matrix = Matrix()
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
        val checkpoint = canvas.save()
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
        canvas.restoreToCount(checkpoint)

        logger.d {
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
        imageInfo: ImageInfo,
        contentSize: IntSizeCompat,
        tileSnapshot: TileSnapshot
    ): Boolean {
        val tileImage = tileSnapshot.tileImage?.takeIf { !it.isRecycled } ?: return false
        val bitmap =
            (tileImage as BitmapTileImage).bitmap.takeIf { !it.isRecycled } ?: return false

        val tileDrawSrcRect = cacheRect2.apply {
            set(0, 0, bitmap.width, bitmap.height)
        }

        val widthScale = imageInfo.width / contentSize.width.toFloat()
        val heightScale = imageInfo.height / contentSize.height.toFloat()
        val tileDrawDstRect = cacheRect1.apply {
            set(
                /* left = */ (tileSnapshot.srcRect.left / widthScale).roundToInt(),
                /* top = */ (tileSnapshot.srcRect.top / heightScale).roundToInt(),
                /* right = */ (tileSnapshot.srcRect.right / widthScale).roundToInt(),
                /* bottom = */ (tileSnapshot.srcRect.bottom / heightScale).roundToInt()
            )
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
        val tileDrawRect = cacheRect3.apply {
            set(
                /* left = */ round(tileSnapshot.srcRect.left / widthScale),
                /* top = */ round(tileSnapshot.srcRect.top / heightScale),
                /* right = */ round(tileSnapshot.srcRect.right / widthScale),
                /* bottom = */ round(tileSnapshot.srcRect.bottom / heightScale)
            )
        }

        val tileBoundsPaint = boundsPaint
        val bitmapNoRecycled = tileSnapshot.tileImage?.isRecycled == false
        val boundsColor = when {
            bitmapNoRecycled && tileSnapshot.state == TileState.STATE_LOADED -> Color.GREEN
            tileSnapshot.state == TileState.STATE_LOADING -> Color.YELLOW
            tileSnapshot.state == TileState.STATE_NONE -> Color.GRAY
            else -> Color.RED
        }
        tileBoundsPaint.color = boundsColor

        canvas.drawRect(tileDrawRect, tileBoundsPaint)
    }
}