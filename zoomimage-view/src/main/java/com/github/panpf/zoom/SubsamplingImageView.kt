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
package com.github.panpf.zoom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.github.panpf.zoom.internal.ImageViewBridge

open class SubsamplingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle), ImageViewBridge {

    val zoomAbility: ZoomAbility
    val subsamplingAbility: SubsamplingAbility

    init {
        @Suppress("LeakingThis")
        zoomAbility = ZoomAbility(this, this)
        @Suppress("LeakingThis")
        subsamplingAbility = SubsamplingAbility(this, this, zoomAbility)
    }

    final override fun superSetImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
    }

    final override fun superGetImageMatrix(): Matrix {
        return super.getImageMatrix()
    }

    final override fun superSetScaleType(scaleType: ImageView.ScaleType) {
        super.setScaleType(scaleType)
    }

    final override fun superGetScaleType(): ImageView.ScaleType {
        return super.getScaleType()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val oldDrawable = this.drawable
        super.setImageDrawable(drawable)
        val newDrawable = this.drawable
        if (oldDrawable !== newDrawable) {
            zoomAbility.onDrawableChanged(oldDrawable, newDrawable)
            subsamplingAbility.onDrawableChanged(oldDrawable, newDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        val oldDrawable = this.drawable
        super.setImageURI(uri)
        val newDrawable = this.drawable
        if (oldDrawable !== newDrawable) {
            zoomAbility.onDrawableChanged(oldDrawable, newDrawable)
            subsamplingAbility.onDrawableChanged(oldDrawable, newDrawable)
        }
    }

    final override fun setScaleType(scaleType: ImageView.ScaleType) {
        if (!zoomAbility.setScaleType(scaleType)) {
            super.setScaleType(scaleType)
        }
    }

    final override fun getScaleType(): ImageView.ScaleType {
        return zoomAbility.getScaleType() ?: super.getScaleType()
    }

    final override fun setImageMatrix(matrix: Matrix?) {
        // intercept
    }

    final override fun getImageMatrix(): Matrix {
        return super.getImageMatrix()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        zoomAbility.onAttachedToWindow()
        subsamplingAbility.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        zoomAbility.onDetachedFromWindow()
        subsamplingAbility.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        zoomAbility.onSizeChanged(w, h, oldw, oldh)
        subsamplingAbility.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        subsamplingAbility.onDraw(canvas)
        zoomAbility.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return zoomAbility.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        subsamplingAbility.onVisibilityChanged(changedView, visibility)
    }

    override fun canScrollHorizontally(direction: Int): Boolean =
        zoomAbility.canScrollHorizontally(direction)

    override fun canScrollVertically(direction: Int): Boolean =
        zoomAbility.canScrollVertically(direction)
}