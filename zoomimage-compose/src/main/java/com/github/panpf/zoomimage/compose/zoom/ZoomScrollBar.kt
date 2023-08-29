/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.rotateInSpace
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

fun Modifier.zoomScrollBar(
    zoomableState: ZoomableState,
    scrollBarSpec: ScrollBarSpec = ScrollBarSpec.Default
): Modifier = composed {
    val contentSize = zoomableState.contentSize
    val contentVisibleRect = zoomableState.contentVisibleRect
    val rotation = zoomableState.transform.rotation
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
            val rotatedContentVisibleRect = contentVisibleRect
                .rotateInSpace(contentSize, rotation.roundToInt())
            val rotatedContentSize = contentSize.rotate(rotation.roundToInt())

            @Suppress("UnnecessaryVariable")
            val scrollBarSize = sizePx
            val drawSize = this.size
            if (rotatedContentVisibleRect.width < rotatedContentSize.width) {
                val widthScale = (drawSize.width - marginPx * 4) / rotatedContentSize.width
                drawRoundRect(
                    color = scrollBarSpec.color,
                    topLeft = Offset(
                        x = (marginPx * 2) + (rotatedContentVisibleRect.left * widthScale),
                        y = drawSize.height - marginPx - scrollBarSize
                    ),
                    size = Size(
                        width = rotatedContentVisibleRect.width * widthScale,
                        height = scrollBarSize
                    ),
                    cornerRadius = cornerRadius,
                    style = Fill,
                    alpha = alpha
                )
            }
            if (rotatedContentVisibleRect.height < rotatedContentSize.height) {
                val heightScale = (drawSize.height - marginPx * 4) / rotatedContentSize.height
                drawRoundRect(
                    color = scrollBarSpec.color,
                    topLeft = Offset(
                        x = drawSize.width - marginPx - scrollBarSize,
                        y = (marginPx * 2) + (rotatedContentVisibleRect.top * heightScale)
                    ),
                    size = Size(
                        width = scrollBarSize,
                        height = rotatedContentVisibleRect.height * heightScale
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