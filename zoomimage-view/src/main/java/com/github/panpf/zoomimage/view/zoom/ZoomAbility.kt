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
import com.github.panpf.zoomimage.core.IntOffsetCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.zoom.internal.ImageViewBridge
import com.github.panpf.zoomimage.view.zoom.internal.ScrollBarEngine
import com.github.panpf.zoomimage.view.zoom.internal.UnifiedGestureDetector
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine

// todo 不支持 center 等 ScaleType
class ZoomAbility constructor(
    private val view: View,
    private val imageViewBridge: ImageViewBridge,
    logger: Logger,
) {
    private val logger = logger.newLogger(module = "ZoomAbility")
    private var scrollBarEngine: ScrollBarEngine? = null
    private val gestureDetector: UnifiedGestureDetector
    private val cacheImageMatrix = Matrix()
    private var onViewTapListenerList: MutableSet<OnViewTapListener>? = null
    private var onViewLongPressListenerList: MutableSet<OnViewLongPressListener>? = null
    internal val zoomEngine = ZoomEngine(logger = this.logger, view = view)

    var scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default
        set(value) {
            if (field != value) {
                field = value
                resetScrollBarHelper()
            }
        }
    var threeStepScale: Boolean
        get() = zoomEngine.threeStepScale
        set(value) {
            zoomEngine.threeStepScale = value
        }
    var rubberBandScale: Boolean
        get() = zoomEngine.rubberBandScale
        set(value) {
            zoomEngine.rubberBandScale = value
        }
    var readMode: ReadMode?
        get() = zoomEngine.readMode
        set(value) {
            zoomEngine.readMode = value
        }
    var defaultMediumScaleMultiple: Float
        get() = zoomEngine.defaultMediumScaleMultiple
        set(value) {
            zoomEngine.defaultMediumScaleMultiple = value
        }
    var animationSpec: ZoomAnimationSpec
        get() = zoomEngine.animationSpec
        set(value) {
            zoomEngine.animationSpec = value
        }
    var allowParentInterceptOnEdge: Boolean
        get() = zoomEngine.allowParentInterceptOnEdge
        set(value) {
            zoomEngine.allowParentInterceptOnEdge = value
        }

    init {
        val initScaleType = imageViewBridge.superGetScaleType()
        require(initScaleType != ScaleType.MATRIX) { "ScaleType cannot be MATRIX" }
        imageViewBridge.superSetScaleType(ScaleType.MATRIX)

        zoomEngine.scaleType = initScaleType
        zoomEngine.addOnMatrixChangeListener {
            imageViewBridge.superSetImageMatrix(
                cacheImageMatrix.apply { zoomEngine.getDisplayMatrix(this) }
            )
            scrollBarEngine?.onMatrixChanged()
        }

        gestureDetector = UnifiedGestureDetector(
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
                zoomEngine.doubleTap(e.x, e.y)
                true
            },
            onDragCallback = { dx: Float, dy: Float, scaling: Boolean ->
                if (!scaling) {
                    zoomEngine.doDrag(dx, dy)
                }
            },
            onFlingCallback = { velocityX: Float, velocityY: Float ->
                zoomEngine.doFling(velocityX, velocityY)
            },
            onScaleCallback = { scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float ->
                zoomEngine.doScale(scaleFactor, focusX, focusY, dx, dy)
            },
            onScaleBeginCallback = { zoomEngine.doScaleBegin() },
            onScaleEndCallback = { zoomEngine.doScaleEnd() },
            onActionDownCallback = { zoomEngine.actionDown() },
            onActionUpCallback = { zoomEngine.actionUp() },
            onActionCancelCallback = { zoomEngine.actionUp() },
        )

        resetDrawableSize()
        resetScrollBarHelper()
    }


    /**************************************** Internal ********************************************/

    private fun resetDrawableSize() {
        val drawable = imageViewBridge.getDrawable()
        zoomEngine.drawableSize =
            drawable?.let { IntSizeCompat(it.intrinsicWidth, it.intrinsicHeight) }
                ?: IntSizeCompat.Zero
    }

    private fun resetScrollBarHelper() {
        scrollBarEngine?.cancel()
        scrollBarEngine = null
        val scrollBarSpec = this@ZoomAbility.scrollBarSpec
        if (scrollBarSpec != null) {
            scrollBarEngine = ScrollBarEngine(view.context, zoomEngine, scrollBarSpec)
        }
    }

    private fun destroy() {
        zoomEngine.clean()
    }


    /*************************************** Interaction with consumers ******************************************/

    /**
     * Sets the dimensions of the original image, which is used to calculate the scale of double-click scaling
     */
    fun setImageSize(size: IntSizeCompat?) {
        zoomEngine.imageSize = size ?: IntSizeCompat.Zero
    }

    /**
     * Locate to the location specified on the drawable image. You don't have to worry about scaling and rotation
     *
     * @param x Drawable the x coordinate on the diagram
     * @param y Drawable the y-coordinate on the diagram
     */
    fun location(
        offsetOfContent: IntOffsetCompat,
        targetScale: Float = scale.scaleX,
        animated: Boolean = false
    ) {
        zoomEngine.location(offsetOfContent, targetScale, animated)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     *
     * @param focalX  Scale the x coordinate of the center point on the drawable image
     * @param focalY  Scale the y coordinate of the center point on the drawable image
     */
    fun scale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        zoomEngine.scale(scale, focalX, focalY, animate)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     */
    fun scale(scale: Float, animate: Boolean = false) {
        zoomEngine.scale(scale, animate)
    }

    /**
     * Rotate the image to the specified degrees
     *
     * @param degrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateTo(degrees: Int) {
        zoomEngine.rotateTo(degrees)
    }

    /**
     * Rotate an degrees based on the current rotation degrees
     *
     * @param addDegrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateBy(addDegrees: Int) {
        zoomEngine.rotateBy(addDegrees)
    }

    fun getNextStepScale(): Float = zoomEngine.getNextStepScale()

    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        zoomEngine.canScroll(horizontal, direction)

    val rotateDegrees: Int
        get() = zoomEngine.rotateDegrees

    val scrollEdge: ScrollEdge
        get() = zoomEngine.scrollEdge

    val isScaling: Boolean
        get() = zoomEngine.isScaling

    val userScale: Float
        get() = zoomEngine.userScale
    val userOffset: OffsetCompat
        get() = zoomEngine.userOffset

    val baseScale: ScaleFactorCompat
        get() = zoomEngine.baseScale
    val baseOffset: OffsetCompat
        get() = zoomEngine.baseOffset

    val scale: ScaleFactorCompat
        get() = zoomEngine.scale
    val offset: OffsetCompat
        get() = zoomEngine.offset

    val minScale: Float
        get() = zoomEngine.minScale
    val mediumScale: Float
        get() = zoomEngine.mediumScale
    val maxScale: Float
        get() = zoomEngine.maxScale

    val viewSize: IntSizeCompat
        get() = zoomEngine.viewSize
    val imageSize: IntSizeCompat
        get() = zoomEngine.imageSize
    val drawableSize: IntSizeCompat
        get() = zoomEngine.drawableSize

    fun getDisplayMatrix(matrix: Matrix) = zoomEngine.getDisplayMatrix(matrix)

    fun getDisplayRect(rectF: RectF) = zoomEngine.getDisplayRect(rectF)

    fun getDisplayRect(): RectF = zoomEngine.getDisplayRect()

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(rect: Rect) = zoomEngine.getVisibleRect(rect)

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(): Rect = zoomEngine.getVisibleRect()

    fun touchPointToDrawablePoint(touchPoint: PointF): Point? {
        return zoomEngine.touchPointToDrawablePoint(touchPoint)
    }

    fun addOnMatrixChangeListener(listener: OnMatrixChangeListener) {
        zoomEngine.addOnMatrixChangeListener(listener)
    }

    fun removeOnMatrixChangeListener(listener: OnMatrixChangeListener): Boolean {
        return zoomEngine.removeOnMatrixChangeListener(listener)
    }

    fun addOnRotateChangeListener(listener: OnRotateChangeListener) {
        zoomEngine.addOnRotateChangeListener(listener)
    }

    fun removeOnRotateChangeListener(listener: OnRotateChangeListener): Boolean {
        return zoomEngine.removeOnRotateChangeListener(listener)
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
    }

    fun onDetachedFromWindow() {
        destroy()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        val viewWidth = view.width - view.paddingLeft - view.paddingRight
        val viewHeight = view.height - view.paddingTop - view.paddingBottom
        zoomEngine.viewSize = IntSizeCompat(viewWidth, viewHeight)
    }

    fun onDraw(canvas: Canvas) {
        scrollBarEngine?.onDraw(canvas)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    fun setScaleType(scaleType: ScaleType): Boolean {
        zoomEngine.scaleType = scaleType
        return true
    }

    fun getScaleType(): ScaleType = zoomEngine.scaleType
}