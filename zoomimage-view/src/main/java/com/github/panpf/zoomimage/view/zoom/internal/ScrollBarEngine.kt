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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.core.view.ViewCompat
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import kotlin.math.abs
import kotlin.math.roundToInt

// todo 重构
class ScrollBarEngine(
    context: Context,
    private val zoomEngine: ZoomEngine,
    private val scrollBarSpec: ScrollBarSpec,
) {
    private val scrollBarRadius: Int = (scrollBarSpec.size / 2).roundToInt()
    private val scrollBarAlpha: Int = 255
    private val scrollBarPaint: Paint = Paint().apply {
        color = scrollBarSpec.color
        alpha = scrollBarAlpha
    }
    private val view = zoomEngine.view
    private val displayRectF = RectF()
    private val scrollBarRectF = RectF()
    private val fadeRunnable: FadeRunnable = FadeRunnable(context, this)
    private val delayFadeRunnable: DelayFadeRunnable = DelayFadeRunnable(this, fadeRunnable)

    fun onDraw(canvas: Canvas) {
        val displayRectF = displayRectF
            .apply { zoomEngine.getDisplayRect(this) }
            .takeIf { !it.isEmpty }
            ?: return
        val (viewWidth, viewHeight) = zoomEngine.viewSize.takeIf { !it.isEmpty() } ?: return
        val drawWidth = displayRectF.width()
        val drawHeight = displayRectF.height()
        val margin = scrollBarSpec.margin + scrollBarSpec.size + scrollBarSpec.margin
        val viewAvailableWidth = viewWidth - (margin * 2) - view.paddingLeft - view.paddingRight
        val viewAvailableHeight = viewHeight - (margin * 2) - view.paddingTop - view.paddingBottom

        // draw hor scroll bar
        if (drawWidth.toInt() > viewWidth) {
            val widthScale = viewWidth.toFloat() / drawWidth
            val horScrollBarWidth =
                (viewAvailableWidth * widthScale).coerceAtLeast(scrollBarSpec.size).toInt()
            val horScrollBarRectF = scrollBarRectF.apply {
                val mapLeft = if (displayRectF.left < 0) {
                    (abs(displayRectF.left) / displayRectF.width() * viewAvailableWidth).toInt()
                } else 0
                left = (view.paddingLeft + margin + mapLeft)
                right = left + horScrollBarWidth
                top = (view.paddingTop + margin + viewAvailableHeight + scrollBarSpec.margin)
                bottom = top + scrollBarSpec.size
            }
            canvas.drawRoundRect(
                horScrollBarRectF,
                scrollBarRadius.toFloat(),
                scrollBarRadius.toFloat(),
                scrollBarPaint
            )
        }

        // draw ver scroll bar
        if (drawHeight.toInt() > viewHeight) {
            val heightScale = viewHeight.toFloat() / drawHeight
            val verScrollBarHeight =
                (viewAvailableHeight * heightScale).coerceAtLeast(scrollBarSpec.size).toInt()
            val verScrollBarRectF = scrollBarRectF.apply {
                val mapTop = if (displayRectF.top < 0) {
                    (abs(displayRectF.top) / displayRectF.height() * viewAvailableHeight).toInt()
                } else 0
                left = (view.paddingLeft + margin + viewAvailableWidth + scrollBarSpec.margin)
                right = left + scrollBarSpec.size
                top = (view.paddingTop + margin + mapTop)
                bottom = top + verScrollBarHeight
            }
            canvas.drawRoundRect(
                verScrollBarRectF,
                scrollBarRadius.toFloat(),
                scrollBarRadius.toFloat(),
                scrollBarPaint
            )
        }
    }

    fun onMatrixChanged() {
        scrollBarPaint.alpha = scrollBarAlpha
        if (fadeRunnable.isRunning) {
            fadeRunnable.cancel()
        }
        delayFadeRunnable.start()
    }

    fun cancel() {
        delayFadeRunnable.cancel()
        fadeRunnable.cancel()
    }

    // todo 不再依赖 scrollBarEngine
    private class DelayFadeRunnable(
        val scrollBarEngine: ScrollBarEngine,
        val fadeRunnable: FadeRunnable
    ) : Runnable {

        override fun run() {
            fadeRunnable.start()
        }

        fun start() {
            cancel()
            scrollBarEngine.view.postDelayed(this, 800)
        }

        fun cancel() {
            scrollBarEngine.view.removeCallbacks(this)
        }
    }

    // todo 不再依赖 scrollBarEngine
    private class FadeRunnable(context: Context, val scrollBarEngine: ScrollBarEngine) : Runnable {

        private val scroller: Scroller = Scroller(context, DecelerateInterpolator())

        val isRunning: Boolean
            get() = !scroller.isFinished

        fun start() {
            cancel()

            val startX = scrollBarEngine.scrollBarAlpha
            val dx = -startX
            scroller.startScroll(startX, 0, dx, 0, 300)
            scrollBarEngine.view.post(this)
        }

        fun cancel() {
            scrollBarEngine.view.removeCallbacks(this)
            scroller.forceFinished(true)
        }

        override fun run() {
            if (!scroller.isFinished && scroller.computeScrollOffset()) {
                scrollBarEngine.scrollBarPaint.alpha = scroller.currX
                scrollBarEngine.view.invalidate()
                ViewCompat.postOnAnimation(scrollBarEngine.view, this)
            }
        }
    }
}