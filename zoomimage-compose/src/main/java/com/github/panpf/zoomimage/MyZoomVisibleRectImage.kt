package com.github.panpf.zoomimage

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

@Composable
fun MyZoomVisibleRectImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    state: MyZoomState,
    animateScale: Boolean,
    animationDurationMillis: Int,
) {
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier.then(Modifier.fillMaxSize())) {
        val imageMaxWidth = (this.maxWidth * 0.3f).toPx()
        val imageMaxHeight = (this.maxHeight * 0.3f).toPx()
        val scale =
            (imageMaxWidth / painter.intrinsicSize.width).coerceAtMost(imageMaxHeight / painter.intrinsicSize.height)
        val imageNodeSizeState = remember { mutableStateOf(Size.Zero) }
        Image(
            painter = painter,
            contentDescription = contentDescription ?: "Visible Rect",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(
                    width = (painter.intrinsicSize.width * scale).toDp(),
                    height = (painter.intrinsicSize.height * scale).toDp()
                )
                .aspectRatio(painter.intrinsicSize.let { it.width / it.height })
                .drawWithContent {
                    drawContent()
                    val drawSize = size
                    val coreSize = painter.intrinsicSize
                    val coreVisibleRect = state.contentVisibleRect
                    val drawScaleWithCore = drawSize.width / coreSize.width
                    val drawVisibleRect = coreVisibleRect.scale(drawScaleWithCore)
                    drawRect(
                        color = Color.Red,
                        topLeft = drawVisibleRect.topLeft,
                        size = drawVisibleRect.size,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                .onSizeChanged {
                    imageNodeSizeState.value = it.toSize()
                }
                .pointerInput(animateScale, animationDurationMillis) {
                    detectTapGestures(
                        onTap = {
                            val imageNodeSize = imageNodeSizeState.value
                            if (!imageNodeSize.isEmpty()) {
                                coroutineScope.launch {
                                    if (animateScale) {
                                        state.animateScaleTo(
                                            newScale = state.maxScale,
                                            newScaleContentCentroid = Centroid(
                                                x = it.x / imageNodeSize.width,
                                                y = it.y / imageNodeSize.height
                                            ),
                                            animationDurationMillis = animationDurationMillis
                                        )
                                    } else {
                                        state.snapScaleTo(
                                            newScale = state.maxScale,
                                            newScaleContentCentroid = Centroid(
                                                x = it.x / imageNodeSize.width,
                                                y = it.y / imageNodeSize.height
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
        )
    }
}