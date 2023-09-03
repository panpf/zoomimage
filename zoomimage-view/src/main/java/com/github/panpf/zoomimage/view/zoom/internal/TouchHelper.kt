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
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.OnViewTapListener
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine

class TouchHelper(view: View, zoomableEngine: ZoomableEngine) {

    private val gestureDetector: UnifiedGestureDetector
    var onViewTapListener: OnViewTapListener? = null
    var onViewLongPressListener: OnViewLongPressListener? = null

    init {
        gestureDetector = UnifiedGestureDetector(
            view = view,
            onDownCallback = { true },
            onSingleTapConfirmedCallback = { e: MotionEvent ->
                onViewTapListener?.onViewTap(view, e.x, e.y)
                onViewTapListener != null || view.performClick()
            },
            onLongPressCallback = { e: MotionEvent ->
                onViewLongPressListener?.onViewLongPress(view, e.x, e.y)
                onViewLongPressListener != null || view.performLongClick()
            },
            onDoubleTapCallback = { e: MotionEvent ->
                val touchPoint = OffsetCompat(x = e.x, y = e.y)
                val centroidContentPoint = zoomableEngine.touchPointToContentPoint(touchPoint)
                zoomableEngine.switchScale(
                    centroidContentPoint = centroidContentPoint,
                    animated = true
                )
                true
            },
            onDragCallback = { dx: Float, dy: Float ->
                zoomableEngine.transform(
                    centroid = zoomableEngine.containerSize.toSize().center,
                    panChange = OffsetCompat(dx, dy),
                    zoomChange = 1f,
                    rotationChange = 0f,
                )
            },
            onFlingCallback = { velocityX: Float, velocityY: Float ->
                zoomableEngine.fling(velocityX, velocityY)
            },
            onScaleCallback = { scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float ->
                zoomableEngine.transform(
                    centroid = OffsetCompat(x = focusX, y = focusY),
                    panChange = OffsetCompat(x = dx, y = dy),
                    zoomChange = scaleFactor,
                    rotationChange = 0f,
                )
            },
            onScaleBeginCallback = {
                zoomableEngine.transforming = true
                true
            },
            onScaleEndCallback = {
                zoomableEngine.transforming = false
                zoomableEngine.rollbackScale(it)
            },
            onActionDownCallback = {
                zoomableEngine.stopAllAnimation("onActionDown")
            },
            onActionUpCallback = { },
            onActionCancelCallback = { },
            canDrag = { horizontal: Boolean, direction: Int ->
                zoomableEngine.canScroll(horizontal, direction)
            }
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}