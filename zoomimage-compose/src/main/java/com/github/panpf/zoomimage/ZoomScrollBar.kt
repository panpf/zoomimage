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
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.ScrollBarStyle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

fun Modifier.zoomScrollBar(
    zoomableState: ZoomableState,
    style: ScrollBarStyle = ScrollBarStyle.Default
): Modifier = composed {
    val contentSize = zoomableState.contentSize
    val contentVisibleRect = zoomableState.contentVisibleRect
    val density = LocalDensity.current
    val sizePx = remember(style.size) { with(density) { style.size.toPx() } }
    val marginPx = remember(style.margin) { with(density) { style.margin.toPx() } }
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
    if (contentSize.isSpecified && !contentSize.isEmpty() && !contentVisibleRect.isEmpty) {
        this.drawWithContent {
            drawContent()

            @Suppress("UnnecessaryVariable")
            val scrollBarSize = sizePx
            val drawSize = this.size
            if (contentVisibleRect.width.roundToInt() < contentSize.width.roundToInt()) {
                val widthScale = (drawSize.width - marginPx * 4) / contentSize.width
                drawRoundRect(
                    color = style.color,
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
            if (contentVisibleRect.height.roundToInt() < contentSize.height.roundToInt()) {
                val heightScale = (drawSize.height - marginPx * 4) / contentSize.height
                drawRoundRect(
                    color = style.color,
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