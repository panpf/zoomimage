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

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Edge
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.core.IntOffsetCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.TransformCompat
import com.github.panpf.zoomimage.core.div
import com.github.panpf.zoomimage.core.internal.DefaultMediumScaleMultiple
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.computeUserScales
import com.github.panpf.zoomimage.core.internal.limitScaleWithRubberBand
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.core.rotate
import com.github.panpf.zoomimage.core.roundToCompatIntOffset
import com.github.panpf.zoomimage.core.toShortString
import com.github.panpf.zoomimage.view.internal.computeScaleFactor
import com.github.panpf.zoomimage.view.internal.format
import com.github.panpf.zoomimage.view.internal.getScale
import com.github.panpf.zoomimage.view.internal.getTranslation
import com.github.panpf.zoomimage.view.internal.isSafe
import com.github.panpf.zoomimage.view.zoom.OnDrawableSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.OnMatrixChangeListener
import com.github.panpf.zoomimage.view.zoom.OnRotateChangeListener
import com.github.panpf.zoomimage.view.zoom.OnViewSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import kotlin.math.abs
import kotlin.math.roundToInt

// todo 参照 ZoomableState 重构
class ZoomEngine constructor(logger: Logger, val view: View) {

    private val logger: Logger = logger.newLogger(module = "ZoomEngine")
    private val baseMatrix = Matrix()
    private val userMatrix = Matrix()
    private val displayMatrix = Matrix()
    private val cacheDisplayRectF = RectF()
    private var lastScaleFocusX: Float = 0f
    private var lastScaleFocusY: Float = 0f
    private var scaleAnimatable: FloatAnimatable? = null
    private var flingAnimatable: FlingAnimatable? = null
    private var _scrollEdge: ScrollEdge = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH)
    private var blockParentIntercept: Boolean = false
    private var dragging = false
    var manualScaling = false
        internal set(value) {
            if (field != value) {
                field = value
                notifyMatrixChanged()
            }
        }

    private var onMatrixChangeListeners: MutableSet<OnMatrixChangeListener>? = null
    private var onRotateChangeListeners: MutableSet<OnRotateChangeListener>? = null
    private var onViewSizeChangeListeners: MutableSet<OnViewSizeChangeListener>? = null
    private var onDrawableSizeChangeListeners: MutableSet<OnDrawableSizeChangeListener>? = null

    /** Initial scale and translate for base matrix */
    private var baseInitialTransform: TransformCompat = TransformCompat.Origin

    /** Initial scale and translate for user matrix */
    private var userInitialTransform: TransformCompat = TransformCompat.Origin

//    var displayTransform: TransformCompat = TransformCompat.Origin
//        private set

    val scrollEdge: ScrollEdge
        get() = _scrollEdge

    val scaling: Boolean
        get() = scaleAnimatable?.running == true || manualScaling
    val fling: Boolean
        get() = flingAnimatable?.running == true

    val userScale: Float
        get() = userMatrix.getScale().scaleX
    val userOffset: OffsetCompat
        get() = userMatrix.getTranslation()

    val baseScale: ScaleFactorCompat
        get() = baseMatrix.getScale()
    val baseOffset: OffsetCompat
        get() = baseMatrix.getTranslation()

    val scale: ScaleFactorCompat
        get() = displayMatrix.apply { getDisplayMatrix(this) }.getScale()
    val offset: OffsetCompat
        get() = displayMatrix.apply { getDisplayMatrix(this) }.getTranslation()

    /** Allows the parent ViewGroup to intercept events while sliding to an edge */
    var allowParentInterceptOnEdge: Boolean = true

    var viewSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset()
                notifyViewSizeChanged()
            }
        }

    /**
     * Dimensions of the original image, which is used to calculate the scale of double-click scaling
     */
    var imageSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var drawableSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset()
                notifyDrawableSizeChanged()
            }
        }
    var scaleType: ScaleType = ScaleType.FIT_CENTER
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var readMode: ReadMode? = null
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var defaultMediumScaleMultiple: Float = DefaultMediumScaleMultiple
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default

    var threeStepScale: Boolean = false
        internal set
    var rubberBandScale: Boolean = true
        internal set
    var minScale: Float = 1.0f
        private set
    var mediumScale: Float = 1.0f
        private set
    var maxScale: Float = 1.0f
        private set
    var rotation: Int = 0
        private set


    /**************************************** Internal ********************************************/

    init {
        reset()
    }

    fun reset() {
        val drawableSize = drawableSize
        val imageSize = imageSize
        val viewSize = viewSize
        if (drawableSize.isEmpty() || viewSize.isEmpty()) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
            baseInitialTransform = TransformCompat.Origin
            userInitialTransform = TransformCompat.Origin
        } else {
            val rotatedDrawableSize = drawableSize.rotate(rotation)
            val rotatedImageSize = imageSize.rotate(rotation)
            val userStepScales = computeUserScales(
                contentSize = rotatedDrawableSize,
                contentOriginSize = rotatedImageSize,
                containerSize = viewSize,
                scaleMode = scaleType.toScaleMode(),
                baseScale = scaleType.computeScaleFactor(
                    srcSize = rotatedDrawableSize,
                    dstSize = viewSize
                ),
                defaultMediumScaleMultiple = defaultMediumScaleMultiple
            )
            baseInitialTransform = scaleType
                .computeTransform(srcSize = rotatedDrawableSize, dstSize = viewSize)
            minScale = userStepScales[0] * baseInitialTransform.scaleX
            mediumScale = userStepScales[1] * baseInitialTransform.scaleX
            maxScale = userStepScales[2] * baseInitialTransform.scaleX
            val readMode = scaleType.supportReadMode()
                    && readMode?.should(srcSize = rotatedDrawableSize, dstSize = viewSize) == true
            userInitialTransform = if (readMode) {
                val readModeTransform = computeReadModeTransform(
                    srcSize = rotatedDrawableSize,
                    dstSize = viewSize,
                    scaleType = scaleType,
                )
                readModeTransform.div(baseInitialTransform.scale)
            } else {
                TransformCompat.Origin
            }
        }
        logger.d {
            "reset. viewSize=$viewSize, " +
                    "imageSize=$imageSize, " +
                    "drawableSize=$drawableSize, " +
                    "rotateDegrees=$rotation, " +
                    "scaleType=$scaleType, " +
                    "readMode=$readMode, " +
                    "minUserScale=$minScale, " +
                    "mediumUserScale=$mediumScale, " +
                    "maxUserScale=$maxScale, " +
                    "baseInitialTransform=$baseInitialTransform, " +
                    "userInitialTransform=$userInitialTransform"
        }
        resetBaseMatrix()
        resetUserMatrix()
        checkAndApplyMatrix()
    }


    /*************************************** Interaction ******************************************/

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     */
    fun scale(
        newScale: Float,
        centroid: OffsetCompat = OffsetCompat(viewSize.width / 2f, viewSize.height / 2f),
        animate: Boolean = false
    ) {
        stopAllAnimation()
        val currentScale = scale.scaleX
        // todo 参照 ZoomableState#scale 就用当前的 centroid
        val focal = if (newScale > currentScale) {
            centroid
        } else {
            OffsetCompat(viewSize.width / 2f, viewSize.height / 2f)
        }
        val limitedNewScale = newScale.coerceIn(minScale, maxScale)
        val finalFocalX = focal.x
        val finalFocalY = focal.y
        val startUserScale = userScale
        val endUserScale = limitedNewScale / baseScale.scaleX
        if (animate) {
            scaleAnimatable = FloatAnimatable(
                view = view,
                startValue = 0f,
                endValue = 1f,
                durationMillis = animationSpec.durationMillis,
                interpolator = animationSpec.interpolator,
                onUpdateValue = { value ->
                    val currentUserScale = userScale
                    val progressUserScale = startUserScale + value * (endUserScale - startUserScale)
                    val deltaScale = progressUserScale / currentUserScale
                    doScale(deltaScale, finalFocalX, finalFocalY, 0f, 0f)
                },
                onEnd = { notifyMatrixChanged() }
            )
            scaleAnimatable?.start()
        } else {
            val addUserScale = endUserScale / startUserScale
            require(addUserScale.isSafe()) { "scaleBy addUserScale=$addUserScale is invalid" }
            userMatrix.postScale(addUserScale, addUserScale, finalFocalX, finalFocalY)
            checkAndApplyMatrix()
        }
    }

    // todo 和 scale 合并
    fun doScale(userScaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float) {
        logger.d {
            "onScale. scaleFactor: $userScaleFactor, focusX: $focusX, focusY: $focusY, dx: $dx, dy: $dy"
        }

        /* Simulate a rubber band effect when zoomed to max or min */
        var newUserScaleFactor = userScaleFactor
        lastScaleFocusX = focusX
        lastScaleFocusY = focusY
        val currentUserScale = userScale
        val newUserScale = currentUserScale * newUserScaleFactor
        val minUserScale = minScale / baseScale.scaleX
        val maxUserScale = maxScale / baseScale.scaleX
        val limitedNewUserScale = if (rubberBandScale) {
            limitScaleWithRubberBand(
                currentScale = currentUserScale,
                targetScale = newUserScale,
                minScale = minUserScale,
                maxScale = maxUserScale,
            )
        } else {
            newUserScale.coerceIn(minimumValue = minUserScale, maximumValue = maxUserScale)
        }
        newUserScaleFactor = limitedNewUserScale / currentUserScale

        require(dx.isSafe() && dy.isSafe() && newUserScaleFactor.isSafe()) { "doScale dx=$dx, dy=$dy, newUserScaleFactor=$newUserScaleFactor is invalid" }
        userMatrix.postScale(newUserScaleFactor, newUserScaleFactor, focusX, focusY)
        userMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()
    }

    fun offset(offset: IntOffsetCompat, animate: Boolean = false) {
        val currentOffset = this.offset
        val fl = offset.x - currentOffset.x
        val fy = offset.y - currentOffset.y
        // todo 实现 animate
        userMatrix.postTranslate(fl, fy)
        checkAndApplyMatrix()
    }

    // todo 和 offset 合并
    fun doDrag(dx: Float, dy: Float) {
        logger.d { "onDrag. dx: $dx, dy: $dy" }

        require(dx.isSafe() && dy.isSafe()) { "doDrag dx=$dx, dy=$dy is invalid" }
        userMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()

        val disallowParentInterceptOnEdge = !allowParentInterceptOnEdge
        val blockParent = blockParentIntercept
        val disallow = if (dragging || blockParent || disallowParentInterceptOnEdge) {
            logger.d {
                "onDrag. DisallowParentIntercept. dragging=$dragging, blockParent=$blockParent, disallowParentInterceptOnEdge=$disallowParentInterceptOnEdge"
            }
            true
        } else {
            val slop = view.resources.displayMetrics.density * 3
            val result = (scrollEdge.horizontal == Edge.NONE && (dx >= slop || dx <= -slop))
                    || (scrollEdge.horizontal == Edge.START && dx <= -slop)
                    || (scrollEdge.horizontal == Edge.END && dx >= slop)
                    || (scrollEdge.vertical == Edge.NONE && (dy >= slop || dy <= -slop))
                    || (scrollEdge.vertical == Edge.START && dy <= -slop)
                    || (scrollEdge.vertical == Edge.END && dy >= slop)
            val type = if (result) "DisallowParentIntercept" else "AllowParentIntercept"
            logger.d {
                "onDrag. $type. scrollEdge=${scrollEdge.horizontal}-${scrollEdge.vertical}, d=${dx}x${dy}"
            }
            dragging = result
            result
        }
        requestDisallowInterceptTouchEvent(disallow)
    }

    fun location(
        offsetOfContent: IntOffsetCompat,
        targetScale: Float = scale.scaleX,
        animated: Boolean = false
    ) {
        val viewSize = viewSize.takeIf { !it.isEmpty() } ?: return

        stopAllAnimation()

        val nowScale = scale.scaleX
        if (nowScale.format(2) != targetScale.format(2)) {
            scale(newScale = targetScale, animate = false)
        }

        val displayRectF = cacheDisplayRectF.apply { getDisplayRect(this) }
        val start = IntOffsetCompat(
            x = abs(displayRectF.left.toInt()),
            y = abs(displayRectF.top.toInt())
        )
        val rotatedOffsetOfContent = offsetOfContent.rotateInContainer(drawableSize, rotation)
        val centerLocation =
            computeLocationOffset(rotatedOffsetOfContent, viewSize, displayRectF, scale)
        logger.d {
            "location. " +
                    "offsetOfContent=${offsetOfContent.toShortString()}, " +
                    "start=${start.toShortString()}, " +
                    "end=${centerLocation.toShortString()}"
        }
        if (animated) {
            var lastX = start.x
            var lastY = start.y
            scaleAnimatable = FloatAnimatable(
                view = view,
                startValue = 0f,
                endValue = 1f,
                durationMillis = animationSpec.durationMillis,
                interpolator = animationSpec.interpolator,
                onUpdateValue = { value ->
                    val mDeltaX = centerLocation.x - start.x
                    val mDeltaY = centerLocation.y - start.y
                    val newX = start.x + (value * mDeltaX).roundToInt()
                    val newY = start.y + (value * mDeltaY).roundToInt()
                    val dx = (lastX - newX).toFloat()
                    val dy = (lastY - newY).toFloat()
//                    offsetBy(dx, dy)
                    val add = IntOffsetCompat(dx.roundToInt(), dy.roundToInt())
                    offset(offset.roundToCompatIntOffset() + add)
                    lastX = newX
                    lastY = newY
                },
                onEnd = { notifyMatrixChanged() }
            )
            scaleAnimatable?.start()
        } else {
            val dx = -(centerLocation.x - start.x).toFloat()
            val dy = -(centerLocation.y - start.y).toFloat()
            val add = IntOffsetCompat(dx.roundToInt(), dy.roundToInt())
            offset(offset.roundToCompatIntOffset() + add)
        }
    }

    /**
     * Rotate the image to the specified degrees
     *
     * @param rotation Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotation(rotation: Int) {
        require(rotation >= 0) { "rotation must be greater than or equal to 0: $rotation" }
        require(rotation % 90 == 0) { "rotation must be in multiples of 90: $rotation" }
        val limitedRotation = rotation % 360
        if (this@ZoomEngine.rotation == limitedRotation) return
        this@ZoomEngine.rotation = limitedRotation
        reset()
        notifyRotationChanged()
    }

    fun fling(velocityX: Float, velocityY: Float) {
        stopAllAnimation()
        val drawRectF = RectF()
            .apply { getDisplayRect(this) }
            .takeIf { !it.isEmpty }
            ?: return
        val (viewWidth, viewHeight) = viewSize
        val minX: Int
        val maxX: Int
        val startX = (-drawRectF.left).roundToInt()
        if (viewWidth < drawRectF.width()) {
            minX = 0
            maxX = (drawRectF.width() - viewWidth).roundToInt()
        } else {
            maxX = startX
            minX = maxX
        }

        val minY: Int
        val maxY: Int
        val startY = (-drawRectF.top).roundToInt()
        if (viewHeight < drawRectF.height()) {
            minY = 0
            maxY = (drawRectF.height() - viewHeight).roundToInt()
        } else {
            maxY = startY
            minY = maxY
        }
        val bounds = Rect(
            /* left = */ minX,
            /* top = */ minY,
            /* right = */ maxX,
            /* bottom = */ maxY,
        )
        val startUserOffset = IntOffsetCompat(x = startX, y = startY)
        val velocity = IntOffsetCompat(-velocityX.roundToInt(), -velocityY.roundToInt())
        logger.d {
            "fling. start. " +
                    "start=${startUserOffset.toShortString()}, " +
                    "bounds=${bounds.toShortString()}, " +
                    "velocity=${velocity.toShortString()}"
        }
        var currentX = startUserOffset.x
        var currentY = startUserOffset.y
        flingAnimatable = FlingAnimatable(
            view = view,
            start = startUserOffset,
            bounds = bounds,
            velocity = velocity,
            onUpdateValue = { value ->
                val newX = value.x
                val newY = value.y
                val dx = (currentX - newX).toFloat()
                val dy = (currentY - newY).toFloat()
                val add = IntOffsetCompat(dx.roundToInt(), dy.roundToInt())
                offset(offset.roundToCompatIntOffset() + add)
                currentX = newX
                currentY = newY
            },
            onEnd = { notifyMatrixChanged() }
        )
        flingAnimatable?.start()
    }

    /**
     * Roll back to minimum or maximum scaling
     */
    fun rollbackScale() {
        val currentScale = scale.scaleX
        val minScale = minScale
        val maxScale = maxScale
        val targetScale = when {
            currentScale.format(2) < minScale.format(2) -> minScale
            currentScale.format(2) > maxScale.format(2) -> maxScale
            else -> null
        }
        val lastScaleFocusX = lastScaleFocusX
        val lastScaleFocusY = lastScaleFocusY
        if (targetScale != null && lastScaleFocusX != 0f && lastScaleFocusY != 0f) {
            scale(
                newScale = targetScale,
                centroid = OffsetCompat(x = lastScaleFocusX, y = lastScaleFocusY),
                animate = true
            )
        }
    }

    fun switchScale(fx: Float, fy: Float) {
        // todo 参考 ZoomableState#switchScale 用 location 实现
        scale(
            newScale = getNextStepScale(),
            centroid = OffsetCompat(x = fx, y = fy),
            animate = true
        )
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScale) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, scale.scaleX)
    }

    fun clean() {
        scaleAnimatable?.stop()
        scaleAnimatable = null
        flingAnimatable?.stop()
        flingAnimatable = null
    }

    fun stopAllAnimation() {
        scaleAnimatable?.stop()
        flingAnimatable?.stop()
    }

    fun getDisplayMatrix(matrix: Matrix) {
        matrix.set(baseMatrix)
        matrix.postConcat(userMatrix)
    }

    fun getDisplayRect(rectF: RectF) {
        val drawableSize = drawableSize
        val displayMatrix = displayMatrix.apply { getDisplayMatrix(this) }
        rectF.set(0f, 0f, drawableSize.width.toFloat(), drawableSize.height.toFloat())
        displayMatrix.mapRect(rectF)
    }

    fun getDisplayRect(): RectF {
        return RectF().apply { getDisplayRect(this) }
    }

    /**
     * Gets the area that the user can see on the drawable (not affected by rotation)
     */
    fun getVisibleRect(rect: Rect) {
        rect.setEmpty()
        val displayRectF =
            cacheDisplayRectF.apply { getDisplayRect(this) }.takeIf { !it.isEmpty } ?: return
        val viewSize = viewSize.takeIf { !it.isEmpty() } ?: return
        val drawableSize = drawableSize.takeIf { !it.isEmpty() } ?: return
        val (drawableWidth, drawableHeight) = drawableSize.let {
            if (rotation % 180 == 0) it else IntSizeCompat(it.height, it.width)
        }
        val displayWidth = displayRectF.width()
        val displayHeight = displayRectF.height()
        val widthScale = displayWidth / drawableWidth
        val heightScale = displayHeight / drawableHeight
        var left: Float = if (displayRectF.left >= 0)
            0f else abs(displayRectF.left)
        var right: Float = if (displayWidth >= viewSize.width)
            viewSize.width + left else displayRectF.right - displayRectF.left
        var top: Float = if (displayRectF.top >= 0)
            0f else abs(displayRectF.top)
        var bottom: Float = if (displayHeight >= viewSize.height)
            viewSize.height + top else displayRectF.bottom - displayRectF.top
        left /= widthScale
        right /= widthScale
        top /= heightScale
        bottom /= heightScale
        rect.set(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
        reverseRotateRect(rect, rotation, drawableSize)
    }

    /**
     * Gets the area that the user can see on the drawable (not affected by rotation)
     */
    fun getVisibleRect(): Rect {
        return Rect().apply { getVisibleRect(this) }
    }

    fun touchPointToDrawablePoint(touchPoint: PointF): Point? {
        val drawableSize = drawableSize.takeIf { !it.isEmpty() } ?: return null
        val displayRect = getDisplayRect()
        if (!displayRect.contains(touchPoint.x, touchPoint.y)) {
            return null
        }

        val zoomScale = scale
        val drawableX =
            ((touchPoint.x - displayRect.left) / zoomScale.scaleX).roundToInt()
                .coerceIn(0, drawableSize.width)
        val drawableY =
            ((touchPoint.y - displayRect.top) / zoomScale.scaleY).roundToInt()
                .coerceIn(0, drawableSize.height)
        return Point(drawableX, drawableY)
    }

    /**
     * Whether you can scroll horizontally or vertical in the specified direction
     *
     * @param direction Negative to check scrolling left, positive to check scrolling right.
     */
    fun canScroll(horizontal: Boolean, direction: Int): Boolean {
        return com.github.panpf.zoomimage.core.internal.canScroll(horizontal, direction, scrollEdge)
    }

    fun actionDown() {
        // todo 改造
        logger.d {
            "onActionDown. disallow parent intercept touch event"
        }
        stopAllAnimation()
        lastScaleFocusX = 0f
        lastScaleFocusY = 0f
        dragging = false

        requestDisallowInterceptTouchEvent(true)
    }

    fun addOnMatrixChangeListener(listener: OnMatrixChangeListener) {
        this.onMatrixChangeListeners = (onMatrixChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    fun removeOnMatrixChangeListener(listener: OnMatrixChangeListener): Boolean {
        return onMatrixChangeListeners?.remove(listener) == true
    }

    fun addOnRotateChangeListener(listener: OnRotateChangeListener) {
        this.onRotateChangeListeners = (onRotateChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    fun removeOnRotateChangeListener(listener: OnRotateChangeListener): Boolean {
        return onRotateChangeListeners?.remove(listener) == true
    }

    fun addOnViewSizeChangeListener(listener: OnViewSizeChangeListener) {
        this.onViewSizeChangeListeners = (onViewSizeChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    fun removeOnViewSizeChangeListener(listener: OnViewSizeChangeListener): Boolean {
        return onViewSizeChangeListeners?.remove(listener) == true
    }

    fun addOnDrawableSizeChangeListener(listener: OnDrawableSizeChangeListener) {
        this.onDrawableSizeChangeListeners = (onDrawableSizeChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    fun removeOnDrawableSizeChangeListener(listener: OnDrawableSizeChangeListener): Boolean {
        return onDrawableSizeChangeListeners?.remove(listener) == true
    }

    private fun resetBaseMatrix() {
        baseMatrix.apply {
            reset()
            val transform = baseInitialTransform
            require(transform.scale.scaleX > 0f && transform.scale.scaleY > 0f) { "resetBaseMatrix transform scale=$transform is invalid" }
            postScale(transform.scale.scaleX, transform.scale.scaleY)
            postTranslate(transform.offset.x, transform.offset.y)
            postRotate(rotation.toFloat())
        }
    }

    private fun resetUserMatrix() {
        userMatrix.apply {
            reset()
            val userTransform = userInitialTransform
            postScale(userTransform.scale.scaleX, userTransform.scale.scaleY)
            postTranslate(userTransform.offset.x, userTransform.offset.y)
        }
    }

    private fun checkAndApplyMatrix() {
        if (checkMatrixBounds()) {
            notifyMatrixChanged()
        }
    }

    private fun checkMatrixBounds(): Boolean {
        val displayRectF = cacheDisplayRectF.apply { getDisplayRect(this) }
        if (displayRectF.isEmpty) {
            _scrollEdge = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH)
            return false
        }

        var deltaX = 0f
        val viewWidth = viewSize.width
        val displayWidth = displayRectF.width()
        when {
            displayWidth.toInt() <= viewWidth -> {
                deltaX = when (scaleType) {
                    ScaleType.FIT_START -> -displayRectF.left
                    ScaleType.FIT_END -> viewWidth - displayWidth - displayRectF.left
                    else -> (viewWidth - displayWidth) / 2 - displayRectF.left
                }
            }

            displayRectF.left.toInt() > 0 -> {
                deltaX = -displayRectF.left
            }

            displayRectF.right.toInt() < viewWidth -> {
                deltaX = viewWidth - displayRectF.right
            }
        }

        var deltaY = 0f
        val viewHeight = viewSize.height
        val displayHeight = displayRectF.height()
        when {
            displayHeight.toInt() <= viewHeight -> {
                deltaY = when (scaleType) {
                    ScaleType.FIT_START -> -displayRectF.top
                    ScaleType.FIT_END -> viewHeight - displayHeight - displayRectF.top
                    else -> (viewHeight - displayHeight) / 2 - displayRectF.top
                }
            }

            displayRectF.top.toInt() > 0 -> {
                deltaY = -displayRectF.top
            }

            displayRectF.bottom.toInt() < viewHeight -> {
                deltaY = viewHeight - displayRectF.bottom
            }
        }

        // Finally actually translate the matrix

        require(deltaX.isSafe() && deltaY.isSafe()) { "checkMatrixBounds deltaX=${deltaX}, deltaY=${deltaY} is invalid" }
        logger.d {
            "checkMatrixBounds. deltaX=${deltaX}, deltaY=${deltaY}, displayRectF=$displayRectF, viewSize=$viewSize"
        }
        userMatrix.postTranslate(deltaX, deltaY)

        _scrollEdge = ScrollEdge(
            horizontal = when {
                displayWidth.toInt() <= viewWidth -> Edge.BOTH
                displayRectF.left.toInt() >= 0 -> Edge.START
                displayRectF.right.toInt() <= viewWidth -> Edge.END
                else -> Edge.NONE
            },
            vertical = when {
                displayHeight.toInt() <= viewHeight -> Edge.BOTH
                displayRectF.top.toInt() >= 0 -> Edge.START
                displayRectF.bottom.toInt() <= viewHeight -> Edge.END
                else -> Edge.NONE
            },
        )
        return true
    }

    private fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        view.parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun limitScale(targetScale: Float): Float {
        return targetScale.coerceIn(minimumValue = this.minScale, maximumValue = this.maxScale)
    }

    private fun notifyMatrixChanged() {
        onMatrixChangeListeners?.forEach { listener ->
            listener.onMatrixChanged()
        }
    }

    private fun notifyViewSizeChanged() {
        onViewSizeChangeListeners?.forEach {
            it.onSizeChanged()
        }
    }

    private fun notifyDrawableSizeChanged() {
        onDrawableSizeChangeListeners?.forEach {
            it.onSizeChanged()
        }
    }

    private fun notifyRotationChanged() {
        val rotation = rotation
        onRotateChangeListeners?.forEach {
            it.onRotateChanged(rotation)
        }
    }
}