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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toRect
import androidx.compose.ui.unit.toSize
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.toCompatIntSize
import com.github.panpf.zoomimage.core.Origin
import com.github.panpf.zoomimage.sample.ui.util.compose.scale
import com.github.panpf.zoomimage.sample.ui.util.compose.toDp
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomImageMinimap(
    sketchImageUri: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    state: ZoomableState,
    alignment: Alignment = Alignment.BottomStart,
) {
    val contentSize = state.contentSize.takeIf { it.isNotEmpty() } ?: IntSize.Zero
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier.then(Modifier.fillMaxSize())) {
        val density = LocalDensity.current
        val viewSize = remember(contentSize) {
            val containerSize =
                with(density) { IntSize(maxWidth.roundToPx(), maxHeight.roundToPx()) }
            computeViewSize(contentSize, containerSize)
        }
        if (viewSize.isNotEmpty()) {
            val imageNodeSizeState = remember { mutableStateOf(Size.Zero) }
            AsyncImage(
                request = DisplayRequest(LocalContext.current, sketchImageUri) {
                    crossfade()
                },
                contentDescription = contentDescription ?: "Visible Rect",
                modifier = Modifier
                    .align(alignment)
                    .size(
                        width = viewSize.width
                            .toFloat()
                            .toDp(),
                        height = viewSize.height
                            .toFloat()
                            .toDp()
                    )
                    .clipToBounds()
                    .drawWithContent {
                        drawContent()
                        val contentVisibleRect = state.contentVisibleRect
                        val drawScaleWithContent = ScaleFactor(
                            scaleX = viewSize.width / contentSize.width.toFloat(),
                            scaleY = viewSize.height / contentSize.height.toFloat()
                        )
                        val drawVisibleRect =
                            contentVisibleRect
                                .scale(drawScaleWithContent)
                                .toRect()
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
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                val imageNodeSize = imageNodeSizeState.value
                                if (!imageNodeSize.isEmpty()) {
                                    coroutineScope.launch {
                                        state.location(
                                            contentOrigin = Origin(
                                                x = it.x / imageNodeSize.width,
                                                y = it.y / imageNodeSize.height
                                            ),
                                            targetScale = state.transform.scaleX.coerceAtLeast(state.mediumScale),
                                            animated = true,
                                        )
                                    }
                                }
                            }
                        )
                    },
            )
        }
    }
}

private fun computeViewSize(contentSize: IntSize, containerSize: IntSize): IntSize {
    if (contentSize.isEmpty()) return IntSize.Zero
    if (containerSize.isEmpty()) return IntSize.Zero
    val contentWidth = contentSize.width
    val contentHeight = contentSize.height
    val containerWidth = containerSize.width
    val containerHeight = containerSize.height
    val sameDirection =
        (contentWidth >= contentHeight && containerWidth >= containerHeight) ||
                (contentWidth < contentHeight && containerWidth < containerHeight)
    val isLongImage = ReadMode.LongImageDecider()
        .should(srcSize = contentSize.toCompatIntSize(), dstSize = containerSize.toCompatIntSize())
    val maxPercentage = if (isLongImage) 0.6f else if (sameDirection) 0.3f else 0.4f
    val maxWidth = containerWidth * maxPercentage
    val maxHeight = containerHeight * maxPercentage
    val scale = min(maxWidth / contentWidth, maxHeight / contentHeight)
    return IntSize((contentWidth * scale).roundToInt(), (contentHeight * scale).roundToInt())
}