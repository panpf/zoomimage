/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.view.internal.applyTransform
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.internal.toAlignment
import com.github.panpf.zoomimage.view.internal.toContentScale
import com.github.panpf.zoomimage.view.subsampling.internal.SubsamplingEngine
import com.github.panpf.zoomimage.view.zoom.internal.ImageViewBridge
import com.github.panpf.zoomimage.view.zoom.internal.ScrollBarEngine
import com.github.panpf.zoomimage.view.zoom.internal.UnifiedGestureDetector
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import kotlin.math.roundToInt

/**
 * Wrap [ZoomEngine] and connect [ZoomEngine] and [ImageView]
 */
class ZoomAbility constructor(
    private val view: View,
    private val imageViewBridge: ImageViewBridge,
    logger: Logger,
) {

    val logger = logger.newLogger(module = "ZoomAbility")
    internal val zoomEngine = ZoomEngine(logger = this.logger, view = view)
    private var scrollBarEngine: ScrollBarEngine? = null
    private val gestureDetector: UnifiedGestureDetector
    private val cacheImageMatrix = Matrix()
    private var onViewTapListenerList: MutableSet<OnViewTapListener>? = null
    private var onViewLongPressListenerList: MutableSet<OnViewLongPressListener>? = null
    private var scaleType: ScaleType = ScaleType.FIT_CENTER
        set(value) {
            if (field != value) {
                field = value
                zoomEngine.contentScale = value.toContentScale()
                zoomEngine.alignment = value.toAlignment()
            }
        }

    /**
     * The size of the container that holds the content, this is usually the size of the [ZoomImageView] component
     */
    val containerSize: IntSizeCompat
        get() = zoomEngine.containerSize

    /**
     * The size of the content, usually Painter.intrinsicSize.round(), setup by the [ZoomImageView] component
     */
    val contentSize: IntSizeCompat
        get() = zoomEngine.contentSize

    /**
     * The original size of the content, it is usually set by [SubsamplingEngine] after parsing the original size of the image
     */
    val contentOriginSize: IntSizeCompat
        get() = zoomEngine.contentOriginSize


    /* *********************************** Configurable properties ****************************** */

    /**
     * The scale of the content, usually set by [ZoomImageView] component
     */
    var contentScale: ContentScaleCompat
        get() = zoomEngine.contentScale
        set(value) {
            zoomEngine.contentScale = value
        }

    /**
     * The alignment of the content, usually set by [ZoomImageView] component
     */
    var alignment: AlignmentCompat
        get() = zoomEngine.alignment
        set(value) {
            zoomEngine.alignment = value
        }

    /**
     * Setup whether to enable read mode and configure read mode
     */
    var readMode: ReadMode?
        get() = zoomEngine.readMode
        set(value) {
            zoomEngine.readMode = value
        }

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    var scalesCalculator: ScalesCalculator
        get() = zoomEngine.scalesCalculator
        set(value) {
            zoomEngine.scalesCalculator = value
        }

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    var threeStepScale: Boolean
        get() = zoomEngine.threeStepScale
        set(value) {
            zoomEngine.threeStepScale = value
        }

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    var rubberBandScale: Boolean
        get() = zoomEngine.rubberBandScale
        set(value) {
            zoomEngine.rubberBandScale = value
        }

    /**
     * The animation configuration for the zoom animation
     */
    var animationSpec: ZoomAnimationSpec
        get() = zoomEngine.animationSpec
        set(value) {
            zoomEngine.animationSpec = value
        }

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    var limitOffsetWithinBaseVisibleRect: Boolean
        get() = zoomEngine.limitOffsetWithinBaseVisibleRect
        set(value) {
            zoomEngine.limitOffsetWithinBaseVisibleRect = value
        }

    /**
     * Setup whether to enable scroll bar and configure scroll bar style
     */
    var scrollBar: ScrollBarSpec? = ScrollBarSpec.Default
        set(value) {
            if (field != value) {
                field = value
                resetScrollBarHelper()
            }
        }


    /* *********************************** Information properties ******************************* */

    /**
     * Base transformation, include the base scale, offset, rotation,
     * which is affected by [contentScale], [alignment] properties and [rotate] method
     */
    val baseTransform: TransformCompat
        get() = zoomEngine.baseTransform

    /**
     * User transformation, include the user scale, offset, rotation,
     * which is affected by the user's gesture, [readMode] properties and [scale], [offset], [locate] method
     */
    val userTransform: TransformCompat
        get() = zoomEngine.userTransform

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of [baseTransform] and [userTransform]
     */
    val transform: TransformCompat
        get() = zoomEngine.transform

    /**
     * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    val minScale: Float
        get() = zoomEngine.minScale

    /**
     * Medium scale factor, only as a target value for one of when switch scale
     */
    val mediumScale: Float
        get() = zoomEngine.mediumScale

    /**
     * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    val maxScale: Float
        get() = zoomEngine.maxScale

    /**
     * If true, a transformation is currently in progress, possibly in a continuous gesture operation, or an animation is in progress
     */
    val transforming: Boolean
        get() = zoomEngine.transforming

    /**
     * The content region in the container after the baseTransform transformation
     */
    val contentBaseDisplayRect: IntRectCompat
        get() = zoomEngine.contentBaseDisplayRect

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    val contentBaseVisibleRect: IntRectCompat
        get() = zoomEngine.contentBaseVisibleRect

    /**
     * The content region in the container after the final transform transformation
     */
    val contentDisplayRect: IntRectCompat
        get() = zoomEngine.contentDisplayRect

    /**
     * The content is visible region to the user after the final transform transformation
     */
    val contentVisibleRect: IntRectCompat
        get() = zoomEngine.contentVisibleRect

    /**
     * Edge state for the current offset
     */
    val scrollEdge: ScrollEdge
        get() = zoomEngine.scrollEdge

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    val userOffsetBounds: IntRectCompat
        get() = zoomEngine.userOffsetBounds

    init {
        val initScaleType = imageViewBridge.superGetScaleType()
        require(initScaleType != ScaleType.MATRIX) { "ScaleType cannot be MATRIX" }
        imageViewBridge.superSetScaleType(ScaleType.MATRIX)

        scaleType = initScaleType
        zoomEngine.registerOnTransformChangeListener {
            val matrix =
                cacheImageMatrix.applyTransform(zoomEngine.transform, zoomEngine.containerSize)
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
                val touchPoint = OffsetCompat(x = e.x, y = e.y)
                val centroidContentPoint = zoomEngine.touchPointToContentPoint(touchPoint)
                switchScale(centroidContentPoint = centroidContentPoint, animated = true)
                true
            },
            onDragCallback = { dx: Float, dy: Float ->
                zoomEngine.transform(
                    centroid = zoomEngine.containerSize.toSize().center,
                    panChange = OffsetCompat(dx, dy),
                    zoomChange = 1f,
                    rotationChange = 0f,
                )
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
                zoomEngine.transforming = true
                true
            },
            onScaleEndCallback = {
                zoomEngine.transforming = false
                zoomEngine.rollbackScale(it)
            },
            onActionDownCallback = {
                zoomEngine.stopAllAnimation("onActionDown")
            },
            onActionUpCallback = { },
            onActionCancelCallback = { },
            canDrag = { horizontal: Boolean, direction: Int ->
                zoomEngine.canScroll(horizontal, direction)
            }
        )

        resetDrawableSize()
        resetScrollBarHelper()
    }


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Reset [transform] and [minScale], [mediumScale], [maxScale], automatically called when [containerSize],
     * [contentSize], [contentOriginSize], [contentScale], [alignment], [rotate], [scalesCalculator], [readMode] changes
     */
    fun reset() {
        zoomEngine.reset("consumer")
    }

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffsetCompat = contentVisibleRect.center,
        animated: Boolean = false
    ) = zoomEngine.scale(targetScale, centroidContentPoint, animated)

    /**
     * Scale to the next step scale and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * If [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only cycle between [minScale] and [mediumScale]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    fun switchScale(
        centroidContentPoint: IntOffsetCompat = contentVisibleRect.center,
        animated: Boolean = false
    ): Float = zoomEngine.switchScale(centroidContentPoint, animated)

    /**
     * Pan the image to the [targetOffset] position, and animation occurs when [animated] is true
     */
    fun offset(
        targetOffset: OffsetCompat,
        animated: Boolean = false
    ) = zoomEngine.offset(targetOffset, animated)

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    fun locate(
        contentPoint: IntOffsetCompat,
        targetScale: Float = zoomEngine.transform.scaleX,
        animated: Boolean = false,
    ) = zoomEngine.locate(contentPoint, targetScale, animated)

    /**
     * Rotate the content to [targetRotation]
     */
    fun rotate(targetRotation: Int) = zoomEngine.rotate(targetRotation)

    /**
     * Gets the next step scale factor,
     * and if [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only loop between [minScale], [mediumScale].
     */
    fun getNextStepScale(): Float = zoomEngine.getNextStepScale()

    /**
     * If true is returned, scrolling can continue on the specified axis and direction
     *
     * @param horizontal Whether to scroll horizontally
     * @param direction positive means scroll to the right or scroll down, negative means scroll to the left or scroll up
     */
    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        zoomEngine.canScroll(horizontal, direction)

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: OffsetCompat): IntOffsetCompat =
        zoomEngine.touchPointToContentPoint(touchPoint)

    /**
     * Register a [transform] property change listener
     */
    fun registerOnTransformChangeListener(listener: OnTransformChangeListener) =
        zoomEngine.registerOnTransformChangeListener(listener)

    /**
     * Unregister a [transform] property change listener
     */
    fun unregisterOnTransformChangeListener(listener: OnTransformChangeListener): Boolean =
        zoomEngine.unregisterOnTransformChangeListener(listener)

    /**
     * Register a [containerSize] property change listener
     */
    fun registerOnContainerSizeChangeListener(listener: OnContainerSizeChangeListener) =
        zoomEngine.registerOnContainerSizeChangeListener(listener)

    /**
     * Unregister a [containerSize] property change listener
     */
    fun unregisterOnContainerSizeChangeListener(listener: OnContainerSizeChangeListener): Boolean =
        zoomEngine.unregisterOnContainerSizeChangeListener(listener)

    /**
     * Register a [contentSize] property change listener
     */
    fun registerOnContentSizeChangeListener(listener: OnContentSizeChangeListener) =
        zoomEngine.registerOnContentSizeChangeListener(listener)

    /**
     * Unregister a [contentSize] property change listener
     */
    fun unregisterOnContentSizeChangeListener(listener: OnContentSizeChangeListener): Boolean =
        zoomEngine.unregisterOnContentSizeChangeListener(listener)

    /**
     * Register a single click event listener
     */
    fun registerOnViewTapListener(listener: OnViewTapListener) {
        this.onViewTapListenerList = (onViewTapListenerList ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a single click event listener
     */
    fun unregisterOnViewTapListener(listener: OnViewTapListener): Boolean {
        return onViewTapListenerList?.remove(listener) == true
    }

    /**
     * Register a long press event listener
     */
    fun registerOnViewLongPressListener(listener: OnViewLongPressListener) {
        this.onViewLongPressListenerList = (onViewLongPressListenerList ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a long press event listener
     */
    fun unregisterOnViewLongPressListener(listener: OnViewLongPressListener): Boolean {
        return onViewLongPressListenerList?.remove(listener) == true
    }


    /* *********************************** Interact with View *********************************** */

    @Suppress("UNUSED_PARAMETER")
    fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        clean()
        if (view.isAttachedToWindowCompat) {
            resetDrawableSize()
        }
    }

    fun onAttachedToWindow() {
    }

    fun onDetachedFromWindow() {
        clean()
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
            containerSize = zoomEngine.containerSize,
            contentSize = zoomEngine.contentSize,
            contentVisibleRect = zoomEngine.contentVisibleRect,
            rotation = zoomEngine.transform.rotation.roundToInt(),
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    fun setScaleType(scaleType: ScaleType): Boolean {
        this@ZoomAbility.scaleType = scaleType
        return true
    }

    fun getScaleType(): ScaleType = this@ZoomAbility.scaleType


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
        val scrollBarSpec = this@ZoomAbility.scrollBar
        if (scrollBarSpec != null) {
            scrollBarEngine = ScrollBarEngine(view, scrollBarSpec)
        }
    }

    private fun clean() {
        zoomEngine.clean()
    }
}