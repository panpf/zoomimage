/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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

import android.content.Context
import android.widget.OverScroller
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.toShortString
import kotlin.math.roundToInt

internal class FlingRunnable(
    context: Context,
    private val logger: Logger,
    private val engine: ZoomEngine,
    private val scaleDragHelper: ScaleDragHelper,
    private val velocityX: Int,
    private val velocityY: Int,
) : Runnable {

    private val scroller: OverScroller = OverScroller(context)

    @Suppress("unused")
    val isRunning: Boolean
        get() = !scroller.isFinished

    fun start() {
        cancel()

        val offset = engine.offset
        val startX = offset.x.roundToInt()
        val startY = offset.y.roundToInt()
        val bounds = computeOffsetBounds(
            containerSize = engine.viewSize,
            contentSize = engine.drawableSize,
            scaleType = engine.scaleType,
            scale = engine.scale
        )
        val minX: Int = bounds.left
        val maxX: Int = bounds.right
        val minY: Int = bounds.top
        val maxY: Int = bounds.bottom
        logger.d {
            "fling. start. " +
                    "velocity=($velocityX, $velocityY), " +
                    "start=($startX,$startY), " +
                    "min=($minX,$minY), " +
                    "max=($maxX,$maxY), " +
                    "offset=${offset.toShortString()}"
        }
        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0)
        engine.view.post(this)
    }

    fun cancel() {
        engine.view.removeCallbacks(this)
        scroller.forceFinished(true)
    }

    override fun run() {
        if (scroller.isFinished) {
            return  // remaining post that should not be handled
        }

        if (scroller.computeScrollOffset()) {
            val start = OffsetCompat(scroller.startX.toFloat(), scroller.startY.toFloat())
            scaleDragHelper.offsetTo(scroller.currX.toFloat(), scroller.currY.toFloat())
            val offset = engine.offset
            val distance = OffsetCompat(
                x = offset.x - start.x,
                y = offset.y - start.y
            )
            logger.d {
                "fling. running. " +
                        "velocity=($velocityX, $velocityY), " +
                        "start=${start.toShortString()}, " +
                        "offset=${offset.toShortString()}, " +
                        "distance=${distance.toShortString()}"
            }
            // Post On animation
            engine.view.postOnAnimation(this)
        }
    }
}