package com.github.panpf.zoomimage

import androidx.compose.foundation.Image
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
import com.github.panpf.zoomimage.compose.ScrollBar
import com.github.panpf.zoomimage.compose.internal.roundToIntSize
import com.github.panpf.zoomimage.subsampling.BindZoomableStateAndSubsamplingState
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingState
import com.github.panpf.zoomimage.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.subsampling.subsampling

@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    zoomableState: ZoomableState = rememberZoomableState(),
    subsamplingState: SubsamplingState = rememberSubsamplingState(),
    subsamplingImageSource: ImageSource? = null,// TODO 干掉这个参数
    scrollBar: ScrollBar? = ScrollBar.Default,
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
    subsamplingState.setImageSource(subsamplingImageSource)

    val modifier1 = modifier
        .clipToBounds()
        .let { if (scrollBar != null) it.zoomScrollBar(zoomableState, scrollBar) else it }
        .zoomable(state = zoomableState, onLongPress = onLongPress, onTap = onTap)
        .graphicsLayer {
            val transform = zoomableState.userTransform
            scaleX = transform.scaleX
            scaleY = transform.scaleY
            rotationZ = transform.rotation
            translationX = transform.offsetX
            translationY = transform.offsetY
            transformOrigin = transform.origin
        }
        .subsampling(zoomableState = zoomableState, subsamplingState = subsamplingState)

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier1,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}