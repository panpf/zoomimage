package com.github.panpf.zoomimage

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.core.internal.computeCanDrag
import com.github.panpf.zoomimage.internal.detectCanDragGestures
import com.github.panpf.zoomimage.internal.detectZoomGestures
import com.github.panpf.zoomimage.internal.toRect
import com.github.panpf.zoomimage.internal.toSize
import kotlinx.coroutines.launch

fun Modifier.zoomable(state: ZoomableState): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    this
        .onSizeChanged {
            val newContainerSize = it.toSize()
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
                onDoubleTap = { offset ->
                    coroutineScope.launch {
                        state.animateScaleTo(
                            newScale = state.getNextStepScale(),
                            touchPosition = offset
                        )
                    }
                })
        }
        .pointerInput(Unit) {
            detectCanDragGestures(
                canDrag = { horizontal: Boolean, direction: Int ->
                    computeCanDrag(
                        contentSize = state.contentSize.toSize(),
                        contentVisibleRect = state.contentVisibleRect.toRect(),
                        horizontal = horizontal,
                        direction = direction
                    )
                },
                onDrag = { _, dragAmount ->
                    coroutineScope.launch {
                        state.snapTranslationBy(dragAmount)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        state.fling(it)
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