package com.github.panpf.zoomimage

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.compose.ScrollBarStyle

@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: ZoomableState = rememberZoomableState(),
    animationConfig: AnimationConfig = AnimationConfig(),
    scrollBarEnabled: Boolean = true,
    scrollBarStyle: ScrollBarStyle = ScrollBarStyle.Default,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    if (state.contentAlignment != alignment) {
        state.contentAlignment = alignment
    }
    if (state.contentScale != contentScale) {
        state.contentScale = contentScale
    }
    val painterSize = painter.intrinsicSize
    if (state.contentSize != painterSize) {
        state.contentSize = painterSize
    }

    val modifier1 = modifier
        .clipToBounds()
        .let { if (scrollBarEnabled) it.zoomScrollBar(state, scrollBarStyle) else it }
        .zoomable(state, animationConfig)
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            rotationZ = state.rotation
            translationX = state.translation.translationX
            translationY = state.translation.translationY
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