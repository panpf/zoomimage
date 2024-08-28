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

package com.github.panpf.zoomimage.view.zoom.internal

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.OnViewTapListener
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class TouchHelper(view: View, zoomable: ZoomableEngine) {

    private val gestureDetector: UnifiedGestureDetector
    private var longPressExecuted = false
    private var doubleTapPressPoint: OffsetCompat? = null
    private var oneFingerScaleExecuted = false
    private var panDistance = OffsetCompat.Zero
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    var onViewTapListener: OnViewTapListener? = null
    var onViewLongPressListener: OnViewLongPressListener? = null

    init {
        gestureDetector = UnifiedGestureDetector(
            view = view,
            onActionDownCallback = {
                longPressExecuted = false
                doubleTapPressPoint = null
                oneFingerScaleExecuted = false
                panDistance = OffsetCompat.Zero
                coroutineScope.launch(Dispatchers.Main) {
                    zoomable.stopAllAnimation("onActionDown")
                }
            },
            onSingleTapConfirmedCallback = { e: MotionEvent ->
                val onViewTapListener = onViewTapListener
                if (onViewTapListener != null) {
                    onViewTapListener.onViewTap(view, OffsetCompat(x = e.x, y = e.y))
                    true
                } else {
                    view.performClick()
                }
            },
            onLongPressCallback = { e: MotionEvent ->
                // Once sliding occurs, the long press can no longer be triggered.
                val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
                if (panDistance.x < touchSlop && panDistance.y < touchSlop) {
                    val onViewLongPressListener = onViewLongPressListener
                    longPressExecuted = if (onViewLongPressListener != null) {
                        val longPressPoint = OffsetCompat(x = e.x, y = e.y)
                        onViewLongPressListener.onViewLongPress(view, longPressPoint)
                        true
                    } else if (view.isLongClickable) {
                        view.performLongClick()
                        true
                    } else {
                        false
                    }
                }
            },
            onDoubleTapPressCallback = { e: MotionEvent ->
                doubleTapPressPoint = OffsetCompat(x = e.x, y = e.y)
                true
            },
            onDoubleTapUpCallback = { e: MotionEvent ->
                doubleTapPressPoint = null
                val supportDoubleTapScale =
                    zoomable.checkSupportGestureType(GestureType.DOUBLE_TAP_SCALE)
                if (supportDoubleTapScale && !oneFingerScaleExecuted && !longPressExecuted) {
                    val touchPoint = OffsetCompat(x = e.x, y = e.y)
                    val centroidContentPoint = zoomable.touchPointToContentPoint(touchPoint)
                    coroutineScope.launch {
                        zoomable.switchScale(centroidContentPoint, animated = true)
                    }
                }
                true
            },
            canDrag = { horizontal: Boolean, direction: Int ->
                val supportDrag = zoomable.checkSupportGestureType(GestureType.ONE_FINGER_DRAG)
                val canScroll = zoomable.canScroll(horizontal, direction)
                val supportOneFingerScale =
                    zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                val doubleTapPressPoint = doubleTapPressPoint
                (supportDrag && canScroll) || (supportOneFingerScale && doubleTapPressPoint != null)
            },
            onGestureCallback = { scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int ->
                coroutineScope.launch {
                    panDistance += panChange.let { OffsetCompat(x = abs(it.x), y = abs(it.y)) }
                    val longPressExecuted = longPressExecuted
                    val doubleTapPressPoint = doubleTapPressPoint
                    val supportOneFingerScale =
                        zoomable.checkSupportGestureType(GestureType.ONE_FINGER_SCALE)
                    val supportTwoFingerScale =
                        zoomable.checkSupportGestureType(GestureType.TWO_FINGER_SCALE)
                    val supportDrag = zoomable.checkSupportGestureType(GestureType.ONE_FINGER_DRAG)
                    zoomable.logger.v {
                        "ZoomableEngine. onGesture. " +
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
                        val oneFingerScaleSpec = zoomable.oneFingerScaleSpecState.value
                        val scale = oneFingerScaleSpec.panToScaleTransformer.transform(panChange.y)
                        zoomable._continuousTransformTypeState.value =
                            ContinuousTransformType.GESTURE
                        zoomable.gestureTransform(
                            centroid = doubleTapPressPoint,
                            panChange = OffsetCompat.Zero,
                            zoomChange = scale,
                            rotationChange = 0f
                        )
                    } else {
                        oneFingerScaleExecuted = false
                        if (supportTwoFingerScale || supportDrag) {
                            val finalPan = if (supportDrag) panChange else OffsetCompat.Zero
                            val finalZoom = if (supportTwoFingerScale) scaleFactor else 1f
                            zoomable._continuousTransformTypeState.value =
                                ContinuousTransformType.GESTURE
                            zoomable.gestureTransform(
                                centroid = focus,
                                panChange = finalPan,
                                zoomChange = finalZoom,
                                rotationChange = 0f,
                            )
                        }
                    }
                }
            },
            onEndCallback = { focus, velocity ->
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
                        "ZoomableEngine. onEnd. " +
                                "focus=${focus.toShortString()}, " +
                                "velocity=${velocity.toShortString()}, " +
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
                            zoomable._continuousTransformTypeState.value = 0
                        }
                    } else {
                        val rollbackScaleExecuted =
                            supportTwoFingerScale && zoomable.rollbackScale(focus)
                        var flingExecuted = false
                        if (!rollbackScaleExecuted) {
                            flingExecuted = supportDrag && zoomable.fling(velocity)
                        }
                        if ((supportTwoFingerScale || supportDrag) && (!rollbackScaleExecuted && !flingExecuted)) {
                            zoomable._continuousTransformTypeState.value = 0
                        }
                    }
                }
            },
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}