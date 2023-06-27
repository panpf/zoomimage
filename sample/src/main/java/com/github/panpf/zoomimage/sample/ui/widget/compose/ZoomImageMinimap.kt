package com.github.panpf.zoomimage.sample.ui.widget.compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.resize.DefaultLongImageDecider
import com.github.panpf.zoomimage.Centroid
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.sample.ui.util.compose.scale
import com.github.panpf.zoomimage.sample.ui.util.compose.toDp
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun ZoomImageMinimap(
    sketchImageUri: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    state: ZoomableState,
    animateScale: Boolean,
    animationDurationMillis: Int,
) {
    val contentSize = state.contentSize.takeIf { it.isSpecified } ?: Size.Zero
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier.then(Modifier.fillMaxSize())) {
        val density = LocalDensity.current
        val viewSize = remember(contentSize) {
            val containerSize = with(density) { Size(maxWidth.toPx(), maxHeight.toPx()) }
            computeViewSize(contentSize, containerSize)
        }
        if (viewSize.isSpecified) {
            val imageNodeSizeState = remember { mutableStateOf(Size.Zero) }
            AsyncImage(
                imageUri = sketchImageUri,
                contentDescription = contentDescription ?: "Visible Rect",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(
                        width = viewSize.width.toDp(),
                        height = viewSize.height.toDp()
                    )
                    .drawWithContent {
                        drawContent()
                        val drawSize = size
                        val coreSize = state.contentSize
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
}

private fun computeViewSize(contentSize: Size, containerSize: Size): Size {
    if (contentSize.isUnspecified || contentSize.isEmpty()) return Size.Unspecified
    if (containerSize.isUnspecified || containerSize.isEmpty()) return Size.Unspecified
    val contentWidth = contentSize.width
    val contentHeight = contentSize.height
    val containerWidth = containerSize.width
    val containerHeight = containerSize.height
    val sameDirection =
        (contentWidth >= contentHeight && containerWidth >= containerHeight) ||
                (contentWidth < contentHeight && containerWidth < containerHeight)
    val isLongImage = DefaultLongImageDecider()
        .isLongImage(
            imageWidth = contentWidth.toInt(),
            imageHeight = contentHeight.toInt(),
            targetWidth = containerWidth.toInt(),
            targetHeight = containerHeight.toInt()
        )
    val maxPercentage = if (isLongImage) 0.6f else if (sameDirection) 0.3f else 0.4f
    val maxWidth = containerWidth * maxPercentage
    val maxHeight = containerHeight * maxPercentage
    val scale = min(maxWidth / contentWidth, maxHeight / contentHeight)
    return Size(contentWidth * scale, contentHeight * scale)
}