/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalDensity
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.util.toShortString
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTapGestures
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.launch

/**
 * A Modifier that can recognize gestures such as click, long press, double-click, one-finger zoom, two-finger zoom, drag, fling, etc.,
 * and then apply the gesture changes to the component. It can be used on any composable component.
 *
 * Since it consumes all gestures, [Modifier.clickable] and [Modifier.combinedClickable] will not work.
 * You can pass [onTap] and [onLongPress] parameters instead.
 *
 * If the zoomed content does not fill the container, you can set the size, scaling and alignment of the content through
 * the contentSize, contentScale, and alignment properties of [ZoomableState]. This will only translate within the content area after scaling.
 */
fun Modifier.zoom(
    zoomable: ZoomableState,
    userSetupContentSize: Boolean = false,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = this
    .zoomable(
        zoomable = zoomable,
        userSetupContentSize = userSetupContentSize,
        onLongPress = onLongPress,
        onTap = onTap
    )
    .zooming(zoomable)

/**
 * A Modifier that can recognize gestures such as click, long press, double-click, one-finger zoom, two-finger zoom, drag, fling, etc.
 * It can be used on any composable component.
 *
 * It stores the changes caused by the gesture into [ZoomableState].transform. You need to read the transform and then
 * apply it to the component through graphicsLayer. You can also use the [zooming] Modifier directly.
 *
 * Since it consumes all gestures, [Modifier.clickable] and [Modifier.combinedClickable] will not work.
 * You can pass [onTap] and [onLongPress] parameters instead.
 *
 * If the zoomed content does not fill the container, you can set the size, scaling and alignment of the content through
 * the contentSize, contentScale, and alignment properties of [ZoomableState]. This will only translate within the content area after scaling.
 */
fun Modifier.zoomable(
    zoomable: ZoomableState,
    userSetupContentSize: Boolean = false,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = this
    .onSizeChanged {
        zoomable.containerSize = it
        if (!userSetupContentSize) {
            zoomable.contentSize = it
        }
    }
    .then(ZoomableElement(zoomable, onLongPress, onTap))

/**
 * A Modifier that applies changes in [ZoomableState].transform to the component. It can be used on any composable component.
 */
fun Modifier.zooming(
    zoomable: ZoomableState
): Modifier = this
    .clipToBounds()
    .graphicsLayer {
        val transform = zoomable.transform
        zoomable.logger.v { "ZoomableState. graphicsLayer. transform=$transform" }
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
) : DelegatingNode(), CompositionLocalConsumerModifierNode {

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
                val supportDrag = zoomable.checkSupportGestureType(GestureType.ONE_FINGER_DRAG)
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
                    val supportDrag = zoomable.checkSupportGestureType(GestureType.ONE_FINGER_DRAG)
                    zoomable.logger.v {
                        "ZoomableState. zoomable. onGesture. " +
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
                            // Only allow one-finger dragging
                            val finalPan = if (supportDrag && pointCount == 1) pan else Offset.Zero
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
                        zoomable.checkSupportGestureType(GestureType.ONE_FINGER_DRAG)
                    zoomable.logger.v {
                        "ZoomableState. zoomable. onEnd. " +
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
                            zoomable.continuousTransformType = 0
                        }
                    } else {
                        val rollbackScaleExecuted =
                            supportTwoFingerScale && zoomable.rollbackScale(centroid)
                        var flingExecuted = false
                        if (!rollbackScaleExecuted) {
                            val density = currentValueOf(LocalDensity)
                            flingExecuted = supportDrag && zoomable.fling(velocity, density)
                        }
                        if ((supportTwoFingerScale || supportDrag) && (!rollbackScaleExecuted && !flingExecuted)) {
                            zoomable.continuousTransformType = 0
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