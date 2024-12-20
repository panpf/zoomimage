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

@file:Suppress("UnnecessaryVariable", "RedundantConstructorKeyword")

package com.github.panpf.zoomimage.zoom.internal

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.requiredMainThread
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toRect
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.BaseZoomAnimationSpec
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
import com.github.panpf.zoomimage.zoom.contentPointToContainerPoint
import com.github.panpf.zoomimage.zoom.contentPointToTouchPoint
import com.github.panpf.zoomimage.zoom.limitScaleWithRubberBand
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.rtlFlipped
import com.github.panpf.zoomimage.zoom.touchPointToContentPoint
import com.github.panpf.zoomimage.zoom.transformAboutEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
class Zoomable constructor(
    val logger: Logger,
    val module: String,
    val rtlLayoutDirection: Boolean,
    val animationAdapter: AnimationAdapter,
) {

    /* *********************************** Properties initialized by the component ****************************** */

    val containerSizeState = MutableStateFlow(IntSizeCompat.Zero)
    val contentSizeState = MutableStateFlow(IntSizeCompat.Zero)
    val contentOriginSizeState = MutableStateFlow(IntSizeCompat.Zero)


    /* *********************************** Properties configured by the user ****************************** */

    val contentScaleState = MutableStateFlow(ContentScaleCompat.Fit)
    val alignmentState = MutableStateFlow(AlignmentCompat.Center)
    val readModeState = MutableStateFlow<ReadMode?>(null)
    val scalesCalculatorState = MutableStateFlow<ScalesCalculator>(ScalesCalculator.Dynamic)
    val threeStepScaleState = MutableStateFlow(false)
    val rubberBandScaleState = MutableStateFlow(true)
    val oneFingerScaleSpecState = MutableStateFlow(OneFingerScaleSpec.Default)
    val animationSpecState = MutableStateFlow<BaseZoomAnimationSpec?>(null)
    val limitOffsetWithinBaseVisibleRectState = MutableStateFlow(false)
    var containerWhitespaceMultipleState = MutableStateFlow(0f)
    var containerWhitespaceState = MutableStateFlow(ContainerWhitespace.Zero)
    var disabledGestureTypesState = MutableStateFlow(0)


    /* *********************************** Properties readable by the user ******************************* */

    private val _baseTransformState = MutableStateFlow(TransformCompat.Origin)
    private val _userTransformState = MutableStateFlow(TransformCompat.Origin)
    private val _transformState = MutableStateFlow(TransformCompat.Origin)
    private val _minScaleState = MutableStateFlow(1.0f)
    private val _mediumScaleState = MutableStateFlow(1.0f)
    private val _maxScaleState = MutableStateFlow(1.0f)
    private val _contentBaseDisplayRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _contentBaseVisibleRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _contentDisplayRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _contentVisibleRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _scrollEdgeState = MutableStateFlow(ScrollEdge.Default)
    private val _userOffsetBoundsState = MutableStateFlow(IntRectCompat.Zero)
    private val _continuousTransformTypeState = MutableStateFlow(0)
    val baseTransformState: StateFlow<TransformCompat> = _baseTransformState
    val userTransformState: StateFlow<TransformCompat> = _userTransformState
    val transformState: StateFlow<TransformCompat> = _transformState
    val minScaleState: StateFlow<Float> = _minScaleState
    val mediumScaleState: StateFlow<Float> = _mediumScaleState
    val maxScaleState: StateFlow<Float> = _maxScaleState
    val contentBaseDisplayRectState: StateFlow<IntRectCompat> = _contentBaseDisplayRectState
    val contentBaseVisibleRectState: StateFlow<IntRectCompat> = _contentBaseVisibleRectState
    val contentDisplayRectState: StateFlow<IntRectCompat> = _contentDisplayRectState
    val contentVisibleRectState: StateFlow<IntRectCompat> = _contentVisibleRectState
    val userOffsetBoundsState: StateFlow<IntRectCompat> = _userOffsetBoundsState
    val scrollEdgeState: StateFlow<ScrollEdge> = _scrollEdgeState
    val continuousTransformTypeState: StateFlow<Int> = _continuousTransformTypeState

    private var resetParams = ResetParams(
        containerSize = containerSizeState.value,
        contentSize = contentSizeState.value,
        contentOriginSize = contentOriginSizeState.value,
        rotation = 0,
        contentScale = contentScaleState.value,
        alignment = alignmentState.value,
        readMode = readModeState.value,
        scalesCalculator = scalesCalculatorState.value,
        limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
        containerWhitespaceMultiple = containerWhitespaceMultipleState.value,
        containerWhitespace = containerWhitespaceState.value,
    )
    private var lastResetParams: ResetParams? = null
    private var coroutineScope: CoroutineScope? = null
    private var lastInitialUserTransform: TransformCompat = TransformCompat.Origin


    /* *********************************** Interactive with user ******************************* */

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffsetCompat = contentVisibleRectState.value.center,
        animated: Boolean = false,
        animationSpec: BaseZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val currentBaseTransform = baseTransformState.value
        val currentUserTransform = userTransformState.value
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val rotation = resetParams.rotation

        stopAllAnimation("scale")

        val targetUserScale = targetScale / currentBaseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset
        val touchPoint = contentPointToTouchPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment.rtlFlipped(rtlLayoutDirection),
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
            "$module. scale. " +
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
        animationSpec: BaseZoomAnimationSpec? = null,
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
        animationSpec: BaseZoomAnimationSpec? = null,
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
            "$module. offset. " +
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
        animationSpec: BaseZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        val containerSize =
            containerSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSizeState.value.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val rotation = resetParams.rotation
        val currentBaseTransform = baseTransformState.value
        val currentUserTransform = userTransformState.value

        stopAllAnimation("locate")

        val containerPoint = contentPointToContainerPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment.rtlFlipped(rtlLayoutDirection),
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
        val currentRotation = resetParams.rotation
        if (currentRotation == limitedTargetRotation) return@coroutineScope
        resetParams = resetParams.copy(rotation = limitedTargetRotation)
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
        val rotation = resetParams.rotation
        val contentPoint = touchPointToContentPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment.rtlFlipped(rtlLayoutDirection),
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

    fun setCoroutineScope(coroutineScope: CoroutineScope?) {
        this.coroutineScope = coroutineScope

        /*
         * Must be immediate, otherwise the user will see the image move quickly from the top to the center
         */
        coroutineScope?.launch(Dispatchers.Main.immediate) {
            containerSizeState.collect {
                resetParams = resetParams.copy(containerSize = it)
                reset("containerSizeChanged")
            }
        }
        coroutineScope?.launch(Dispatchers.Main.immediate) {
            contentSizeState.collect {
                resetParams = resetParams.copy(contentSize = it)
                reset("contentSizeChanged")
            }
        }
        coroutineScope?.launch(Dispatchers.Main.immediate) {
            contentOriginSizeState.collect {
                resetParams = resetParams.copy(contentOriginSize = it)
                reset("contentOriginSizeChanged")
            }
        }

        coroutineScope?.launch {
            contentScaleState.collect {
                resetParams = resetParams.copy(contentScale = it)
                reset("contentScaleChanged")
            }
        }
        coroutineScope?.launch {
            alignmentState.collect {
                resetParams = resetParams.copy(alignment = it)
                reset("alignmentChanged")
            }
        }
        coroutineScope?.launch {
            readModeState.collect {
                resetParams = resetParams.copy(readMode = it)
                reset("readModeChanged")
            }
        }
        coroutineScope?.launch {
            scalesCalculatorState.collect {
                resetParams = resetParams.copy(scalesCalculator = it)
                reset("scalesCalculatorChanged")
            }
        }
        coroutineScope?.launch {
            limitOffsetWithinBaseVisibleRectState.collect {
                resetParams = resetParams.copy(limitOffsetWithinBaseVisibleRect = it)
                reset("limitOffsetWithinBaseVisibleRectChanged")
            }
        }
        coroutineScope?.launch {
            containerWhitespaceMultipleState.collect {
                resetParams = resetParams.copy(containerWhitespaceMultiple = it)
                reset("containerWhitespaceMultipleChanged")
            }
        }
        coroutineScope?.launch {
            containerWhitespaceState.collect {
                resetParams = resetParams.copy(containerWhitespace = it)
                reset("containerWhitespaceChanged")
            }
        }
    }

    /**
     * Reset [transformState] and [minScaleState], [mediumScaleState], [maxScaleState], automatically called when [containerSizeState],
     * [contentSizeState], [contentOriginSizeState], [contentScaleState], [alignmentState], [rotate], [scalesCalculatorState], [readModeState] changes
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun reset(caller: String) {
        requiredMainThread()
        stopAllAnimation("reset:$caller")

        val paramsChanged = resetParams.different(lastResetParams)
        if (paramsChanged == 0) {
            logger.d { "$module. reset:$caller. skipped. All parameters unchanged" }
            return
        }

        val newInitialZoom = calculateInitialZoom(
            containerSize = resetParams.containerSize,
            contentSize = resetParams.contentSize,
            contentOriginSize = resetParams.contentOriginSize,
            contentScale = resetParams.contentScale,
            alignment = resetParams.alignment.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
            readMode = resetParams.readMode,
            scalesCalculator = resetParams.scalesCalculator,
        )
        val newBaseTransform = newInitialZoom.baseTransform

        val onlyContainerSizeChanged = paramsChanged == 1
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
                containerSize = resetParams.containerSize,
                contentSize = resetParams.contentSize,
                contentScale = resetParams.contentScale,
                alignment = resetParams.alignment.rtlFlipped(rtlLayoutDirection),
                rotation = resetParams.rotation,
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
            "$module. reset:$caller. " +
                    "containerSize=${resetParams.containerSize.toShortString()}, " +
                    "contentSize=${resetParams.contentSize.toShortString()}, " +
                    "contentOriginSize=${resetParams.contentOriginSize.toShortString()}, " +
                    "contentScale=${resetParams.contentScale.name}, " +
                    "alignment=${resetParams.alignment.name}, " +
                    "rotation=${resetParams.rotation}, " +
                    "scalesCalculator=${resetParams.scalesCalculator}, " +
                    "readMode=${resetParams.readMode}. " +
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
            containerSize = resetParams.containerSize,
            contentSize = resetParams.contentSize,
            contentScale = resetParams.contentScale,
            alignment = resetParams.alignment.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
        ).round()
        _contentBaseVisibleRectState.value = calculateContentBaseVisibleRect(
            containerSize = resetParams.containerSize,
            contentSize = resetParams.contentSize,
            contentScale = resetParams.contentScale,
            alignment = resetParams.alignment.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
        ).round()
        _baseTransformState.value = newBaseTransform
        updateUserTransform(newUserTransform)
        lastResetParams = resetParams
    }

    fun setContinuousTransformType(continuousTransformType: Int) {
        _continuousTransformTypeState.value = continuousTransformType
    }

    fun stopAllAnimation(caller: String) {
        if (animationAdapter.stopAnimation()) {
            logger.d { "$module. stopTransformAnimation:$caller" }
        }

        if (animationAdapter.stopFlingAnimation()) {
            logger.d { "$module. stopFlingAnimation:$caller" }
        }

        val lastContinuousTransformType = continuousTransformTypeState.value
        if (lastContinuousTransformType != 0) {
            _continuousTransformTypeState.value = 0
        }
    }

    /**
     * Roll back to minimum or maximum scaling
     */
    suspend fun rollbackScale(focus: OffsetCompat? = null): Boolean = coroutineScope {
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
                "$module. rollbackScale. " +
                        "focus=${focus?.toShortString()}. " +
                        "startScale=${startScale.format(4)}, " +
                        "endScale=${endScale.format(4)}"
            }
            val centroid = focus ?: containerSize.toSize().center
            suspendCancellableCoroutine { continuation ->
                _continuousTransformTypeState.value = ContinuousTransformType.SCALE
                animationAdapter.startAnimation(
                    animationSpec = animationSpec,
                    onProgress = { value ->
                        val frameScale = lerp(
                            start = startScale,
                            stop = endScale,
                            fraction = value
                        )
                        val nowScale = this@Zoomable.transformState.value.scaleX
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

                continuation.invokeOnCancellation {
                    animationAdapter.stopAnimation()
                }
            }
        }
        targetScale != null
    }

    suspend fun gestureTransform(
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
            "$module. transform. " +
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

    suspend fun fling(velocity: OffsetCompat): Boolean = coroutineScope {
        val containerSize = containerSizeState.value.takeIf { it.isNotEmpty() }
            ?: return@coroutineScope false
        val contentSize = contentSizeState.value.takeIf { it.isNotEmpty() }
            ?: return@coroutineScope false
        val contentScale = contentScaleState.value
        val alignment = alignmentState.value
        val rotation = resetParams.rotation
        val currentUserTransform = userTransformState.value

        stopAllAnimation("fling")

        val startUserOffset = currentUserTransform.offset
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment.rtlFlipped(rtlLayoutDirection),
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
            containerWhitespace = resetParams.realContainerWhitespace.rtlFlipped(rtlLayoutDirection),
        ).let {
            IntRectCompat(
                /* left = */ it.left.roundToInt(),
                /* top = */ it.top.roundToInt(),
                /* right = */ it.right.roundToInt(),
                /* bottom = */ it.bottom.roundToInt()
            )
        }
        logger.d {
            "$module. fling. start. " +
                    "start=${startUserOffset.toShortString()}, " +
                    "bounds=${userOffsetBounds.toShortString()}, " +
                    "velocity=${velocity.toShortString()}"
        }
        suspendCancellableCoroutine { continuation ->
            _continuousTransformTypeState.value = ContinuousTransformType.FLING
            animationAdapter.startFlingAnimation(
                start = startUserOffset.round(),
                bounds = userOffsetBounds,
                velocity = velocity.round(),
                onUpdateValue = { value ->
                    val newUserOffset =
                        this@Zoomable.userTransformState.value.copy(offset = value.toOffset())
                    logger.d {
                        "$module. fling. running. " +
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

            continuation.invokeOnCancellation {
                animationAdapter.stopFlingAnimation()
            }
        }
        true
    }

    fun checkSupportGestureType(@GestureType gestureType: Int): Boolean =
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
            alignment = alignmentState.value.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
            userScale = userScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
            containerWhitespace = resetParams.realContainerWhitespace.rtlFlipped(rtlLayoutDirection),
        ).round().toRect()      // round() makes sense
        return userOffset.limitTo(userOffsetBounds)
    }

    private suspend fun animatedUpdateUserTransform(
        targetUserTransform: TransformCompat,
        newContinuousTransformType: Int?,
        animationSpec: BaseZoomAnimationSpec?,
        caller: String
    ) {
        suspendCancellableCoroutine { continuation ->
            val finalAnimationSpec = animationSpec ?: animationSpecState.value
            val startUserTransform = userTransformState.value
            if (newContinuousTransformType != null) {
                _continuousTransformTypeState.value = newContinuousTransformType
            }
            animationAdapter.startAnimation(
                animationSpec = finalAnimationSpec,
                onProgress = { value ->
                    val userTransform = lerp(
                        start = startUserTransform,
                        stop = targetUserTransform,
                        fraction = value
                    )
                    logger.d {
                        "$module. $caller. animated running. fraction=$value, transform=${userTransform.toShortString()}"
                    }
                    this@Zoomable._userTransformState.value = userTransform
                    updateTransform()
                },
                onEnd = {
                    if (newContinuousTransformType != null) {
                        _continuousTransformTypeState.value = 0
                    }
                    continuation.resumeWith(Result.success(0))
                }
            )
            continuation.invokeOnCancellation {
                animationAdapter.stopAnimation()
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
            alignment = alignmentState.value.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()
        _contentVisibleRectState.value = calculateContentVisibleRect(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).round()

        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSizeState.value,
            contentSize = contentSizeState.value,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value.rtlFlipped(rtlLayoutDirection),
            rotation = resetParams.rotation,
            userScale = userTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRectState.value,
            containerWhitespace = resetParams.realContainerWhitespace.rtlFlipped(rtlLayoutDirection),
        )
        this._userOffsetBoundsState.value = userOffsetBounds.round()

        _scrollEdgeState.value = calculateScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset,
        )
    }
}