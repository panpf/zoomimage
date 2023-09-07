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
            onDragCallback = { panChange: OffsetCompat ->
//                zoomableEngine.transform(
//                    centroid = zoomableEngine.containerSize.toSize().center,
//                    panChange = panChange,
//                    zoomChange = 1f,
//                    rotationChange = 0f,
//                )
                zoomableEngine.drag(panChange)
            },
            onFlingCallback = { velocity: OffsetCompat ->
                zoomableEngine.fling(velocity)
            },
            onScaleCallback = { scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat ->
                zoomableEngine.transform(
                    centroid = focus,
                    panChange = panChange,
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