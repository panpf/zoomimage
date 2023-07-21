package com.github.panpf.zoomimage.sample.ui.widget.compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toRect
import androidx.compose.ui.unit.toSize
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.tools4a.dimen.ktx.dp2pxF
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.toCompatIntSize
import com.github.panpf.zoomimage.compose.internal.toIntSize
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.Origin
import com.github.panpf.zoomimage.sample.ui.util.compose.scale
import com.github.panpf.zoomimage.sample.ui.util.compose.toDp
import com.github.panpf.zoomimage.subsampling.SubsamplingState
import com.github.panpf.zoomimage.subsampling.Tile
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomImageMinimap(
    sketchImageUri: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    alignment: Alignment = Alignment.BottomStart,
    ignoreExifOrientation: Boolean = false,
) {
    val contentSize = zoomableState.contentSize.takeIf { it.isNotEmpty() } ?: IntSize.Zero
    val coroutineScope = rememberCoroutineScope()
    val strokeWidth = remember { 1f.dp2pxF }
    BoxWithConstraints(modifier = modifier.then(Modifier.fillMaxSize())) {
        val density = LocalDensity.current
        val viewSize = remember(contentSize) {
            val containerSize =
                with(density) { IntSize(maxWidth.roundToPx(), maxHeight.roundToPx()) }
            computeViewSize(contentSize, containerSize)
        }
        if (viewSize.isNotEmpty()) {
            val imageNodeSizeState = remember { mutableStateOf(Size.Zero) }
            AsyncImage(
                request = DisplayRequest(LocalContext.current, sketchImageUri) {
                    crossfade()
                    ignoreExifOrientation(ignoreExifOrientation)
                },
                contentDescription = contentDescription ?: "Visible Rect",
                modifier = Modifier
                    .align(alignment)
                    .size(
                        width = viewSize.width
                            .toFloat()
                            .toDp(),
                        height = viewSize.height
                            .toFloat()
                            .toDp()
                    )
                    .clipToBounds()
                    .drawWithContent {
                        drawContent()

                        // Trigger a refresh todo Verify that tilesChanged works
                        @Suppress("UNUSED_VARIABLE") val changeCount = subsamplingState.tilesChanged
                        val imageSize =
                            subsamplingState.imageInfo?.size?.toIntSize() ?: IntSize.Zero
                        val tileList = subsamplingState.tileList
                        val imageLoadRect = subsamplingState.imageLoadRect
                        if (contentSize.isNotEmpty() && imageSize.isNotEmpty()) {
                            drawTilesBounds(
                                tileList = tileList,
                                imageSize = imageSize,
                                viewSize = viewSize,
                                imageLoadRect = imageLoadRect,
                                strokeWidth = strokeWidth,
                            )
                        }

                        if (contentSize.isNotEmpty() && viewSize.isNotEmpty()) {
                            drawVisibleRect(
                                contentVisibleRect = zoomableState.contentVisibleRect,
                                contentSize = contentSize,
                                viewSize = viewSize,
                                strokeWidth = strokeWidth,
                            )
                        }
                    }
                    .onSizeChanged {
                        imageNodeSizeState.value = it.toSize()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                val imageNodeSize = imageNodeSizeState.value
                                if (!imageNodeSize.isEmpty()) {
                                    coroutineScope.launch {
                                        zoomableState.location(
                                            contentOrigin = Origin(
                                                pivotFractionX = it.x / imageNodeSize.width,
                                                pivotFractionY = it.y / imageNodeSize.height
                                            ),
                                            targetScale = zoomableState.transform.scaleX
                                                .coerceAtLeast(zoomableState.mediumScale),
                                            animated = true,
                                        )
                                    }
                                }
                            }
                        )
                    },
            )
        }
    }
}

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
        .should(srcSize = contentSize.toCompatIntSize(), dstSize = containerSize.toCompatIntSize())
    val maxPercentage = if (isLongImage) 0.6f else if (sameDirection) 0.3f else 0.4f
    val maxWidth = containerWidth * maxPercentage
    val maxHeight = containerHeight * maxPercentage
    val scale = min(maxWidth / contentWidth, maxHeight / contentHeight)
    return IntSize((contentWidth * scale).roundToInt(), (contentHeight * scale).roundToInt())
}

private fun ContentDrawScope.drawTilesBounds(
    tileList: List<Tile>,
    imageSize: IntSize,
    viewSize: IntSize,
    imageLoadRect: IntRectCompat,
    strokeWidth: Float,
) {
    val widthTargetScale = imageSize.width.toFloat() / viewSize.width
    val heightTargetScale = imageSize.height.toFloat() / viewSize.height
    val strokeHalfWidth = strokeWidth / 2
    tileList.forEach { tile ->
        val load = tile.srcRect.overlaps(imageLoadRect)
        val tileBitmap = tile.bitmap
        val tileSrcRect = tile.srcRect
        val tileDrawRect = IntRect(
            left = floor((tileSrcRect.left / widthTargetScale) + strokeHalfWidth).toInt(),
            top = floor((tileSrcRect.top / heightTargetScale) + strokeHalfWidth).toInt(),
            right = ceil((tileSrcRect.right / widthTargetScale) - strokeHalfWidth).toInt(),
            bottom = ceil((tileSrcRect.bottom / heightTargetScale) - strokeHalfWidth).toInt()
        )
        val boundsColor = when {
            !load -> android.graphics.Color.parseColor("#00BFFF")
            tileBitmap != null -> android.graphics.Color.GREEN
            tile.loadJob?.isActive == true -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.RED
        }
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
    val drawVisibleRect = contentVisibleRect.scale(drawScaleWithContent).toRect()
    drawRect(
        color = Color.Magenta,
        topLeft = drawVisibleRect.topLeft,
        size = drawVisibleRect.size,
        style = Stroke(width = strokeWidth)
    )
}