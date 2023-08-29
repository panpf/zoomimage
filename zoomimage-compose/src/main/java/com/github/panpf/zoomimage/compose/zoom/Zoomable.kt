/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import kotlinx.coroutines.launch

fun Modifier.zoomable(
    state: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val updatedOnTap by rememberUpdatedState(newValue = onTap)
    val updatedOnLongPress by rememberUpdatedState(newValue = onLongPress)
    val coroutineScope = rememberCoroutineScope()

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
                        val centroidContentPoint = state.touchPointToContentPoint(touchPoint)
                        state.switchScale(centroidContentPoint, animated = true)
                    }
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
                    coroutineScope.launch {
                        state.transforming = true
                        state.gestureTransform(
                            centroid = centroid,
                            panChange = pan,
                            zoomChange = zoom,
                            rotationChange = rotation
                        )
                    }
                },
                onEnd = { centroid, velocity ->
                    coroutineScope.launch {
                        state.transforming = false
                        if (!state.rollbackScale(centroid)) {
                            state.fling(velocity, density)
                        }
                    }
                }
            )
        }
}