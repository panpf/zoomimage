package com.github.panpf.zoomimage

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.internal.detectCanDragGestures
import com.github.panpf.zoomimage.compose.internal.detectZoomGestures
import kotlinx.coroutines.launch

fun Modifier.zoomable(
    state: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    this
        .onSizeChanged {
            val newContainerSize = it
            val oldContainerSize = state.containerSize
            if (newContainerSize != oldContainerSize) {
                state.containerSize = newContainerSize
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    state.stopAnimation("onPress")
                },
                onDoubleTap = { offset ->
                    coroutineScope.launch {
                        state.switchUserScale(
                            contentOrigin = state.touchOffsetToContentOrigin(offset),
                            animated = true
                        )
                    }
                },
                onLongPress = {
                    onLongPress?.invoke(it)
                },
                onTap = {
                    onTap?.invoke(it)
                },
            )
        }
        .pointerInput(Unit) {
            detectCanDragGestures(
                canDrag = { horizontal: Boolean, direction: Int ->
                    state.canDrag(horizontal = horizontal, direction = direction)
                },
                onDrag = { _, dragAmount ->
                    coroutineScope.launch {
                        state.offset(
                            targetUserOffset = state.userTransform.offset + dragAmount,
                            animated = false
                        )
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        state.fling(it, density)
                    }
                },
            )
        }
        .pointerInput(Unit) {
            detectZoomGestures(
                panZoomLock = true,
                onGesture = { centroid: Offset, zoomChange: Float, _ ->
                    coroutineScope.launch {
                        state.scale(
                            targetUserScale = state.userTransform.scaleX * zoomChange,
                            centroid = centroid,
                            animated = false,
                            rubberBandScale = true,
                        )
                    }
                },
                onEnd = { centroid ->
                    coroutineScope.launch {
                        state.reboundUserScale(centroid = centroid)
                    }
                }
            )
        }
}