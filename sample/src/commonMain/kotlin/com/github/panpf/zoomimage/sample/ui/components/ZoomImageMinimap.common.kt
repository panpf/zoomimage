package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toRect
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.sample.ui.util.isEmpty
import com.github.panpf.zoomimage.sample.ui.util.isNotEmpty
import com.github.panpf.zoomimage.sample.ui.util.times
import com.github.panpf.zoomimage.sample.ui.util.toDp
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.tileColor
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomImageMinimap(
    imageUri: String,
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val contentSize = zoomableState.contentSize.takeIf { it.isNotEmpty() } ?: IntSize.Zero
    val density = LocalDensity.current
    val strokeWidth = remember { with(density) { 1.dp.toPx() } }
    BoxWithConstraints(modifier = modifier.then(Modifier.fillMaxSize())) {
        val viewSize = remember(contentSize) {
            val containerSize =
                with(density) { IntSize(maxWidth.roundToPx(), maxHeight.roundToPx()) }
            computeViewSize(contentSize, containerSize)
        }
        if (viewSize.isNotEmpty()) {
            var imageNodeSize by remember { mutableStateOf(Size.Zero) }
            val imageSize =
                subsamplingState.imageInfo?.size?.toPlatform() ?: IntSize.Zero
            val imageModifier = Modifier
                .align(Alignment.BottomStart)
                .size(
                    width = viewSize.width.toFloat().toDp(),
                    height = viewSize.height.toFloat().toDp()
                )
                .clipToBounds()
                .drawWithContent {
                    drawContent()
                    val foregroundTiles = subsamplingState.foregroundTiles
                    val imageLoadRect = subsamplingState.imageLoadRect.toCompat()
                    if (contentSize.isNotEmpty() && imageSize.isNotEmpty()) {
                        drawTilesBounds(
                            tileSnapshotList = foregroundTiles,
                            imageSize = imageSize,
                            viewSize = viewSize,
                            imageLoadRect = imageLoadRect,
                            strokeWidth = strokeWidth,
                        )
                    }

                    val contentVisibleRect = zoomableState.contentVisibleRect
                    if (contentSize.isNotEmpty() && viewSize.isNotEmpty()) {
                        drawVisibleRect(
                            contentVisibleRect = contentVisibleRect,
                            contentSize = contentSize,
                            viewSize = viewSize,
                            strokeWidth = strokeWidth,
                        )
                    }
                }
                .onSizeChanged {
                    imageNodeSize = it.toSize()
                }
                .pointerInput(contentSize, imageNodeSize) {
                    detectTapGestures(
                        onTap = {
                            if (!imageNodeSize.isEmpty()) {
                                coroutineScope.launch {
                                    zoomableState.locate(
                                        contentPoint = IntOffset(
                                            x = ((it.x / imageNodeSize.width) * contentSize.width).roundToInt(),
                                            y = ((it.y / imageNodeSize.height) * contentSize.height).roundToInt(),
                                        ),
                                        targetScale = zoomableState.transform.scaleX
                                            .coerceAtLeast(zoomableState.mediumScale),
                                        animated = true,
                                    )
                                }
                            }
                        }
                    )
                }

            Box(modifier = imageModifier) {
                ZoomImageMinimapContent(imageUri)
            }
        }
    }
}

@Composable
expect fun ZoomImageMinimapContent(sketchImageUri: String)

private fun computeViewSize(contentSize: IntSize, containerSize: IntSize): IntSize {
    if (contentSize.isEmpty()) return IntSize.Zero
    if (containerSize.isEmpty()) return IntSize.Zero
    val contentWidth = contentSize.width
    val contentHeight = contentSize.height
    val containerWidth = containerSize.width
    val containerHeight = containerSize.height
    val sameDirection =
        (contentWidth >= contentHeight && containerWidth >= containerHeight) ||
                (contentWidth < contentHeight && containerWidth < containerHeight)
    val isLongImage = ReadMode.LongImageDecider()
        .should(contentSize = contentSize.toCompat(), containerSize = containerSize.toCompat())
    val maxPercentage = if (isLongImage) 0.6f else if (sameDirection) 0.3f else 0.4f
    val maxWidth = containerWidth * maxPercentage
    val maxHeight = containerHeight * maxPercentage
    val scale = min(maxWidth / contentWidth, maxHeight / contentHeight)
    return IntSize((contentWidth * scale).roundToInt(), (contentHeight * scale).roundToInt())
}

private fun ContentDrawScope.drawTilesBounds(
    tileSnapshotList: List<TileSnapshot>,
    imageSize: IntSize,
    viewSize: IntSize,
    imageLoadRect: IntRectCompat,
    strokeWidth: Float,
) {
    val widthTargetScale = imageSize.width.toFloat() / viewSize.width
    val heightTargetScale = imageSize.height.toFloat() / viewSize.height
    val strokeHalfWidth = strokeWidth / 2
    tileSnapshotList.forEach { tileSnapshot ->
        val load = tileSnapshot.srcRect.overlaps(imageLoadRect)
        val tileSrcRect = tileSnapshot.srcRect
        val tileDrawRect = IntRect(
            left = floor((tileSrcRect.left / widthTargetScale) + strokeHalfWidth).toInt(),
            top = floor((tileSrcRect.top / heightTargetScale) + strokeHalfWidth).toInt(),
            right = ceil((tileSrcRect.right / widthTargetScale) - strokeHalfWidth).toInt(),
            bottom = ceil((tileSrcRect.bottom / heightTargetScale) - strokeHalfWidth).toInt()
        )
        val boundsColor = tileColor(
            state = tileSnapshot.state,
            withinLoadArea = load,
            fromCache = tileSnapshot.tileImage?.fromCache ?: false,
        )
        drawRect(
            color = Color(boundsColor),
            topLeft = tileDrawRect.topLeft.toOffset(),
            size = tileDrawRect.size.toSize(),
            style = Stroke(width = strokeWidth),
        )
    }
}

private fun ContentDrawScope.drawVisibleRect(
    contentVisibleRect: IntRect,
    contentSize: IntSize,
    viewSize: IntSize,
    strokeWidth: Float,
) {
    val drawScaleWithContent = ScaleFactor(
        scaleX = viewSize.width / contentSize.width.toFloat(),
        scaleY = viewSize.height / contentSize.height.toFloat()
    )
    val drawVisibleRect = contentVisibleRect.times(drawScaleWithContent).toRect()
    drawRect(
        color = Color.Magenta,
        topLeft = drawVisibleRect.topLeft,
        size = drawVisibleRect.size,
        style = Stroke(width = strokeWidth)
    )
}