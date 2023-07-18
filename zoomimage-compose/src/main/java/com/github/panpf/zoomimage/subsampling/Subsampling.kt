package com.github.panpf.zoomimage.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.toShortString
import kotlin.math.ceil
import kotlin.math.floor

fun Modifier.subsampling(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    this.drawWithContent {
        drawContent()

        @Suppress("UNUSED_VARIABLE") val changeCount =
            subsamplingState.tilesChanged  // Trigger a refresh
        val tileList = subsamplingState.tileList
        val imageInfo = subsamplingState.imageInfo
        val drawableSize = subsamplingState.contentSize
        if (tileList.isEmpty() || imageInfo == null || drawableSize.isEmpty()) {
            subsamplingState.logger.d {
                "drawTiles. params is empty. " +
                        "tileListSize=${tileList.size}, " +
                        "imageInfo=${imageInfo?.toShortString()}, " +
                        "drawableSize=${drawableSize.toShortString()}"
            }
            return@drawWithContent
        }

        val baseTransform = zoomableState.baseTransform
        subsamplingState.logger.d {
            "drawTiles. " +
                    "tileListSize=${tileList.size}, " +
                    "imageInfo=${imageInfo.toShortString()}, " +
                    "drawableSize=${drawableSize.toShortString()}, " +
                    "baseTransform=${baseTransform.toShortString()}"
        }
        val widthScale = imageInfo.width / (drawableSize.width * baseTransform.scaleX)
        val heightScale = imageInfo.height / (drawableSize.height * baseTransform.scaleY)
        tileList.forEach { tile ->
            if (tile.srcRect.overlaps(subsamplingState.imageLoadRect)) {
                val tileBitmap = tile.bitmap
                val tileSrcRect = tile.srcRect
                val tileDrawRect = IntRect(
                    left = floor(tileSrcRect.left / widthScale).toInt(),
                    top = floor(tileSrcRect.top / heightScale).toInt(),
                    right = floor(tileSrcRect.right / widthScale).toInt(),
                    bottom = floor(tileSrcRect.bottom / heightScale).toInt()
                ).translate(baseTransform.offset.round())
                if (tileBitmap != null) {
                    val srcRect = IntRect(0, 0, tileBitmap.width, tileBitmap.height)
                    drawImage(
                        image = tileBitmap.asImageBitmap(),
                        srcOffset = srcRect.topLeft,
                        srcSize = srcRect.size,
                        dstOffset = tileDrawRect.topLeft,
                        dstSize = tileDrawRect.size,
//                        alpha = 0.5f,
                    )
                }

                if (subsamplingState.showTileBounds) {
                    val boundsColor = when {
                        tileBitmap != null -> Color.Green
                        tile.loadJob?.isActive == true -> Color.Yellow
                        else -> Color.Red
                    }
                    val boundsStrokeWidth = 1.dp.toPx()
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
            }
        }
    }
}