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

@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.view.zoom

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
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.zoom.internal.FlingAnimatable
import com.github.panpf.zoomimage.view.zoom.internal.FloatAnimatable
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.calculateContentBaseDisplayRect
import com.github.panpf.zoomimage.zoom.calculateContentBaseVisibleRect
import com.github.panpf.zoomimage.zoom.calculateContentDisplayRect
import com.github.panpf.zoomimage.zoom.calculateContentVisibleRect
import com.github.panpf.zoomimage.zoom.calculateInitialZoom
import com.github.panpf.zoomimage.zoom.calculateLocateUserOffset
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Engines that control scale, pan, rotation
 */
class ZoomableEngine constructor(logger: Logger, val view: View) {

    private val logger: Logger = logger.newLogger(module = "ZoomableEngine")
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var lastScaleAnimatable: FloatAnimatable? = null
    private var lastFlingAnimatable: FlingAnimatable? = null
    private var rotation: Int = 0


    /**
     * The size of the container that holds the content, this is usually the size of the [ZoomImageView] component
     */
    val containerSizeState = MutableStateFlow(IntSizeCompat.Zero)

    /**
     * The size of the content, this is usually the size of the thumbnail Drawable, setup by the [ZoomImageView] component
     */
    val contentSizeState = MutableStateFlow(IntSizeCompat.Zero)

    /**
     * The original size of the content, it is usually set by [SubsamplingEngine] after parsing the original size of the image
     */
    val contentOriginSizeState = MutableStateFlow(IntSizeCompat.Zero)


    /* *********************************** Configurable properties ****************************** */

    /**
     * The scale of the content, usually set by [ZoomImageView] component
     */
    val contentScaleState = MutableStateFlow(ContentScaleCompat.Fit)

    /**
     * The alignment of the content, usually set by [ZoomImageView] component
     */
    val alignmentState = MutableStateFlow(AlignmentCompat.Center)

    /**
     * Setup whether to enable read mode and configure read mode
     */
    val readModeState = MutableStateFlow<ReadMode?>(null)

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    val scalesCalculatorState = MutableStateFlow<ScalesCalculator>(ScalesCalculator.Dynamic)

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    val threeStepScaleState = MutableStateFlow(false)

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    val rubberBandScaleState = MutableStateFlow(true)

    // todo longPressSlideScale

    /**
     * The animation configuration for the zoom animation
     */
    val animationSpecState = MutableStateFlow(ZoomAnimationSpec.Default)

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    val limitOffsetWithinBaseVisibleRectState = MutableStateFlow(false)


    /* *********************************** Information properties ******************************* */

    private val _baseTransformState = MutableStateFlow(TransformCompat.Origin)
    private val _userTransformState = MutableStateFlow(TransformCompat.Origin)
    private val _transformState = MutableStateFlow(TransformCompat.Origin)
    private val _minScaleState = MutableStateFlow(1.0f)
    private val _mediumScaleState = MutableStateFlow(1.0f)
    private val _maxScaleState = MutableStateFlow(1.0f)
    internal val _continuousTransformTypeState = MutableStateFlow(ContinuousTransformType.NONE)
    private val _contentBaseDisplayRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _contentBaseVisibleRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _contentDisplayRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _contentVisibleRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _scrollEdgeState = MutableStateFlow(ScrollEdge.Default)
    private val _userOffsetBoundsState = MutableStateFlow(IntRectCompat.Zero)

    /**
     * Base transformation, include the base scale, offset, rotation,
     * which is affected by [contentScaleState], [alignmentState] properties and [rotate] method
     */
    val baseTransformState: StateFlow<TransformCompat> = _baseTransformState

    /**
     * User transformation, include the user scale, offset, rotation,
     * which is affected by the user's gesture, [readModeState] properties and [scale], [offset], [locate] method
     */
    val userTransformState: StateFlow<TransformCompat> = _userTransformState

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of baseTransform and userTransform
     */
    val transformState: StateFlow<TransformCompat> = _transformState

    /**
     * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    val minScaleState: StateFlow<Float> = _minScaleState

    /**
     * Medium scale factor, only as a target value for one of when switch scale
     */
    val mediumScaleState: StateFlow<Float> = _mediumScaleState

    /**
     * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    val maxScaleState: StateFlow<Float> = _maxScaleState

    /**
     * The type of transformation currently in progress
     *
     * @see ContinuousTransformType
     */
    val continuousTransformTypeState: StateFlow<Int> = _continuousTransformTypeState

    /**
     * The content region in the container after the baseTransform transformation
     */
    val contentBaseDisplayRectState: StateFlow<IntRectCompat> = _contentBaseDisplayRectState

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    val contentBaseVisibleRectState: StateFlow<IntRectCompat> = _contentBaseVisibleRectState

    /**
     * The content region in the container after the final transform transformation
     */
    val contentDisplayRectState: StateFlow<IntRectCompat> = _contentDisplayRectState

    /**
     * The content is visible region to the user after the final transform transformation
     */
    val contentVisibleRectState: StateFlow<IntRectCompat> = _contentVisibleRectState

    /**
     * Edge state for the current offset
     */
    val scrollEdgeState: StateFlow<ScrollEdge> = _scrollEdgeState

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    val userOffsetBoundsState: StateFlow<IntRectCompat> = _userOffsetBoundsState

    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
            }

            override fun onViewDetachedFromWindow(v: View) {
                clean()
            }
        })

        coroutineScope.launch {
            containerSizeState.collect {
                reset("containerSizeChanged")
            }
        }
        coroutineScope.launch {
            contentSizeState.collect {
                reset("contentSizeChanged")
            }
        }
        coroutineScope.launch {
            contentOriginSizeState.collect {
                reset("contentOriginSizeChanged")
            }
        }

        coroutineScope.launch {
            contentScaleState.collect {
                reset("contentScaleChanged")
            }
        }
        coroutineScope.launch {
            alignmentState.collect {
                reset("alignmentChanged")
            }
        }
        coroutineScope.launch {
            readModeState.collect {
                reset("readModeChanged")
            }
        }
        coroutineScope.launch {
            scalesCalculatorState.collect {
                reset("scalesCalculatorChanged")
            }
        }
        coroutineScope.launch {
            limitOffsetWithinBaseVisibleRectState.collect {
                reset("limitOffsetWithinBaseVisibleRectChanged")
            }
        }
    }


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Reset [transformState] and [minScaleState], [mediumScaleState], [maxScaleState], automatically called when [containerSizeState],
     * [contentSizeState], [contentOriginSizeState], [contentScaleState], [alignmentState], [rotate], [scalesCalculatorState], [readModeState] changes
     */
    fun reset(caller: String) {
        requiredMainThread()
        stopAllAnimation("reset:$caller")

        val containerSize = containerSizeState.value
        val contentSize = contentSizeState.value
        val contentOriginSize = contentOriginSizeState.value
        val readMode = readModeState.value
        val rotation = rotation
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val scalesCalculator = scalesCalculatorState.value

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
            "reset:$caller. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "rotation=${rotation}, " +
                    "scalesCalculator=${scalesCalculator}, " +
                    "readMode=${readMode}. " +
                    "minScale=${initialZoom.minScale.format(4)}, " +
                    "mediumScale=${initialZoom.mediumScale.format(4)}, " +
                    "maxScale=${initialZoom.maxScale.format(4)}, " +
                    "baseTransform=${initialZoom.baseTransform.toShortString()}, " +
                    "initialUserTransform=${initialZoom.userTransform.toShortString()}, " +
                    "transform=${transform.toShortString()}"
        }

        _minScaleState.value = initialZoom.minScale
        _mediumScaleState.value = initialZoom.mediumScale
        _maxScaleState.value = initialZoom.maxScale
        _baseTransformState.value = initialZoom.baseTransform
        updateUserTransform(
            targetUserTransform = initialZoom.userTransform,
            newContinuousTransformType = null,
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
        centroidContentPoint: IntOffsetCompat = contentVisibleRectState.value.center,
        animated: Boolean = false
    ) {
        val containerSize = containerSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val currentBaseTransform = baseTransformState.value
        val currentUserTransform = userTransformState.value
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
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
            newContinuousTransformType = ContinuousTransformType.SCALE,
            animated = animated,
            caller = "scale"
        )
    }

    /**
     * Scale to the next step scale and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * If [threeStepScaleState] is true, it will cycle between [minScaleState], [mediumScaleState], [maxScaleState],
     * otherwise it will only cycle between [minScaleState] and [mediumScaleState]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    fun switchScale(
        centroidContentPoint: IntOffsetCompat = contentVisibleRectState.value.center,
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
        containerSizeState.value.takeIf { it.isNotEmpty() } ?: return
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val currentBaseTransform = baseTransformState.value
        val currentUserTransform = userTransformState.value

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
            newContinuousTransformType = ContinuousTransformType.OFFSET,
            animated = animated,
            caller = "offset"
        )
    }

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    fun locate(
        contentPoint: IntOffsetCompat,
        targetScale: Float = transformState.value.scaleX,
        animated: Boolean = false,
    ) {
        val containerSize = containerSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val rotation = rotation
        val currentBaseTransform = baseTransformState.value
        val currentUserTransform = userTransformState.value

        stopAllAnimation("locate")

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

        val targetUserOffset = calculateLocateUserOffset(
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
            "locate. " +
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
            newContinuousTransformType = ContinuousTransformType.LOCATE,
            animated = animated,
            caller = "locate"
        )
    }

    /**
     * Rotate the content to [targetRotation]
     */
    fun rotate(targetRotation: Int) {
        require(targetRotation % 90 == 0) { "rotation must be in multiples of 90: $targetRotation" }
        val limitedTargetRotation = (targetRotation % 360).let { if (it < 0) 360 + it else it }
        val currentRotation = rotation
        if (currentRotation == limitedTargetRotation) return

        stopAllAnimation("rotate")

        rotation = limitedTargetRotation
        reset("rotate")
    }

    /**
     * Gets the next step scale factor,
     * and if [threeStepScaleState] is true, it will cycle between [minScaleState], [mediumScaleState], [maxScaleState],
     * otherwise it will only loop between [minScaleState], [mediumScaleState].
     */
    fun getNextStepScale(): Float {
        val minScale = minScaleState.value
        val mediumScale = mediumScaleState.value
        val maxScale = maxScaleState.value
        val threeStepScale = threeStepScaleState.value
        val transform = transformState.value
        val stepScales = if (threeStepScale) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, transform.scaleX)
    }

    fun clean() {
        stopAllAnimation("clean")
    }

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: OffsetCompat): IntOffsetCompat {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return IntOffsetCompat.Zero
        val contentSize =
            contentSizeState.value.takeIf { it.isNotEmpty() } ?: return IntOffsetCompat.Zero
        val currentUserTransform = userTransformState.value
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
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
        return canScrollByEdge(scrollEdgeState.value, horizontal, direction)
    }


    /* *************************************** Internal ***************************************** */

    internal fun stopAllAnimation(caller: String) {
        val lastScaleAnimatable = lastScaleAnimatable
        if (lastScaleAnimatable?.running == true) {
            lastScaleAnimatable.stop()
            logger.d { "stopScaleAnimation:$caller" }
        }

        val lastFlingAnimatable = lastFlingAnimatable
        if (lastFlingAnimatable?.running == true) {
            lastFlingAnimatable.stop()
            logger.d { "stopFlingAnimation:$caller" }
        }

        val lastContinuousTransformType = _continuousTransformTypeState.value
        if (lastContinuousTransformType != ContinuousTransformType.NONE) {
            _continuousTransformTypeState.value = ContinuousTransformType.NONE
        }
    }

    /**
     * Roll back to minimum or maximum scaling
     */
    internal fun rollbackScale(lastFocus: OffsetCompat? = null): Boolean {
        val containerSize = containerSizeState.value.takeIf { it.isNotEmpty() } ?: return false
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return false
        val minScale = minScaleState.value
        val maxScale = maxScaleState.value
        val animationSpec = animationSpecState.value

        val currentScale = transformState.value.scaleX
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
                    val nowScale = this@ZoomableEngine.transformState.value.scaleX
                    val addScale = frameScale / nowScale
                    transform(
                        centroid = centroid,
                        panChange = OffsetCompat.Zero,
                        zoomChange = addScale,
                        rotationChange = 0f
                    )
                },
                onEnd = {
                    _continuousTransformTypeState.value = ContinuousTransformType.NONE
                }
            )

            _continuousTransformTypeState.value = ContinuousTransformType.SCALE
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
        containerSizeState.value.takeIf { it.isNotEmpty() } ?: return
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val currentUserTransform = userTransformState.value

        val targetScale = transformState.value.scaleX * zoomChange
        val targetUserScale = targetScale / baseTransformState.value.scaleX
        val limitedTargetUserScale = if (rubberBandScaleState.value) {
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
            newContinuousTransformType = null,
            animated = false,
            caller = "transform"
        )
    }

    internal fun drag(panChange: OffsetCompat) {
        containerSizeState.value.takeIf { it.isNotEmpty() } ?: return
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return
        val currentUserTransform = userTransformState.value

        val currentUserScale = currentUserTransform.scale.scaleX
        val currentUserOffset = currentUserTransform.offset
        val targetUserOffset = currentUserOffset + panChange
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, currentUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(offset = limitedTargetUserOffset)
        logger.d {
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "drag. " +
                    "panChange=${panChange.toShortString()}, " +
                    "targetUserOffset=${targetUserOffset.toShortString()}, " +
                    "limitedTargetUserOffset=${limitedTargetUserOffset.toShortString()}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            newContinuousTransformType = null,
            animated = false,
            caller = "transform"
        )
    }

    internal fun fling(velocity: OffsetCompat): Boolean {
        val containerSize = containerSizeState.value.takeIf { it.isNotEmpty() } ?: return false
        val contentSize = contentSizeState.value.takeIf { it.isNotEmpty() } ?: return false
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val rotation = rotation
        val currentUserTransform = userTransformState.value

        stopAllAnimation("fling")

        val startUserOffset = currentUserTransform.offset
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
        ).let {
            Rect(
                it.left.roundToInt(),
                it.top.roundToInt(),
                it.right.roundToInt(),
                it.bottom.roundToInt()
            )
        }
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
            velocity = velocity.round(),
            onUpdateValue = { value ->
                val targetUserOffset =
                    this@ZoomableEngine.userTransformState.value.copy(offset = value.toOffset())
                updateUserTransform(
                    targetUserTransform = targetUserOffset,
                    newContinuousTransformType = null,
                    animated = false,
                    caller = "fling"
                )
            },
            onEnd = {
                _continuousTransformTypeState.value = ContinuousTransformType.NONE
            }
        )
        _continuousTransformTypeState.value = ContinuousTransformType.FLING
        lastFlingAnimatable?.start()
        return true
    }

    private fun limitUserScale(targetUserScale: Float): Float {
        val minUserScale = minScaleState.value / baseTransformState.value.scaleX
        val maxUserScale = maxScaleState.value / baseTransformState.value.scaleX
        return targetUserScale.coerceIn(minimumValue = minUserScale, maximumValue = maxUserScale)
    }

    private fun limitUserScaleWithRubberBand(targetUserScale: Float): Float {
        val minUserScale = minScaleState.value / baseTransformState.value.scaleX
        val maxUserScale = maxScaleState.value / baseTransformState.value.scaleX
        return limitScaleWithRubberBand(
            currentScale = userTransformState.value.scaleX,
            targetScale = targetUserScale,
            minScale = minUserScale,
            maxScale = maxUserScale
        )
    }

    private fun limitUserOffset(userOffset: OffsetCompat, userScale: Float): OffsetCompat {
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
            rotation = rotation,
            userScale = userScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
        ).round().toRect()      // round() makes sense
        return userOffset.limitTo(userOffsetBounds)
    }

    private fun updateUserTransform(
        targetUserTransform: TransformCompat,
        newContinuousTransformType: Int?,
        animated: Boolean,
        caller: String
    ) {
        if (animated) {
            val currentUserTransform = userTransformState.value
            lastScaleAnimatable = FloatAnimatable(
                view = view,
                startValue = 0f,
                endValue = 1f,
                durationMillis = animationSpecState.value.durationMillis,
                interpolator = animationSpecState.value.interpolator,
                onUpdateValue = { value ->
                    val userTransform = lerp(
                        start = currentUserTransform,
                        stop = targetUserTransform,
                        fraction = value
                    )
                    logger.d {
                        "$caller. animated running. fraction=$value, transform=${userTransform.toShortString()}"
                    }
                    this@ZoomableEngine._userTransformState.value = userTransform
                    updateTransform()
                },
                onEnd = {
                    if (newContinuousTransformType != null) {
                        _continuousTransformTypeState.value = ContinuousTransformType.NONE
                    }
                }
            )
            if (newContinuousTransformType != null) {
                _continuousTransformTypeState.value = newContinuousTransformType
            }
            lastScaleAnimatable?.start()
        } else {
            this._userTransformState.value = targetUserTransform
            updateTransform()
        }
    }

    private fun updateTransform() {
        val userTransform = userTransformState.value
        _transformState.value = baseTransformState.value + userTransform

        _contentBaseDisplayRectState.value = calculateContentBaseDisplayRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
            rotation = rotation,
        ).round()
        _contentBaseVisibleRectState.value = calculateContentBaseVisibleRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
            rotation = rotation,
        ).round()
        _contentDisplayRectState.value = calculateContentDisplayRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()
        _contentVisibleRectState.value = calculateContentVisibleRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()

        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
            rotation = rotation,
            userScale = userTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
        )
        this._userOffsetBoundsState.value = userOffsetBounds.round()

        _scrollEdgeState.value = calculateScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset,
        )
    }
}