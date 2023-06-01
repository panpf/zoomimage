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

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView.ScaleType
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.findLastSketchDrawable
import com.github.panpf.zoom.Edge.NONE
import com.github.panpf.zoom.ScaleState.Factory
import com.github.panpf.zoom.internal.ImageViewBridge
import com.github.panpf.zoom.internal.ZoomerHelper
import com.github.panpf.zoom.internal.isAttachedToWindowCompat

class ZoomAbility(val view: View, val imageViewBridge: ImageViewBridge) {

    private var zoomerHelper: ZoomerHelper? = null
    private var onMatrixChangeListenerList: MutableSet<OnMatrixChangeListener>? = null
    private var onRotateChangeListenerList: MutableSet<OnRotateChangeListener>? = null
    private var onDragFlingListenerList: MutableSet<OnDragFlingListener>? = null
    private var onScaleChangeListenerList: MutableSet<OnScaleChangeListener>? = null
    private var onOnViewDragListenerList: MutableSet<OnViewDragListener>? = null
    private val imageMatrix = Matrix()


    var scrollBarEnabled: Boolean = true
        set(value) {
            field = value
            zoomerHelper?.scrollBarEnabled = value
        }
    var readModeEnabled: Boolean = false
        set(value) {
            field = value
            zoomerHelper?.readModeEnabled = value
        }
    var readModeDecider: ReadModeDecider? = null
        set(value) {
            field = value
            zoomerHelper?.readModeDecider = value
        }
    var scaleStateFactory: Factory? = null
        set(value) {
            if (field != value) {
                field = value
                zoomerHelper?.scaleStateFactory = value ?: DefaultScaleStateFactory()
            }
        }
    var scaleAnimationDuration: Int = 200
        set(value) {
            if (value > 0) {
                field = value
                zoomerHelper?.scaleAnimationDuration = value
            }
        }
    var scaleAnimationInterpolator: Interpolator? = null
        set(value) {
            if (field != value) {
                field = value
                zoomerHelper?.scaleAnimationInterpolator =
                    value ?: AccelerateDecelerateInterpolator()
            }
        }
    var allowParentInterceptOnEdge: Boolean = true
        set(value) {
            field = value
            zoomerHelper?.allowParentInterceptOnEdge = value
        }
    var onViewLongPressListener: OnViewLongPressListener? = null
        set(value) {
            field = value
            zoomerHelper?.onViewLongPressListener = value
        }
    var onViewTapListener: OnViewTapListener? = null
        set(value) {
            field = value
            zoomerHelper?.onViewTapListener = value
        }

    init {
        val newZoomerHelper = newZoomerHelper(view, imageViewBridge)
        zoomerHelper = newZoomerHelper
        resetDrawableSize()
        imageViewBridge.superSetScaleType(ScaleType.MATRIX)
        addOnMatrixChangeListener {
            val container = imageViewBridge
            val zoomer = zoomerHelper
            if (container != null && zoomer != null) {
                val matrix = imageMatrix.apply { zoomer.getDrawMatrix(this) }
                container.superSetImageMatrix(matrix)
            }
        }
    }


    /*************************************** Interaction ******************************************/

    /**
     * Locate to the location specified on the drawable image. You don't have to worry about scaling and rotation
     *
     * @param x Drawable the x coordinate on the diagram
     * @param y Drawable the y-coordinate on the diagram
     */
    fun location(x: Float, y: Float, animate: Boolean = false) {
        zoomerHelper?.location(x, y, animate)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     *
     * @param focalX  Scale the x coordinate of the center point on the drawable image
     * @param focalY  Scale the y coordinate of the center point on the drawable image
     */
    fun scale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        zoomerHelper?.scale(scale, focalX, focalY, animate)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     */
    fun scale(scale: Float, animate: Boolean = false) {
        zoomerHelper?.scale(scale, animate)
    }

    /**
     * Rotate the image to the specified degrees
     *
     * @param degrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateTo(degrees: Int) {
        zoomerHelper?.rotateTo(degrees)
    }

    /**
     * Rotate an degrees based on the current rotation degrees
     *
     * @param addDegrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateBy(addDegrees: Int) {
        zoomerHelper?.rotateBy(addDegrees)
    }


    /***************************************** Information ****************************************/

    val rotateDegrees: Int
        get() = zoomerHelper?.rotateDegrees ?: 0

    fun canScrollHorizontally(direction: Int): Boolean =
        zoomerHelper?.canScrollHorizontally(direction) == true

    fun canScrollVertically(direction: Int): Boolean =
        zoomerHelper?.canScrollVertically(direction) == true

    val horScrollEdge: Edge
        get() = zoomerHelper?.horScrollEdge ?: NONE

    val verScrollEdge: Edge
        get() = zoomerHelper?.verScrollEdge ?: NONE

    val scale: Float
        get() = zoomerHelper?.scale ?: 1f

    val baseScale: Float
        get() = zoomerHelper?.baseScale ?: 1f

    val supportScale: Float
        get() = zoomerHelper?.supportScale ?: 1f

    /** Zoom ratio that makes the image fully visible */
    val fullScale: Float
        get() = zoomerHelper?.fullScale ?: 1f

    /** Gets the zoom that fills the image with the ImageView display */
    val fillScale: Float
        get() = zoomerHelper?.fillScale ?: 1f

    /** Gets the scale that allows the image to be displayed at scale to scale */
    val originScale: Float
        get() = zoomerHelper?.originScale ?: 1f

    val minScale: Float
        get() = zoomerHelper?.minScale ?: 1f

    val maxScale: Float
        get() = zoomerHelper?.maxScale ?: 1f

    val stepScales: FloatArray?
        get() = zoomerHelper?.stepScales

    val isScaling: Boolean
        get() = zoomerHelper?.isScaling == true

    val imageSize: Size?
        get() = zoomerHelper?.imageSize

    val drawableSize: Size
        get() = zoomerHelper?.drawableSize ?: Size(0, 0)

    fun getDrawMatrix(matrix: Matrix) = zoomerHelper?.getDrawMatrix(matrix)

    fun getDrawRect(rectF: RectF) = zoomerHelper?.getDrawRect(rectF)

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(rect: Rect) = zoomerHelper?.getVisibleRect(rect)

    fun touchPointToDrawablePoint(touchPoint: PointF): Point? {
        return zoomerHelper?.touchPointToDrawablePoint(touchPoint)
    }

    fun addOnMatrixChangeListener(listener: OnMatrixChangeListener) {
        this.onMatrixChangeListenerList = (onMatrixChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
        zoomerHelper?.addOnMatrixChangeListener(listener)
    }

    fun removeOnMatrixChangeListener(listener: OnMatrixChangeListener): Boolean {
        zoomerHelper?.removeOnMatrixChangeListener(listener)
        return onMatrixChangeListenerList?.remove(listener) == true
    }

    fun addOnRotateChangeListener(listener: OnRotateChangeListener) {
        this.onRotateChangeListenerList = (onRotateChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
        zoomerHelper?.addOnRotateChangeListener(listener)
    }

    fun removeOnRotateChangeListener(listener: OnRotateChangeListener): Boolean {
        zoomerHelper?.removeOnRotateChangeListener(listener)
        return onRotateChangeListenerList?.remove(listener) == true
    }

    fun addOnDragFlingListener(listener: OnDragFlingListener) {
        this.onDragFlingListenerList = (onDragFlingListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
        zoomerHelper?.addOnDragFlingListener(listener)
    }

    fun removeOnDragFlingListener(listener: OnDragFlingListener): Boolean {
        zoomerHelper?.removeOnDragFlingListener(listener)
        return onDragFlingListenerList?.remove(listener) == true
    }

    fun addOnScaleChangeListener(listener: OnScaleChangeListener) {
        this.onScaleChangeListenerList = (onScaleChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
        zoomerHelper?.addOnScaleChangeListener(listener)
    }

    fun removeOnScaleChangeListener(listener: OnScaleChangeListener): Boolean {
        zoomerHelper?.removeOnScaleChangeListener(listener)
        return onScaleChangeListenerList?.remove(listener) == true
    }

    fun addOnViewDragListener(listener: OnViewDragListener) {
        this.onOnViewDragListenerList = (onOnViewDragListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
        zoomerHelper?.addOnViewDragListener(listener)
    }

    fun removeOnViewDragListener(listener: OnViewDragListener): Boolean {
        zoomerHelper?.removeOnViewDragListener(listener)
        return onOnViewDragListenerList?.remove(listener) == true
    }


    /**************************************** Internal ********************************************/

    fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        val imageView = view ?: return
        destroy()
        if (imageView.isAttachedToWindowCompat) {
            initialize()
        }
    }

    fun onAttachedToWindow() {
        initialize()
    }

    fun onDetachedFromWindow() {
        destroy()
    }

    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        val view = view ?: return
        val viewWidth = view.width - view.paddingLeft - view.paddingRight
        val viewHeight = view.height - view.paddingTop - view.paddingBottom
        zoomerHelper?.viewSize = Size(viewWidth, viewHeight)
    }

    fun onDraw(canvas: Canvas) {
        zoomerHelper?.onDraw(canvas)
    }

    fun onTouchEvent(event: MotionEvent): Boolean =
        zoomerHelper?.onTouchEvent(event) ?: false

    fun setScaleType(scaleType: ScaleType): Boolean {
        val zoomerHelper = zoomerHelper
        zoomerHelper?.scaleType = scaleType
        return zoomerHelper != null
    }

    fun getScaleType(): ScaleType? = zoomerHelper?.scaleType

    private fun initialize() {
        resetDrawableSize()
    }

    private fun destroy() {
        zoomerHelper?.clean()
    }

    private fun newZoomerHelper(
        view: View,
        imageViewBridge: ImageViewBridge
    ): ZoomerHelper {
        val scaleType = imageViewBridge.superGetScaleType()
        require(scaleType != ScaleType.MATRIX) {
            "ScaleType cannot be MATRIX"
        }
        return ZoomerHelper(
            context = view.context,
            logger = view.context.sketch.logger,
            view = view,
            scaleType = scaleType,
        ).apply {
            this@apply.readModeEnabled = this@ZoomAbility.readModeEnabled
            this@apply.readModeDecider = this@ZoomAbility.readModeDecider
            this@apply.scrollBarEnabled = this@ZoomAbility.scrollBarEnabled
            this@apply.scaleAnimationDuration = this@ZoomAbility.scaleAnimationDuration
            this@apply.allowParentInterceptOnEdge = this@ZoomAbility.allowParentInterceptOnEdge
            this@apply.onViewLongPressListener = this@ZoomAbility.onViewLongPressListener
            this@apply.onViewTapListener = this@ZoomAbility.onViewTapListener
            this@ZoomAbility.scaleAnimationInterpolator?.let {
                this@apply.scaleAnimationInterpolator = it
            }
            this@ZoomAbility.scaleStateFactory?.let {
                this@apply.scaleStateFactory = it
            }
            this@ZoomAbility.onMatrixChangeListenerList?.forEach {
                this@apply.addOnMatrixChangeListener(it)
            }
            this@ZoomAbility.onScaleChangeListenerList?.forEach {
                this@apply.addOnScaleChangeListener(it)
            }
            this@ZoomAbility.onRotateChangeListenerList?.forEach {
                this@apply.addOnRotateChangeListener(it)
            }
            this@ZoomAbility.onDragFlingListenerList?.forEach {
                this@apply.addOnDragFlingListener(it)
            }
        }
    }

    private fun resetDrawableSize() {
        val imageViewSuperBridge = imageViewBridge ?: return
        val zoomerHelper = zoomerHelper ?: return
        val drawable = imageViewSuperBridge.getDrawable()
        zoomerHelper.drawableSize =
            Size(drawable?.intrinsicWidth ?: 0, drawable?.intrinsicHeight ?: 0)
        val sketchDrawable = drawable?.findLastSketchDrawable()
        zoomerHelper.imageSize =
            Size(sketchDrawable?.imageInfo?.width ?: 0, sketchDrawable?.imageInfo?.height ?: 0)
    }
}