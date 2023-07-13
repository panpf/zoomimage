package com.github.panpf.zoomimage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.ScrollBar
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import kotlinx.coroutines.delay

fun Modifier.zoomScrollBar(
    zoomableState: ZoomableState,
    scrollBar: ScrollBar = ScrollBar.Default
): Modifier = composed {
    val contentSize = zoomableState.contentSize
    val contentVisibleRect = zoomableState.contentVisibleRect
    val density = LocalDensity.current
    val sizePx = remember(scrollBar.size) { with(density) { scrollBar.size.toPx() } }
    val marginPx = remember(scrollBar.margin) { with(density) { scrollBar.margin.toPx() } }
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
    val alpha by remember { derivedStateOf { alphaAnimatable.value } }
    if (contentSize.isNotEmpty() && !contentVisibleRect.isEmpty) {
        this.drawWithContent {
            drawContent()

            @Suppress("UnnecessaryVariable")
            val scrollBarSize = sizePx
            val drawSize = this.size
            if (contentVisibleRect.width < contentSize.width) {
                val widthScale = (drawSize.width - marginPx * 4) / contentSize.width
                drawRoundRect(
                    color = scrollBar.color,
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
                    color = scrollBar.color,
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