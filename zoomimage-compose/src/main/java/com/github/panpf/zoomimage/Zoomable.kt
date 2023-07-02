package com.github.panpf.zoomimage

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.internal.detectCanDragGestures
import com.github.panpf.zoomimage.internal.detectZoomGestures
import kotlinx.coroutines.launch

fun Modifier.zoomable(
    state: ZoomableState,
    animationConfig: AnimationConfig    // todo 挪到 ZoomableState 里
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val flingAnimationSpec = rememberSplineBasedDecay<Float>()
    this
        .onSizeChanged {
            val newContainerSize = it.toSize()
            val oldContainerSize = state.containerSize
            if (newContainerSize != oldContainerSize) {
                state.containerSize = newContainerSize
            }
        }
        .pointerInput(animationConfig) {
            detectTapGestures(
                onPress = {
                    state.stopAllAnimation("onPress")
                },
                onDoubleTap = { offset ->
                    coroutineScope.launch {
                        val nextStepScale = state.getNextStepScale()
                        if (animationConfig.doubleTapScaleEnabled) {
                            state.animateScaleTo(
                                newScale = nextStepScale,
                                touchPosition = offset,
                                animationDurationMillis = animationConfig.durationMillis,
                                animationEasing = animationConfig.easing,
                                initialVelocity = animationConfig.initialVelocity
                            )
                        } else {
                            state.snapScaleTo(newScale = nextStepScale, touchPosition = offset)
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
                onDrag = { _, dragAmount ->
                    coroutineScope.launch {
                        state.snapTranslationBy(dragAmount)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        state.fling(it, flingAnimationSpec)
                    }
                },
            )
        }
        .pointerInput(Unit) {
            detectZoomGestures(panZoomLock = true) { centroid: Offset, zoomChange: Float, rotationChange: Float ->
                coroutineScope.launch {
                    state.transform(
                        centroid = centroid,
                        zoomChange = zoomChange,
                        rotationChange = rotationChange
                    )
                }
            }
        }
}