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
package com.github.panpf.zoomimage.view.zoom

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
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.view.zoom.internal.ImageViewBridge
import com.github.panpf.zoomimage.view.zoom.internal.UnifiedGestureDetector
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat

class ZoomAbility constructor(
    private val view: View,
    private val imageViewBridge: ImageViewBridge,
    logger: Logger,
) {
    // todo 不支持 center 等 ScaleType
    internal val engine: ZoomEngine
    private val imageMatrix = Matrix()
    private val unifiedGestureDetector: UnifiedGestureDetector
    private val logger = logger.newLogger(module = "ZoomAbility")

    private var onViewTapListenerList: MutableSet<OnViewTapListener>? = null
    private var onViewLongPressListenerList: MutableSet<OnViewLongPressListener>? = null

    init {
        val initScaleType = imageViewBridge.superGetScaleType()
        require(initScaleType != ScaleType.MATRIX) { "ScaleType cannot be MATRIX" }
        imageViewBridge.superSetScaleType(ScaleType.MATRIX)

        engine = ZoomEngine(logger = logger, view = view).apply {
            this.scaleType = initScaleType
        }
        resetDrawableSize()
        addOnMatrixChangeListener {
            val matrix = imageMatrix.apply { engine.getDisplayMatrix(this) }
            imageViewBridge.superSetImageMatrix(matrix)
        }

        unifiedGestureDetector = UnifiedGestureDetector(
            context = view.context,
            onDownCallback = { true },
            onSingleTapConfirmedCallback = { e: MotionEvent ->
                val onViewTapListenerList = onViewTapListenerList
                onViewTapListenerList?.forEach {
                    it.onViewTap(view, e.x, e.y)
                }
                onViewTapListenerList?.isNotEmpty() == true || view.performClick()
            },
            onLongPressCallback = { e: MotionEvent ->
                val onViewLongPressListenerList = onViewLongPressListenerList
                onViewLongPressListenerList?.forEach {
                    it.onViewLongPress(view, e.x, e.y)
                }
                onViewLongPressListenerList?.isNotEmpty() == true || view.performLongClick()
            },
            onDoubleTapCallback = { e: MotionEvent ->
                engine.doubleTap(e.x, e.y)
                true
            },
            onDragCallback = { dx: Float, dy: Float, scaling: Boolean ->
                if (!scaling) {
                    engine.doDrag(dx, dy)
                }
            },
            onFlingCallback = { velocityX: Float, velocityY: Float ->
                engine.doFling(velocityX, velocityY)
            },
            onScaleCallback = { scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float ->
                engine.doScale(scaleFactor, focusX, focusY, dx, dy)
            },
            onScaleBeginCallback = { engine.doScaleBegin() },
            onScaleEndCallback = { engine.doScaleEnd() },
            onActionDownCallback = { engine.actionDown() },
            onActionUpCallback = { engine.actionUp() },
            onActionCancelCallback = { engine.actionUp() },
        )
    }


    /*************************************** Interaction with consumers ******************************************/

    /**
     * Sets the dimensions of the original image, which is used to calculate the scale of double-click scaling
     */
    fun setImageSize(size: IntSizeCompat?) {
        engine.imageSize = size ?: IntSizeCompat.Zero
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

    var scrollBarSpec: ScrollBarSpec?
        get() = engine.scrollBarSpec
        set(value) {
            engine.scrollBarSpec = value
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

    val rotateDegrees: Int
        get() = engine.rotateDegrees

    val scrollEdge: ScrollEdge
        get() = engine.scrollEdge

    val isScaling: Boolean
        get() = engine.isScaling

    val userScale: Float
        get() = engine.userScale
    val userOffset: OffsetCompat
        get() = engine.userOffset

    val baseScale: ScaleFactorCompat
        get() = engine.baseScale
    val baseOffset: OffsetCompat
        get() = engine.baseOffset

    val scale: ScaleFactorCompat
        get() = engine.scale
    val offset: OffsetCompat
        get() = engine.offset

    val minScale: Float
        get() = engine.minScale
    val mediumScale: Float
        get() = engine.mediumScale
    val maxScale: Float
        get() = engine.maxScale

    val viewSize: IntSizeCompat
        get() = engine.viewSize
    val imageSize: IntSizeCompat
        get() = engine.imageSize
    val drawableSize: IntSizeCompat
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

    fun addOnViewTapListener(listener: OnViewTapListener) {
        this.onViewTapListenerList = (onViewTapListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnViewTapListener(listener: OnViewTapListener): Boolean {
        return onViewTapListenerList?.remove(listener) == true
    }

    fun addOnViewLongPressListener(listener: OnViewLongPressListener) {
        this.onViewLongPressListenerList = (onViewLongPressListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnViewLongPressListener(listener: OnViewLongPressListener): Boolean {
        return onViewLongPressListenerList?.remove(listener) == true
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
        engine.viewSize = IntSizeCompat(viewWidth, viewHeight)
    }

    fun onDraw(canvas: Canvas) {
        engine.onDraw(canvas)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        /* Location operations cannot be interrupted */
        if (engine.isLocationRunning()) {
            logger.d {
                "onTouchEvent. requestDisallowInterceptTouchEvent true. locating"
            }
            view.parent?.requestDisallowInterceptTouchEvent(true)
            return true
        }
        return unifiedGestureDetector.onTouchEvent(event)
    }

    fun setScaleType(scaleType: ScaleType): Boolean {
        engine.scaleType = scaleType
        return true
    }

    fun getScaleType(): ScaleType = engine.scaleType


    /**************************************** Internal ********************************************/

    private fun resetDrawableSize() {
        val drawable = imageViewBridge.getDrawable()
        engine.drawableSize =
            drawable?.let { IntSizeCompat(it.intrinsicWidth, it.intrinsicHeight) }
                ?: IntSizeCompat.Zero
    }

    private fun destroy() {
        engine.clean()
    }
}