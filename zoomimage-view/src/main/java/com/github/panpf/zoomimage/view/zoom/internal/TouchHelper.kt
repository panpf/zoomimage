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

internal class TouchHelper(view: View, zoomable: ZoomableEngine) {

    private val gestureDetector: UnifiedGestureDetector
    var onViewTapListener: OnViewTapListener? = null
    var onViewLongPressListener: OnViewLongPressListener? = null
    private var lastLongPressPoint: OffsetCompat? = null
    private var lastPointCount: Int = 0

    init {
        gestureDetector = UnifiedGestureDetector(
            view = view,
            onActionDownCallback = {
                if (zoomable.longPressSlideScaleSpecState.value != null) {
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
                val longPressSlideScaleSpec = zoomable.longPressSlideScaleSpecState.value
                if (longPressSlideScaleSpec != null) {
                    lastLongPressPoint = OffsetCompat(x = e.x, y = e.y)
                    longPressSlideScaleSpec.hapticFeedback.perform()
                }
                onViewLongPressListener?.onViewLongPress(view, e.x, e.y)
                onViewLongPressListener != null || view.performLongClick()
            },
            onDoubleTapCallback = { e: MotionEvent ->
                val touchPoint = OffsetCompat(x = e.x, y = e.y)
                val centroidContentPoint = zoomable.touchPointToContentPoint(touchPoint)
                zoomable.switchScale(centroidContentPoint, animated = true)
                true
            },
            canDrag = { horizontal: Boolean, direction: Int ->
                val longPressPoint = lastLongPressPoint
                longPressPoint != null || zoomable.canScroll(horizontal, direction)
            },
            onGestureCallback = { scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int ->
                zoomable._continuousTransformTypeState.value = ContinuousTransformType.GESTURE

                lastPointCount = pointCount
                val longPressPoint = lastLongPressPoint
                val longPressSlideScaleSpec = zoomable.longPressSlideScaleSpecState.value
                if (pointCount == 1 && longPressPoint != null && longPressSlideScaleSpec != null) {
                    val scale = longPressSlideScaleSpec.panToScaleTransformer.transform(panChange.y)
                    zoomable.gestureTransform(
                        centroid = longPressPoint,
                        panChange = OffsetCompat.Zero,
                        zoomChange = scale,
                        rotationChange = 0f
                    )
                } else {
                    zoomable.gestureTransform(
                        centroid = focus,
                        panChange = panChange,
                        zoomChange = scaleFactor,
                        rotationChange = 0f,
                    )
                }
            },
            onEndCallback = { focus, velocity ->
                val pointCount = lastPointCount
                val longPressPoint = lastLongPressPoint
                val longPressSlideScaleSpec = zoomable.longPressSlideScaleSpecState.value
                if (pointCount == 1 && longPressPoint != null && longPressSlideScaleSpec != null) {
                    zoomable.rollbackScale(longPressPoint)
                } else {
                    if (!zoomable.rollbackScale(focus)) {
                        if (!zoomable.fling(velocity)) {
                            zoomable._continuousTransformTypeState.value =
                                ContinuousTransformType.NONE
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