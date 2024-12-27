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
import com.github.panpf.zoomimage.util.TransformCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Engines that control scale, pan, rotation
 *
 * @see com.github.panpf.zoomimage.view.test.zoom.ZoomableEngineTest
 */
class ZoomableEngine(val logger: Logger, val view: View) {

    private var coroutineScope: CoroutineScope? = null
    private val zoomableCore = ZoomableCore(
        logger = logger,
        module = "ZoomableEngine",
        rtlLayoutDirection = view.layoutDirection == View.LAYOUT_DIRECTION_RTL,
        animationAdapter = ViewAnimationAdapter(view),
        onTransformChanged = {
            _baseTransformState.value = it.baseTransform
            _userTransformState.value = it.userTransform
            _transformState.value = it.transform
            _minScaleState.value = it.minScale
            _mediumScaleState.value = it.mediumScale
            _maxScaleState.value = it.maxScale
            _contentBaseDisplayRectState.value = it.contentBaseDisplayRect
            _contentBaseVisibleRectState.value = it.contentBaseVisibleRect
            _contentDisplayRectState.value = it.contentDisplayRect
            _contentVisibleRectState.value = it.contentVisibleRect
            _userOffsetBoundsState.value = it.userOffsetBounds
            _scrollEdgeState.value = it.scrollEdge
            _continuousTransformTypeState.value = it.continuousTransformType
        }
    )


    /* *********************************** Properties initialized by the component ****************************** */

    /**
     * The size of the container that holds the content, this is usually the size of the [ZoomImageView] component
     */
    val containerSizeState: MutableStateFlow<IntSizeCompat> =
        MutableStateFlow(zoomableCore.containerSize)

    /**
     * The size of the content, this is usually the size of the thumbnail Drawable, setup by the [ZoomImageView] component
     */
    val contentSizeState: MutableStateFlow<IntSizeCompat> =
        MutableStateFlow(zoomableCore.contentSize)

    /**
     * The original size of the content, it is usually set by [SubsamplingEngine] after parsing the original size of the image
     */
    val contentOriginSizeState: MutableStateFlow<IntSizeCompat> =
        MutableStateFlow(zoomableCore.contentOriginSize)


    /* *********************************** Properties configured by the user ****************************** */

    /**
     * The scale of the content, usually set by [ZoomImageView] component
     */
    val contentScaleState: MutableStateFlow<ContentScaleCompat> =
        MutableStateFlow(zoomableCore.contentScale)

    /**
     * The alignment of the content, usually set by [ZoomImageView] component
     */
    val alignmentState: MutableStateFlow<AlignmentCompat> = MutableStateFlow(zoomableCore.alignment)

    /**
     * Setup whether to enable read mode and configure read mode
     */
    val readModeState: MutableStateFlow<ReadMode?> = MutableStateFlow(zoomableCore.readMode)

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    val scalesCalculatorState: MutableStateFlow<ScalesCalculator> =
        MutableStateFlow(zoomableCore.scalesCalculator)

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    val threeStepScaleState: MutableStateFlow<Boolean> =
        MutableStateFlow(zoomableCore.threeStepScale)

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    val rubberBandScaleState: MutableStateFlow<Boolean> =
        MutableStateFlow(zoomableCore.rubberBandScale)

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    val oneFingerScaleSpecState: MutableStateFlow<OneFingerScaleSpec> =
        MutableStateFlow(zoomableCore.oneFingerScaleSpec)

    /**
     * The animation configuration for the zoom animation
     */
    val animationSpecState: MutableStateFlow<ZoomAnimationSpec> =
        MutableStateFlow(ZoomAnimationSpec.Default)

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    val limitOffsetWithinBaseVisibleRectState: MutableStateFlow<Boolean> =
        MutableStateFlow(zoomableCore.limitOffsetWithinBaseVisibleRect)

    /**
     * Add whitespace around containers based on container size
     */
    var containerWhitespaceMultipleState: MutableStateFlow<Float> =
        MutableStateFlow(zoomableCore.containerWhitespaceMultiple)

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultipleState]
     */
    var containerWhitespaceState: MutableStateFlow<ContainerWhitespace> =
        MutableStateFlow(zoomableCore.containerWhitespace)

    /**
     * Disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.GestureType
     */
    var disabledGestureTypesState: MutableStateFlow<Int> = MutableStateFlow(0)


    /* *********************************** Properties readable by the user ******************************* */

    private val _baseTransformState: MutableStateFlow<TransformCompat> =
        MutableStateFlow(zoomableCore.baseTransform)
    private val _userTransformState: MutableStateFlow<TransformCompat> =
        MutableStateFlow(zoomableCore.userTransform)
    private val _transformState: MutableStateFlow<TransformCompat> =
        MutableStateFlow(zoomableCore.transform)
    private val _minScaleState: MutableStateFlow<Float> = MutableStateFlow(zoomableCore.minScale)
    private val _mediumScaleState: MutableStateFlow<Float> =
        MutableStateFlow(zoomableCore.mediumScale)
    private val _maxScaleState: MutableStateFlow<Float> = MutableStateFlow(zoomableCore.maxScale)
    private val _contentBaseDisplayRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(zoomableCore.contentBaseDisplayRect)
    private val _contentBaseVisibleRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(zoomableCore.contentBaseVisibleRect)
    private val _contentDisplayRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(zoomableCore.contentDisplayRect)
    private val _contentVisibleRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(zoomableCore.contentVisibleRect)
    private val _scrollEdgeState: MutableStateFlow<ScrollEdge> =
        MutableStateFlow(zoomableCore.scrollEdge)
    private val _userOffsetBoundsState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(zoomableCore.userOffsetBounds)
    private val _continuousTransformTypeState: MutableStateFlow<Int> =
        MutableStateFlow(zoomableCore.continuousTransformType)

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
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = zoomableCore.scale(
        targetScale = targetScale,
        centroidContentPoint = centroidContentPoint,
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
        centroidContentPoint: IntOffsetCompat = contentVisibleRectState.value.center,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Float? = zoomableCore.switchScale(
        centroidContentPoint = centroidContentPoint,
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
        contentPoint = contentPoint,
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
     * Gets the next step scale factor,
     * and if [threeStepScaleState] is true, it will cycle between [minScaleState], [mediumScaleState], [maxScaleState],
     * otherwise it will only loop between [minScaleState], [mediumScaleState].
     */
    fun getNextStepScale(): Float = zoomableCore.getNextStepScale()

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: OffsetCompat): IntOffsetCompat =
        zoomableCore.touchPointToContentPoint(touchPoint = touchPoint)

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


    /* *************************************** Internal ***************************************** */

    fun onAttachToWindow() {
        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) return

        val newCoroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = newCoroutineScope

        bindProperties(newCoroutineScope)
        zoomableCore.setCoroutineScope(newCoroutineScope)
    }

    fun onDetachFromWindow() {
        val coroutineScope = this.coroutineScope ?: return

        zoomableCore.setCoroutineScope(null)
        this.coroutineScope = null
        coroutineScope.cancel("onDetachFromWindow")
    }

    private fun bindProperties(coroutineScope: CoroutineScope) {
        /*
         * Must be immediate, otherwise the user will see the image move quickly from the top to the center
         */
        coroutineScope.launch(Dispatchers.Main.immediate) {
            containerSizeState.collect {
                zoomableCore.containerSize = it
                zoomableCore.reset("containerSizeChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            contentSizeState.collect {
                zoomableCore.contentSize = it
                zoomableCore.reset("contentSizeChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            contentOriginSizeState.collect {
                zoomableCore.contentOriginSize = it
                zoomableCore.reset("contentOriginSizeChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            contentScaleState.collect {
                zoomableCore.contentScale = it
                zoomableCore.reset("contentScaleChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            alignmentState.collect {
                zoomableCore.alignment = it
                zoomableCore.reset("alignmentChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            readModeState.collect {
                zoomableCore.readMode = it
                zoomableCore.reset("readModeChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            scalesCalculatorState.collect {
                zoomableCore.scalesCalculator = it
                zoomableCore.reset("scalesCalculatorChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            threeStepScaleState.collect {
                zoomableCore.threeStepScale = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            rubberBandScaleState.collect {
                zoomableCore.rubberBandScale = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            oneFingerScaleSpecState.collect {
                zoomableCore.oneFingerScaleSpec = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            animationSpecState.collect {
                zoomableCore.animationSpec = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            limitOffsetWithinBaseVisibleRectState.collect {
                zoomableCore.limitOffsetWithinBaseVisibleRect = it
                zoomableCore.reset("limitOffsetWithinBaseVisibleRectChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            containerWhitespaceMultipleState.collect {
                zoomableCore.containerWhitespaceMultiple = it
                zoomableCore.reset("containerWhitespaceMultipleChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            containerWhitespaceState.collect {
                zoomableCore.containerWhitespace = it
                zoomableCore.reset("containerWhitespaceChanged")
            }
        }
    }

    suspend fun reset(caller: String) = zoomableCore.reset(caller = caller)

    internal suspend fun stopAllAnimation(caller: String) = zoomableCore.stopAllAnimation(caller)

    internal suspend fun rollbackScale(focus: OffsetCompat? = null): Boolean =
        zoomableCore.rollbackScale(focus)

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