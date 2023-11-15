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

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnActionListener
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnGestureListener

internal class UnifiedGestureDetector(
    view: View,
    onActionDownCallback: ((ev: MotionEvent) -> Unit)? = null,
    onActionUpCallback: ((ev: MotionEvent) -> Unit)? = null,
    onActionCancelCallback: ((ev: MotionEvent) -> Unit)? = null,
    onSingleTapConfirmedCallback: (e: MotionEvent) -> Boolean,
    onLongPressCallback: (e: MotionEvent) -> Unit,
    onDoubleTapCallback: (e: MotionEvent) -> Boolean,
    canDrag: (horizontal: Boolean, direction: Int) -> Boolean,
    onGestureCallback: (scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int) -> Unit,
    onEndCallback: (focus: OffsetCompat, velocity: OffsetCompat) -> Unit,
) {

    private var doubleTapExecuted = false

    private val tapGestureDetector =
        GestureDetector(view.context, object : SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return onSingleTapConfirmedCallback(e)
            }

            override fun onLongPress(e: MotionEvent) {
                if (!doubleTapExecuted) {
                    onLongPressCallback(e)
                }
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                doubleTapExecuted = true
                return onDoubleTapCallback(e)
            }
        })

    private val scaleDragGestureDetector =
        ScaleDragGestureDetector(view, canDrag, object : OnGestureListener {
            override fun onGesture(
                scaleFactor: Float, focus: OffsetCompat, panChange: OffsetCompat, pointCount: Int
            ) {
                if (!doubleTapExecuted) {
                    onGestureCallback(scaleFactor, focus, panChange, pointCount)
                }
            }

            override fun onEnd(focus: OffsetCompat, velocity: OffsetCompat) {
                if (!doubleTapExecuted) {
                    onEndCallback(focus, velocity)
                }
            }
        }).apply {
            onActionListener = object : OnActionListener {
                override fun onActionDown(ev: MotionEvent) {
                    onActionDownCallback?.invoke(ev)
                }

                override fun onActionUp(ev: MotionEvent) {
                    onActionUpCallback?.invoke(ev)
                }

                override fun onActionCancel(ev: MotionEvent) {
                    onActionCancelCallback?.invoke(ev)
                }
            }
        }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN && event.pointerCount == 1) {
            doubleTapExecuted = false
        }
        val scaleAndDragConsumed = scaleDragGestureDetector.onTouchEvent(event)
        val tapConsumed = tapGestureDetector.onTouchEvent(event)
        return scaleAndDragConsumed || tapConsumed
    }
}