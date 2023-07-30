package com.github.panpf.zoomimage

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.NoClipImage
import com.github.panpf.zoomimage.compose.internal.roundToIntSize
import com.github.panpf.zoomimage.compose.internal.toPx
import com.github.panpf.zoomimage.compose.subsampling.BindZoomableStateAndSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.compose.zoom.zoomable
import kotlin.math.roundToInt

@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    logger: Logger = rememberZoomImageLogger(),
    zoomableState: ZoomableState = rememberZoomableState(logger),
    subsamplingState: SubsamplingState = rememberSubsamplingState(logger),
    scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    if (zoomableState.contentAlignment != alignment) {
        zoomableState.contentAlignment = alignment
    }
    if (zoomableState.contentScale != contentScale) {
        zoomableState.contentScale = contentScale
    }
    val painterSize = painter.intrinsicSize.roundToIntSize()
    if (zoomableState.contentSize != painterSize) {
        zoomableState.contentSize = painterSize
    }

    BindZoomableStateAndSubsamplingState(zoomableState, subsamplingState)

    BoxWithConstraints(modifier = modifier) {
        // Here use BoxWithConstraints and then actively set containerSize and call reset(),
        // In order to prepare the transform in advance, so that when the position of the image needs to be adjusted,
        // the position change will not be seen by the user
        val maxWidthPx = maxWidth.toPx().roundToInt()
        val maxHeightPx = maxHeight.toPx().roundToInt()
        val oldContainerSize = zoomableState.containerSize
        if (oldContainerSize.width != maxWidthPx || oldContainerSize.height != maxHeightPx) {
            zoomableState.containerSize = IntSize(maxWidthPx, maxHeightPx)
            zoomableState.reset("BoxWithConstraints", immediate = true)
        }
        val transform = zoomableState.transform
        val modifier1 = Modifier
            .fillMaxSize()
            .clipToBounds()
            .let { modifier ->
                scrollBarSpec?.let { modifier.zoomScrollBar(zoomableState, it) } ?: modifier
            }
            .zoomable(state = zoomableState, onLongPress = onLongPress, onTap = onTap)
            .graphicsLayer {
                scaleX = transform.scaleX
                scaleY = transform.scaleY
                translationX = transform.offsetX
                translationY = transform.offsetY
                transformOrigin = transform.origin
            }
            .graphicsLayer {
                rotationZ = transform.rotation
                transformOrigin = TransformOrigin.Center
            }
            .subsampling(subsamplingState = subsamplingState, zoomableState = null)
        NoClipImage(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier1,
            alignment = Alignment.TopStart,
            contentScale = ContentScale.None,
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}

@Composable
fun rememberZoomImageLogger(tag: String = "ZoomImage", level: Int = Logger.INFO): Logger =
    remember {
        Logger(tag = tag).apply { this.level = level }
    }