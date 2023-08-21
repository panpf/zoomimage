package com.github.panpf.zoomimage

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.internal.NoClipImage
import com.github.panpf.zoomimage.compose.internal.round
import com.github.panpf.zoomimage.compose.internal.toPx
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
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
    state: ZoomState = rememberZoomState(),
    scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    val zoomableState = state.zoomable
    val subsamplingState = state.subsampling
    if (zoomableState.alignment != alignment) {
        zoomableState.alignment = alignment
    }
    if (zoomableState.contentScale != contentScale) {
        zoomableState.contentScale = contentScale
    }
    val painterSize = painter.intrinsicSize.round()
    if (zoomableState.contentSize != painterSize) {
        zoomableState.contentSize = painterSize
    }

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
                transformOrigin = transform.scaleOrigin
            }
            .graphicsLayer {
                rotationZ = transform.rotation
                transformOrigin = transform.rotationOrigin
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