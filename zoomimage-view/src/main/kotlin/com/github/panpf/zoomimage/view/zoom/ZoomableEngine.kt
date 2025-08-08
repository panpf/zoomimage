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

import android.view.View
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.util.format
import com.github.panpf.zoomimage.view.zoom.internal.ViewAnimationAdapter
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import com.github.panpf.zoomimage.zoom.internal.ZoomableCore
import com.github.panpf.zoomimage.zoom.name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Engines that control scale, pan, rotation
 *
 * @see com.github.panpf.zoomimage.view.test.zoom.ZoomableEngineTest
 */
@Suppress("RedundantConstructorKeyword")
class ZoomableEngine constructor(val logger: Logger, val view: View) {

    private val zoomableCore = ZoomableCore(
        logger = logger,
        module = "ZoomableEngine",
        animationAdapter = ViewAnimationAdapter(view),
        onTransformChanged = {
            _baseTransformState.value = it.baseTransform
            _userTransformState.value = it.userTransform
            _transformState.value = it.transform
            _minScaleState.value = it.minScale
            _mediumScaleState.value = it.mediumScale
            _maxScaleState.value = it.maxScale
            _contentBaseDisplayRectFState.value = it.contentBaseDisplayRect
            _contentBaseDisplayRectState.value = it.contentBaseDisplayRect.round()
            _contentBaseVisibleRectFState.value = it.contentBaseVisibleRect
            _contentBaseVisibleRectState.value = it.contentBaseVisibleRect.round()
            _contentDisplayRectFState.value = it.contentDisplayRect
            _contentDisplayRectState.value = it.contentDisplayRect.round()
            _contentVisibleRectFState.value = it.contentVisibleRect
            _contentVisibleRectState.value = it.contentVisibleRect.round()
            _sourceScaleFactor.value = it.sourceScaleFactor
            _sourceVisibleRectFState.value = it.sourceVisibleRect
            _sourceVisibleRectState.value = it.sourceVisibleRect.round()
            _userOffsetBoundsRectFState.value = it.userOffsetBoundsRect
            _userOffsetBoundsRectState.value = it.userOffsetBoundsRect.round()
            _scrollEdgeState.value = it.scrollEdge
            _continuousTransformTypeState.value = it.continuousTransformType
        }
    )


    /* *********************************** Configured properties ****************************** */

    private val _containerSizeState: MutableStateFlow<IntSizeCompat> =
        MutableStateFlow(value = zoomableCore.containerSize)
    private val _contentSizeState: MutableStateFlow<IntSizeCompat> =
        MutableStateFlow(value = zoomableCore.contentSize)
    private val _contentOriginSizeState: MutableStateFlow<IntSizeCompat> =
        MutableStateFlow(value = zoomableCore.contentOriginSize)
    private val _contentScaleState: MutableStateFlow<ContentScaleCompat> =
        MutableStateFlow(value = zoomableCore.contentScale)
    private val _alignmentState: MutableStateFlow<AlignmentCompat> =
        MutableStateFlow(value = zoomableCore.alignment)
    private val _rtlLayoutDirectionState: MutableStateFlow<Boolean> =
        MutableStateFlow(value = zoomableCore.rtlLayoutDirection)
    private val _readModeState: MutableStateFlow<ReadMode?> =
        MutableStateFlow(value = zoomableCore.readMode)
    private val _scalesCalculatorState: MutableStateFlow<ScalesCalculator> =
        MutableStateFlow(value = zoomableCore.scalesCalculator)
    private val _threeStepScaleState: MutableStateFlow<Boolean> =
        MutableStateFlow(value = zoomableCore.threeStepScale)
    private val _rubberBandScaleState: MutableStateFlow<Boolean> =
        MutableStateFlow(value = zoomableCore.rubberBandScale)
    private val _oneFingerScaleSpecState: MutableStateFlow<OneFingerScaleSpec> =
        MutableStateFlow(value = zoomableCore.oneFingerScaleSpec)
    private val _animationSpecState: MutableStateFlow<ZoomAnimationSpec> =
        MutableStateFlow(value = ZoomAnimationSpec.Default)
    private val _limitOffsetWithinBaseVisibleRectState: MutableStateFlow<Boolean> =
        MutableStateFlow(value = zoomableCore.limitOffsetWithinBaseVisibleRect)
    private val _containerWhitespaceMultipleState: MutableStateFlow<Float> =
        MutableStateFlow(value = zoomableCore.containerWhitespaceMultiple)
    private val _containerWhitespaceState: MutableStateFlow<ContainerWhitespace> =
        MutableStateFlow(value = zoomableCore.containerWhitespace)
    private val _keepTransformWhenSameAspectRatioContentSizeChangedState: MutableStateFlow<Boolean> =
        MutableStateFlow(value = zoomableCore.keepTransformWhenSameAspectRatioContentSizeChanged)
    private val _disabledGestureTypesState: MutableStateFlow<Int> = MutableStateFlow(value = 0)

    /**
     * The size of the container that holds the content, this is usually the size of the [ZoomImageView] component, setup by the [ZoomImageView] component
     */
    val containerSizeState: StateFlow<IntSizeCompat> = _containerSizeState

    /**
     * The size of the content, this is usually the size of the thumbnail Drawable, setup by the [ZoomImageView] component
     */
    val contentSizeState: StateFlow<IntSizeCompat> = _contentSizeState

    /**
     * The original size of the content, it is usually set by [SubsamplingEngine] after parsing the original size of the image.
     * If not empty, it means that the subsampling function has been enabled
     */
    val contentOriginSizeState: StateFlow<IntSizeCompat> = _contentOriginSizeState

    /**
     * The scale of the content, usually set by [ZoomImageView] component
     */
    val contentScaleState: StateFlow<ContentScaleCompat> = _contentScaleState

    /**
     * The alignment of the content, usually set by [ZoomImageView] component
     */
    val alignmentState: StateFlow<AlignmentCompat> = _alignmentState

    /**
     * The layout direction of the content, usually set by [ZoomImageView] component
     */
    val rtlLayoutDirectionState: StateFlow<Boolean> = _rtlLayoutDirectionState

    /**
     * Setup whether to enable read mode and configure read mode
     */
    val readModeState: StateFlow<ReadMode?> = _readModeState

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    val scalesCalculatorState: StateFlow<ScalesCalculator> = _scalesCalculatorState

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    val threeStepScaleState: StateFlow<Boolean> = _threeStepScaleState

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will rollback to the minimum or maximum zoom factor
     */
    val rubberBandScaleState: StateFlow<Boolean> = _rubberBandScaleState

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    val oneFingerScaleSpecState: StateFlow<OneFingerScaleSpec> = _oneFingerScaleSpecState

    /**
     * The animation configuration for the zoom animation
     */
    val animationSpecState: StateFlow<ZoomAnimationSpec> = _animationSpecState

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    val limitOffsetWithinBaseVisibleRectState: StateFlow<Boolean> =
        _limitOffsetWithinBaseVisibleRectState

    /**
     * Add whitespace around containers based on container size
     */
    val containerWhitespaceMultipleState: StateFlow<Float> = _containerWhitespaceMultipleState

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultipleState]
     */
    val containerWhitespaceState: StateFlow<ContainerWhitespace> = _containerWhitespaceState

    /**
     * Transform are keep when content with the same aspect ratio is switched
     */
    val keepTransformWhenSameAspectRatioContentSizeChangedState: StateFlow<Boolean> =
        _keepTransformWhenSameAspectRatioContentSizeChangedState

    /**
     * Disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.GestureType
     */
    val disabledGestureTypesState: StateFlow<Int> = _disabledGestureTypesState


    /* *********************************** Transform status properties ******************************* */

    private val _transformState: MutableStateFlow<TransformCompat> =
        MutableStateFlow(value = zoomableCore.transform)
    private val _baseTransformState: MutableStateFlow<TransformCompat> =
        MutableStateFlow(value = zoomableCore.baseTransform)
    private val _userTransformState: MutableStateFlow<TransformCompat> =
        MutableStateFlow(value = zoomableCore.userTransform)
    private val _minScaleState: MutableStateFlow<Float> =
        MutableStateFlow(value = zoomableCore.minScale)
    private val _mediumScaleState: MutableStateFlow<Float> =
        MutableStateFlow(value = zoomableCore.mediumScale)
    private val _maxScaleState: MutableStateFlow<Float> =
        MutableStateFlow(value = zoomableCore.maxScale)
    private val _contentBaseDisplayRectFState: MutableStateFlow<RectCompat> =
        MutableStateFlow(value = zoomableCore.contentBaseDisplayRect)
    private val _contentBaseDisplayRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(value = zoomableCore.contentBaseDisplayRect.round())
    private val _contentBaseVisibleRectFState: MutableStateFlow<RectCompat> =
        MutableStateFlow(value = zoomableCore.contentBaseVisibleRect)
    private val _contentBaseVisibleRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(value = zoomableCore.contentBaseVisibleRect.round())
    private val _contentDisplayRectFState: MutableStateFlow<RectCompat> =
        MutableStateFlow(value = zoomableCore.contentDisplayRect)
    private val _contentDisplayRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(value = zoomableCore.contentDisplayRect.round())
    private val _contentVisibleRectFState: MutableStateFlow<RectCompat> =
        MutableStateFlow(value = zoomableCore.contentVisibleRect)
    private val _contentVisibleRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(value = zoomableCore.contentVisibleRect.round())
    private val _sourceScaleFactor: MutableStateFlow<ScaleFactorCompat> =
        MutableStateFlow(value = zoomableCore.sourceScaleFactor)
    private val _sourceVisibleRectFState: MutableStateFlow<RectCompat> =
        MutableStateFlow(value = zoomableCore.sourceVisibleRect)
    private val _sourceVisibleRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(value = zoomableCore.sourceVisibleRect.round())
    private val _userOffsetBoundsRectFState: MutableStateFlow<RectCompat> =
        MutableStateFlow(value = zoomableCore.userOffsetBoundsRect)
    private val _userOffsetBoundsRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(value = zoomableCore.userOffsetBoundsRect.round())
    private val _scrollEdgeState: MutableStateFlow<ScrollEdge> =
        MutableStateFlow(value = zoomableCore.scrollEdge)
    private val _continuousTransformTypeState: MutableStateFlow<Int> =
        MutableStateFlow(value = zoomableCore.continuousTransformType)

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of baseTransform and userTransform
     */
    val transformState: StateFlow<TransformCompat> = _transformState

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
    val contentBaseDisplayRectFState: StateFlow<RectCompat> = _contentBaseDisplayRectFState

    /**
     * The content region in the container after the baseTransform transformation
     */
    val contentBaseDisplayRectState: StateFlow<IntRectCompat> = _contentBaseDisplayRectState

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    val contentBaseVisibleRectFState: StateFlow<RectCompat> = _contentBaseVisibleRectFState

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    val contentBaseVisibleRectState: StateFlow<IntRectCompat> = _contentBaseVisibleRectState

    /**
     * The content region in the container after the final transform transformation
     */
    val contentDisplayRectFState: StateFlow<RectCompat> = _contentDisplayRectFState

    /**
     * The content region in the container after the final transform transformation
     */
    val contentDisplayRectState: StateFlow<IntRectCompat> = _contentDisplayRectState

    /**
     * The content is visible region to the user after the final transform transformation
     */
    val contentVisibleRectFState: StateFlow<RectCompat> = _contentVisibleRectFState

    /**
     * The content is visible region to the user after the final transform transformation
     */
    val contentVisibleRectState: StateFlow<IntRectCompat> = _contentVisibleRectState

    /**
     * The current scaling ratio of the original image
     */
    val sourceScaleFactorState: StateFlow<ScaleFactorCompat> = _sourceScaleFactor

    /**
     * The the current visible region of the original image
     */
    val sourceVisibleRectFState: StateFlow<RectCompat> = _sourceVisibleRectFState

    /**
     * The the current visible region of the original image
     */
    val sourceVisibleRectState: StateFlow<IntRectCompat> = _sourceVisibleRectState

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    val userOffsetBoundsRectFState: StateFlow<RectCompat> = _userOffsetBoundsRectFState

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    val userOffsetBoundsRectState: StateFlow<IntRectCompat> = _userOffsetBoundsRectState

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    @Deprecated("Use userOffsetBoundsRectState instead", ReplaceWith("userOffsetBoundsRectState"))
    val userOffsetBoundsState: StateFlow<IntRectCompat> = userOffsetBoundsRectState

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


    /* *********************************** Interactive with component ******************************* */

    /**
     * Set the container size, this is usually the size of the [ZoomImageView] component, setup by the [ZoomImageView] component
     */
    fun setContainerSize(containerSize: IntSizeCompat) {
        _containerSizeState.value = containerSize
        zoomableCore.setContainerSize(containerSize)
    }

    /**
     * Set the content size, this is usually the size of the thumbnail Drawable, setup by the [ZoomImageView] component
     */
    fun setContentSize(contentSize: IntSizeCompat) {
        _contentSizeState.value = contentSize
        zoomableCore.setContentSize(contentSize)
    }

    /**
     * Set the original content size, it is usually set by [SubsamplingEngine] after parsing the original size of the image.
     */
    fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
        _contentOriginSizeState.value = contentOriginSize
        zoomableCore.setContentOriginSize(contentOriginSize)
    }

    /**
     * Set the content scale, usually set by ZoomImage component.
     */
    fun setContentScale(contentScale: ContentScaleCompat) {
        _contentScaleState.value = contentScale
        zoomableCore.setContentScale(contentScale)
    }

    /**
     * Set the content alignment, usually set by ZoomImage component.
     */
    fun setAlignment(alignment: AlignmentCompat) {
        _alignmentState.value = alignment
        zoomableCore.setAlignment(alignment)
    }

    /**
     * Set the layout direction of the content, usually set by ZoomImage component.
     */
    fun setRtlLayoutDirection(rtlLayoutDirection: Boolean) {
        _rtlLayoutDirectionState.value = rtlLayoutDirection
        zoomableCore.setRtlLayoutDirection(rtlLayoutDirection)
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
     * it will switch cyclically between [minScaleState], [mediumScaleState], and [maxScaleState]
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
     * Setup add whitespace around containers, has higher priority than [containerWhitespaceMultipleState]
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


    /* *********************************** Interactive with user ******************************* */

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffsetCompat? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: OffsetCompat = contentVisibleRectFState.value.center,
    ): Boolean = zoomableCore.scale(
        targetScale = targetScale,
        centroidContentPoint = centroidContentPoint?.toOffset() ?: centroidContentPointF,
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
        centroidContentPoint: IntOffsetCompat? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: OffsetCompat = contentVisibleRectFState.value.center,
    ): Boolean = zoomableCore.scale(
        targetScale = zoomableCore.transform.scaleX * addScale,
        centroidContentPoint = centroidContentPoint?.toOffset() ?: centroidContentPointF,
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
        centroidContentPoint: IntOffsetCompat? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: OffsetCompat = contentVisibleRectFState.value.center,
    ): Boolean = zoomableCore.scale(
        targetScale = zoomableCore.transform.scaleX + addScale,
        centroidContentPoint = centroidContentPoint?.toOffset() ?: centroidContentPointF,
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Scale to the next step scale and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * If [threeStepScaleState] is true, it will cycle between [minScaleState], [mediumScaleState], [maxScaleState],
     * otherwise it will only cycle between [minScaleState] and [mediumScaleState]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun switchScale(
        centroidContentPoint: IntOffsetCompat? = null,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
        centroidContentPointF: OffsetCompat = contentVisibleRectFState.value.center,
    ): Float? = zoomableCore.switchScale(
        centroidContentPoint = centroidContentPoint?.toOffset() ?: centroidContentPointF,
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the image to the [targetOffset] position, and animation occurs when [animated] is true
     */
    suspend fun offset(
        targetOffset: OffsetCompat,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.offset(
        targetOffset = targetOffset,
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the image by the [addOffset] position, and animation occurs when [animated] is true
     */
    suspend fun offsetBy(
        addOffset: OffsetCompat,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.offset(
        targetOffset = zoomableCore.transform.offset + addOffset,
        animated = animated,
        animationSpec = animationSpec
    )

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    suspend fun locate(
        contentPoint: OffsetCompat,
        targetScale: Float = transformState.value.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.locate(
        contentPoint = contentPoint,
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
        contentPoint: IntOffsetCompat,
        targetScale: Float = transformState.value.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.locate(
        contentPoint = contentPoint.toOffset(),
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
     * and if [threeStepScaleState] is true, it will cycle between [minScaleState], [mediumScaleState], [maxScaleState],
     * otherwise it will only loop between [minScaleState], [mediumScaleState].
     */
    fun getNextStepScale(): Float = zoomableCore.getNextStepScale()

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPointF(touchPoint: OffsetCompat): OffsetCompat =
        zoomableCore.touchPointToContentPoint(touchPoint = touchPoint)

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: OffsetCompat): IntOffsetCompat =
        touchPointToContentPointF(touchPoint = touchPoint).round()

    /**
     * Convert point of the original image into the current drawing coordinate system
     */
    fun sourceToDraw(point: OffsetCompat): OffsetCompat = zoomableCore.sourceToDraw(point)

    /**
     * Convert the rect of the original image to the current drawing coordinate system
     */
    fun sourceToDraw(rect: RectCompat): RectCompat = zoomableCore.sourceToDraw(rect)

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

    fun onAttachToWindow() {
        // ...
    }

    fun onDetachFromWindow() {
        // ...
    }

    internal suspend fun stopAllAnimation(caller: String) = zoomableCore.stopAllAnimation(caller)

    internal suspend fun rollback(focus: OffsetCompat? = null): Boolean =
        zoomableCore.rollback(focus)

    internal suspend fun gestureTransform(
        centroid: OffsetCompat,
        panChange: OffsetCompat,
        zoomChange: Float,
        rotationChange: Float
    ): Unit = zoomableCore.gestureTransform(
        centroid = centroid,
        panChange = panChange,
        zoomChange = zoomChange,
        rotationChange = rotationChange
    )

    internal suspend fun fling(velocity: OffsetCompat): Boolean =
        zoomableCore.fling(velocity = velocity, extras = emptyMap())

    internal fun setContinuousTransformType(
        @ContinuousTransformType continuousTransformType: Int
    ) = zoomableCore.setContinuousTransformType(continuousTransformType)

    internal fun checkSupportGestureType(@GestureType gestureType: Int): Boolean =
        zoomableCore.checkSupportGestureType(disabledGestureTypesState.value, gestureType)

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