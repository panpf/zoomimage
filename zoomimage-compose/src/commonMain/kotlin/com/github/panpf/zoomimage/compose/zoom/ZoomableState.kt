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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.round
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.util.name
import com.github.panpf.zoomimage.compose.util.roundToPlatform
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toCompatOffset
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.util.toShortString
import com.github.panpf.zoomimage.compose.zoom.internal.ComposeAnimationAdapter
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.MouseWheelScaleCalculator
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import com.github.panpf.zoomimage.zoom.internal.ZoomableCore

/**
 * Creates and remember a [ZoomableState] that can be used to control the scale, pan, rotation of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ZoomableStateTest.testRememberZoomableState
 */
@Composable
fun rememberZoomableState(logger: Logger = rememberZoomImageLogger()): ZoomableState {
    val zoomableState = remember(logger) {
        ZoomableState(logger)
    }
    return zoomableState
}

/**
 * A state object that can be used to control the scale, pan, rotation of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ZoomableStateTest
 */
@Stable
@Suppress("RedundantConstructorKeyword")
class ZoomableState constructor(val logger: Logger) : RememberObserver {

    private var rememberedCount = 0
    private val zoomableCore = ZoomableCore(
        logger = logger,
        module = "ZoomableState",
        animationAdapter = ComposeAnimationAdapter(),
        onTransformChanged = {
            baseTransform = it.baseTransform.toPlatform()
            userTransform = it.userTransform.toPlatform()
            transform = it.transform.toPlatform()
            minScale = it.minScale
            mediumScale = it.mediumScale
            maxScale = it.maxScale
            contentBaseDisplayRectF = it.contentBaseDisplayRect.toPlatform()
            contentBaseDisplayRect = it.contentBaseDisplayRect.roundToPlatform()
            contentBaseVisibleRectF = it.contentBaseVisibleRect.toPlatform()
            contentBaseVisibleRect = it.contentBaseVisibleRect.roundToPlatform()
            contentDisplayRectF = it.contentDisplayRect.toPlatform()
            contentDisplayRect = it.contentDisplayRect.roundToPlatform()
            contentVisibleRectF = it.contentVisibleRect.toPlatform()
            contentVisibleRect = it.contentVisibleRect.roundToPlatform()
            sourceScaleFactor = it.sourceScaleFactor.toPlatform()
            sourceVisibleRectF = it.sourceVisibleRect.toPlatform()
            sourceVisibleRect = it.sourceVisibleRect.roundToPlatform()
            userOffsetBoundsRectF = it.userOffsetBoundsRect.toPlatform()
            userOffsetBoundsRect = it.userOffsetBoundsRect.roundToPlatform()
            userOffsetBounds = it.userOffsetBoundsRect.roundToPlatform()
            scrollEdge = it.scrollEdge
            continuousTransformType = it.continuousTransformType
        }
    )


    /* *********************************** Configured properties ****************************** */

    private val _containerSizeState: MutableState<IntSize> =
        mutableStateOf(value = zoomableCore.containerSize.toPlatform())
    private val _contentSizeState: MutableState<IntSize> =
        mutableStateOf(value = zoomableCore.contentSize.toPlatform())
    private val _contentOriginSizeState: MutableState<IntSize> =
        mutableStateOf(value = zoomableCore.contentOriginSize.toPlatform())
    private val _contentScaleState: MutableState<ContentScale> =
        mutableStateOf(value = zoomableCore.contentScale.toPlatform())
    private val _alignmentState: MutableState<Alignment> =
        mutableStateOf(value = zoomableCore.alignment.toPlatform())
    private val _layoutDirectionState: MutableState<LayoutDirection> = mutableStateOf(
        value = if (zoomableCore.rtlLayoutDirection) LayoutDirection.Rtl else LayoutDirection.Ltr
    )
    private val _readModeState: MutableState<ReadMode?> =
        mutableStateOf(value = zoomableCore.readMode)
    private val _scalesCalculatorState: MutableState<ScalesCalculator> =
        mutableStateOf(value = zoomableCore.scalesCalculator)
    private val _threeStepScaleState: MutableState<Boolean> =
        mutableStateOf(value = zoomableCore.threeStepScale)
    private val _rubberBandScaleState: MutableState<Boolean> =
        mutableStateOf(value = zoomableCore.rubberBandScale)
    private val _oneFingerScaleSpecState: MutableState<OneFingerScaleSpec> =
        mutableStateOf(value = zoomableCore.oneFingerScaleSpec)
    private val _animationSpecState: MutableState<ZoomAnimationSpec> =
        mutableStateOf(value = ZoomAnimationSpec.Default)
    private val _limitOffsetWithinBaseVisibleRectState: MutableState<Boolean> =
        mutableStateOf(value = zoomableCore.limitOffsetWithinBaseVisibleRect)
    private val _containerWhitespaceMultipleState: MutableState<Float> =
        mutableStateOf(value = zoomableCore.containerWhitespaceMultiple)
    private val _containerWhitespaceState: MutableState<ContainerWhitespace> =
        mutableStateOf(value = zoomableCore.containerWhitespace)
    private val _keepTransformWhenSameAspectRatioContentSizeChangedState: MutableState<Boolean> =
        mutableStateOf(value = zoomableCore.keepTransformWhenSameAspectRatioContentSizeChanged)
    private val _disabledGestureTypesState: MutableIntState = mutableIntStateOf(value = 0)
    private val _reverseMouseWheelScaleState: MutableState<Boolean> = mutableStateOf(value = false)

    /**
     * The size of the container that holds the content, this is usually the size of the ZoomImage component, setup by the ZoomImage component
     */
    val containerSize: IntSize by _containerSizeState

    /**
     * The size of the content, usually Painter.intrinsicSize.round(), setup by the ZoomImage component
     */
    val contentSize: IntSize by _contentSizeState

    /**
     * The original size of the content, it is usually set by [SubsamplingState] after parsing the original size of the image.
     * If not empty, it means that the subsampling function has been enabled
     */
    val contentOriginSize: IntSize by _contentOriginSizeState

    /**
     * The scale of the content, usually set by ZoomImage component
     */
    val contentScale: ContentScale by _contentScaleState

    /**
     * The alignment of the content, usually set by ZoomImage component
     */
    val alignment: Alignment by _alignmentState

    /**
     * The layout direction of the content, usually set by ZoomImage component
     */
    val layoutDirection: LayoutDirection by _layoutDirectionState

    /**
     * Setup whether to enable read mode and configure read mode
     */
    val readMode: ReadMode? by _readModeState

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    val scalesCalculator: ScalesCalculator by _scalesCalculatorState

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    val threeStepScale: Boolean by _threeStepScaleState

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will rollback to the minimum or maximum zoom factor
     */
    val rubberBandScale: Boolean by _rubberBandScaleState

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    val oneFingerScaleSpec: OneFingerScaleSpec by _oneFingerScaleSpecState

    /**
     * The animation configuration for the zoom animation
     */
    val animationSpec: ZoomAnimationSpec by _animationSpecState

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    val limitOffsetWithinBaseVisibleRect: Boolean by _limitOffsetWithinBaseVisibleRectState

    /**
     * Add whitespace around containers based on container size
     */
    val containerWhitespaceMultiple: Float by _containerWhitespaceMultipleState

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultiple]
     */
    val containerWhitespace: ContainerWhitespace by _containerWhitespaceState

    /**
     * Transform are keep when content with the same aspect ratio is switched
     */
    val keepTransformWhenSameAspectRatioContentSizeChanged: Boolean by _keepTransformWhenSameAspectRatioContentSizeChangedState

    /**
     * Disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see GestureType
     */
    val disabledGestureTypes: Int by _disabledGestureTypesState

    /**
     * Whether to reverse the scale of the mouse wheel, the default is false
     */
    val reverseMouseWheelScale: Boolean by _reverseMouseWheelScaleState

    /**
     * Zoom increment converter when zooming with mouse wheel
     */
    @Deprecated("Use mouseWheelScaleCalculator instead")
    var mouseWheelScaleScrollDeltaConverter: ((Float) -> Float)? = null
        private set

    /**
     * Calculate the scaling factor based on the increment of the mouse wheel scroll
     */
    var mouseWheelScaleCalculator: MouseWheelScaleCalculator = MouseWheelScaleCalculator.Default
        private set


    /* *********************************** Transform status properties ******************************* */

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of [baseTransform] and [userTransform]
     */
    var transform: Transform by mutableStateOf(zoomableCore.transform.toPlatform())
        private set

    /**
     * Base transformation, include the base scale, offset, rotation,
     * which is affected by [contentScale], [alignment] properties and [rotate] method
     */
    var baseTransform: Transform by mutableStateOf(zoomableCore.baseTransform.toPlatform())
        private set

    /**
     * User transformation, include the user scale, offset, rotation,
     * which is affected by the user's gesture, [readMode] properties and [scale], [offset], [locate] method
     */
    var userTransform: Transform by mutableStateOf(zoomableCore.userTransform.toPlatform())
        private set

    /**
     * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    var minScale: Float by mutableFloatStateOf(zoomableCore.minScale)
        private set

    /**
     * Medium scale factor, only as a target value for one of when switch scale
     */
    var mediumScale: Float by mutableFloatStateOf(zoomableCore.mediumScale)
        private set

    /**
     * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    var maxScale: Float by mutableFloatStateOf(zoomableCore.maxScale)
        private set

    /**
     * The content region in the container after the baseTransform transformation
     */
    var contentBaseDisplayRectF: Rect by mutableStateOf(zoomableCore.contentBaseDisplayRect.toPlatform())
        private set

    /**
     * The content region in the container after the baseTransform transformation
     */
    var contentBaseDisplayRect: IntRect by mutableStateOf(zoomableCore.contentBaseDisplayRect.roundToPlatform())
        private set

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    var contentBaseVisibleRectF: Rect by mutableStateOf(zoomableCore.contentBaseVisibleRect.toPlatform())
        private set

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    var contentBaseVisibleRect: IntRect by mutableStateOf(zoomableCore.contentBaseVisibleRect.roundToPlatform())
        private set

    /**
     * The content region in the container after the final transform transformation
     */
    var contentDisplayRectF: Rect by mutableStateOf(zoomableCore.contentDisplayRect.toPlatform())
        private set

    /**
     * The content region in the container after the final transform transformation
     */
    var contentDisplayRect: IntRect by mutableStateOf(zoomableCore.contentDisplayRect.roundToPlatform())
        private set

    /**
     * The content is visible region to the user after the final transform transformation
     */
    var contentVisibleRectF: Rect by mutableStateOf(zoomableCore.contentVisibleRect.toPlatform())
        private set

    /**
     * The content is visible region to the user after the final transform transformation
     */
    var contentVisibleRect: IntRect by mutableStateOf(zoomableCore.contentVisibleRect.roundToPlatform())
        private set

    /**
     * The current scaling ratio of the original image
     */
    var sourceScaleFactor: ScaleFactor by mutableStateOf(zoomableCore.sourceScaleFactor.toPlatform())
        private set

    /**
     * The the current visible region of the original image
     */
    var sourceVisibleRectF: Rect by mutableStateOf(zoomableCore.sourceVisibleRect.toPlatform())
        private set

    /**
     * The the current visible region of the original image
     */
    var sourceVisibleRect: IntRect by mutableStateOf(zoomableCore.sourceVisibleRect.roundToPlatform())
        private set

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    var userOffsetBoundsRectF: Rect by mutableStateOf(zoomableCore.userOffsetBoundsRect.toPlatform())
        private set

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    var userOffsetBoundsRect: IntRect by mutableStateOf(zoomableCore.userOffsetBoundsRect.roundToPlatform())
        private set

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    @Deprecated("Use userOffsetBoundsRect instead", ReplaceWith("userOffsetBoundsRect"))
    var userOffsetBounds: IntRect by mutableStateOf(zoomableCore.userOffsetBoundsRect.roundToPlatform())
        private set

    /**
     * Edge state for the current offset
     */
    var scrollEdge: ScrollEdge by mutableStateOf(zoomableCore.scrollEdge)
        private set

    /**
     * The type of transformation currently in progress
     *
     * @see ContinuousTransformType
     */
    var continuousTransformType: Int by mutableIntStateOf(zoomableCore.continuousTransformType)
        private set


    /* *********************************** Interactive with component ******************************* */

    /**
     * Set the container size, this is usually the size of the ZoomImage component, setup by the ZoomImage component.
     */
    fun setContainerSize(containerSize: IntSize) {
        // In order to allow zoomableCore to receive containerSize changes immediately (which is very important),
        // we can only give up the snapshotFlow method
        _containerSizeState.value = containerSize
        zoomableCore.setContainerSize(containerSize.toCompat())
    }

    /**
     * Set the content size, usually Painter.intrinsicSize.round(), setup by the ZoomImage component.
     */
    fun setContentSize(contentSize: IntSize) {
        // In order to allow zoomableCore to receive contentSize changes immediately (which is very important),
        // we can only give up the snapshotFlow method
        _contentSizeState.value = contentSize
        zoomableCore.setContentSize(contentSize.toCompat())
    }

    /**
     * Set the original content size, it is usually set by [SubsamplingState] after parsing the original size of the image.
     */
    fun setContentOriginSize(contentOriginSize: IntSize) {
        _contentOriginSizeState.value = contentOriginSize
        zoomableCore.setContentOriginSize(contentOriginSize.toCompat())
    }

    /**
     * Set the content scale, usually set by ZoomImage component.
     */
    fun setContentScale(contentScale: ContentScale) {
        _contentScaleState.value = contentScale
        zoomableCore.setContentScale(contentScale.toCompat())
    }

    /**
     * Set the content alignment, usually set by ZoomImage component.
     */
    fun setAlignment(alignment: Alignment) {
        _alignmentState.value = alignment
        zoomableCore.setAlignment(alignment.toCompat())
    }

    /**
     * Set the layout direction of the content, usually set by ZoomImage component.
     */
    fun setLayoutDirection(layoutDirection: LayoutDirection) {
        _layoutDirectionState.value = layoutDirection
        zoomableCore.setRtlLayoutDirection(layoutDirection == LayoutDirection.Rtl)
    }


    /* *********************************** Interactive with user ******************************* */

    /**
     * Setup whether to enable read mode and configure read mode
     */
    fun setReadMode(readMode: ReadMode?) {
        _readModeState.value = readMode
        zoomableCore.setReadMode(readMode)
    }

    /**
     * Setup [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    fun setScalesCalculator(scalesCalculator: ScalesCalculator) {
        _scalesCalculatorState.value = scalesCalculator
        zoomableCore.setScalesCalculator(scalesCalculator)
    }

    /**
     * Setup whether to enable three-step scaling. After turning on,
     * it will switch cyclically between [minScale], [mediumScale], and [maxScale]
     */
    fun setThreeStepScale(threeStepScale: Boolean) {
        _threeStepScaleState.value = threeStepScale
        zoomableCore.setThreeStepScale(threeStepScale)
    }

    /**
     * Setup whether to enable the rubber band zoom effect.
     * When the user zooms to the minimum or maximum scaling factor through gestures,
     * continuing to zoom will have a rubber band effect
     */
    fun setRubberBandScale(rubberBandScale: Boolean) {
        _rubberBandScaleState.value = rubberBandScale
        zoomableCore.setRubberBandScale(rubberBandScale)
    }

    /**
     * Setup one finger scale configuration
     */
    fun setOneFingerScaleSpec(oneFingerScaleSpec: OneFingerScaleSpec) {
        _oneFingerScaleSpecState.value = oneFingerScaleSpec
        zoomableCore.setOneFingerScaleSpec(oneFingerScaleSpec)
    }

    /**
     * Setup the configuration of the transformation animation
     */
    fun setAnimationSpec(animationSpec: ZoomAnimationSpec) {
        _animationSpecState.value = animationSpec
        zoomableCore.setAnimationSpec(animationSpec)
    }

    /**
     * Setup whether to limit the offset of the user's pan to within the base visible rect
     */
    fun setLimitOffsetWithinBaseVisibleRect(limitOffsetWithinBaseVisibleRect: Boolean) {
        _limitOffsetWithinBaseVisibleRectState.value = limitOffsetWithinBaseVisibleRect
        zoomableCore.setLimitOffsetWithinBaseVisibleRect(limitOffsetWithinBaseVisibleRect)
    }

    /**
     * Setup add whitespace around containers based on container size
     */
    fun setContainerWhitespaceMultiple(containerWhitespaceMultiple: Float) {
        _containerWhitespaceMultipleState.value = containerWhitespaceMultiple
        zoomableCore.setContainerWhitespaceMultiple(containerWhitespaceMultiple)
    }

    /**
     * Setup add whitespace around containers, has higher priority than [containerWhitespaceMultiple]
     */
    fun setContainerWhitespace(containerWhitespace: ContainerWhitespace) {
        _containerWhitespaceState.value = containerWhitespace
        zoomableCore.setContainerWhitespace(containerWhitespace)
    }

    /**
     * Setup whether transform are keep when content with the same aspect ratio is switched
     */
    fun setKeepTransformWhenSameAspectRatioContentSizeChanged(keepTransform: Boolean) {
        _keepTransformWhenSameAspectRatioContentSizeChangedState.value = keepTransform
        zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(keepTransform)
    }

    /**
     * Setup disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see GestureType
     */
    fun setDisabledGestureTypes(disabledGestureTypes: Int) {
        _disabledGestureTypesState.value = disabledGestureTypes
    }

    /**
     * Setup whether to reverse the scale of the mouse wheel, the default is false
     */
    fun setReverseMouseWheelScale(reverseMouseWheelScale: Boolean) {
        _reverseMouseWheelScaleState.value = reverseMouseWheelScale
    }

    /**
     * Setup zoom increment converter when zooming with mouse wheel
     */
    @Deprecated("Use setMouseWheelScaleCalculator(MouseWheelScaleCalculator) instead")
    fun setMouseWheelScaleScrollDeltaConverter(
        mouseWheelScaleScrollDeltaConverter: ((Float) -> Float)?
    ) {
        this.mouseWheelScaleScrollDeltaConverter = mouseWheelScaleScrollDeltaConverter
    }

    /**
     * Setup calculate the scaling factor based on the increment of the mouse wheel scroll
     */
    fun setMouseWheelScaleCalculator(mouseWheelScaleCalculator: MouseWheelScaleCalculator) {
        this.mouseWheelScaleCalculator = mouseWheelScaleCalculator
    }

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffset? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: Offset = contentVisibleRectF.center,
    ): Boolean = zoomableCore.scale(
        targetScale = targetScale,
        centroidContentPoint = centroidContentPoint?.toCompatOffset()
            ?: centroidContentPointF.toCompat(),
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Zoom in multiplication [addScale] multiples and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scaleBy(
        addScale: Float,
        centroidContentPoint: IntOffset? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: Offset = contentVisibleRectF.center,
    ): Boolean = zoomableCore.scale(
        targetScale = zoomableCore.transform.scaleX * addScale,
        centroidContentPoint = centroidContentPoint?.toCompatOffset()
            ?: centroidContentPointF.toCompat(),
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Zoom in image by addition [addScale] multiple and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scaleByPlus(
        addScale: Float,
        centroidContentPoint: IntOffset? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: Offset = contentVisibleRectF.center,
    ): Boolean = zoomableCore.scale(
        targetScale = zoomableCore.transform.scaleX + addScale,
        centroidContentPoint = centroidContentPoint?.toCompatOffset()
            ?: centroidContentPointF.toCompat(),
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Scale to the next step scale and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * If [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only cycle between [minScale] and [mediumScale]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun switchScale(
        centroidContentPoint: IntOffset? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: Offset = contentVisibleRectF.center,
    ): Float? = zoomableCore.switchScale(
        centroidContentPoint = centroidContentPoint?.toCompatOffset()
            ?: centroidContentPointF.toCompat(),
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the image to the [targetOffset] position, and animation occurs when [animated] is true
     */
    suspend fun offset(
        targetOffset: Offset,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.offset(
        targetOffset = targetOffset.toCompat(),
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the image by the [addOffset] position, and animation occurs when [animated] is true
     */
    suspend fun offsetBy(
        addOffset: Offset,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.offset(
        targetOffset = zoomableCore.transform.offset + addOffset.toCompat(),
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    suspend fun locate(
        contentPoint: Offset,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.locate(
        contentPoint = contentPoint.toCompat(),
        targetScale = targetScale,
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    suspend fun locate(
        contentPoint: IntOffset,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.locate(
        contentPoint = contentPoint.toCompatOffset(),
        targetScale = targetScale,
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Rotate the content to [targetRotation]
     */
    suspend fun rotate(targetRotation: Int): Unit =
        zoomableCore.rotate(targetRotation = targetRotation)

    /**
     * Rotate the content by [addRotation]
     */
    suspend fun rotateBy(addRotation: Int): Unit =
        zoomableCore.rotate(targetRotation = zoomableCore.rotation + addRotation)

    /**
     * Gets the next step scale factor,
     * and if [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only loop between [minScale], [mediumScale].
     */
    fun getNextStepScale(): Float = zoomableCore.getNextStepScale()

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPointF(touchPoint: Offset): Offset =
        zoomableCore.touchPointToContentPoint(touchPoint = touchPoint.toCompat()).toPlatform()

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: Offset): IntOffset =
        touchPointToContentPointF(touchPoint = touchPoint).round()

    /**
     * Convert point of the original image into the current drawing coordinate system
     */
    fun sourceToDraw(point: Offset): Offset =
        zoomableCore.sourceToDraw(point.toCompat()).toPlatform()

    /**
     * Convert the rect of the original image to the current drawing coordinate system
     */
    fun sourceToDraw(rect: Rect): Rect =
        zoomableCore.sourceToDraw(rect.toCompat()).toPlatform()

    /**
     * If true is returned, scrolling can continue on the specified axis and direction
     *
     * @param horizontal Whether to scroll horizontally
     * @param direction positive means scroll to the right or scroll down, negative means scroll to the left or scroll up
     */
    fun canScroll(
        horizontal: Boolean,
        direction: Int
    ): Boolean = zoomableCore.canScroll(
        horizontal = horizontal,
        direction = direction
    )

    /**
     * Force reset the transform state
     */
    fun reset() = zoomableCore.reset(caller = "fromUser", force = true)


    /* *************************************** Internal ***************************************** */

    override fun onRemembered() {
        // Since ZoomableState2 is annotated with @Stable, onRemembered will be executed multiple times,
        // but we only need execute it once
        rememberedCount++
        if (rememberedCount != 1) return

        // ...
    }

    override fun onAbandoned() = onForgotten()
    override fun onForgotten() {
        // Since ZoomableState2 is annotated with @Stable, onForgotten will be executed multiple times,
        // but we only need execute it once
        if (rememberedCount <= 0) return
        rememberedCount--
        if (rememberedCount != 0) return

        // ...
    }

    internal suspend fun stopAllAnimation(caller: String) = zoomableCore.stopAllAnimation(caller)

    internal suspend fun rollback(centroid: Offset? = null): Boolean =
        zoomableCore.rollback(centroid?.toCompat())

    internal suspend fun gestureTransform(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ): Unit = zoomableCore.gestureTransform(
        centroid = centroid.toCompat(),
        panChange = panChange.toCompat(),
        zoomChange = zoomChange,
        rotationChange = rotationChange
    )

    internal suspend fun fling(
        velocity: Velocity,
        density: Density
    ): Boolean = zoomableCore.fling(
        velocity = OffsetCompat(velocity.x, velocity.y),
        extras = mapOf("density" to density)
    )

    internal fun setContinuousTransformType(
        @ContinuousTransformType continuousTransformType: Int
    ) = zoomableCore.setContinuousTransformType(continuousTransformType)

    internal fun checkSupportGestureType(@GestureType gestureType: Int): Boolean =
        zoomableCore.checkSupportGestureType(disabledGestureTypes, gestureType)

    override fun toString(): String =
        "ZoomableState(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentOriginSize=${contentOriginSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}, " +
                "layoutDirection=${layoutDirection.name}, " +
                "minScale=${minScale.format(4)}, " +
                "mediumScale=${mediumScale.format(4)}, " +
                "maxScale=${maxScale.format(4)}, " +
                "transform=${transform.toShortString()}" +
                ")"
}