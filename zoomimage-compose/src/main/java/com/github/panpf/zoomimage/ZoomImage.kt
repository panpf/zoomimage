package com.github.panpf.zoomimage

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.internal.detectCanDragGestures
import com.github.panpf.zoomimage.internal.detectZoomGestures
import kotlinx.coroutines.launch

@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: ZoomState = rememberZoomState(),
    animationConfig: AnimationConfig = AnimationConfig(),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.then(
            Modifier.createZoomModifier(
                state = state,
                painter = painter,
                contentScale = contentScale,
                alignment = alignment,
                animationConfig = animationConfig
            )
        ),
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

@Composable
fun rememberZoomState(
    @FloatRange(from = 0.0) minScale: Float = 1f,
    @FloatRange(from = 0.0) maxScale: Float = 4f,
    debugMode: Boolean = false
): ZoomState {
    return rememberSaveable(saver = ZoomState.Saver) {
        ZoomState(minScale = minScale, maxScale = maxScale, debugMode = debugMode)
    }
}

private fun Modifier.createZoomModifier(
    state: ZoomState,
    painter: Painter,
    contentScale: ContentScale,
    alignment: Alignment,
    animationConfig: AnimationConfig = AnimationConfig()
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    Modifier
        .onSizeChanged {
            state.init(
                containerSize = it.toSize(),
                contentSize = painter.intrinsicSize,
                contentScale = contentScale,
                contentAlignment = alignment
            )
        }
        .pointerInput(animationConfig) {
            detectTapGestures(onDoubleTap = { offset ->
                coroutineScope.launch {
                    val newScale = state.getNextStepScale()
                    if (animationConfig.doubleTapScaleEnabled) {
                        state.animateScaleTo(
                            newScale = newScale,
                            touchPosition = offset,
                            animationDurationMillis = animationConfig.durationMillis,
                            animationEasing = animationConfig.easing,
                            initialVelocity = animationConfig.initialVelocity
                        )
                    } else {
                        state.snapScaleTo(newScale = newScale, touchPosition = offset)
                    }
                }
            })
        }
        .pointerInput(Unit) {
            detectCanDragGestures(
                canDrag = { horizontally: Boolean, direction: Int ->
                    val scrollEdge =
                        if (horizontally) state.horizontalScrollEdge else state.verticalScrollEdge
                    val targetEdge = if (direction > 0) Edge.END else Edge.START
                    scrollEdge == Edge.NONE || scrollEdge == targetEdge
                },
                onDragStart = {
                    coroutineScope.launch {
                        state.dragStart()
                    }
                },
                onDrag = { change, dragAmount ->
                    coroutineScope.launch {
                        state.drag(change, dragAmount)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        state.dragEnd()
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        state.dragCancel()
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectZoomGestures(panZoomLock = true) { centroid: Offset, zoom: Float, _ ->
                coroutineScope.launch {
                    state.transform(zoomChange = zoom, touchCentroid = centroid)
                }
            }
        }
        .clipToBounds()
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
//            rotationZ = state.rotation    // todo support rotation
            translationX = state.translation.x
            translationY = state.translation.y
            transformOrigin = state.transformOrigin
        }
}