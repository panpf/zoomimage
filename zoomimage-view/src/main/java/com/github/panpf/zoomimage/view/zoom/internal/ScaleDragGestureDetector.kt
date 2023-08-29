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
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.view.internal.getPointerIndex
import java.lang.Float.isInfinite
import java.lang.Float.isNaN
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

// todo Refer to detectPowerfulTransformGestures to implement a unified drag-and-zoom gesture detector
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
    private val scaleDetector: ScaleGestureDetector

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var velocityTracker: VelocityTracker? = null
    private var activePointerId: Int = INVALID_POINTER_ID
    private var activePointerIndex: Int = 0

    private var isDragging = false
    private var canDragged = true

    var onActionListener: OnActionListener? = null

    init {
        val configuration = ViewConfiguration.get(view.context)
        minimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        touchSlop = configuration.scaledTouchSlop.toFloat()
        scaleDetector = ScaleGestureDetector(view.context, object : OnScaleGestureListener {
            private var lastFocus: OffsetCompat? = null
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor =
                    detector.scaleFactor.takeIf { !isNaN(it) && !isInfinite(it) } ?: return false
                if (scaleFactor >= 0) {
                    val lastFocus = this.lastFocus ?: OffsetCompat(detector.focusX, detector.focusY)
                    onGestureListener.onScale(
                        scaleFactor = scaleFactor,
                        focusX = detector.focusX,
                        focusY = detector.focusY,
                        dx = detector.focusX - lastFocus.x,
                        dy = detector.focusY - lastFocus.y
                    )
                    this.lastFocus = OffsetCompat(detector.focusX, detector.focusY)
                }
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                view.parent.requestDisallowInterceptTouchEvent(true)
                lastFocus = OffsetCompat(detector.focusX, detector.focusY)
                onGestureListener.onScaleBegin()
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                onGestureListener.onScaleEnd(lastFocus)
                lastFocus = null
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
            scaleDetector.onTouchEvent(ev)
            processTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            // Fix for support lib bug, happening when onDestroy is
        }
        return true
    }

    private fun processTouchEvent(ev: MotionEvent) {
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                view.parent.requestDisallowInterceptTouchEvent(true)
                canDragged = true
                isDragging = false
                activePointerId = ev.getPointerId(0)
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(ev)
                lastTouchX = getActiveX(ev)
                lastTouchY = getActiveY(ev)
                onActionListener?.onActionDown(ev)
            }

            MotionEvent.ACTION_MOVE -> {
                // Disable multi-finger drag, which can prevent the ViewPager from accidentally triggering left and right swipe when the minimum zoom ratio is zoomed in
                if (ev.pointerCount == 1) {
                    val x = getActiveX(ev)
                    val y = getActiveY(ev)
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
                    if (!isDragging) {
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
                        onGestureListener.onDrag(dx, dy)
                        lastTouchX = x
                        lastTouchY = y
                        velocityTracker?.addMovement(ev)
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Ignore deprecation, ACTION_POINTER_ID_MASK and
                // ACTION_POINTER_ID_SHIFT has same value and are deprecated
                // You can have either deprecation or lint target api warning
                val pointerIndex = getPointerIndex(ev.action)
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = ev.getPointerId(newPointerIndex)
                    lastTouchX = ev.getX(newPointerIndex)
                    lastTouchY = ev.getY(newPointerIndex)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                // Recycle Velocity Tracker
                velocityTracker?.recycle()
                velocityTracker = null
                onActionListener?.onActionCancel(ev)
            }

            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID
                if (isDragging) {
                    velocityTracker?.let { velocityTracker ->
                        lastTouchX = getActiveX(ev)
                        lastTouchY = getActiveY(ev)

                        // Compute velocity within the last 1000ms
                        velocityTracker.addMovement(ev)
                        velocityTracker.computeCurrentVelocity(1000)
                        val vX = velocityTracker.xVelocity
                        val vY = velocityTracker.yVelocity

                        // If the velocity is greater than minVelocity, call listener
                        if (max(abs(vX), abs(vY)) >= minimumVelocity) {
                            onGestureListener.onFling(vX, vY)
                        }
                    }
                }

                // Recycle Velocity Tracker
                velocityTracker?.recycle()
                velocityTracker = null
                onActionListener?.onActionUp(ev)
            }
        }

        activePointerIndex =
            ev.findPointerIndex(if (activePointerId != INVALID_POINTER_ID) activePointerId else 0)
    }

    interface OnActionListener {
        fun onActionDown(ev: MotionEvent)
        fun onActionUp(ev: MotionEvent)
        fun onActionCancel(ev: MotionEvent)
    }

    interface OnGestureListener {
        fun onDrag(dx: Float, dy: Float)
        fun onFling(velocityX: Float, velocityY: Float)
        fun onScale(scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float)
        fun onScaleBegin(): Boolean
        fun onScaleEnd(lastFocus: OffsetCompat?)
    }
}