/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

import android.graphics.Rect
import android.view.View
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
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
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.util.format
import com.github.panpf.zoomimage.view.util.requiredMainThread
import com.github.panpf.zoomimage.view.util.rtlFlipped
import com.github.panpf.zoomimage.view.zoom.internal.FlingAnimatable
import com.github.panpf.zoomimage.view.zoom.internal.FloatAnimatable
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import com.github.panpf.zoomimage.zoom.calculateContentBaseDisplayRect
import com.github.panpf.zoomimage.zoom.calculateContentBaseVisibleRect
import com.github.panpf.zoomimage.zoom.calculateContentDisplayRect
import com.github.panpf.zoomimage.zoom.calculateContentVisibleRect
import com.github.panpf.zoomimage.zoom.calculateInitialZoom
import com.github.panpf.zoomimage.zoom.calculateLocateUserOffset
import com.github.panpf.zoomimage.zoom.calculateNextStepScale
import com.github.panpf.zoomimage.zoom.calculateRestoreContentVisibleCenterUserTransform
import com.github.panpf.zoomimage.zoom.calculateScaleUserOffset
import com.github.panpf.zoomimage.zoom.calculateScrollEdge
import com.github.panpf.zoomimage.zoom.calculateTransformOffset
import com.github.panpf.zoomimage.zoom.calculateUserOffsetBounds
import com.github.panpf.zoomimage.zoom.canScrollByEdge
import com.github.panpf.zoomimage.zoom.checkParamsChanges
import com.github.panpf.zoomimage.zoom.contentPointToContainerPoint
import com.github.panpf.zoomimage.zoom.contentPointToTouchPoint
import com.github.panpf.zoomimage.zoom.isEmpty
import com.github.panpf.zoomimage.zoom.limitScaleWithRubberBand
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.touchPointToContentPoint
import com.github.panpf.zoomimage.zoom.transformAboutEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.roundToInt

/**
 * Engines that control scale, pan, rotation
 *
 * @see com.github.panpf.zoomimage.view.test.zoom.ZoomableEngineTest
 */
class ZoomableEngine(val logger: Logger, val view: View) {

    private var coroutineScope: CoroutineScope? = null
    private var lastScaleAnimatable: FloatAnimatable? = null
    private var lastFlingAnimatable: FlingAnimatable? = null
    private var lastInitialUserTransform: TransformCompat = TransformCompat.Origin
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

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    val oneFingerScaleSpecState = MutableStateFlow(OneFingerScaleSpec.Default)

    /**
     * The animation configuration for the zoom animation
     */
    val animationSpecState = MutableStateFlow(ZoomAnimationSpec.Default)

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    val limitOffsetWithinBaseVisibleRectState = MutableStateFlow(false)

    /**
     * Add whitespace around containers based on container size
     */
    var containerWhitespaceMultipleState = MutableStateFlow(0f)

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultipleState]
     */
    var containerWhitespaceState = MutableStateFlow(ContainerWhitespace.Zero)

    /**
     * Disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.GestureType
     */
    var disabledGestureTypesState = MutableStateFlow(0)

    private var lastContainerSize: IntSizeCompat = containerSizeState.value
    private var lastContentSize: IntSizeCompat = contentSizeState.value
    private var lastContentOriginSize: IntSizeCompat = contentOriginSizeState.value
    private var lastContentScale: ContentScaleCompat = contentScaleState.value
    private var lastAlignment: AlignmentCompat = alignmentState.value
    private var lastRotation: Int = rotation
    private var lastReadMode: ReadMode? = readModeState.value
    private var lastScalesCalculator: ScalesCalculator = scalesCalculatorState.value
    private var lastLimitOffsetWithinBaseVisibleRect: Boolean =
        limitOffsetWithinBaseVisibleRectState.value
    private var lastContainerWhitespace: ContainerWhitespace = calculateContainerWhitespace()


    /* *********************************** Information properties ******************************* */

    private val _baseTransformState = MutableStateFlow(TransformCompat.Origin)
    private val _userTransformState = MutableStateFlow(TransformCompat.Origin)
    private val _transformState = MutableStateFlow(TransformCompat.Origin)
    private val _minScaleState = MutableStateFlow(1.0f)
    private val _mediumScaleState = MutableStateFlow(1.0f)
    private val _maxScaleState = MutableStateFlow(1.0f)
    internal val _continuousTransformTypeState = MutableStateFlow(0)
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
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    val userOffsetBoundsState: StateFlow<IntRectCompat> = _userOffsetBoundsState

    /**
     * Edge state for the current offset
     */
    val scrollEdgeState: StateFlow<ScrollEdge> = _scrollEdgeState

    /**
     * The type of transformation currently in progress
     *
     * @see ContinuousTransformType
     */
    val continuousTransformTypeState: StateFlow<Int> = _continuousTransformTypeState


    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttachToWindow()
            }

            override fun onViewDetachedFromWindow(v: View) {
                onDetachFromWindow()
            }
        })
        if (view.isAttachedToWindow) {
            onAttachToWindow()
        }
    }


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Reset [transformState] and [minScaleState], [mediumScaleState], [maxScaleState], automatically called when [containerSizeState],
     * [contentSizeState], [contentOriginSizeState], [contentScaleState], [alignmentState], [rotate], [scalesCalculatorState], [readModeState] changes
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun reset(caller: String): Unit = coroutineScope {
        requiredMainThread()
        stopAllAnimation("reset:$caller")
        view.layoutDirection
        val containerSize = containerSizeState.value
        val contentSize = contentSizeState.value
        val contentOriginSize = contentOriginSizeState.value
        val readMode = readModeState.value
        val rotation = rotation
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val scalesCalculator = scalesCalculatorState.value
        val limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value
        val containerWhitespace = calculateContainerWhitespace()
        val lastContainerSize = lastContainerSize
        val lastContentSize = lastContentSize
        val lastContentOriginSize = lastContentOriginSize
        val lastContentScale = lastContentScale
        val lastAlignment = lastAlignment
        val lastReadMode = lastReadMode
        val lastRotation = lastRotation
        val lastScalesCalculator = lastScalesCalculator
        val lastLimitOffsetWithinBaseVisibleRect = lastLimitOffsetWithinBaseVisibleRect
        val lastContainerWhitespace = lastContainerWhitespace
        val paramsChanges = checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = lastContainerSize,
            lastContentSize = lastContentSize,
            lastContentOriginSize = lastContentOriginSize,
            lastContentScale = lastContentScale,
            lastAlignment = lastAlignment,
            lastRotation = lastRotation,
            lastReadMode = lastReadMode,
            lastScalesCalculator = lastScalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = lastLimitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = lastContainerWhitespace,
        )
        if (paramsChanges == 0) {
            logger.d { "ZoomableEngine. reset:$caller. skipped. All parameters unchanged" }
            return@coroutineScope
        }

        val newInitialZoom = calculateInitialZoom(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment.rtlFlipped(view.layoutDirection),
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
        )
        val newBaseTransform = newInitialZoom.baseTransform

        val onlyContainerSizeChanged = paramsChanges == 1
        val lastInitialUserTransform = lastInitialUserTransform
        val lastUserTransform = userTransformState.value
        val thereAreUserActions = !transformAboutEquals(
            one = lastInitialUserTransform,
            two = lastUserTransform
        )
        val newUserTransform = if (onlyContainerSizeChanged && thereAreUserActions) {
            val lastTransform = transformState.value
            val lastContentVisibleCenter = contentVisibleRectState.value.center
            calculateRestoreContentVisibleCenterUserTransform(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment.rtlFlipped(view.layoutDirection),
                rotation = rotation,
                newBaseTransform = newBaseTransform,
                lastTransform = lastTransform,
                lastContentVisibleCenter = lastContentVisibleCenter,
            ).let {
                val limitUserOffset = limitUserOffset(
                    userOffset = it.offset,
                    userScale = it.scaleX
                )
                it.copy(offset = limitUserOffset)
            }
        } else {
            newInitialZoom.userTransform
        }

        logger.d {
            val transform = newBaseTransform + newUserTransform
            "ZoomableEngine. reset:$caller. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "rotation=${rotation}, " +
                    "scalesCalculator=${scalesCalculator}, " +
                    "readMode=${readMode}. " +
                    "minScale=${newInitialZoom.minScale.format(4)}, " +
                    "mediumScale=${newInitialZoom.mediumScale.format(4)}, " +
                    "maxScale=${newInitialZoom.maxScale.format(4)}, " +
                    "baseTransform=${newBaseTransform.toShortString()}, " +
                    "userTransform=${newUserTransform.toShortString()}, " +
                    "transform=${transform.toShortString()}"
        }

        _minScaleState.value = newInitialZoom.minScale
        _mediumScaleState.value = newInitialZoom.mediumScale
        _maxScaleState.value = newInitialZoom.maxScale
        _contentBaseDisplayRectState.value = calculateContentBaseDisplayRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(view.layoutDirection),
            rotation = rotation,
        ).round()
        _contentBaseVisibleRectState.value = calculateContentBaseVisibleRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(view.layoutDirection),
            rotation = rotation,
        ).round()
        _baseTransformState.value = newBaseTransform
        updateUserTransform(newUserTransform)

        // TODO Improve it. Create a special parameter class to encapsulate it
        this@ZoomableEngine.lastInitialUserTransform = newInitialZoom.userTransform
        this@ZoomableEngine.lastContainerSize = containerSize
        this@ZoomableEngine.lastContentSize = contentSize
        this@ZoomableEngine.lastContentOriginSize = contentOriginSize
        this@ZoomableEngine.lastContentScale = contentScale
        this@ZoomableEngine.lastAlignment = alignment
        this@ZoomableEngine.lastReadMode = readMode
        this@ZoomableEngine.lastRotation = rotation
        this@ZoomableEngine.lastScalesCalculator = scalesCalculator
        this@ZoomableEngine.lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
        this@ZoomableEngine.lastContainerWhitespace = containerWhitespace
    }

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffsetCompat = contentVisibleRectState.value.center,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
            alignment = alignment.rtlFlipped(view.layoutDirection),
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
            "ZoomableEngine. scale. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "centroidContentPoint=${centroidContentPoint.toShortString()}, " +
                    "animated=${animated}. " +
                    "touchPoint=${touchPoint.toShortString()}, " +
                    "targetUserScale=${targetUserScale.format(4)}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${limitedAddUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }
        if (animated) {
            animatedUpdateUserTransform(
                targetUserTransform = limitedTargetUserTransform,
                newContinuousTransformType = ContinuousTransformType.SCALE,
                animationSpec = animationSpec,
                caller = "scale"
            )
        } else {
            updateUserTransform(limitedTargetUserTransform)
        }

        true
    }

    /**
     * Scale to the next step scale and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * If [threeStepScaleState] is true, it will cycle between [minScaleState], [mediumScaleState], [maxScaleState],
     * otherwise it will only cycle between [minScaleState] and [mediumScaleState]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun switchScale(
        centroidContentPoint: IntOffsetCompat = contentVisibleRectState.value.center,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Float? {
        val nextScale = getNextStepScale()
        val scaleResult = scale(
            targetScale = nextScale,
            centroidContentPoint = centroidContentPoint,
            animationSpec = animationSpec,
            animated = animated
        )
        return if (scaleResult) nextScale else null
    }

    /**
     * Pan the image to the [targetOffset] position, and animation occurs when [animated] is true
     */
    suspend fun offset(
        targetOffset: OffsetCompat,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
            "ZoomableEngine. offset. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "animated=${animated}. " +
                    "targetUserOffset=${targetUserOffset.toShortString()}, " +
                    "currentUserScale=${currentUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        if (animated) {
            animatedUpdateUserTransform(
                targetUserTransform = limitedTargetUserTransform,
                newContinuousTransformType = ContinuousTransformType.OFFSET,
                animationSpec = animationSpec,
                caller = "offset"
            )
        } else {
            updateUserTransform(limitedTargetUserTransform)
        }

        true
    }

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    suspend fun locate(
        contentPoint: IntOffsetCompat,
        targetScale: Float = transformState.value.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
            alignment = alignment.rtlFlipped(view.layoutDirection),
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
            "ZoomableEngine. locate. " +
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

        if (animated) {
            animatedUpdateUserTransform(
                targetUserTransform = limitedTargetUserTransform,
                newContinuousTransformType = ContinuousTransformType.LOCATE,
                animationSpec = animationSpec,
                caller = "locate"
            )
        } else {
            updateUserTransform(limitedTargetUserTransform)
        }

        true
    }

    /**
     * Rotate the content to [targetRotation]
     */
    suspend fun rotate(targetRotation: Int): Unit = coroutineScope {
        require(targetRotation % 90 == 0) { "rotation must be in multiples of 90: $targetRotation" }
        val limitedTargetRotation = (targetRotation % 360).let { if (it < 0) 360 + it else it }
        val currentRotation = rotation
        if (currentRotation == limitedTargetRotation) return@coroutineScope
        rotation = limitedTargetRotation
        reset("rotationChanged")
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
            alignment = alignment.rtlFlipped(view.layoutDirection),
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

    private fun onAttachToWindow() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope

        // Must be immediate, otherwise the user will see the image move quickly from the top to the center
        coroutineScope.launch(Dispatchers.Main.immediate) {
            containerSizeState.collect {
                reset("containerSizeChanged")
            }
        }
        // Must be immediate, otherwise the user will see the image move quickly from the top to the center
        coroutineScope.launch(Dispatchers.Main.immediate) {
            contentSizeState.collect {
                reset("contentSizeChanged")
            }
        }
        // Must be immediate, otherwise the user will see the image move quickly from the top to the center
        coroutineScope.launch(Dispatchers.Main.immediate) {
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
        coroutineScope.launch {
            containerWhitespaceMultipleState.collect {
                reset("containerWhitespaceMultipleChanged")
            }
        }
        coroutineScope.launch {
            containerWhitespaceState.collect {
                reset("containerWhitespaceChanged")
            }
        }
    }

    private fun onDetachFromWindow() {
        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) {
            coroutineScope.cancel("onDetachFromWindow")
            this.coroutineScope = null
        }
    }

    internal fun stopAllAnimation(caller: String) {
        val lastScaleAnimatable = lastScaleAnimatable
        if (lastScaleAnimatable?.running == true) {
            lastScaleAnimatable.stop()
            logger.d { "ZoomableEngine. stopScaleAnimation:$caller" }
        }

        val lastFlingAnimatable = lastFlingAnimatable
        if (lastFlingAnimatable?.running == true) {
            lastFlingAnimatable.stop()
            logger.d { "ZoomableEngine. stopFlingAnimation:$caller" }
        }

        val lastContinuousTransformType = _continuousTransformTypeState.value
        if (lastContinuousTransformType != 0) {
            _continuousTransformTypeState.value = 0
        }
    }

    /**
     * Roll back to minimum or maximum scaling
     */
    internal suspend fun rollbackScale(focus: OffsetCompat? = null): Boolean = coroutineScope {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
                "ZoomableEngine. rollbackScale. " +
                        "focus=${focus?.toShortString()}. " +
                        "startScale=${startScale.format(4)}, " +
                        "endScale=${endScale.format(4)}"
            }
            val centroid = focus ?: containerSize.toSize().center
            suspendCancellableCoroutine { continuation ->
                val scaleAnimatable = FloatAnimatable(
                    view = view,
                    startValue = 0f,
                    endValue = 1f,
                    durationMillis = animationSpec.durationMillis,
                    interpolator = animationSpec.interpolator,
                    onUpdateValue = { value ->
                        val frameScale = com.github.panpf.zoomimage.view.util.lerp(
                            start = startScale,
                            stop = endScale,
                            fraction = value
                        )
                        val nowScale = this@ZoomableEngine.transformState.value.scaleX
                        val addScale = frameScale / nowScale
                        coroutineScope?.launch {
                            gestureTransform(
                                centroid = centroid,
                                panChange = OffsetCompat.Zero,
                                zoomChange = addScale,
                                rotationChange = 0f
                            )
                        }
                    },
                    onEnd = {
                        _continuousTransformTypeState.value = 0
                        continuation.resumeWith(Result.success(0))
                    }
                )
                lastScaleAnimatable = scaleAnimatable

                _continuousTransformTypeState.value = ContinuousTransformType.SCALE
                scaleAnimatable.start()

                continuation.invokeOnCancellation {
                    scaleAnimatable.stop()
                }
            }
        }
        targetScale != null
    }

    internal suspend fun gestureTransform(
        centroid: OffsetCompat,
        panChange: OffsetCompat,
        zoomChange: Float,
        rotationChange: Float
    ): Unit = coroutineScope {
        containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope
        contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope
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
            "ZoomableEngine. transform. " +
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

        updateUserTransform(limitedTargetUserTransform)
    }

    internal suspend fun fling(velocity: OffsetCompat): Boolean = coroutineScope {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
            alignment = alignment.rtlFlipped(view.layoutDirection),
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(view.layoutDirection),
        ).let {
            Rect(
                /* left = */ it.left.roundToInt(),
                /* top = */ it.top.roundToInt(),
                /* right = */ it.right.roundToInt(),
                /* bottom = */ it.bottom.roundToInt()
            )
        }
        logger.d {
            "ZoomableEngine. fling. start. " +
                    "start=${startUserOffset.toShortString()}, " +
                    "bounds=${userOffsetBounds.toShortString()}, " +
                    "velocity=${velocity.toShortString()}"
        }
        suspendCancellableCoroutine { continuation ->
            val flingAnimatable = FlingAnimatable(
                view = view,
                start = startUserOffset.round(),
                bounds = userOffsetBounds,
                velocity = velocity.round(),
                onUpdateValue = { value ->
                    val newUserOffset =
                        this@ZoomableEngine.userTransformState.value.copy(offset = value.toOffset())
                    logger.d {
                        "ZoomableEngine. fling. running. " +
                                "velocity=$velocity. " +
                                "startUserOffset=${startUserOffset.toShortString()}, " +
                                "currentUserOffset=${newUserOffset.toShortString()}"
                    }
                    updateUserTransform(newUserOffset)
                },
                onEnd = {
                    _continuousTransformTypeState.value = 0
                    continuation.resumeWith(Result.success(0))
                }
            )
            lastFlingAnimatable = flingAnimatable

            _continuousTransformTypeState.value = ContinuousTransformType.FLING
            flingAnimatable.start()

            continuation.invokeOnCancellation {
                flingAnimatable.stop()
            }
        }
        true
    }

    internal fun checkSupportGestureType(@GestureType gestureType: Int): Boolean =
        disabledGestureTypesState.value.and(gestureType) == 0

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
            maxScale = maxUserScale,
            rubberBandRatio = 2f,
        )
    }

    private fun limitUserOffset(userOffset: OffsetCompat, userScale: Float): OffsetCompat {
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(view.layoutDirection),
            rotation = rotation,
            userScale = userScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(view.layoutDirection),
        ).round().toRect()      // round() makes sense
        return userOffset.limitTo(userOffsetBounds)
    }

    private suspend fun animatedUpdateUserTransform(
        targetUserTransform: TransformCompat,
        newContinuousTransformType: Int?,
        animationSpec: ZoomAnimationSpec?,
        caller: String
    ) {
        val finalAnimationSpec = animationSpec ?: animationSpecState.value
        val currentUserTransform = userTransformState.value
        suspendCancellableCoroutine { continuation ->
            val scaleAnimatable = FloatAnimatable(
                view = view,
                startValue = 0f,
                endValue = 1f,
                durationMillis = finalAnimationSpec.durationMillis,
                interpolator = finalAnimationSpec.interpolator,
                onUpdateValue = { value ->
                    val userTransform = lerp(
                        start = currentUserTransform,
                        stop = targetUserTransform,
                        fraction = value
                    )
                    logger.d {
                        "ZoomableEngine. $caller. animated running. fraction=$value, transform=${userTransform.toShortString()}"
                    }
                    this@ZoomableEngine._userTransformState.value = userTransform
                    updateTransform()
                },
                onEnd = {
                    if (newContinuousTransformType != null) {
                        _continuousTransformTypeState.value = 0
                    }
                    continuation.resumeWith(Result.success(0))
                }
            )
            lastScaleAnimatable = scaleAnimatable

            if (newContinuousTransformType != null) {
                _continuousTransformTypeState.value = newContinuousTransformType
            }
            scaleAnimatable.start()

            continuation.invokeOnCancellation {
                scaleAnimatable.stop()
            }
        }
    }

    private fun updateUserTransform(targetUserTransform: TransformCompat) {
        this._userTransformState.value = targetUserTransform
        updateTransform()
    }

    private fun updateTransform() {
        val userTransform = userTransformState.value
        _transformState.value = baseTransformState.value + userTransform

        _contentDisplayRectState.value = calculateContentDisplayRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(view.layoutDirection),
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()
        _contentVisibleRectState.value = calculateContentVisibleRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(view.layoutDirection),
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()

        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(view.layoutDirection),
            rotation = rotation,
            userScale = userTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(view.layoutDirection),
        )
        this._userOffsetBoundsState.value = userOffsetBounds.round()

        _scrollEdgeState.value = calculateScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset,
        )
    }

    private fun calculateContainerWhitespace(): ContainerWhitespace {
        val containerWhitespace = containerWhitespaceState.value
        val containerSize = containerSizeState.value
        val containerWhitespaceMultiple = containerWhitespaceMultipleState.value
        return if (!containerWhitespace.isEmpty()) {
            containerWhitespace
        } else if (containerSize.isNotEmpty() && containerWhitespaceMultiple != 0f) {
            ContainerWhitespace(
                horizontal = containerSize.width * containerWhitespaceMultiple,
                vertical = containerSize.height * containerWhitespaceMultiple
            )
        } else {
            ContainerWhitespace.Zero
        }
    }

    override fun toString(): String =
        "ZoomableEngine(" +
                "containerSize=${containerSizeState.value.toShortString()}, " +
                "contentSize=${contentSizeState.value.toShortString()}, " +
                "contentOriginSize=${contentOriginSizeState.value.toShortString()}, " +
                "contentScale=${contentScaleState.value.name}, " +
                "alignment=${alignmentState.value.name}, " +
                "minScale=${minScaleState.value.format(4)}, " +
                "mediumScale=${mediumScaleState.value.format(4)}, " +
                "maxScale=${maxScaleState.value.format(4)}, " +
                "transform=${transformState.value.toShortString()}" +
                ")"
}