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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.util.ifLet
import com.github.panpf.zoomimage.compose.util.isNotEmpty
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTapGestures
import com.github.panpf.zoomimage.compose.zoom.internal.detectPowerfulTransformGestures
import com.github.panpf.zoomimage.subsampling.internal.calculateOriginToThumbnailScaleFactor
import com.github.panpf.zoomimage.util.Origin
import com.github.panpf.zoomimage.util.ScaleFactorCompat
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
): Modifier = this then ZoomableElement(zoomable, userSetupContentSize, onLongPress, onTap)

/**
 * A Modifier that applies changes in [ZoomableState].transform to the component. It can be used on any composable component.
 */
fun Modifier.zooming(zoomable: ZoomableState, firstScaleByContentSize: Boolean = false): Modifier =
    this
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
        .ifLet(firstScaleByContentSize) {
            it.graphicsLayer {
                val contentOriginSize = zoomable.contentOriginSize
                val contentSize = zoomable.contentSize
                val scaleFactor = if (contentOriginSize.isNotEmpty() && contentSize.isNotEmpty()) {
                    calculateOriginToThumbnailScaleFactor(
                        originImageSize = contentOriginSize.toCompat(),
                        thumbnailImageSize = contentSize.toCompat()
                    )
                } else {
                    ScaleFactorCompat.Origin
                }
                scaleX = scaleFactor.scaleX
                scaleY = scaleFactor.scaleY
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }

/**
 * A Modifier that applies changes in [ZoomableState].transform to the component. It can be used on any composable component.
 */
fun Modifier.zooming(zoomable: ZoomableState): Modifier =
    this.zooming(zoomable, firstScaleByContentSize = false)

///**
// * A Modifier that restores the content base transform of the [ZoomableState] to the component.
// */
//fun Modifier.restoreContentBaseTransform(zoomable: ZoomableState): Modifier =
//    this.graphicsLayer {
//        // transform.isNotEmpty() can avoid content position drift
//        val transform = zoomable.transform
//        if (transform.isNotEmpty()) {
//            val restoreTransform = calculateRestoreContentBaseTransformTransform(
//                containerSize = zoomable.containerSize.toCompat(),
//                contentSize = zoomable.contentSize.toCompat(),
//                contentScale = zoomable.contentScale.toCompat(),
//                alignment = zoomable.alignment.toCompat(),
//                rtlLayoutDirection = zoomable.layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl,
//            )
//            scaleX = restoreTransform.scaleX
//            scaleY = restoreTransform.scaleY
//            translationX = restoreTransform.offsetX
//            translationY = restoreTransform.offsetY
//            transformOrigin = restoreTransform.scaleOrigin.toPlatform()
//        }
//    }

internal data class ZoomableElement(
    val zoomable: ZoomableState,
    val userSetupContentSize: Boolean,
    val onLongPress: ((Offset) -> Unit)? = null,
    val onTap: ((Offset) -> Unit)? = null,
) : ModifierNodeElement<ZoomableNode>() {

    override fun create(): ZoomableNode {
        return ZoomableNode(
            zoomable = zoomable,
            userSetupContentSize = userSetupContentSize,
            onLongPress = onLongPress,
            onTap = onTap,
        )
    }

    override fun update(node: ZoomableNode) {
        node.update(
            zoomable = zoomable,
            userSetupContentSize = userSetupContentSize,
            onLongPress = onLongPress,
            onTap = onTap,
        )
    }
}

internal class ZoomableNode(
    var zoomable: ZoomableState,
    var userSetupContentSize: Boolean,
    var onLongPress: ((Offset) -> Unit)? = null,
    var onTap: ((Offset) -> Unit)? = null,
) : DelegatingNode(), CompositionLocalConsumerModifierNode, LayoutAwareModifierNode {

    private var longPressExecuted = false
    private var doubleTapExecuted = false
    private var doubleTapPressPoint: Offset? = null
    private var oneFingerScaleExecuted = false
    private var twoFingerScaleCentroid: Offset? = null
    private var previousSize = IntSize(Int.MIN_VALUE, Int.MIN_VALUE)

    private val tapPointerDelegate = delegate(SuspendingPointerInputModifierNode {
        detectPowerfulTapGestures(
            onPress = {
                longPressExecuted = false
                doubleTapExecuted = false
                doubleTapPressPoint = null
                oneFingerScaleExecuted = false
                twoFingerScaleCentroid = null
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
                val supportDoubleTapScale =
                    zoomable.checkSupportGestureType(GestureType.DOUBLE_TAP_SCALE)
                if (supportDoubleTapScale && !oneFingerScaleExecuted && !longPressExecuted) {
                    doubleTapExecuted = true
                    coroutineScope.launch {
                        val centroidContentPoint =
                            zoomable.touchPointToContentPointF(touchPoint)
                        zoomable.switchScale(
                            centroidContentPointF = centroidContentPoint,
                            animated = true
                        )
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
                val alwaysCanDragAtEdge = zoomable.alwaysCanDragAtEdge
                val canScroll = zoomable.canScroll(horizontal, direction)
                val supportOneFingerScale =
                    zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                val doubleTapPressPoint = doubleTapPressPoint
                (supportDrag && (alwaysCanDragAtEdge || canScroll)) || (supportOneFingerScale && doubleTapPressPoint != null)
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
                        zoomable.setContinuousTransformType(ContinuousTransformType.GESTURE)
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
                            zoomable.setContinuousTransformType(ContinuousTransformType.GESTURE)
                            zoomable.gestureTransform(
                                centroid = centroid,
                                panChange = finalPan,
                                zoomChange = finalZoom,
                                rotationChange = rotation
                            )
                        }
                    }
                    if (pointCount == 2 && zoom != 1.0f) {
                        twoFingerScaleCentroid = centroid
                    }
                }
            },
            onEnd = { velocity ->
                coroutineScope.launch {
                    val longPressExecuted = longPressExecuted
                    val doubleTapExecuted = doubleTapExecuted
                    val doubleTapPressPoint = doubleTapPressPoint
                    val oneFingerScaleExecuted = oneFingerScaleExecuted
                    val twoFingerScaleCentroid = twoFingerScaleCentroid
                    val supportOneFingerScale =
                        zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                    val supportTwoFingerScale =
                        zoomable.checkSupportGestureType(GestureType.TWO_FINGER_SCALE)
                    val supportDrag =
                        zoomable.checkSupportGestureType(GestureType.ONE_FINGER_DRAG)
                    zoomable.logger.v {
                        "ZoomableState. zoomable. onEnd. " +
                                "velocity=${velocity.x.format(2)}x${velocity.y.format(2)}, " +
                                "longPressExecuted=$longPressExecuted, " +
                                "doubleTapExecuted=$doubleTapExecuted, " +
                                "doubleTapPressPoint=$doubleTapPressPoint, " +
                                "oneFingerScaleExecuted=$oneFingerScaleExecuted, " +
                                "twoFingerScaleCentroid=$twoFingerScaleCentroid, " +
                                "supportOneFingerScale=$supportOneFingerScale, " +
                                "supportTwoFingerScale=$supportTwoFingerScale, " +
                                "supportDrag=$supportDrag"
                    }
                    if (longPressExecuted || doubleTapExecuted) return@launch

                    val density = currentValueOf(LocalDensity)
                    val centroid = when {
                        supportOneFingerScale && oneFingerScaleExecuted -> doubleTapPressPoint
                        supportTwoFingerScale && twoFingerScaleCentroid != null -> twoFingerScaleCentroid
                        else -> null
                    }
                    if (zoomable.rollback(centroid)) {
                        // If the rollback is successfully executed, nothing needs to be done
                    } else if (supportDrag && zoomable.fling(velocity, density)) {
                        // If the fling is successfully executed, nothing needs to be done
                    } else {
                        zoomable.setContinuousTransformType(ContinuousTransformType.NONE)
                    }
                }
            }
        )
    })

    fun update(
        zoomable: ZoomableState,
        userSetupContentSize: Boolean,
        onLongPress: ((Offset) -> Unit)? = null,
        onTap: ((Offset) -> Unit)? = null,
    ) {
        val zoomableChanged = this.zoomable != zoomable
        val callbackChanged = (this.onTap == null) != (onTap == null) ||
                (this.onLongPress == null) != (onLongPress == null)

        this.zoomable = zoomable
        this.userSetupContentSize = userSetupContentSize
        this.onLongPress = onLongPress
        this.onTap = onTap

        if (zoomableChanged || callbackChanged) {
            tapPointerDelegate.resetPointerInputHandler()
        }
        if (zoomableChanged) {
            gesturePointerDelegate.resetPointerInputHandler()
        }

        // Reset the previous size, so when userSetupContentSize changes the new lambda gets invoked,
        // matching previous behavior
        previousSize = IntSize(Int.MIN_VALUE, Int.MIN_VALUE)
    }

    override fun onRemeasured(size: IntSize) {
        if (previousSize != size) {
            zoomable.containerSize = size
            if (!userSetupContentSize) {
                zoomable.contentSize = size
            }
            previousSize = size
        }
    }
}