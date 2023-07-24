package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.zoom.internal.detectCanDragGestures
import com.github.panpf.zoomimage.compose.zoom.internal.detectZoomGestures
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
                    state.stopAllAnimation("onPress")
                },
                onDoubleTap = { touchPoint ->
                    coroutineScope.launch {
                        state.switchScale(
                            contentCentroid = state.touchPointToContentPoint(touchPoint),
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
                            targetOffset = state.transform.offset + dragAmount,
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
                    state.scaling = true
                    coroutineScope.launch {
                        state.scale(
                            targetScale = state.transform.scaleX * zoomChange,
                            centroid = centroid,
                            animated = false,
                            rubberBandScale = true,
                        )
                    }
                },
                onEnd = { centroid ->
                    state.scaling = false
                    coroutineScope.launch {
                        state.reboundUserScale(centroid = centroid)
                    }
                }
            )
        }
}