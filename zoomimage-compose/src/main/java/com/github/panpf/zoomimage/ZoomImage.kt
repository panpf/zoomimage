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

@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    state: ZoomableState = rememberZoomableState(),
    scrollBar: ScrollBar? = ScrollBar.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    if (state.contentAlignment != alignment) {
        state.contentAlignment = alignment
    }
    if (state.contentScale != contentScale) {
        state.contentScale = contentScale
    }
    val painterSize = painter.intrinsicSize.roundToIntSize()
    if (state.contentSize != painterSize) {
        state.contentSize = painterSize
    }

    val modifier1 = modifier
        .clipToBounds()
        .let { if (scrollBar != null) it.zoomScrollBar(state, scrollBar) else it }
        .zoomable(state = state, onLongPress = onLongPress, onTap = onTap)
        .graphicsLayer {
            scaleX = state.transform.scaleX
            scaleY = state.transform.scaleY
            rotationZ = state.transform.rotation
            translationX = state.transform.offsetX
            translationY = state.transform.offsetY
            transformOrigin = state.transformOrigin
        }

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