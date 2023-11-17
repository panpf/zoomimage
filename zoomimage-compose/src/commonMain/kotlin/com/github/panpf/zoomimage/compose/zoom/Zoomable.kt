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

@file:Suppress("NAME_SHADOWING")

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.getValue
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
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTapGestures
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.launch

fun Modifier.zoom(
    zoomable: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = composed {
    this
        .zoomable(zoomable, onLongPress = onLongPress, onTap = onTap)
        .zooming(zoomable)
}

fun Modifier.zoomable(
    zoomable: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val updatedOnTap by rememberUpdatedState(newValue = onTap)
    val updatedOnLongPress by rememberUpdatedState(newValue = onLongPress)
    var longPressExecuted by remember { mutableStateOf(false) }
    var doubleTapPressPoint by remember { mutableStateOf<Offset?>(null) }
    var oneFingerScaleExecuted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    this
        .onSizeChanged { newContainerSize ->
            val oldContainerSize = zoomable.containerSize
            if (newContainerSize != oldContainerSize) {
                zoomable.containerSize = newContainerSize
            }
        }
        .pointerInput(zoomable) {
            detectPowerfulTapGestures(
                onPress = {
                    longPressExecuted = false
                    doubleTapPressPoint = null
                    oneFingerScaleExecuted = false
                    coroutineScope.launch {
                        zoomable.stopAllAnimation("onPress")
                    }
                },
                onTap = {
                    updatedOnTap?.invoke(it)
                },
                onLongPress = {
                    val updatedOnLongPress = updatedOnLongPress
                    if (updatedOnLongPress != null) {
                        updatedOnLongPress.invoke(it)
                        longPressExecuted = true
                    }
                },
                onDoubleTapPress = { touchPoint ->
                    doubleTapPressPoint = touchPoint
                },
                onDoubleTapUp = { touchPoint ->
                    doubleTapPressPoint = null
                    val supportDoubleTapScale =
                        zoomable.checkSupportGestureType(GestureType.DOUBLE_TAP_SCALE)
                    if (supportDoubleTapScale && !oneFingerScaleExecuted && !longPressExecuted) {
                        coroutineScope.launch {
                            val centroidContentPoint =
                                zoomable.touchPointToContentPoint(touchPoint)
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
                    val supportDrag = zoomable.checkSupportGestureType(GestureType.DRAG)
                    val canScroll = zoomable.canScroll(horizontal, direction)
                    val supportOneFingerScale =
                        zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                    val doubleTapPressPoint = doubleTapPressPoint
                    (supportDrag && canScroll) || (supportOneFingerScale && doubleTapPressPoint != null)
                },
                onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float, pointCount ->
                    coroutineScope.launch {
                        val longPressExecuted = longPressExecuted
                        val doubleTapPressPoint = doubleTapPressPoint
                        val supportOneFingerScale =
                            zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                        val supportTwoFingerScale =
                            zoomable.checkSupportGestureType(GestureType.TWO_FINGER_SCALE)
                        val supportDrag = zoomable.checkSupportGestureType(GestureType.DRAG)
                        zoomable.logger.v {
                            "zoomable. onGesture. " +
                                    "longPressExecuted=$longPressExecuted, " +
                                    "pointCount=$pointCount, " +
                                    "doubleTapPressPoint=$doubleTapPressPoint, " +
                                    "supportOneFingerScale=$supportOneFingerScale, " +
                                    "supportTwoFingerScale=$supportTwoFingerScale, " +
                                    "supportDrag=$supportDrag"
                        }
                        if (longPressExecuted) return@launch
                        if (supportOneFingerScale && pointCount == 1 && doubleTapPressPoint != null) {
                            oneFingerScaleExecuted = true
                            val oneFingerScaleSpec = zoomable.oneFingerScaleSpec
                            val scale = oneFingerScaleSpec.panToScaleTransformer.transform(pan.y)
                            zoomable.continuousTransformType = ContinuousTransformType.GESTURE
                            zoomable.gestureTransform(
                                centroid = doubleTapPressPoint,
                                panChange = Offset.Zero,
                                zoomChange = scale,
                                rotationChange = 0f
                            )
                        } else {
                            oneFingerScaleExecuted = false
                            if (supportTwoFingerScale || supportDrag) {
                                val finalPan = if (supportDrag) pan else Offset.Zero
                                val finalZoom = if (supportTwoFingerScale) zoom else 1f
                                zoomable.continuousTransformType = ContinuousTransformType.GESTURE
                                zoomable.gestureTransform(
                                    centroid = centroid,
                                    panChange = finalPan,
                                    zoomChange = finalZoom,
                                    rotationChange = rotation
                                )
                            }
                        }
                    }
                },
                onEnd = { centroid, velocity ->
                    coroutineScope.launch {
                        val longPressExecuted = longPressExecuted
                        val doubleTapPressPoint = doubleTapPressPoint
                        val oneFingerScaleExecuted = oneFingerScaleExecuted
                        val supportOneFingerScale =
                            zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                        val supportTwoFingerScale =
                            zoomable.checkSupportGestureType(GestureType.TWO_FINGER_SCALE)
                        val supportDrag =
                            zoomable.checkSupportGestureType(GestureType.DRAG)
                        zoomable.logger.v {
                            "zoomable. onEnd. " +
                                    "centroid=${centroid.toShortString()}, " +
                                    "velocity=${velocity.x.format(2)}x${velocity.y.format(2)}, " +
                                    "longPressExecuted=$longPressExecuted, " +
                                    "doubleTapPressPoint=$doubleTapPressPoint, " +
                                    "oneFingerScaleExecuted=$oneFingerScaleExecuted, " +
                                    "supportOneFingerScale=$supportOneFingerScale, " +
                                    "supportTwoFingerScale=$supportTwoFingerScale, " +
                                    "supportDrag=$supportDrag"
                        }
                        if (longPressExecuted) return@launch
                        if (supportOneFingerScale && oneFingerScaleExecuted && doubleTapPressPoint != null) {
                            if (!zoomable.rollbackScale(doubleTapPressPoint)) {
                                zoomable.continuousTransformType = GestureType.NONE
                            }
                        } else {
                            val rollbackScaleExecuted =
                                supportTwoFingerScale && zoomable.rollbackScale(centroid)
                            var flingExecuted = false
                            if (!rollbackScaleExecuted) {
                                flingExecuted = supportDrag && zoomable.fling(velocity, density)
                            }
                            if ((supportTwoFingerScale || supportDrag) && (!rollbackScaleExecuted && !flingExecuted)) {
                                zoomable.continuousTransformType = GestureType.NONE
                            }
                        }
                    }
                }
            )
        }
}

fun Modifier.zooming(zoomable: ZoomableState) = composed {
    val transform = zoomable.transform
    this
        .clipToBounds()
        .graphicsLayer {
            zoomable.logger.v { "graphicsLayer. transform=$transform" }
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
