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

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
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
): Modifier = this
    .zoomable(zoomable, onLongPress = onLongPress, onTap = onTap)
    .zooming(zoomable)

fun Modifier.zoomable(
    zoomable: ZoomableState,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = this
    .then(ZoomableElement(zoomable, onLongPress, onTap))

fun Modifier.zooming(
    zoomable: ZoomableState
): Modifier = this
    .clipToBounds()
    .graphicsLayer {
        val transform = zoomable.transform
        zoomable.logger.v { "graphicsLayer. transform=$transform" }
        scaleX = transform.scaleX
        scaleY = transform.scaleY
        translationX = transform.offsetX
        translationY = transform.offsetY
        transformOrigin = transform.scaleOrigin
    }
    // Because rotationOrigin and rotationOrigin are different, they must be set separately.
    .graphicsLayer {
        val transform = zoomable.transform
        rotationZ = transform.rotation
        transformOrigin = transform.rotationOrigin
    }

internal data class ZoomableElement(
    val zoomable: ZoomableState,
    val onLongPress: ((Offset) -> Unit)? = null,
    val onTap: ((Offset) -> Unit)? = null,
) : ModifierNodeElement<ZoomableNode>() {

    override fun create(): ZoomableNode {
        return ZoomableNode(
            zoomable = zoomable,
            onLongPress = onLongPress,
            onTap = onTap,
        )
    }

    override fun update(node: ZoomableNode) {
        node.update(
            zoomable = zoomable,
            onLongPress = onLongPress,
            onTap = onTap,
        )
    }
}

internal class ZoomableNode(
    var zoomable: ZoomableState,
    var onLongPress: ((Offset) -> Unit)? = null,
    var onTap: ((Offset) -> Unit)? = null,
) : DelegatingNode() {

    private var longPressExecuted = false
    private var doubleTapPressPoint: Offset? = null
    private var oneFingerScaleExecuted = false

    private val tapPointerDelegate = delegate(SuspendingPointerInputModifierNode {
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
                onTap?.invoke(it)
            },
            onLongPress = {
                val updatedOnLongPress = onLongPress
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
    })

    private val gesturePointerDelegate = delegate(SuspendingPointerInputModifierNode {
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
                            flingExecuted = supportDrag && zoomable.fling(velocity)
                        }
                        if ((supportTwoFingerScale || supportDrag) && (!rollbackScaleExecuted && !flingExecuted)) {
                            zoomable.continuousTransformType = GestureType.NONE
                        }
                    }
                }
            }
        )
    })

    fun update(
        zoomable: ZoomableState,
        onLongPress: ((Offset) -> Unit)? = null,
        onTap: ((Offset) -> Unit)? = null,
    ) {
        val zoomableChanged = this.zoomable != zoomable
        val callbackChanged = (this.onTap == null) != (onTap == null) ||
                (this.onLongPress == null) != (onLongPress == null)

        this.zoomable = zoomable
        this.onLongPress = onLongPress
        this.onTap = onTap

        if (zoomableChanged || callbackChanged) {
            tapPointerDelegate.resetPointerInputHandler()
        }
        if (zoomableChanged) {
            gesturePointerDelegate.resetPointerInputHandler()
        }
    }
}