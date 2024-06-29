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

import android.graphics.Rect
import android.view.View
import android.widget.OverScroller
import com.github.panpf.zoomimage.util.IntOffsetCompat

internal class FlingAnimatable(
    private val view: View,
    private val start: IntOffsetCompat,
    private val bounds: Rect?,
    private val velocity: IntOffsetCompat,
    private val onUpdateValue: (value: IntOffsetCompat) -> Unit,
    private val onEnd: () -> Unit = {}
) {

    private val scroller: OverScroller = OverScroller(view.context)
    private val runnable = Runnable { frame() }

    val running: Boolean
        get() = !scroller.isFinished

    fun start() {
        if (running) return
        scroller.fling(
            /* startX = */ start.x,
            /* startY = */ start.y,
            /* velocityX = */ velocity.x,
            /* velocityY = */ velocity.y,
            /* minX = */ bounds?.left ?: 0,
            /* maxX = */ bounds?.right ?: 0,
            /* minY = */ bounds?.top ?: 0,
            /* maxY = */ bounds?.bottom ?: 0,
            /* overX = */ 0,
            /* overY = */ 0
        )
        view.post(runnable)
    }

    fun stop() {
        if (!running) return
        view.removeCallbacks(runnable)
        scroller.forceFinished(true)
        onEnd()
    }

    private fun frame() {
        if (scroller.computeScrollOffset()) {
            onUpdateValue(IntOffsetCompat(scroller.currX, scroller.currY))
            view.postOnAnimation(runnable)
        } else {
            onEnd()
        }
    }
}