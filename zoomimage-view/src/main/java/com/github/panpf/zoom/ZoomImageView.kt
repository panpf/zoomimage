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
import androidx.appcompat.widget.AppCompatImageView
import com.github.panpf.zoom.internal.ImageViewBridge

open class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle), ImageViewBridge {

    // Must be nullable, otherwise it will cause initialization in the constructor to fail
    private var _zoomAbility: ZoomAbility? = null
    val zoomAbility: ZoomAbility
        get() = _zoomAbility ?: throw IllegalStateException("zoomAbility not initialized")

    init {
        @Suppress("LeakingThis")
        _zoomAbility = ZoomAbility(this, this)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val oldDrawable = this.drawable
        super.setImageDrawable(drawable)
        val newDrawable = this.drawable
        if (oldDrawable !== newDrawable) {
            _zoomAbility?.onDrawableChanged(oldDrawable, newDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        val oldDrawable = this.drawable
        super.setImageURI(uri)
        val newDrawable = this.drawable
        if (oldDrawable !== newDrawable) {
            _zoomAbility?.onDrawableChanged(oldDrawable, newDrawable)
        }
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (_zoomAbility?.setScaleType(scaleType) != true) {
            super.setScaleType(scaleType)
        }
    }

    override fun getScaleType(): ScaleType {
        return _zoomAbility?.getScaleType() ?: super.getScaleType()
    }

    override fun setImageMatrix(matrix: Matrix?) {
        // intercept
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        _zoomAbility?.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _zoomAbility?.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _zoomAbility?.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        _zoomAbility?.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return _zoomAbility?.onTouchEvent(event) == true || super.onTouchEvent(event)
    }

    override fun canScrollHorizontally(direction: Int): Boolean =
        _zoomAbility?.canScrollHorizontally(direction) == true

    override fun canScrollVertically(direction: Int): Boolean =
        _zoomAbility?.canScrollVertically(direction) == true

    final override fun superSetImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
    }

    final override fun superSetScaleType(scaleType: ScaleType) {
        super.setScaleType(scaleType)
    }

    final override fun superGetScaleType(): ScaleType {
        return super.getScaleType()
    }
}