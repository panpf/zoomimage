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
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.util.AlignmentCompat
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.view.internal.applyTransform
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.zoom.internal.ImageViewBridge
import com.github.panpf.zoomimage.view.zoom.internal.ScrollBarEngine
import com.github.panpf.zoomimage.view.zoom.internal.UnifiedGestureDetector
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine2

class ZoomAbility2 constructor(
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
    internal val zoomEngine = ZoomEngine2(logger = this.logger, view = view)

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
    var mediumScaleMinMultiple: Float
        get() = zoomEngine.mediumScaleMinMultiple
        set(value) {
            zoomEngine.mediumScaleMinMultiple = value
        }
    var animationSpec: ZoomAnimationSpec
        get() = zoomEngine.animationSpec
        set(value) {
            zoomEngine.animationSpec = value
        }

    val scrollEdge: ScrollEdge
        get() = zoomEngine.scrollEdge

    val scaling: Boolean
        get() = zoomEngine.scaling
    val fling: Boolean
        get() = zoomEngine.fling

    val minScale: Float
        get() = zoomEngine.minScale
    val mediumScale: Float
        get() = zoomEngine.mediumScale
    val maxScale: Float
        get() = zoomEngine.maxScale

    val containerSize: IntSizeCompat
        get() = zoomEngine.containerSize
    val contentSize: IntSizeCompat
        get() = zoomEngine.contentSize
    val contentOriginSize: IntSizeCompat
        get() = zoomEngine.contentOriginSize
    var contentScale: ContentScaleCompat
        get() = zoomEngine.contentScale
        set(value) {
            zoomEngine.contentScale = value
        }
    var contentAlignment: AlignmentCompat
        get() = zoomEngine.contentAlignment
        set(value) {
            zoomEngine.contentAlignment = value
        }

    val containerVisibleRect: IntRectCompat
        get() = zoomEngine.containerVisibleRect
    val contentBaseDisplayRect: IntRectCompat
        get() = zoomEngine.contentBaseDisplayRect
    val contentBaseVisibleRect: IntRectCompat
        get() = zoomEngine.contentBaseVisibleRect
    val contentDisplayRect: IntRectCompat
        get() = zoomEngine.contentDisplayRect
    val contentVisibleRect: IntRectCompat
        get() = zoomEngine.contentVisibleRect

    val baseTransform: TransformCompat
        get() = zoomEngine.baseTransform
    val userTransform: TransformCompat
        get() = zoomEngine.userTransform
    val transform: TransformCompat
        get() = zoomEngine.transform

    init {
        val initScaleType = imageViewBridge.superGetScaleType()
        require(initScaleType != ScaleType.MATRIX) { "ScaleType cannot be MATRIX" }
        imageViewBridge.superSetScaleType(ScaleType.MATRIX)

        zoomEngine.scaleType = initScaleType
        zoomEngine.addOnMatrixChangeListener {
            val matrix = cacheImageMatrix.applyTransform(zoomEngine.transform)
            imageViewBridge.superSetImageMatrix(matrix)
            scrollBarEngine?.onMatrixChanged()
        }

        gestureDetector = UnifiedGestureDetector(
            view = view,
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
                zoomEngine.switchScale(
                    contentPoint = zoomEngine.touchPointToContentPoint(OffsetCompat(e.x, e.y)),
                    animated = true
                )
                true
            },
            onDragCallback = { dx: Float, dy: Float, scaling: Boolean ->
                if (!scaling) {
                    zoomEngine.transform(
                        centroid = zoomEngine.containerSize.toSize().center,    // todo 可能不行
                        panChange = OffsetCompat(dx, dy),
                        zoomChange = 1f,
                        rotationChange = 0f,
                    )
                }
            },
            onFlingCallback = { velocityX: Float, velocityY: Float ->
                zoomEngine.fling(velocityX, velocityY)
            },
            onScaleCallback = { scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float ->
                zoomEngine.transform(
                    centroid = OffsetCompat(x = focusX, y = focusY),
                    panChange = OffsetCompat(x = dx, y = dy),
                    zoomChange = scaleFactor,
                    rotationChange = 0f,
                )
            },
            onScaleBeginCallback = {
                zoomEngine.scaling = true
                true
            },
            onScaleEndCallback = {
                zoomEngine.scaling = false   // todo 这里可能跟 fling 冲突了
            },
            onActionDownCallback = {
                zoomEngine.stopAllAnimation("onActionDown")
            },
            onActionUpCallback = { zoomEngine.rollbackScale() },
            onActionCancelCallback = { zoomEngine.rollbackScale() },
            canDrag = { horizontal: Boolean, direction: Int ->
                zoomEngine.canScroll(horizontal, direction)
            }
        )

        resetDrawableSize()
        resetScrollBarHelper()
    }


    /**************************************** Internal ********************************************/

    private fun resetDrawableSize() {
        val drawable = imageViewBridge.getDrawable()
        zoomEngine.contentSize =
            drawable?.let { IntSizeCompat(it.intrinsicWidth, it.intrinsicHeight) }
                ?: IntSizeCompat.Zero
    }

    private fun resetScrollBarHelper() {
        scrollBarEngine?.cancel()
        scrollBarEngine = null
        val scrollBarSpec = this@ZoomAbility2.scrollBarSpec
        if (scrollBarSpec != null) {
            scrollBarEngine = ScrollBarEngine(view, scrollBarSpec)
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
        zoomEngine.contentOriginSize = size ?: IntSizeCompat.Zero
    }

    fun scale(
        targetScale: Float,
        contentPoint: IntOffsetCompat? = null,
        animated: Boolean = false
    ) {
        zoomEngine.scale(targetScale, contentPoint, animated)
    }

    fun offset(
        targetOffset: OffsetCompat,
        animated: Boolean = false
    ) {
        zoomEngine.offset(targetOffset, animated)
    }

    fun location(
        contentPoint: IntOffsetCompat,
        targetScale: Float = zoomEngine.transform.scaleX,
        animated: Boolean = false,
    ) {
        zoomEngine.location(contentPoint, targetScale, animated)
    }

    fun rotate(targetRotation: Int) {
        zoomEngine.rotate(targetRotation)
    }

    fun getNextStepScale(): Float = zoomEngine.getNextStepScale()

    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        zoomEngine.canScroll(horizontal, direction)

    fun touchPointToContentPoint(touchPoint: OffsetCompat): IntOffsetCompat {
        return zoomEngine.touchPointToContentPoint(touchPoint)
    }

    fun addOnMatrixChangeListener(listener: OnMatrixChangeListener) {
        zoomEngine.addOnMatrixChangeListener(listener)
    }

    fun removeOnMatrixChangeListener(listener: OnMatrixChangeListener): Boolean {
        return zoomEngine.removeOnMatrixChangeListener(listener)
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
        zoomEngine.containerSize = IntSizeCompat(viewWidth, viewHeight)
    }

    fun onDraw(canvas: Canvas) {
        scrollBarEngine?.onDraw(
            canvas = canvas,
            viewSize = zoomEngine.containerSize,
            contentSize = zoomEngine.contentSize,
            contentVisibleRect = zoomEngine.contentVisibleRect.let {
                Rect(it.left, it.top, it.right, it.bottom)
            },
        )
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