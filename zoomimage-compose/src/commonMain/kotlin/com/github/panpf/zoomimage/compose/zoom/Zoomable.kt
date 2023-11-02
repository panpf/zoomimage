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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.launch

fun Modifier.zoom(
    logger: Logger,
    zoomable: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = composed {
    this
        .zoomable(logger, zoomable, onLongPress = onLongPress, onTap = onTap)
        .zooming(logger, zoomable)
}

fun Modifier.zoomable(
    @Suppress("UNUSED_PARAMETER") logger: Logger,
    zoomable: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val updatedOnTap by rememberUpdatedState(newValue = onTap)
    val updatedOnLongPress by rememberUpdatedState(newValue = onLongPress)
    val coroutineScope = rememberCoroutineScope()
    var lastLongPressPoint by remember { mutableStateOf<Offset?>(null) }
    var lastPointCount by remember { mutableIntStateOf(0) }

    this
        .onSizeChanged { newContainerSize ->
            val oldContainerSize = zoomable.containerSize
            if (newContainerSize != oldContainerSize) {
                zoomable.containerSize = newContainerSize
            }
        }
        .pointerInput(zoomable) {
            detectTapGestures(
                onPress = {
                    if (zoomable.oneFingerScaleSpec != null) {
                        lastLongPressPoint = null
                        lastPointCount = 0
                    }
                    zoomable.stopAllAnimation("onPress")
                },
                onTap = {
                    updatedOnTap?.invoke(it)
                },
                onLongPress = {
                    val oneFingerScaleSpec = zoomable.oneFingerScaleSpec
                    if (zoomable.isSupportGestureType(GestureType.ONE_FINGER_SCALE) && oneFingerScaleSpec != null) {
                        lastLongPressPoint = it
                        oneFingerScaleSpec.hapticFeedback.perform()
                    }
                    updatedOnLongPress?.invoke(it)
                },
                onDoubleTap = { touchPoint ->
                    if (zoomable.isSupportGestureType(GestureType.DOUBLE_TAP_SCALE)) {
                        coroutineScope.launch {
                            val centroidContentPoint = zoomable.touchPointToContentPoint(touchPoint)
                            zoomable.switchScale(centroidContentPoint, animated = true)
                        }
                    }
                },
            )
        }
        .pointerInput(zoomable) {
            detectPowerfulTransformGestures(
                panZoomLock = true,
                canDrag = { horizontal: Boolean, direction: Int ->
                    val longPressPoint = lastLongPressPoint
                    val allowDrag = zoomable.isSupportGestureType(GestureType.DRAG)
                    val canScroll = zoomable.canScroll(horizontal, direction)
                    val allowOneFingerScale =
                        zoomable.isSupportGestureType(GestureType.ONE_FINGER_SCALE)
                    val oneFingerAlready = longPressPoint != null
                    (allowDrag && canScroll) || (allowOneFingerScale && oneFingerAlready)
                },
                onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float, pointCount ->
                    coroutineScope.launch {
                        zoomable.continuousTransformType = ContinuousTransformType.GESTURE

                        lastPointCount = pointCount
                        val longPressPoint = lastLongPressPoint
                        val oneFingerScaleSpec = zoomable.oneFingerScaleSpec
                        if (pointCount == 1 && longPressPoint != null && oneFingerScaleSpec != null) {
                            if (zoomable.isSupportGestureType(GestureType.ONE_FINGER_SCALE)) {
                                val scale =
                                    oneFingerScaleSpec.panToScaleTransformer.transform(pan.y)
                                zoomable.gestureTransform(
                                    centroid = longPressPoint,
                                    panChange = Offset.Zero,
                                    zoomChange = scale,
                                    rotationChange = 0f
                                )
                            }
                        } else {
                            if (zoomable.isSupportGestureType(GestureType.TWO_FINGER_SCALE)) {
                                val finalPan =
                                    if (zoomable.isSupportGestureType(GestureType.DRAG)) pan else Offset.Zero
                                zoomable.gestureTransform(
                                    centroid = centroid,
                                    panChange = finalPan,
                                    zoomChange = zoom,
                                    rotationChange = rotation
                                )
                            }
                        }
                    }
                },
                onEnd = { centroid, velocity ->
                    if (zoomable.isSupportGestureType(GestureType.DRAG)) {
                        coroutineScope.launch {
                            val pointCount = lastPointCount
                            val longPressedPoint = lastLongPressPoint
                            val oneFingerScaleSpec = zoomable.oneFingerScaleSpec
                            if (pointCount == 1 && longPressedPoint != null && oneFingerScaleSpec != null) {
                                zoomable.rollbackScale(longPressedPoint)
                            } else if (!zoomable.rollbackScale(centroid)) {
                                if (!zoomable.fling(velocity, density)) {
                                    zoomable.continuousTransformType = ContinuousTransformType.NONE
                                }
                            }
                        }
                    }
                }
            )
        }
}

fun Modifier.zooming(
    @Suppress("UNUSED_PARAMETER") logger: Logger,
    zoomable: ZoomableState,
) = composed {
    val transform = zoomable.transform
    this
        .clipToBounds()
        .graphicsLayer {
            scaleX = transform.scaleX
            scaleY = transform.scaleY
            translationX = transform.offsetX
            translationY = transform.offsetY
            transformOrigin = transform.scaleOrigin
        }
        .graphicsLayer {
            rotationZ = transform.rotation
            transformOrigin = transform.rotationOrigin
        }
}
