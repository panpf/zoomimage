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

internal class ActionGestureDetector(val onActionListener: OnActionListener) {

    fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            when (ev.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> onActionListener.onActionDown(ev)
                MotionEvent.ACTION_CANCEL -> onActionListener.onActionCancel(ev)
                MotionEvent.ACTION_UP -> onActionListener.onActionUp(ev)
            }
        } catch (e: IllegalArgumentException) {
            // Fix for support lib bug, happening when onDestroy is
        }
        return true
    }

    interface OnActionListener {
        fun onActionDown(ev: MotionEvent)
        fun onActionUp(ev: MotionEvent)
        fun onActionCancel(ev: MotionEvent)
    }
}