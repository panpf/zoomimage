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

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnGestureListener

internal class UnifiedGestureDetector(
    view: View,
    onActionDownCallback: ((ev: MotionEvent) -> Unit)? = null,
    onActionUpCallback: ((ev: MotionEvent) -> Unit)? = null,
    onActionCancelCallback: ((ev: MotionEvent) -> Unit)? = null,
    onSingleTapConfirmedCallback: (e: MotionEvent) -> Boolean,
    onLongPressCallback: (e: MotionEvent) -> Unit,
    onDoubleTapPressCallback: (e: MotionEvent) -> Boolean,
    onDoubleTapUpCallback: (e: MotionEvent) -> Boolean,
    canDrag: (horizontal: Boolean, direction: Int) -> Boolean,
    onGestureCallback: (scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int) -> Unit,
    onEndCallback: (focus: OffsetCompat, velocity: OffsetCompat) -> Unit,
) {

    private var doubleTapPressed = false

    private val actionGestureDetector =
        ActionGestureDetector(object : ActionGestureDetector.OnActionListener {
            override fun onActionDown(ev: MotionEvent) {
                if (ev.pointerCount == 1) {
                    doubleTapPressed = false
                }
                onActionDownCallback?.invoke(ev)
            }

            override fun onActionUp(ev: MotionEvent) {
                if (ev.pointerCount == 1 && doubleTapPressed) {
                    doubleTapPressed = false
                    onDoubleTapUpCallback(ev)
                }
                onActionUpCallback?.invoke(ev)
            }

            override fun onActionCancel(ev: MotionEvent) {
                onActionCancelCallback?.invoke(ev)
            }
        })

    private val tapGestureDetector =
        GestureDetector(view.context, object : SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return onSingleTapConfirmedCallback(e)
            }

            override fun onLongPress(e: MotionEvent) {
                onLongPressCallback(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                doubleTapPressed = true
                return onDoubleTapPressCallback(e)
            }
        })

    private val scaleDragGestureDetector =
        ScaleDragGestureDetector(view, canDrag, object : OnGestureListener {
            override fun onGesture(
                scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int
            ) {
                onGestureCallback(scaleFactor, focus, panChange, pointCount)
            }

            override fun onEnd(focus: OffsetCompat, velocity: OffsetCompat) {
                onEndCallback(focus, velocity)
            }
        })

    fun onTouchEvent(event: MotionEvent): Boolean {
        val actionConsumed = actionGestureDetector.onTouchEvent(event)
        val tapConsumed = tapGestureDetector.onTouchEvent(event)
        val scaleAndDragConsumed = scaleDragGestureDetector.onTouchEvent(event)
        return actionConsumed || tapConsumed || scaleAndDragConsumed
    }
}