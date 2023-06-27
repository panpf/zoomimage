package com.github.panpf.zoomimage

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

fun Modifier.zoomScrollBar(
    zoomableState: ZoomableState,
    color: Color = Color.Black.copy(alpha = 0.2f),
    strokeColor: Color = Color.White.copy(alpha = 0.3f),
    size: Dp = 3.dp,
    margin: Dp = 6.dp
): Modifier = composed {
    // todo support animate hidden
    val contentSize = zoomableState.contentSize
    val contentVisibleRect = zoomableState.contentVisibleRect
    val density = LocalDensity.current
    val sizePx = remember(size) { with(density) { size.toPx() } }
    val marginPx = remember(margin) { with(density) { margin.toPx() } }
    val cornerRadius = remember(sizePx) { CornerRadius(sizePx / 2f, sizePx / 2f) }
    val stroke = remember { Stroke(width = with(density) { 0.5.dp.toPx() }) }
    if (contentSize.isSpecified && !contentSize.isEmpty() && !contentVisibleRect.isEmpty) {
        this.drawWithContent {
            drawContent()

            @Suppress("UnnecessaryVariable")
            val scrollBarSize = sizePx
            val drawSize = this.size
            if (contentVisibleRect.width.roundToInt() < contentSize.width.roundToInt()) {
                val widthScale = (drawSize.width - marginPx * 4) / contentSize.width
                val topLeft = Offset(
                    x = (marginPx * 2) + (contentVisibleRect.left * widthScale),
                    y = drawSize.height - marginPx - scrollBarSize
                )
                val scrollBarRectSize = Size(
                    width = contentVisibleRect.width * widthScale,
                    height = scrollBarSize
                )
                drawRoundRect(color, topLeft, scrollBarRectSize, cornerRadius, Fill)
                drawRoundRect(strokeColor, topLeft, scrollBarRectSize, cornerRadius, stroke)
            }
            if (contentVisibleRect.height.roundToInt() < contentSize.height.roundToInt()) {
                val heightScale = (drawSize.height - marginPx * 4) / contentSize.height
                val topLeft = Offset(
                    x = drawSize.width - marginPx - scrollBarSize,
                    y = (marginPx * 2) + (contentVisibleRect.top * heightScale)
                )
                val scrollBarRectSize = Size(
                    width = scrollBarSize,
                    height = contentVisibleRect.height * heightScale
                )
                drawRoundRect(color, topLeft, scrollBarRectSize, cornerRadius, Fill)
                drawRoundRect(strokeColor, topLeft, scrollBarRectSize, cornerRadius, stroke)
            }
        }
    } else {
        this
    }
}