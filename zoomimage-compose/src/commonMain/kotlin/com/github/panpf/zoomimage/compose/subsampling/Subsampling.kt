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

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.util.Logger
import kotlin.math.ceil
import kotlin.math.floor

fun Modifier.subsampling(
    logger: Logger,
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    val density = LocalDensity.current
    this.drawWithContent {
        drawContent()
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
                if (drawTile(imageInfo, contentSize, tileSnapshot)) {
                    backgroundCount++
                }
            }
        }
        foregroundTiles.forEach { tileSnapshot ->
            if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                insideLoadCount++
                if (drawTile(imageInfo, contentSize, tileSnapshot)) {
                    realDrawCount++
                }
                if (subsamplingState.showTileBounds) {
                    drawTileBounds(imageInfo, contentSize, tileSnapshot, density)
                }
            } else {
                outsideLoadCount++
            }
        }

        logger.d {
            "drawTiles. tiles=${foregroundTiles.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}, " +
                    "backgroundCount=${backgroundCount}. " +
                    "'${subsamplingState.imageKey}'"
        }
    }
}

private fun ContentDrawScope.drawTile(
    imageInfo: ImageInfo,
    contentSize: IntSize,
    tileSnapshot: TileSnapshot,
): Boolean {
    val tileBitmap = tileSnapshot.tileBitmap?.takeIf { !it.isRecycled } ?: return false
    val imageBitmap = (tileBitmap as ComposeTileBitmap).imageBitmap
    val widthScale: Float = imageInfo.width / (contentSize.width.toFloat())
    val heightScale: Float = imageInfo.height / (contentSize.height.toFloat())
    val tileDrawRect = IntRect(
        left = floor(tileSnapshot.srcRect.left / widthScale).toInt(),
        top = floor(tileSnapshot.srcRect.top / heightScale).toInt(),
        right = floor(tileSnapshot.srcRect.right / widthScale).toInt(),
        bottom = floor(tileSnapshot.srcRect.bottom / heightScale).toInt()
    )
    val srcOffset = IntOffset.Zero
    val srcSize = IntSize(tileBitmap.width, tileBitmap.height)
    val dstOffset = tileDrawRect.topLeft
    val dstSize = tileDrawRect.size
    val alpha = tileSnapshot.alpha / 255f
    drawImage(
        image = imageBitmap,
        srcOffset = srcOffset,
        srcSize = srcSize,
        dstOffset = dstOffset,
        dstSize = dstSize,
        alpha = alpha,
    )
    return true
}

private fun ContentDrawScope.drawTileBounds(
    imageInfo: ImageInfo,
    contentSize: IntSize,
    tileSnapshot: TileSnapshot,
    density: Density,
) {
    val bitmapNoRecycled = tileSnapshot.tileBitmap?.isRecycled == false
    val boundsColor = when {
        bitmapNoRecycled && tileSnapshot.state == TileState.STATE_LOADED -> Color.Green
        tileSnapshot.state == TileState.STATE_LOADING -> Color.Yellow
        tileSnapshot.state == TileState.STATE_NONE -> Color.Gray
        else -> Color.Red
    }

    val widthScale: Float = imageInfo.width / (contentSize.width.toFloat())
    val heightScale: Float = imageInfo.height / (contentSize.height.toFloat())
    val tileDrawRect = IntRect(
        left = floor(tileSnapshot.srcRect.left / widthScale).toInt(),
        top = floor(tileSnapshot.srcRect.top / heightScale).toInt(),
        right = floor(tileSnapshot.srcRect.right / widthScale).toInt(),
        bottom = floor(tileSnapshot.srcRect.bottom / heightScale).toInt()
    )
    val boundsStrokeWidth = 1f * density.density
    val boundsStrokeHalfWidth = boundsStrokeWidth / 2
    val tileBoundsRect = Rect(
        left = floor(tileDrawRect.left + boundsStrokeHalfWidth),
        top = floor(tileDrawRect.top + boundsStrokeHalfWidth),
        right = ceil(tileDrawRect.right - boundsStrokeHalfWidth),
        bottom = ceil(tileDrawRect.bottom - boundsStrokeHalfWidth)
    )

    drawRect(
        color = boundsColor,
        topLeft = tileBoundsRect.topLeft,
        size = tileBoundsRect.size,
        style = Stroke(width = boundsStrokeWidth),
        alpha = 0.5f,
    )
}