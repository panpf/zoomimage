package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.zoom.internal.detectCanDragGestures
import com.github.panpf.zoomimage.compose.zoom.internal.detectZoomGestures

fun Modifier.zoomable(
    state: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
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
                    state.switchScale(
                        contentPoint = state.touchPointToContentPoint(touchPoint),
                        animated = true
                    )
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
                    state.offset(
                        targetOffset = state.transform.offset + dragAmount,
                        animated = false
                    )
                },
                onDragEnd = {
                    state.fling(it, density)
                },
            )
        }
        .pointerInput(Unit) {
            detectZoomGestures(
                panZoomLock = true,
                onGesture = { centroid: Offset, pan: Offset, zoomChange: Float, _ ->
                    state.scaling = true
                    state.scale(
                        targetScale = state.transform.scaleX * zoomChange,
                        centroid = centroid,
                        pan = pan,
                        animated = false,
                        rubberBandScale = true,
                    )
                    // todo 参考 Telephoto，实现拖动、fling、双指缩放在一个手势中完成，这样双指拖动就可以有 fling 了
                },
                onEnd = { centroid ->
                    state.scaling = false
                    state.rollbackScale(centroid = centroid)
                }
            )
        }
}