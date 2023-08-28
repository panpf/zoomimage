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
@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.view.zoom.internal

import android.view.View
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toRect
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.view.internal.Rect
import com.github.panpf.zoomimage.view.internal.format
import com.github.panpf.zoomimage.view.internal.requiredMainThread
import com.github.panpf.zoomimage.view.subsampling.internal.SubsamplingEngine
import com.github.panpf.zoomimage.view.zoom.OnContainerSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.OnContentSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.OnTransformChangeListener
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.calculateContentBaseDisplayRect
import com.github.panpf.zoomimage.zoom.calculateContentBaseVisibleRect
import com.github.panpf.zoomimage.zoom.calculateContentDisplayRect
import com.github.panpf.zoomimage.zoom.calculateContentVisibleRect
import com.github.panpf.zoomimage.zoom.calculateInitialZoom
import com.github.panpf.zoomimage.zoom.calculateLocationUserOffset
import com.github.panpf.zoomimage.zoom.calculateNextStepScale
import com.github.panpf.zoomimage.zoom.calculateScaleUserOffset
import com.github.panpf.zoomimage.zoom.calculateScrollEdge
import com.github.panpf.zoomimage.zoom.calculateTransformOffset
import com.github.panpf.zoomimage.zoom.calculateUserOffsetBounds
import com.github.panpf.zoomimage.zoom.canScrollByEdge
import com.github.panpf.zoomimage.zoom.contentPointToContainerPoint
import com.github.panpf.zoomimage.zoom.contentPointToTouchPoint
import com.github.panpf.zoomimage.zoom.limitScaleWithRubberBand
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.touchPointToContentPoint
import kotlin.math.roundToInt

/**
 * Engines that control scale, pan, rotation
 */
class ZoomEngine constructor(logger: Logger, val view: View) {

    val logger: Logger = logger.newLogger(module = "ZoomEngine")

    private var lastScaleAnimatable: FloatAnimatable? = null
    private var lastFlingAnimatable: FlingAnimatable? = null
    private var rotation: Int = 0
    private var onTransformChangeListeners: MutableSet<OnTransformChangeListener>? = null
    private var onContainerSizeChangeListeners: MutableSet<OnContainerSizeChangeListener>? = null
    private var onContentSizeChangeListeners: MutableSet<OnContentSizeChangeListener>? = null

    /**
     * The size of the container that holds the content, this is usually the size of the [ZoomImageView] component
     */
    var containerSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset("containerSizeChanged")
                notifyContainerSizeChanged(value)
            }
        }

    /**
     * The size of the content, usually Painter.intrinsicSize.round(), setup by the [ZoomImageView] component
     */
    var contentSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset("contentSizeChanged")
                notifyContentSizeChanged(value)
            }
        }

    /**
     * The original size of the content, it is usually set by [SubsamplingEngine] after parsing the original size of the image
     */
    var contentOriginSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset("contentOriginSizeChanged")
            }
        }


    /*
     * Configurable properties
     */

    /**
     * The scale of the content, usually set by [ZoomImageView] component
     */
    var contentScale: ContentScaleCompat = ContentScaleCompat.Fit
        set(value) {
            if (field != value) {
                field = value
                reset("contentScaleChanged")
            }
        }

    /**
     * The alignment of the content, usually set by [ZoomImageView] component
     */
    var alignment: AlignmentCompat = AlignmentCompat.Center
        set(value) {
            if (field != value) {
                field = value
                reset("alignmentChanged")
            }
        }

    /**
     * Setup whether to enable read mode and configure read mode
     */
    var readMode: ReadMode? = null
        set(value) {
            if (field != value) {
                field = value
                reset("readModeChanged")
            }
        }

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    var scalesCalculator: ScalesCalculator = ScalesCalculator.Dynamic
        set(value) {
            if (field != value) {
                field = value
                reset("scalesCalculatorChanged")
            }
        }

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    var threeStepScale: Boolean = false

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    var rubberBandScale: Boolean = true

    /**
     * The animation configuration for the zoom animation
     */
    var animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    var limitOffsetWithinBaseVisibleRect: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                reset("limitOffsetWithinBaseVisibleRectChanged")
            }
        }


    /*
     * Information properties
     */

    /**
     * Base transformation, include the base scale, offset, rotation,
     * which is affected by [contentScale], [alignment] properties and [rotate] method
     */
    var baseTransform = TransformCompat.Origin
        private set

    /**
     * User transformation, include the user scale, offset, rotation,
     * which is affected by the user's gesture, [readMode] properties and [scale], [offset], [location] method
     */
    var userTransform = TransformCompat.Origin
        private set

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of [baseTransform] and [userTransform]
     */
    var transform = TransformCompat.Origin
        private set

    /**
     * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    var minScale: Float = 1.0f
        private set

    /**
     * Medium scale factor, only as a target value for one of when switch scale
     */
    var mediumScale: Float = 1.0f
        private set

    /**
     * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    var maxScale: Float = 1.0f
        private set

    /**
     * If true, a transformation is currently in progress, possibly in a continuous gesture operation, or an animation is in progress
     */
    var transforming = false
        internal set(value) {
            if (field != value) {
                field = value
                notifyTransformChanged()
            }
        }

    /**
     * The content region in the container after the baseTransform transformation
     */
    var contentBaseDisplayRect: IntRectCompat = IntRectCompat.Zero
        private set

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    var contentBaseVisibleRect: IntRectCompat = IntRectCompat.Zero
        private set

    /**
     * The content region in the container after the transform transformation
     */
    var contentDisplayRect: IntRectCompat = IntRectCompat.Zero
        private set

    /**
     * The content is visible region to the user after the transform transformation
     */
    var contentVisibleRect: IntRectCompat = IntRectCompat.Zero
        private set

    /**
     * Edge state for the current offset
     */
    var scrollEdge: ScrollEdge = ScrollEdge.Default
        private set

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    var userOffsetBounds: IntRectCompat = IntRectCompat.Zero
        private set

    init {
        reset("init")
    }

    /**
     * Reset [transform] and [minScale], [mediumScale], [maxScale], automatically called when [containerSize],
     * [contentSize], [contentOriginSize], [contentScale], [alignment], [rotate], [scalesCalculator], [readMode] changes
     */
    fun reset(caller: String) {
        requiredMainThread()
        stopAllAnimation("reset:$caller")

        val containerSize = containerSize
        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val readMode = readMode
        val rotation = rotation
        val contentScale = contentScale
        val alignment = alignment
        val scalesCalculator = scalesCalculator

        val initialZoom = calculateInitialZoom(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
        )
        logger.d {
            val transform = initialZoom.baseTransform + initialZoom.userTransform
            "reset. containerSize=$containerSize, " +
                    "contentSize=$contentSize, " +
                    "contentOriginSize=$contentOriginSize, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "rotation=$rotation, " +
                    "scalesCalculator=$scalesCalculator, " +
                    "readMode=$readMode. " +
                    "minScale=${initialZoom.minScale}, " +
                    "mediumScale=${initialZoom.mediumScale}, " +
                    "maxScale=${initialZoom.maxScale}, " +
                    "baseTransform=${initialZoom.baseTransform}, " +
                    "initialUserTransform=${initialZoom.userTransform}, " +
                    "transform=${transform.toShortString()}"
        }

        minScale = initialZoom.minScale
        mediumScale = initialZoom.mediumScale
        maxScale = initialZoom.maxScale
        baseTransform = initialZoom.baseTransform
        updateUserTransform(
            targetUserTransform = initialZoom.userTransform,
            animated = false,
            caller = "reset"
        )
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
    ) {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform
        val contentScale = contentScale
        val alignment = alignment
        val rotation = rotation

        stopAllAnimation("scale")

        val targetUserScale = targetScale / currentBaseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset
        val touchPoint = contentPointToTouchPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = currentUserScale,
            userOffset = currentUserOffset,
            contentPoint = centroidContentPoint.toOffset(),
        )
        val targetUserOffset = calculateScaleUserOffset(
            currentUserScale = currentUserTransform.scaleX,
            currentUserOffset = currentUserTransform.offset,
            targetUserScale = limitedTargetUserScale,
            centroid = touchPoint,
        )
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactorCompat(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "scale. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "centroidContentPoint=${centroidContentPoint.toShortString()}, " +
                    "animated=${animated}. " +
                    "touchPoint=${touchPoint.toShortString()}, " +
                    "targetUserScale=${targetUserScale.format(4)}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${limitedAddUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "scale"
        )
    }

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
    ): Float {
        val nextScale = getNextStepScale()
        scale(
            targetScale = nextScale,
            centroidContentPoint = centroidContentPoint,
            animated = animated
        )
        return nextScale
    }

    /**
     * Pan the image to the [targetOffset] position, and animation occurs when [animated] is true
     */
    fun offset(
        targetOffset: OffsetCompat,
        animated: Boolean = false
    ) {
        containerSize.takeIf { it.isNotEmpty() } ?: return
        contentSize.takeIf { it.isNotEmpty() } ?: return
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform

        stopAllAnimation("offset")

        val scaledBaseOffset = currentBaseTransform.offset.times(currentUserTransform.scale)
        val targetUserOffset = targetOffset - scaledBaseOffset
        val currentUserScale = currentUserTransform.scaleX
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, currentUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(offset = limitedTargetUserOffset)
        logger.d {
            val currentUserOffset = currentUserTransform.offset
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddUserOffset = limitedTargetUserOffset - currentUserOffset
            "offset. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "animated=${animated}. " +
                    "targetUserOffset=${targetUserOffset.toShortString()}, " +
                    "currentUserScale=${currentUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "offset"
        )
    }

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    fun location(
        contentPoint: IntOffsetCompat,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
    ) {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val contentSize =
            contentSize.takeIf { it.isNotEmpty() } ?: return
        val contentScale = contentScale
        val alignment = alignment
        val rotation = rotation
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform

        stopAllAnimation("location")

        val containerPoint = contentPointToContainerPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            contentPoint = contentPoint.toOffset(),
        )

        val targetUserScale = targetScale / currentBaseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)

        val targetUserOffset = calculateLocationUserOffset(
            containerSize = containerSize,
            containerPoint = containerPoint,
            userScale = limitedTargetUserScale,
        )
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactorCompat(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val currentUserScale = currentUserTransform.scaleX
            val currentUserOffset = currentUserTransform.offset
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedTargetAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddUserOffset = limitedTargetUserOffset - currentUserOffset
            val limitedTargetAddUserScaleFormatted = limitedTargetAddUserScale.format(4)
            "location. " +
                    "contentPoint=${contentPoint.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "animated=${animated}. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerPoint=${containerPoint.toShortString()}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> $limitedTargetAddUserScaleFormatted, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "location"
        )
    }

    /**
     * Rotate the content to [targetRotation]
     */
    fun rotate(targetRotation: Int) {
        require(targetRotation % 90 == 0) { "rotation must be in multiples of 90: $targetRotation" }
        val limitedTargetRotation = (targetRotation % 360).let { if (it < 0) 360 - it else it }
        val currentRotation = rotation
        if (currentRotation == limitedTargetRotation) return

        stopAllAnimation("rotate")

        rotation = limitedTargetRotation
        reset("rotate")
    }

    /**
     * Gets the next step scale factor,
     * and if [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only loop between [minScale], [mediumScale].
     */
    fun getNextStepScale(): Float {
        val minScale = minScale
        val mediumScale = mediumScale
        val maxScale = maxScale
        val threeStepScale = threeStepScale
        val transform = transform
        val stepScales = if (threeStepScale) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, transform.scaleX)
    }

    fun clean() {
        lastScaleAnimatable?.stop()
        lastScaleAnimatable = null
        lastFlingAnimatable?.stop()
        lastFlingAnimatable = null
    }

    fun stopAllAnimation(caller: String) {
        val lastScaleAnimatable = lastScaleAnimatable
        if (lastScaleAnimatable?.running == true) {
            lastScaleAnimatable.stop()
            transforming = false
            logger.d { "stopScaleAnimation:$caller" }
        }

        val lastFlingAnimatable = lastFlingAnimatable
        if (lastFlingAnimatable?.running == true) {
            lastFlingAnimatable.stop()
            transforming = false
            logger.d { "stopFlingAnimation:$caller" }
        }
    }

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: OffsetCompat): IntOffsetCompat {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return IntOffsetCompat.Zero
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return IntOffsetCompat.Zero
        val currentUserTransform = userTransform
        val contentScale = contentScale
        val alignment = alignment
        val rotation = rotation
        val contentPoint = touchPointToContentPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            userOffset = currentUserTransform.offset,
            touchPoint = touchPoint
        )
        return contentPoint.round()
    }

    /**
     * If true is returned, scrolling can continue on the specified axis and direction
     *
     * @param horizontal Whether to scroll horizontally
     * @param direction positive means scroll to the right or scroll down, negative means scroll to the left or scroll up
     */
    fun canScroll(horizontal: Boolean, direction: Int): Boolean {
        return canScrollByEdge(scrollEdge, horizontal, direction)
    }

    /**
     * Register a [transform] property change listener
     */
    fun registerOnTransformChangeListener(listener: OnTransformChangeListener) {
        this.onTransformChangeListeners = (onTransformChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a [transform] property change listener
     */
    fun unregisterOnTransformChangeListener(listener: OnTransformChangeListener): Boolean {
        return onTransformChangeListeners?.remove(listener) == true
    }

    /**
     * Register a [containerSize] property change listener
     */
    fun registerOnContainerSizeChangeListener(listener: OnContainerSizeChangeListener) {
        this.onContainerSizeChangeListeners = (onContainerSizeChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a [containerSize] property change listener
     */
    fun unregisterOnContainerSizeChangeListener(listener: OnContainerSizeChangeListener): Boolean {
        return onContainerSizeChangeListeners?.remove(listener) == true
    }

    /**
     * Register a [contentSize] property change listener
     */
    fun registerOnContentSizeChangeListener(listener: OnContentSizeChangeListener) {
        this.onContentSizeChangeListeners = (onContentSizeChangeListeners ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a [contentSize] property change listener
     */
    fun unregisterOnContentSizeChangeListener(listener: OnContentSizeChangeListener): Boolean {
        return onContentSizeChangeListeners?.remove(listener) == true
    }

    /**
     * Roll back to minimum or maximum scaling
     */
    internal fun rollbackScale(lastFocus: OffsetCompat? = null): Boolean {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return false
        contentSize.takeIf { it.isNotEmpty() } ?: return false
        val minScale = minScale
        val maxScale = maxScale
        val animationSpec = animationSpec

        val currentScale = transform.scaleX
        val targetScale = when {
            currentScale.format(2) > maxScale.format(2) -> maxScale
            currentScale.format(2) < minScale.format(2) -> minScale
            else -> null
        }
        if (targetScale != null) {
            val startScale = currentScale
            val endScale = targetScale
            logger.d {
                "rollbackScale. " +
                        "lastFocus=${lastFocus?.toShortString()}. " +
                        "startScale=${startScale.format(4)}, " +
                        "endScale=${endScale.format(4)}"
            }
            val centroid = lastFocus ?: containerSize.toSize().center
            lastScaleAnimatable = FloatAnimatable(
                view = view,
                startValue = 0f,
                endValue = 1f,
                durationMillis = animationSpec.durationMillis,
                interpolator = animationSpec.interpolator,
                onUpdateValue = { value ->
                    val frameScale = com.github.panpf.zoomimage.view.internal.lerp(
                        start = startScale,
                        stop = endScale,
                        fraction = value
                    )
                    val nowScale = this@ZoomEngine.transform.scaleX
                    val addScale = frameScale / nowScale
                    transform(
                        centroid = centroid,
                        panChange = OffsetCompat.Zero,
                        zoomChange = addScale,
                        rotationChange = 0f
                    )
                },
                onEnd = {
                    transforming = false
                    notifyTransformChanged()
                }
            )

            transforming = true
            lastScaleAnimatable?.start()
        }
        return targetScale != null
    }

    internal fun transform(
        centroid: OffsetCompat,
        panChange: OffsetCompat,
        zoomChange: Float,
        rotationChange: Float
    ) {
        containerSize.takeIf { it.isNotEmpty() } ?: return
        contentSize.takeIf { it.isNotEmpty() } ?: return
        val currentUserTransform = userTransform

        val targetScale = transform.scaleX * zoomChange
        val targetUserScale = targetScale / baseTransform.scaleX
        val limitedTargetUserScale = if (rubberBandScale) {
            limitUserScaleWithRubberBand(targetUserScale)
        } else {
            limitUserScale(targetUserScale)
        }
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset
        val targetUserOffset = calculateTransformOffset(
            currentScale = currentUserScale,
            currentOffset = currentUserOffset,
            targetScale = limitedTargetUserScale,
            centroid = centroid,
            pan = panChange,
            gestureRotate = 0f,
        )
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactorCompat(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "transform. " +
                    "centroid=${centroid.toShortString()}, " +
                    "panChange=${panChange.toShortString()}, " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "rotationChange=${rotationChange.format(4)}. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "targetUserScale=${targetUserScale.format(4)}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${limitedAddUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = false,
            caller = "transform"
        )
    }

    internal fun fling(velocityX: Float, velocityY: Float) {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return
        val contentScale = contentScale
        val alignment = alignment
        val rotation = rotation
        val currentUserTransform = userTransform

        stopAllAnimation("fling")

        val startUserOffset = currentUserTransform.offset
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
        ).let {
            Rect(
                it.left.roundToInt(),
                it.top.roundToInt(),
                it.right.roundToInt(),
                it.bottom.roundToInt()
            )
        }
        val velocity = IntOffsetCompat(velocityX.roundToInt(), velocityY.roundToInt())
        logger.d {
            "fling. start. " +
                    "start=${startUserOffset.toShortString()}, " +
                    "bounds=${userOffsetBounds.toShortString()}, " +
                    "velocity=${velocity.toShortString()}"
        }
        lastFlingAnimatable = FlingAnimatable(
            view = view,
            start = startUserOffset.round(),
            bounds = userOffsetBounds,
            velocity = velocity,
            onUpdateValue = { value ->
                val targetUserOffset =
                    this@ZoomEngine.userTransform.copy(offset = value.toOffset())
                updateUserTransform(targetUserOffset, false, "fling")
            },
            onEnd = {
                transforming = false
                notifyTransformChanged()
            }
        )
        transforming = true
        lastFlingAnimatable?.start()
    }

    private fun limitUserScale(targetUserScale: Float): Float {
        val minUserScale = minScale / baseTransform.scaleX
        val maxUserScale = maxScale / baseTransform.scaleX
        return targetUserScale.coerceIn(minimumValue = minUserScale, maximumValue = maxUserScale)
    }

    private fun limitUserScaleWithRubberBand(targetUserScale: Float): Float {
        val minUserScale = minScale / baseTransform.scaleX
        val maxUserScale = maxScale / baseTransform.scaleX
        return limitScaleWithRubberBand(
            currentScale = userTransform.scaleX,
            targetScale = targetUserScale,
            minScale = minUserScale,
            maxScale = maxUserScale
        )
    }

    private fun limitUserOffset(userOffset: OffsetCompat, userScale: Float): OffsetCompat {
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = userScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
        ).round().toRect()      // round() makes sense
        return userOffset.limitTo(userOffsetBounds)
    }

    private fun updateUserTransform(
        targetUserTransform: TransformCompat,
        animated: Boolean,
        caller: String
    ) {
        if (animated) {
            val currentUserTransform = userTransform
            lastScaleAnimatable = FloatAnimatable(
                view = view,
                startValue = 0f,
                endValue = 1f,
                durationMillis = animationSpec.durationMillis,
                interpolator = animationSpec.interpolator,
                onUpdateValue = { value ->
                    val userTransform = lerp(
                        start = currentUserTransform,
                        stop = targetUserTransform,
                        fraction = value
                    )
                    logger.d {
                        "$caller. animated running. transform=${userTransform.toShortString()}"
                    }
                    this@ZoomEngine.userTransform = userTransform
                    updateTransform()
                },
                onEnd = {
                    transforming = false
                    notifyTransformChanged()
                }
            )
            transforming = true
            lastScaleAnimatable?.start()
        } else {
            this.userTransform = targetUserTransform
            updateTransform()
        }
    }

    private fun updateTransform() {
        transform = baseTransform + userTransform

        contentBaseDisplayRect = calculateContentBaseDisplayRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
        ).round()
        contentBaseVisibleRect = calculateContentBaseVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
        ).round()
        contentDisplayRect = calculateContentDisplayRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()
        contentVisibleRect = calculateContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()

        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = userTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
        )
        this.userOffsetBounds = userOffsetBounds.round()

        scrollEdge = calculateScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset,
        )

        notifyTransformChanged()
    }

    private fun notifyTransformChanged() {
        val transform = this@ZoomEngine.transform
        onTransformChangeListeners?.forEach { listener ->
            listener.onTransformChanged(transform)
        }
    }

    private fun notifyContainerSizeChanged(containerSize: IntSizeCompat) {
        onContainerSizeChangeListeners?.forEach {
            it.onContainerSizeChanged(containerSize)
        }
    }

    private fun notifyContentSizeChanged(contentSize: IntSizeCompat) {
        onContentSizeChangeListeners?.forEach {
            it.onContentSizeChanged(contentSize)
        }
    }
}