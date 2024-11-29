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
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.github.panpf.zoomimage.util.OffsetCompat
import java.lang.Float.isInfinite
import java.lang.Float.isNaN
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

// TODO Refer to detectPowerfulTransformGestures to implement a unified drag-and-zoom gesture detector
internal class ScaleDragGestureDetector(
    val view: View,
    val canDrag: (horizontal: Boolean, direction: Int) -> Boolean,
    val onGestureListener: OnGestureListener
) {
    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    private val touchSlop: Float
    private val minimumVelocity: Float
    private val scaleDetector: FasterScaleGestureDetector

    private var firstTouch: OffsetCompat? = null
    private var lastTouch: OffsetCompat? = null
    private var velocityTracker: VelocityTracker? = null
    private var activePointerId: Int = INVALID_POINTER_ID
    private var activePointerIndex: Int = 0

    private var isDragging = false
    private var canDragged = true
    private var singlePointLastFocus: OffsetCompat? = null
    private var multiPointLastFocus: OffsetCompat? = null
    private var multiPointCount = 0
    private var pointCount = 0

    init {
        val configuration = ViewConfiguration.get(view.context)
        minimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        touchSlop = configuration.scaledTouchSlop.toFloat()
        scaleDetector = FasterScaleGestureDetector(
            view.context,
            object : FasterScaleGestureDetector.OnScaleGestureListener {
                override fun onScale(detector: FasterScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                    .takeIf { !isNaN(it) && !isInfinite(it) }
                    ?: return false
                if (pointCount > 1 && scaleFactor >= 0) {
                    val multiPointLastFocus = this@ScaleDragGestureDetector.multiPointLastFocus
                        ?.takeIf { multiPointCount == pointCount }
                        ?: OffsetCompat(detector.focusX, detector.focusY)
                    val panChange = OffsetCompat(
                        detector.focusX - multiPointLastFocus.x,
                        detector.focusY - multiPointLastFocus.y
                    )
                    onGestureListener.onGesture(
                        scaleFactor = scaleFactor,
                        focus = OffsetCompat(detector.focusX, detector.focusY),
                        panChange = panChange,
                        pointCount
                    )
                    this@ScaleDragGestureDetector.multiPointLastFocus =
                        OffsetCompat(detector.focusX, detector.focusY)
                    this@ScaleDragGestureDetector.multiPointCount = pointCount
                }
                return true
            }

                override fun onScaleBegin(detector: FasterScaleGestureDetector): Boolean {
                view.parent.requestDisallowInterceptTouchEvent(true)
                return true
            }

                override fun onScaleEnd(detector: FasterScaleGestureDetector) {

            }
        })
    }

    private fun getActiveX(ev: MotionEvent): Float = try {
        ev.getX(activePointerIndex)
    } catch (e: Exception) {
        ev.x
    }

    private fun getActiveY(ev: MotionEvent): Float = try {
        ev.getY(activePointerIndex)
    } catch (e: Exception) {
        ev.y
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            processTouchEvent(ev)
            scaleDetector.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            // Fix for support lib bug, happening when onDestroy is
        }
        return true
    }

    private fun processTouchEvent(ev: MotionEvent) {
        pointCount = ev.pointerCount
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                view.parent.requestDisallowInterceptTouchEvent(true)
                canDragged = true
                isDragging = false
                activePointerId = ev.getPointerId(0)
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(ev)
                // Avoid changing from two fingers to one finger, lastTouchX and lastTouchY mutations, causing the image to pan instantly
                firstTouch = null
                singlePointLastFocus = null
                multiPointLastFocus = null
                multiPointCount = 0
            }

            MotionEvent.ACTION_MOVE -> {
                // Disable multi-finger drag, which can prevent the ViewPager from accidentally triggering left and right swipe when the minimum zoom ratio is zoomed in
                if (ev.pointerCount == 1) {
                    val touch = OffsetCompat(getActiveX(ev), getActiveY(ev))
                    // Avoid changing from two fingers to one finger, lastTouchX and lastTouchY mutations, causing the image to pan instantly
                    val firstTouch = firstTouch
                    val lastTouch = lastTouch
                    if (firstTouch != null && lastTouch != null) {
                        if (!isDragging) {
                            val d = touch - firstTouch
                            val dx = d.x
                            val dy = d.y
                            // Use Pythagoras to see if drag length is larger than touch slop
                            isDragging = sqrt((dx * dx) + (dy * dy).toDouble()) >= touchSlop
                            if (isDragging) {
                                canDragged = if (abs(dx) > abs(dy)) {
                                    dx != 0f && canDrag(true, if (dx > 0f) -1 else 1)
                                } else {
                                    dy != 0f && canDrag(false, if (dy > 0f) -1 else 1)
                                }
                                if (!canDragged) {
                                    view.parent.requestDisallowInterceptTouchEvent(false)
                                    isDragging = false
                                }
                            }
                        }
                        if (isDragging && canDragged) {
                            val panChange = touch - lastTouch
                            singlePointLastFocus = touch
                            onGestureListener.onGesture(
                                scaleFactor = 1f,
                                focus = touch,
                                panChange = panChange,
                                pointCount
                            )
                            velocityTracker?.addMovement(ev)
                        }
                    } else {
                        this@ScaleDragGestureDetector.firstTouch = touch
                    }
                    this@ScaleDragGestureDetector.lastTouch = touch
                } else {
                    firstTouch = null
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Ignore deprecation, ACTION_POINTER_ID_MASK and
                // ACTION_POINTER_ID_SHIFT has same value and are deprecated
                // You can have either deprecation or lint target api warning
                val pointerIndex = ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = ev.getPointerId(newPointerIndex)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                // Recycle Velocity Tracker
                velocityTracker?.recycle()
                velocityTracker = null

                val focus = singlePointLastFocus ?: multiPointLastFocus
                if (focus != null) {
                    onGestureListener.onEnd(focus, OffsetCompat.Zero)
                }
            }

            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID

                val velocity = velocityTracker?.takeIf { isDragging }?.let { velocityTracker ->
                    // Compute velocity within the last 1000ms
                    velocityTracker.addMovement(ev)
                    velocityTracker.computeCurrentVelocity(1000)
                    OffsetCompat(
                        x = velocityTracker.xVelocity,
                        y = velocityTracker.yVelocity,
                    )
                }?.takeIf {
                    max(abs(it.x), abs(it.y)) >= minimumVelocity
                } ?: OffsetCompat.Zero

                // Recycle Velocity Tracker
                velocityTracker?.recycle()
                velocityTracker = null

                val focus = singlePointLastFocus ?: multiPointLastFocus
                if (focus != null) {
                    onGestureListener.onEnd(focus, velocity)
                }
            }
        }

        activePointerIndex =
            ev.findPointerIndex(if (activePointerId != INVALID_POINTER_ID) activePointerId else 0)
    }

    interface OnGestureListener {

        fun onGesture(
            scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int
        )

        fun onEnd(focus: OffsetCompat, velocity: OffsetCompat)
    }
}