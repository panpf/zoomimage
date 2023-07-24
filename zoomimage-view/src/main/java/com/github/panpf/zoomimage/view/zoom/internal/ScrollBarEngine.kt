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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import kotlin.math.roundToInt

class ScrollBarEngine(
    private val view: View,
    private val scrollBarSpec: ScrollBarSpec,
) {

    private val startAlpha: Int = 255
    private val roundCornersRadius: Int = (scrollBarSpec.size / 2).roundToInt()

    @Suppress("JoinDeclarationAndAssignment")
    private val cachePaint: Paint
    private val cacheRectF = RectF()
    private val fadeAnimatable: FloatAnimatable

    init {
        cachePaint = Paint().apply {
            color = scrollBarSpec.color
            alpha = startAlpha
        }

        val startValue = startAlpha.toFloat()
        val endValue = 0f
        fadeAnimatable = FloatAnimatable(
            view = view,
            startValue = startValue,
            endValue = endValue,
            durationMillis = 300,
            interpolator = DecelerateInterpolator(),
            onUpdateValue = { value ->
                cachePaint.alpha = value.roundToInt()
                view.invalidate()
            },
            onEnd = {}
        )
    }

    fun onDraw(
        canvas: Canvas,
        viewSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentVisibleRect: Rect
    ) {
        if (viewSize.isEmpty()) return
        if (contentSize.isEmpty()) return

        if (contentVisibleRect.width() < contentSize.width) {
            val widthScale = (viewSize.width - scrollBarSpec.margin * 4) / contentSize.width
            val left = (scrollBarSpec.margin * 2) + (contentVisibleRect.left * widthScale)
            val top = viewSize.height - scrollBarSpec.margin - scrollBarSpec.size
            val horScrollBarRectF = cacheRectF.apply {
                set(
                    /* left = */ left,
                    /* top = */ top,
                    /* right = */ left + contentVisibleRect.width() * widthScale,
                    /* bottom = */ top + scrollBarSpec.size
                )
            }
            canvas.drawRoundRect(
                /* rect = */ horScrollBarRectF,
                /* rx = */ roundCornersRadius.toFloat(),
                /* ry = */ roundCornersRadius.toFloat(),
                /* paint = */ cachePaint
            )
        }
        if (contentVisibleRect.height() < contentSize.height) {
            val heightScale = (viewSize.height - scrollBarSpec.margin * 4) / contentSize.height
            val verScrollBarRectF = cacheRectF.apply {
                val left = viewSize.width - scrollBarSpec.margin - scrollBarSpec.size
                val top = (scrollBarSpec.margin * 2) + (contentVisibleRect.top * heightScale)
                set(
                    /* left = */ left,
                    /* top = */ top,
                    /* right = */ left + scrollBarSpec.size,
                    /* bottom = */ top + contentVisibleRect.height() * heightScale
                )
            }
            canvas.drawRoundRect(
                /* rect = */ verScrollBarRectF,
                /* rx = */ roundCornersRadius.toFloat(),
                /* ry = */ roundCornersRadius.toFloat(),
                /* paint = */ cachePaint
            )
        }
    }

    fun onMatrixChanged() {
        cachePaint.alpha = startAlpha
        fadeAnimatable.restart(800)
    }

    fun cancel() {
        fadeAnimatable.stop()
    }
}