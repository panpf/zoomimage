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

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.TileState
import kotlin.math.round
import kotlin.math.roundToInt

expect fun isCloseAntiAliasForDrawTile(): Boolean

fun Modifier.subsampling(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    val density = LocalDensity.current
    val tilePaint = remember {
        Paint().apply {
            isAntiAlias = !isCloseAntiAliasForDrawTile()
        }
    }
    val boundsPaint = remember {
        Paint().apply {
            style = PaintingStyle.Stroke
            strokeWidth = 0.5f * density.density
        }
    }
    this.drawWithContent {
        drawContent()

        val canvas = drawContext.canvas
        val imageInfo = subsamplingState.imageInfo ?: return@drawWithContent
        val contentSize = zoomableState.contentSize
            .takeIf { !it.isEmpty() } ?: return@drawWithContent
        val foregroundTiles = subsamplingState.foregroundTiles
            .takeIf { it.isNotEmpty() } ?: return@drawWithContent
        val backgroundTiles = subsamplingState.backgroundTiles
        val imageLoadRect = subsamplingState.imageLoadRect
            .takeIf { !it.isEmpty }?.toCompat() ?: return@drawWithContent

        var backgroundCount = 0
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0
        backgroundTiles.forEach { tileSnapshot ->
            if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                if (drawTile(canvas, imageInfo, contentSize, tileSnapshot, tilePaint)) {
                    backgroundCount++
                }
            }
        }
        foregroundTiles.forEach { tileSnapshot ->
            if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                insideLoadCount++
                if (drawTile(canvas, imageInfo, contentSize, tileSnapshot, tilePaint)) {
                    realDrawCount++
                }
                if (subsamplingState.showTileBounds) {
                    drawTileBounds(canvas, imageInfo, contentSize, tileSnapshot, boundsPaint)
                }
            } else {
                outsideLoadCount++
            }
        }

        subsamplingState.logger.d {
            "drawTiles. tiles=${foregroundTiles.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}, " +
                    "backgroundCount=${backgroundCount}. " +
                    "'${subsamplingState.imageKey}'"
        }
    }
}

private fun drawTile(
    canvas: Canvas,
    imageInfo: ImageInfo,
    contentSize: IntSize,
    tileSnapshot: TileSnapshot,
    tilePaint: Paint
): Boolean {
    val tileBitmap = tileSnapshot.tileBitmap?.takeIf { !it.isRecycled } ?: return false
    val imageBitmap = (tileBitmap as ComposeTileBitmap).imageBitmap

    val widthScale: Float = imageInfo.width / (contentSize.width.toFloat())
    val heightScale: Float = imageInfo.height / (contentSize.height.toFloat())
    val tileDrawRect = IntRect(
        left = (tileSnapshot.srcRect.left / widthScale).roundToInt(),
        top = (tileSnapshot.srcRect.top / heightScale).roundToInt(),
        right = (tileSnapshot.srcRect.right / widthScale).roundToInt(),
        bottom = (tileSnapshot.srcRect.bottom / heightScale).roundToInt()
    )

    tilePaint.alpha = tileSnapshot.alpha / 255f

    val srcSize = IntSize(imageBitmap.width, imageBitmap.height)
    val dstOffset = tileDrawRect.topLeft
    val dstSize = tileDrawRect.size
    canvas.drawImageRect(
        image = imageBitmap,
        srcOffset = IntOffset.Zero,
        srcSize = srcSize,
        dstOffset = dstOffset,
        dstSize = dstSize,
        paint = tilePaint
    )
    return true
}

private fun drawTileBounds(
    canvas: Canvas,
    imageInfo: ImageInfo,
    contentSize: IntSize,
    tileSnapshot: TileSnapshot,
    boundsPaint: Paint
) {
    val widthScale: Float = imageInfo.width / (contentSize.width.toFloat())
    val heightScale: Float = imageInfo.height / (contentSize.height.toFloat())
    val tileDrawRect = Rect(
        left = round(tileSnapshot.srcRect.left / widthScale),
        top = round(tileSnapshot.srcRect.top / heightScale),
        right = round(tileSnapshot.srcRect.right / widthScale),
        bottom = round(tileSnapshot.srcRect.bottom / heightScale)
    )

    val bitmapNoRecycled = tileSnapshot.tileBitmap?.isRecycled == false
    val boundsColor = when {
        bitmapNoRecycled && tileSnapshot.state == TileState.STATE_LOADED -> Color.Green
        tileSnapshot.state == TileState.STATE_LOADING -> Color.Yellow
        tileSnapshot.state == TileState.STATE_NONE -> Color.Gray
        else -> Color.Red
    }
    boundsPaint.color = boundsColor

    canvas.drawRect(rect = tileDrawRect, paint = boundsPaint)
}