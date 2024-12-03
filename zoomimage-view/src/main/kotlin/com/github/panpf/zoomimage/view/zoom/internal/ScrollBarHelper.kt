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

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlin.math.roundToInt

internal class ScrollBarHelper(
    private val view: View,
    private val scrollBarSpec: ScrollBarSpec,
    private val zoomableEngine: ZoomableEngine,
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

        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
            }

            override fun onViewDetachedFromWindow(v: View) {
                cancel()
            }
        })
    }

    fun onDraw(canvas: Canvas) {
        val containerSize =
            zoomableEngine.containerSizeState.value.takeIf { !it.isEmpty() } ?: return
        val contentSize = zoomableEngine.contentSizeState.value.takeIf { !it.isEmpty() } ?: return
        val contentVisibleRect = zoomableEngine.contentVisibleRectState.value
        val rotation = zoomableEngine.transformState.value.rotation.roundToInt()

        val rotatedContentVisibleRect = contentVisibleRect.rotateInSpace(contentSize, rotation)
        val rotatedContentSize = contentSize.rotate(rotation)
        val minLength = 10f * Resources.getSystem().displayMetrics.density
        if (rotatedContentVisibleRect.width < rotatedContentSize.width) {
            val widthScale =
                (containerSize.width - scrollBarSpec.margin * 4) / rotatedContentSize.width
            val left = (scrollBarSpec.margin * 2) + (rotatedContentVisibleRect.left * widthScale)
            val top = containerSize.height - scrollBarSpec.margin - scrollBarSpec.size
            val horScrollBarRectF = cacheRectF.apply {
                set(
                    /* left = */ left,
                    /* top = */ top,
                    /* right = */
                    left + (rotatedContentVisibleRect.width * widthScale).coerceAtLeast(minLength),
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
        if (rotatedContentVisibleRect.height < rotatedContentSize.height) {
            val heightScale =
                (containerSize.height - scrollBarSpec.margin * 4) / rotatedContentSize.height
            val verScrollBarRectF = cacheRectF.apply {
                val left = containerSize.width - scrollBarSpec.margin - scrollBarSpec.size
                val top = (scrollBarSpec.margin * 2) + (rotatedContentVisibleRect.top * heightScale)
                set(
                    /* left = */ left,
                    /* top = */ top,
                    /* right = */ left + scrollBarSpec.size,
                    /* bottom = */
                    top + (rotatedContentVisibleRect.height * heightScale).coerceAtLeast(minLength)
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