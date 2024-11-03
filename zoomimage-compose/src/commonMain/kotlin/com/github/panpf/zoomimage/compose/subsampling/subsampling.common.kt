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

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.isEmpty
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.tileColor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Whether to turn off anti-aliasing when drawing Tiles
 *
 * @see com.github.panpf.zoomimage.compose.android.test.subsampling.SubsamplingAndroidTest.testIsCloseAntiAliasForDrawTile
 * @see com.github.panpf.zoomimage.compose.nonandroid.test.subsampling.SubsamplingNonAndroidTest.testIsCloseAntiAliasForDrawTile
 */
expect fun isCloseAntiAliasForDrawTile(): Boolean

/**
 * A Modifier that draws the Tiles of [SubsamplingState] for the component.
 */
fun Modifier.subsampling(
    zoomable: ZoomableState,
    subsampling: SubsamplingState,
): Modifier = this
    .then(SubsamplingDrawTilesElement(zoomable, subsampling))

internal data class SubsamplingDrawTilesElement(
    val zoomable: ZoomableState,
    val subsampling: SubsamplingState,
) : ModifierNodeElement<SubsamplingDrawTilesNode>() {

    override fun create(): SubsamplingDrawTilesNode {
        return SubsamplingDrawTilesNode(
            zoomable = zoomable,
            subsampling = subsampling,
        )
    }

    override fun update(node: SubsamplingDrawTilesNode) {
        node.update(
            zoomable = zoomable,
            subsampling = subsampling,
        )
    }
}

internal class SubsamplingDrawTilesNode(
    var zoomable: ZoomableState,
    var subsampling: SubsamplingState,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val tilePaint = Paint().apply {
        isAntiAlias = !isCloseAntiAliasForDrawTile()
    }
    private val boundsPaint = Paint().apply {
        style = PaintingStyle.Stroke
    }

    fun update(
        zoomable: ZoomableState,
        subsampling: SubsamplingState,
    ) {
        this.zoomable = zoomable
        this.subsampling = subsampling
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        val canvas = drawContext.canvas
        val imageInfo = subsampling.imageInfo ?: return
        val contentSize = zoomable.contentSize
            .takeIf { !it.isEmpty() } ?: return
        val foregroundTiles = subsampling.foregroundTiles
            .takeIf { it.isNotEmpty() } ?: return
        val backgroundTiles = subsampling.backgroundTiles
        val imageLoadRect = subsampling.imageLoadRect
            .takeIf { !it.isEmpty }?.toCompat() ?: return

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

        val density = currentValueOf(LocalDensity)
        boundsPaint.strokeWidth = 0.5f * density.density
        foregroundTiles.forEach { tileSnapshot ->
            if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                insideLoadCount++
                if (drawTile(canvas, imageInfo, contentSize, tileSnapshot, tilePaint)) {
                    realDrawCount++
                }
                if (subsampling.showTileBounds) {
                    drawTileBounds(canvas, imageInfo, contentSize, tileSnapshot, boundsPaint)
                }
            } else {
                outsideLoadCount++
            }
        }

        subsampling.logger.d {
            "SubsamplingState. drawTiles. tiles=${foregroundTiles.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}, " +
                    "backgroundCount=${backgroundCount}. " +
                    "'${subsampling.subsamplingImage?.key}'"
        }
    }

    private fun drawTile(
        canvas: Canvas,
        imageInfo: ImageInfo,
        contentSize: IntSize,
        tileSnapshot: TileSnapshot,
        tilePaint: Paint
    ): Boolean {
        val tileImage = tileSnapshot.tileImage?.takeIf { !it.isRecycled } ?: return false
        val imageBitmap = (tileImage as ComposeTileImage).bitmap

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

        val boundsColor = tileColor(
            state = tileSnapshot.state,
            withinLoadArea = true,
            fromCache = tileSnapshot.tileImage?.fromCache ?: false,
        )
        boundsPaint.color = Color(boundsColor)

        canvas.drawRect(rect = tileDrawRect, paint = boundsPaint)
    }
}