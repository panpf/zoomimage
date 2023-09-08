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

@file:OptIn(ExperimentalLayoutApi::class)

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.zoom.internal.NavigationBarHeightState
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import kotlinx.coroutines.launch
import kotlin.math.abs

fun Modifier.zoomable(
    state: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val updatedOnTap by rememberUpdatedState(newValue = onTap)
    val updatedOnLongPress by rememberUpdatedState(newValue = onLongPress)
    val coroutineScope = rememberCoroutineScope()
    val navigationBarHeightState = remember { NavigationBarHeightState() }
    val navigationBarsInsets = WindowInsets.navigationBarsIgnoringVisibility

    this
        .onSizeChanged {
            val oldContainerSize = state.containerSize
            val newContainerSize = it
            if (newContainerSize != oldContainerSize) {
                /*
                 * In the model MIX4; ROM: 14.0.6.0; on Android 13, when the navigation bar is displayed, the following occurs:
                 * 1. When the ZoomImageView is unlocked again after the screen is locked, the height of the ZoomImageView will first increase and then change back to normal, and the difference is exactly the height of the current navigation bar
                 * 2. Due to the height of the ZoomImageView, the containerSize of the ZoomableEngine will also change
                 * 3. This causes the ZoomableEngine's transform to be reset, so this needs to be blocked here
                 */
                val newNavigationBarHeight = navigationBarsInsets.getBottom(density)
                if (newNavigationBarHeight != 0 && newNavigationBarHeight != navigationBarHeightState.navigationBarHeight) {
                    navigationBarHeightState.navigationBarHeight = newNavigationBarHeight
                }
                val navigationBarHeight = navigationBarHeightState.navigationBarHeight
                val diffSize = IntSize(
                    width = newContainerSize.width - oldContainerSize.width,
                    height = newContainerSize.height - oldContainerSize.height
                )
                if (navigationBarHeight == 0 ||
                    (abs(diffSize.width) != navigationBarHeight && abs(diffSize.height) != navigationBarHeight)
                ) {
                    state.containerSize = newContainerSize
                }
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