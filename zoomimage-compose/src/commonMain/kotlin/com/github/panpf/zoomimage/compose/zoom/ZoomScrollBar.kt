/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.util.isEmpty
import com.github.panpf.zoomimage.compose.util.rotate
import com.github.panpf.zoomimage.compose.util.rotateInSpace
import com.github.panpf.zoomimage.compose.util.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A scroll bar Modifier that displays the scroll state of [ZoomableState] for the component.
 * The [scrollBarSpec] parameter configures the size, color, margins and other properties of the scroll bar.
 */
fun Modifier.zoomScrollBar(
    zoomable: ZoomableState,
    scrollBarSpec: ScrollBarSpec = ScrollBarSpec.Default
): Modifier = this
    .then(ZoomScrollBarElement(zoomable, scrollBarSpec))

internal data class ZoomScrollBarElement(
    val zoomable: ZoomableState,
    val scrollBarSpec: ScrollBarSpec,
) : ModifierNodeElement<ZoomScrollBarNode>() {

    override fun create(): ZoomScrollBarNode {
        return ZoomScrollBarNode(
            zoomable = zoomable,
            scrollBarSpec = scrollBarSpec,
        )
    }

    override fun update(node: ZoomScrollBarNode) {
        node.update(
            zoomable = zoomable,
            scrollBarSpec = scrollBarSpec,
        )
    }
}

internal class ZoomScrollBarNode(
    var zoomable: ZoomableState,
    var scrollBarSpec: ScrollBarSpec,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val alphaAnimatable = Animatable(1f)
    private var lastContentVisibleRect: IntRect? = null
    private var lastDelayJob: Job? = null

    fun update(
        zoomable: ZoomableState,
        scrollBarSpec: ScrollBarSpec,
    ) {
        this.zoomable = zoomable
        this.scrollBarSpec = scrollBarSpec
        invalidateDraw()
    }

    @Suppress("UnnecessaryVariable")
    override fun ContentDrawScope.draw() {
        drawContent()

        val contentSize = zoomable.contentSize
        val contentVisibleRect = zoomable.contentVisibleRectF.round()
        if (contentSize.isEmpty() || contentVisibleRect.isEmpty) return

        if (lastContentVisibleRect != contentVisibleRect) {
            lastContentVisibleRect = contentVisibleRect
            lastDelayJob?.cancel()
            // Alpha required reset immediately, so must be immediate
            lastDelayJob = coroutineScope.launch(Dispatchers.Main.immediate) {
                alphaAnimatable.snapTo(targetValue = 1f)
                delay(800)
                alphaAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = TweenSpec(300, easing = LinearOutSlowInEasing)
                )
            }
        }

        val rotation = zoomable.transform.rotation
        val density = currentValueOf(LocalDensity)
        val scrollBarSize = with(density) { scrollBarSpec.size.toPx() }
        val sideMarginPx = with(density) { scrollBarSpec.sideMargin.toPx() }
        val endsMarginPx = with(density) { scrollBarSpec.endsMargin.toPx() }
        val cornerRadius = CornerRadius(scrollBarSize / 2f, scrollBarSize / 2f)
        val minLength = with(density) { 10.dp.toPx() }
        val alpha = alphaAnimatable.value
        val rotatedContentVisibleRect = contentVisibleRect
            .rotateInSpace(contentSize, rotation.roundToInt())
        val rotatedContentSize = contentSize.rotate(rotation.roundToInt())
        val drawSize = this.size
        if (rotatedContentVisibleRect.width < rotatedContentSize.width) {
            val leftSpace = endsMarginPx
            val rightSpace = endsMarginPx
            val bottomSpace = sideMarginPx
            val validDrawWidth = drawSize.width - leftSpace - rightSpace
            val widthScale = validDrawWidth / rotatedContentSize.width
            val mappedRotatedContentVisibleLeft = rotatedContentVisibleRect.left * widthScale
            val mappedRotatedContentVisibleWidth = (rotatedContentVisibleRect.width * widthScale)
                .coerceAtLeast(minLength)
            val left = leftSpace + mappedRotatedContentVisibleLeft
            val top = drawSize.height - bottomSpace - scrollBarSize
            drawRoundRect(
                color = scrollBarSpec.color,
                topLeft = Offset(x = left, y = top),
                size = Size(width = mappedRotatedContentVisibleWidth, height = scrollBarSize),
                cornerRadius = cornerRadius,
                style = Fill,
                alpha = alpha
            )
        }

        if (rotatedContentVisibleRect.height < rotatedContentSize.height) {
            val topSpace = endsMarginPx
            val bottomSpace = endsMarginPx
            val rightSpace = sideMarginPx
            val validDrawHeight = drawSize.height - topSpace - bottomSpace
            val heightScale = validDrawHeight / rotatedContentSize.height
            val mappedRotatedContentVisibleTop = rotatedContentVisibleRect.top * heightScale
            val mappedRotatedContentVisibleHeight = (rotatedContentVisibleRect.height * heightScale)
                .coerceAtLeast(minLength)
            val left = drawSize.width - rightSpace - scrollBarSize
            val top = topSpace + mappedRotatedContentVisibleTop
            drawRoundRect(
                color = scrollBarSpec.color,
                topLeft = Offset(x = left, y = top),
                size = Size(width = scrollBarSize, height = mappedRotatedContentVisibleHeight),
                cornerRadius = cornerRadius,
                style = Fill,
                alpha = alpha
            )
        }
    }
}