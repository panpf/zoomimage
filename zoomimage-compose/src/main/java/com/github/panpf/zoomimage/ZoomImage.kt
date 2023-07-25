package com.github.panpf.zoomimage

import androidx.compose.foundation.Image
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
import com.github.panpf.zoomimage.compose.internal.roundToIntSize
import com.github.panpf.zoomimage.compose.subsampling.BindZoomableStateAndSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.compose.zoom.zoomable

@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    logger: Logger = rememberLogger(tag = "ZoomImage"),
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

    val baseTransform = zoomableState.baseTransform
    val userTransform = zoomableState.userTransform
    val modifier1 = modifier
        .clipToBounds()
        .let { if (scrollBarSpec != null) it.zoomScrollBar(zoomableState, scrollBarSpec) else it }
        .zoomable(state = zoomableState, onLongPress = onLongPress, onTap = onTap)
        .graphicsLayer {
            rotationZ = baseTransform.rotation
            transformOrigin = TransformOrigin.Center
        }
        .graphicsLayer {
            scaleX = userTransform.scaleX
            scaleY = userTransform.scaleY
            translationX = userTransform.offsetX
            translationY = userTransform.offsetY
            transformOrigin = userTransform.origin
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

@Composable
fun rememberLogger(tag: String = "ZoomImage", level: Int = Logger.INFO): Logger =
    remember {
        Logger(tag = tag).apply { this.level = level }
    }