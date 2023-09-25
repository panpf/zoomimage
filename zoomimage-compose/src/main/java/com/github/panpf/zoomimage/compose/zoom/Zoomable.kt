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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.zoom.internal.NavigationBarHeightState
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.launch
import kotlin.math.abs

fun Modifier.zoomable(
    logger: Logger,
    zoomable: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val context = LocalContext.current
    val density = LocalDensity.current
    val updatedOnTap by rememberUpdatedState(newValue = onTap)
    val updatedOnLongPress by rememberUpdatedState(newValue = onLongPress)
    val coroutineScope = rememberCoroutineScope()
    val navigationBarHeightState = remember { NavigationBarHeightState() }
    val navigationBarsInsets = WindowInsets.navigationBarsIgnoringVisibility
    var longPressedPoint by remember { mutableStateOf<Offset?>(null) }

    this
        .onSizeChanged {
            val oldContainerSize = zoomable.containerSize
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
                    zoomable.containerSize = newContainerSize
                } else {
                    logger.d {
                        "onSizeChanged. intercepted. " +
                                "oldContainerSize=$oldContainerSize, " +
                                "newContainerSize=$newContainerSize, " +
                                "diffSize=$diffSize, " +
                                "navigationBarHeight=$navigationBarHeight"
                    }
                }
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    if (zoomable.longPressSlideScaleSpec != null) {
                        longPressedPoint = null
                    }
                    zoomable.stopAllAnimation("onPress")
                },
                onDoubleTap = { touchPoint ->
                    coroutineScope.launch {
                        val centroidContentPoint = zoomable.touchPointToContentPoint(touchPoint)
                        zoomable.switchScale(centroidContentPoint, animated = true)
                    }
                },
                onLongPress = {
                    val longPressSlideScaleSpec = zoomable.longPressSlideScaleSpec
                    if (longPressSlideScaleSpec != null) {
                        longPressedPoint = it
                        longPressSlideScaleSpec.hapticFeedback.perform(context)
                    }
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
                    longPressedPoint != null
                            || zoomable.canScroll(horizontal = horizontal, direction = direction)
                },
                onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float, pointCount ->
                    coroutineScope.launch {
                        zoomable.continuousTransformType = ContinuousTransformType.GESTURE
                        val longPressedPoint1 = longPressedPoint
                        val longPressSlideScaleSpec = zoomable.longPressSlideScaleSpec
                        if (longPressSlideScaleSpec != null && pointCount <= 1 && longPressedPoint1 != null) {
                            val scale = longPressSlideScaleSpec.panToScaleTransformer.transform(pan.y)
                            zoomable.gestureTransform(
                                centroid = longPressedPoint1,
                                panChange = Offset.Zero,
                                zoomChange = scale,
                                rotationChange = 0f
                            )
                        } else {
                            zoomable.gestureTransform(
                                centroid = centroid,
                                panChange = pan,
                                zoomChange = zoom,
                                rotationChange = rotation
                            )
                        }
                    }
                },
                onEnd = { centroid, velocity ->
                    coroutineScope.launch {
                        if (!zoomable.rollbackScale(centroid)) {
                            if (!zoomable.fling(velocity, density)) {
                                zoomable.continuousTransformType = ContinuousTransformType.NONE
                            }
                        }
                    }
                }
            )
        }
}