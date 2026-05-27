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
import androidx.core.graphics.Insets
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.round
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
    private var insets: Insets? = null

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
        val contentVisibleRect = zoomableEngine.contentVisibleRectFState.value.round()
        val rotation = zoomableEngine.transformState.value.rotation.roundToInt()

        val rotatedContentVisibleRect = contentVisibleRect.rotateInSpace(contentSize, rotation)
        val rotatedContentSize = contentSize.rotate(rotation)
        val minLength = 10f * Resources.getSystem().displayMetrics.density
        val insets = if (scrollBarSpec.enabledWindowInsets) this.insets else null
        if (rotatedContentVisibleRect.width < rotatedContentSize.width) {
            val horScrollBarRectF = cacheRectF.apply {
                val leftSpace = (insets?.left ?: 0) + scrollBarSpec.margin * 2
                val rightSpace = (insets?.right ?: 0) + scrollBarSpec.margin * 2
                val bottomSpace = (insets?.bottom ?: 0) + scrollBarSpec.margin
                val validContainerWidth = containerSize.width - leftSpace - rightSpace
                val widthScale = validContainerWidth / rotatedContentSize.width
                val mappedRotatedContentVisibleLeft = rotatedContentVisibleRect.left * widthScale
                val mappedRotatedContentVisibleWidth =
                    (rotatedContentVisibleRect.width * widthScale).coerceAtLeast(minLength)
                val left = leftSpace + mappedRotatedContentVisibleLeft
                val right = left + mappedRotatedContentVisibleWidth
                val top = containerSize.height - bottomSpace - scrollBarSpec.size
                val bottom = top + scrollBarSpec.size
                set(/* left = */ left,/* top = */ top,/* right = */ right,/* bottom = */ bottom)
            }
            canvas.drawRoundRect(
                /* rect = */ horScrollBarRectF,
                /* rx = */ roundCornersRadius.toFloat(),
                /* ry = */ roundCornersRadius.toFloat(),
                /* paint = */ cachePaint
            )
        }
        if (rotatedContentVisibleRect.height < rotatedContentSize.height) {
            val verScrollBarRectF = cacheRectF.apply {
                val topSpace = (insets?.top ?: 0) + scrollBarSpec.margin * 2
                val bottomSpace = (insets?.bottom ?: 0) + scrollBarSpec.margin * 2
                val rightSpace = (insets?.right ?: 0) + scrollBarSpec.margin
                val validContainerHeight = containerSize.height - topSpace - bottomSpace
                val heightScale = validContainerHeight / rotatedContentSize.height
                val mappedRotatedContentVisibleTop = rotatedContentVisibleRect.top * heightScale
                val mappedRotatedContentVisibleHeight =
                    (rotatedContentVisibleRect.height * heightScale).coerceAtLeast(minLength)
                val top = topSpace + mappedRotatedContentVisibleTop
                val bottom = top + mappedRotatedContentVisibleHeight
                val left = containerSize.width - rightSpace - scrollBarSpec.size
                val right = left + scrollBarSpec.size
                set(/* left = */ left,/* top = */ top,/* right = */ right,/* bottom = */ bottom)
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

    fun setInsets(insets: Insets?) {
        this.insets = insets
        view.invalidate()
    }
}