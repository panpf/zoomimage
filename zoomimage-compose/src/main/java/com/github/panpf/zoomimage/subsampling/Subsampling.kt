package com.github.panpf.zoomimage.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.round
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.toShortString
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

fun Modifier.subsampling(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    this.drawWithContent {
        drawContent()

        @Suppress("UNUSED_VARIABLE") val changeCount =
            subsamplingState.tilesChanged  // Trigger a refresh
        val tileList = subsamplingState.rowTileList
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
                val tileDrawRect0 = IntRect(
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
                        dstOffset = tileDrawRect0.topLeft,
                        dstSize = tileDrawRect0.size,
//                        alpha = 0.5f,
                    )
                }
            }
        }
    }
}
