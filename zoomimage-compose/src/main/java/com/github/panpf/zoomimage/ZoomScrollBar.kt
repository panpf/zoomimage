package com.github.panpf.zoomimage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.ScrollBarSpec
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import kotlinx.coroutines.delay

fun Modifier.zoomScrollBar(
    zoomableState: ZoomableState,
    scrollBarSpec: ScrollBarSpec = ScrollBarSpec.Default
): Modifier = composed {
    val contentSize = zoomableState.contentSize
    val contentVisibleRect = zoomableState.contentVisibleRect
    val density = LocalDensity.current
    val sizePx = remember(scrollBarSpec.size) { with(density) { scrollBarSpec.size.toPx() } }
    val marginPx = remember(scrollBarSpec.margin) { with(density) { scrollBarSpec.margin.toPx() } }
    val cornerRadius = remember(sizePx) { CornerRadius(sizePx / 2f, sizePx / 2f) }
    val alphaAnimatable = remember { Animatable(1f) }
    LaunchedEffect(contentVisibleRect) {
        alphaAnimatable.snapTo(targetValue = 1f)
        delay(800)
        alphaAnimatable.animateTo(
            targetValue = 0f,
            animationSpec = TweenSpec(300, easing = LinearOutSlowInEasing)
        )
    }
    if (contentSize.isNotEmpty() && !contentVisibleRect.isEmpty) {
        this.drawWithContent {
            drawContent()

            val alpha = alphaAnimatable.value
            @Suppress("UnnecessaryVariable")
            val scrollBarSize = sizePx
            val drawSize = this.size
            if (contentVisibleRect.width < contentSize.width) {
                val widthScale = (drawSize.width - marginPx * 4) / contentSize.width
                drawRoundRect(
                    color = scrollBarSpec.color,
                    topLeft = Offset(
                        x = (marginPx * 2) + (contentVisibleRect.left * widthScale),
                        y = drawSize.height - marginPx - scrollBarSize
                    ),
                    size = Size(
                        width = contentVisibleRect.width * widthScale,
                        height = scrollBarSize
                    ),
                    cornerRadius = cornerRadius,
                    style = Fill,
                    alpha = alpha
                )
            }
            if (contentVisibleRect.height < contentSize.height) {
                val heightScale = (drawSize.height - marginPx * 4) / contentSize.height
                drawRoundRect(
                    color = scrollBarSpec.color,
                    topLeft = Offset(
                        x = drawSize.width - marginPx - scrollBarSize,
                        y = (marginPx * 2) + (contentVisibleRect.top * heightScale)
                    ),
                    size = Size(
                        width = scrollBarSize,
                        height = contentVisibleRect.height * heightScale
                    ),
                    cornerRadius = cornerRadius,
                    style = Fill,
                    alpha = alpha
                )
            }
        }
    } else {
        this
    }
}