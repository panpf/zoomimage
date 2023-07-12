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
package com.github.panpf.zoomimage

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.SizeCompat
import com.github.panpf.zoomimage.view.ScrollBar
import com.github.panpf.zoomimage.view.ZoomAnimationSpec
import com.github.panpf.zoomimage.view.internal.ImageViewBridge
import com.github.panpf.zoomimage.view.internal.ZoomEngine
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat

class ZoomAbility(
    private val view: View,
    private val imageViewBridge: ImageViewBridge,
) {
    // todo 参考 ZoomImage 将 ZoomEngine 拆分成 State 和 手势处理两部分
    internal val engine: ZoomEngine
    private val imageMatrix = Matrix()
    val logger: Logger = Logger()

    init {
        val initScaleType = imageViewBridge.superGetScaleType()
        require(initScaleType != ScaleType.MATRIX) { "ScaleType cannot be MATRIX" }
        imageViewBridge.superSetScaleType(ScaleType.MATRIX)

        engine = ZoomEngine(view.context, logger, view).apply {
            this.scaleType = initScaleType
        }
        resetDrawableSize()
        addOnMatrixChangeListener {
            val matrix = imageMatrix.apply { engine.getDisplayMatrix(this) }
            imageViewBridge.superSetImageMatrix(matrix)
        }
    }


    /*************************************** Interaction with consumers ******************************************/

    /**
     * Sets the dimensions of the original image, which is used to calculate the scale of double-click scaling
     */
    fun setImageSize(size: SizeCompat?) {
        engine.imageSize = size ?: SizeCompat.Empty
    }

    /**
     * Locate to the location specified on the drawable image. You don't have to worry about scaling and rotation
     *
     * @param x Drawable the x coordinate on the diagram
     * @param y Drawable the y-coordinate on the diagram
     */
    fun location(x: Float, y: Float, animate: Boolean = false) {
        engine.location(x, y, animate)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     *
     * @param focalX  Scale the x coordinate of the center point on the drawable image
     * @param focalY  Scale the y coordinate of the center point on the drawable image
     */
    fun scale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        engine.scale(scale, focalX, focalY, animate)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     */
    fun scale(scale: Float, animate: Boolean = false) {
        engine.scale(scale, animate)
    }

    /**
     * Rotate the image to the specified degrees
     *
     * @param degrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateTo(degrees: Int) {
        engine.rotateTo(degrees)
    }

    /**
     * Rotate an degrees based on the current rotation degrees
     *
     * @param addDegrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateBy(addDegrees: Int) {
        engine.rotateBy(addDegrees)
    }

    fun getNextStepScale(): Float = engine.getNextStepScale()

    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        engine.canScroll(horizontal, direction)

    var threeStepScale: Boolean
        get() = engine.threeStepScale
        set(value) {
            engine.threeStepScale = value
        }

    var rubberBandScale: Boolean
        get() = engine.rubberBandScale
        set(value) {
            engine.rubberBandScale = value
        }

    var scrollBar: ScrollBar?
        get() = engine.scrollBar
        set(value) {
            engine.scrollBar = value
        }

    var readMode: ReadMode?
        get() = engine.readMode
        set(value) {
            engine.readMode = value
        }

    var defaultMediumScaleMultiple: Float
        get() = engine.defaultMediumScaleMultiple
        set(value) {
            engine.defaultMediumScaleMultiple = value
        }

    var animationSpec: ZoomAnimationSpec
        get() = engine.animationSpec
        set(value) {
            engine.animationSpec = value
        }

    var allowParentInterceptOnEdge: Boolean
        get() = engine.allowParentInterceptOnEdge
        set(value) {
            engine.allowParentInterceptOnEdge = value
        }

    var onViewLongPressListener: OnViewLongPressListener?
        get() = engine.onViewLongPressListener
        set(value) {
            engine.onViewLongPressListener = value
        }

    var onViewTapListener: OnViewTapListener?
        get() = engine.onViewTapListener
        set(value) {
            engine.onViewTapListener = value
        }

    val rotateDegrees: Int
        get() = engine.rotateDegrees

    val scrollEdge: ScrollEdge
        get() = engine.scrollEdge

    val isScaling: Boolean
        get() = engine.isScaling

    val scale: Float
        get() = engine.scale
    val offset: OffsetCompat
        get() = engine.offset

    val baseScale: ScaleFactorCompat
        get() = engine.baseScale
    val baseOffset: OffsetCompat
        get() = engine.baseOffset

    val displayScale: ScaleFactorCompat
        get() = engine.displayScale
    val displayOffset: OffsetCompat
        get() = engine.displayOffset

    val minScale: Float
        get() = engine.minScale
    val mediumScale: Float
        get() = engine.mediumScale
    val maxScale: Float
        get() = engine.maxScale

    val viewSize: SizeCompat
        get() = engine.viewSize
    val imageSize: SizeCompat
        get() = engine.imageSize
    val drawableSize: SizeCompat
        get() = engine.drawableSize

    fun getDisplayMatrix(matrix: Matrix) = engine.getDisplayMatrix(matrix)

    fun getDisplayRect(rectF: RectF) = engine.getDisplayRect(rectF)

    fun getDisplayRect(): RectF = engine.getDisplayRect()

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(rect: Rect) = engine.getVisibleRect(rect)

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(): Rect = engine.getVisibleRect()

    fun touchPointToDrawablePoint(touchPoint: PointF): Point? {
        return engine.touchPointToDrawablePoint(touchPoint)
    }

    fun addOnMatrixChangeListener(listener: OnMatrixChangeListener) {
        engine.addOnMatrixChangeListener(listener)
    }

    fun removeOnMatrixChangeListener(listener: OnMatrixChangeListener): Boolean {
        return engine.removeOnMatrixChangeListener(listener)
    }

    fun addOnRotateChangeListener(listener: OnRotateChangeListener) {
        engine.addOnRotateChangeListener(listener)
    }

    fun removeOnRotateChangeListener(listener: OnRotateChangeListener): Boolean {
        return engine.removeOnRotateChangeListener(listener)
    }

    fun addOnDragFlingListener(listener: OnDragFlingListener) {
        engine.addOnDragFlingListener(listener)
    }

    fun removeOnDragFlingListener(listener: OnDragFlingListener): Boolean {
        return engine.removeOnDragFlingListener(listener)
    }

    fun addOnScaleChangeListener(listener: OnScaleChangeListener) {
        engine.addOnScaleChangeListener(listener)
    }

    fun removeOnScaleChangeListener(listener: OnScaleChangeListener): Boolean {
        return engine.removeOnScaleChangeListener(listener)
    }

    fun addOnViewDragListener(listener: OnViewDragListener) {
        engine.addOnViewDragListener(listener)
    }

    fun removeOnViewDragListener(listener: OnViewDragListener): Boolean {
        return engine.removeOnViewDragListener(listener)
    }


    /**************************************** Interact with View ********************************************/

    @Suppress("UNUSED_PARAMETER")
    fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        destroy()
        if (view.isAttachedToWindowCompat) {
            resetDrawableSize()
        }
    }

    fun onAttachedToWindow() {
        resetDrawableSize()
    }

    fun onDetachedFromWindow() {
        destroy()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        val viewWidth = view.width - view.paddingLeft - view.paddingRight
        val viewHeight = view.height - view.paddingTop - view.paddingBottom
        engine.viewSize = SizeCompat(viewWidth, viewHeight)
    }

    fun onDraw(canvas: Canvas) {
        engine.onDraw(canvas)
    }

    fun onTouchEvent(event: MotionEvent): Boolean =
        engine.onTouchEvent(event)

    fun setScaleType(scaleType: ScaleType): Boolean {
        engine.scaleType = scaleType
        return true
    }

    fun getScaleType(): ScaleType = engine.scaleType


    /**************************************** Internal ********************************************/

    private fun resetDrawableSize() {
        val drawable = imageViewBridge.getDrawable()
        engine.drawableSize =
            drawable?.let { SizeCompat(it.intrinsicWidth, it.intrinsicHeight) } ?: SizeCompat.Empty
    }

    private fun destroy() {
        engine.clean()
    }
}