package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures

fun Modifier.zoomable(
    state: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val updatedOnTap by rememberUpdatedState(newValue = onTap)
    val updatedOnLongPress by rememberUpdatedState(newValue = onLongPress)
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
                    state.switchScale(
                        contentPoint = state.touchPointToContentPoint(touchPoint),
                        animated = true
                    )
                },
                onLongPress = {
                    updatedOnLongPress?.invoke(it)
                },
                onTap = {
                    updatedOnTap?.invoke(it)
                },
            )
        }
        .pointerInput(Unit) {
            detectPowerfulTransformGestures(
                panZoomLock = true,
                canDrag = { horizontal: Boolean, direction: Int ->
                    state.canScroll(horizontal = horizontal, direction = direction)
                },
                onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float ->
                    state.scaling = zoom != 1.0f
                    state.transform(
                        centroid = centroid,
                        panChange = pan,
                        zoomChange = zoom,
                        rotationChange = rotation
                    )
                },
                onEnd = { centroid, velocity ->
                    state.scaling = false
                    if (!state.rollbackScale(centroid)) {
                        state.fling(velocity, density)
                    }
                }
            )
        }
}