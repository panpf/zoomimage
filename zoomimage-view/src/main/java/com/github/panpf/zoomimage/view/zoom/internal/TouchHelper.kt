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

package com.github.panpf.zoomimage.view.zoom.internal

import android.view.MotionEvent
import android.view.View
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.OnViewTapListener
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class TouchHelper(view: View, zoomable: ZoomableEngine) {

    private val gestureDetector: UnifiedGestureDetector
    private var lastLongPressPoint: OffsetCompat? = null
    private var lastPointCount: Int = 0
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var onViewTapListener: OnViewTapListener? = null
    var onViewLongPressListener: OnViewLongPressListener? = null

    init {
        gestureDetector = UnifiedGestureDetector(
            view = view,
            onActionDownCallback = {
                if (zoomable.oneFingerScaleSpecState.value != null) {
                    lastLongPressPoint = null
                    lastPointCount = 0
                }
                zoomable.stopAllAnimation("onActionDown")
            },
            onDownCallback = { true },
            onSingleTapConfirmedCallback = { e: MotionEvent ->
                onViewTapListener?.onViewTap(view, e.x, e.y)
                onViewTapListener != null || view.performClick()
            },
            onLongPressCallback = { e: MotionEvent ->
                val oneFingerScaleSpec = zoomable.oneFingerScaleSpecState.value
                if (zoomable.isSupportGestureType(GestureType.ONE_FINGER_SCALE) && oneFingerScaleSpec != null) {
                    lastLongPressPoint = OffsetCompat(x = e.x, y = e.y)
                    coroutineScope.launch {
                        oneFingerScaleSpec.hapticFeedback.perform()
                    }
                }
                onViewLongPressListener?.onViewLongPress(view, e.x, e.y)
                onViewLongPressListener != null || view.performLongClick()
            },
            onDoubleTapCallback = { e: MotionEvent ->
                if (zoomable.isSupportGestureType(GestureType.DOUBLE_TAP_SCALE)) {
                    val touchPoint = OffsetCompat(x = e.x, y = e.y)
                    val centroidContentPoint = zoomable.touchPointToContentPoint(touchPoint)
                    zoomable.switchScale(centroidContentPoint, animated = true)
                }
                true
            },
            canDrag = { horizontal: Boolean, direction: Int ->
                val longPressPoint = lastLongPressPoint
                val allowDrag = zoomable.isSupportGestureType(GestureType.DRAG)
                val canScroll = zoomable.canScroll(horizontal, direction)
                val allowOneFingerScale =
                    zoomable.isSupportGestureType(GestureType.ONE_FINGER_SCALE)
                val oneFingerAlready = longPressPoint != null
                (allowDrag && canScroll) || (allowOneFingerScale && oneFingerAlready)
            },
            onGestureCallback = { scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int ->
                zoomable._continuousTransformTypeState.value = ContinuousTransformType.GESTURE

                lastPointCount = pointCount
                val longPressPoint = lastLongPressPoint
                val oneFingerScaleSpec = zoomable.oneFingerScaleSpecState.value
                if (pointCount == 1 && longPressPoint != null && oneFingerScaleSpec != null) {
                    if (zoomable.isSupportGestureType(GestureType.ONE_FINGER_SCALE)) {
                        val scale = oneFingerScaleSpec.panToScaleTransformer.transform(panChange.y)
                        zoomable.gestureTransform(
                            centroid = longPressPoint,
                            panChange = OffsetCompat.Zero,
                            zoomChange = scale,
                            rotationChange = 0f
                        )
                    }
                } else {
                    if (zoomable.isSupportGestureType(GestureType.TWO_FINGER_SCALE)) {
                        val finalPan =
                            if (zoomable.isSupportGestureType(GestureType.DRAG)) panChange else OffsetCompat.Zero
                        zoomable.gestureTransform(
                            centroid = focus,
                            panChange = finalPan,
                            zoomChange = scaleFactor,
                            rotationChange = 0f,
                        )
                    }
                }
            },
            onEndCallback = { focus, velocity ->
                if (zoomable.isSupportGestureType(GestureType.DRAG)) {
                    val pointCount = lastPointCount
                    val longPressPoint = lastLongPressPoint
                    val oneFingerScaleSpec = zoomable.oneFingerScaleSpecState.value
                    if (pointCount == 1 && longPressPoint != null && oneFingerScaleSpec != null) {
                        zoomable.rollbackScale(longPressPoint)
                    } else {
                        if (!zoomable.rollbackScale(focus)) {
                            if (!zoomable.fling(velocity)) {
                                zoomable._continuousTransformTypeState.value =
                                    ContinuousTransformType.NONE
                            }
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