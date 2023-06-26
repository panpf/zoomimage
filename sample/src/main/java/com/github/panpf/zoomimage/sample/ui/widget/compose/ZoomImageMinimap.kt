package com.github.panpf.zoomimage.sample.ui.widget.compose

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
import com.github.panpf.sketch.resize.DefaultLongImageDecider
import com.github.panpf.zoomimage.Centroid
import com.github.panpf.zoomimage.ZoomState
import com.github.panpf.zoomimage.sample.ui.util.compose.scale
import com.github.panpf.zoomimage.sample.ui.util.compose.toDp
import com.github.panpf.zoomimage.sample.ui.util.compose.toPx
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun ZoomImageMinimap(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    state: ZoomState,
    animateScale: Boolean,
    animationDurationMillis: Int,
) {
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier.then(Modifier.fillMaxSize())) {
        val drawableWidth = painter.intrinsicSize.width
        val drawableHeight = painter.intrinsicSize.height
        val containerWidth = this.maxWidth
        val containerHeight = this.maxHeight
        val sameDirection =
            (drawableWidth >= drawableHeight && containerWidth >= containerHeight) ||
                    (drawableWidth < drawableHeight && containerWidth < containerHeight)
        val isLongImage = DefaultLongImageDecider()
            .isLongImage(
                drawableWidth.toInt(),
                drawableHeight.toInt(),
                containerWidth.toPx().toInt(),
                containerHeight.toPx().toInt()
            )
        val maxPercentage = if (isLongImage) 0.6f else if (sameDirection) 0.3f else 0.4f
        val imageMaxWidth = (containerWidth * maxPercentage).toPx()
        val imageMaxHeight = (containerHeight * maxPercentage).toPx()
        val scale = min(imageMaxWidth / drawableWidth, imageMaxHeight / drawableHeight)
        val viewWidth = drawableWidth * scale
        val viewHeight = drawableHeight * scale
        val imageNodeSizeState = remember { mutableStateOf(Size.Zero) }
        Image(
            painter = painter,
            contentDescription = contentDescription ?: "Visible Rect",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(
                    width = viewWidth.toDp(),
                    height = viewHeight.toDp()
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