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
package com.github.panpf.zoomimage.sample.ui.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.github.panpf.sketch.resize.DefaultLongImageDecider
import com.github.panpf.tools4a.dimen.ktx.dp2pxF
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.isNotEmpty
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class ZoomImageMinimapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val tileBoundsPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f.dp2pxF
    }
    private val strokeHalfWidth = tileBoundsPaint.strokeWidth / 2
    private val mapVisibleRect = Rect()
    private val tileDrawRect = Rect()
    private var zoomView: ZoomImageView? = null

    private val detector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            location(e.x, e.y)
            return true
        }
    })

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewWidth = width.takeIf { it > 0 } ?: return
        val viewHeight = height.takeIf { it > 0 } ?: return
        val zoomView = zoomView ?: return
        val contentSize = zoomView.zoomAbility.contentSize.takeIf { !it.isEmpty() } ?: return

        val contentOriginSize = zoomView.zoomAbility.contentOriginSize
        if (contentOriginSize.isNotEmpty()) {
            val widthTargetScale = contentOriginSize.width.toFloat() / viewWidth
            val heightTargetScale = contentOriginSize.height.toFloat() / viewHeight
            val imageLoadRect = zoomView.subsamplingAbility.imageLoadRect
            zoomView.subsamplingAbility.tileList?.forEach { tile ->
                val load = tile.srcRect.overlaps(imageLoadRect)
                val tileBitmap = tile.bitmap
                val tileSrcRect = tile.srcRect
                val tileDrawRect = tileDrawRect.apply {
                    set(
                        floor((tileSrcRect.left / widthTargetScale) + strokeHalfWidth).toInt(),
                        floor((tileSrcRect.top / heightTargetScale) + strokeHalfWidth).toInt(),
                        ceil((tileSrcRect.right / widthTargetScale) - strokeHalfWidth).toInt(),
                        ceil((tileSrcRect.bottom / heightTargetScale) - strokeHalfWidth).toInt()
                    )
                }
                val boundsColor = when {
                    !load -> Color.parseColor("#00BFFF")
                    tileBitmap != null -> Color.GREEN
                    tile.loadJob?.isActive == true -> Color.YELLOW
                    else -> Color.RED
                }
                tileBoundsPaint.color = boundsColor
                canvas.drawRect(tileDrawRect, tileBoundsPaint)
            }
        }

        val contentVisibleRect = zoomView.zoomAbility.contentVisibleRect.takeIf { !it.isEmpty } ?: return
        val mapVisibleRect = mapVisibleRect.apply {
            val widthScaled = contentSize.width / viewWidth.toFloat()
            val heightScaled = contentSize.height / viewHeight.toFloat()
            set(
                floor(contentVisibleRect.left / widthScaled).toInt(),
                floor(contentVisibleRect.top / heightScaled).toInt(),
                ceil(contentVisibleRect.right / widthScaled).toInt(),
                ceil(contentVisibleRect.bottom / heightScaled).toInt()
            )
        }
        tileBoundsPaint.color = Color.MAGENTA
        canvas.drawRect(mapVisibleRect, tileBoundsPaint)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (zoomView != null && ViewCompat.isAttachedToWindow(this)) {
            resetViewSize("setImageDrawable")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (drawable != null && zoomView != null) {
            resetViewSize("onAttachedToWindow")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        return true
    }

    fun setZoomImageView(zoomView: ZoomImageView) {
        this.zoomView = zoomView
        zoomView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            post {
                if (drawable != null && ViewCompat.isAttachedToWindow(this)) {
                    resetViewSize("zoomView#addOnLayoutChangeListener")
                }
            }
        }
        zoomView.zoomAbility.registerOnTransformChangeListener {
            invalidate()
        }
        zoomView.subsamplingAbility.addOnTileChangedListener {
            invalidate()
        }
    }

    private fun resetViewSize(caller: String): Boolean {
        val drawable = drawable ?: return true
        val zoomView = zoomView ?: return true

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val containerWidth = zoomView.width
        val containerHeight = zoomView.height
        if (drawableWidth <= 0 || drawableHeight <= 0 || containerWidth <= 0 || containerHeight <= 0) {
            return false
        }
        val sameDirection =
            (drawableWidth >= drawableHeight && containerWidth >= containerHeight) ||
                    (drawableWidth < drawableHeight && containerWidth < containerHeight)
        val isLongImage = DefaultLongImageDecider()
            .isLongImage(drawableWidth, drawableHeight, containerWidth, containerHeight)
        val maxPercentage = if (isLongImage) 0.6f else if (sameDirection) 0.3f else 0.4f
        val maxWidth = containerWidth * maxPercentage
        val maxHeight = containerHeight * maxPercentage
        val scale = min(maxWidth / drawableWidth, maxHeight / drawableHeight)
        val viewWidth: Int = (drawableWidth * scale).roundToInt()
        val viewHeight: Int = (drawableHeight * scale).roundToInt()
        updateLayoutParams<LayoutParams> {
            width = viewWidth
            height = viewHeight
        }
        Log.d(
            "ZoomImageMinimapView",
            "resetViewSize:$caller. " +
                    "viewSize=${viewWidth}x${viewHeight}. " +
                    "drawableSize=${drawableWidth}x${drawableHeight}, " +
                    "containerSize=${containerWidth}x${containerHeight}"
        )
        return true
    }

    private fun location(x: Float, y: Float) {
        val zoomView = zoomView ?: return
        val viewWidth = width.takeIf { it > 0 } ?: return
        val viewHeight = height.takeIf { it > 0 } ?: return
        val drawable = zoomView.drawable
            ?.takeIf { it.intrinsicWidth != 0 && it.intrinsicHeight != 0 }
            ?: return

        val widthScale = drawable.intrinsicWidth.toFloat() / viewWidth
        val heightScale = drawable.intrinsicHeight.toFloat() / viewHeight
        val realX = (x * widthScale).roundToInt()
        val realY = (y * heightScale).roundToInt()

        zoomView.zoomAbility.location(
            contentPoint = IntOffsetCompat(x = realX, y = realY),
            targetScale = zoomView.zoomAbility.transform.scaleX
                .coerceAtLeast(zoomView.zoomAbility.mediumScale),
            animated = true
        )
    }
}
