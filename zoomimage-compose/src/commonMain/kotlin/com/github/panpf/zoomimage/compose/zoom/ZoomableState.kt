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
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
    private var coroutineScope: CoroutineScope? = null
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
            userOffsetBoundsRectF = it.userOffsetBoundsRect.toPlatform()
            userOffsetBoundsRect = it.userOffsetBoundsRect.roundToPlatform()
            userOffsetBoundsRect = it.userOffsetBoundsRect.roundToPlatform()
            scrollEdge = it.scrollEdge
            continuousTransformType = it.continuousTransformType
        }
    )


    /* *********************************** Properties initialized by the component ****************************** */

    /**
     * The size of the container that holds the content, this is usually the size of the ZoomImage component
     */
    var containerSize: IntSize by mutableStateOf(zoomableCore.containerSize.toPlatform())

    /**
     * The size of the content, usually Painter.intrinsicSize.round(), setup by the ZoomImage component
     */
    var contentSize: IntSize by mutableStateOf(zoomableCore.contentSize.toPlatform())

    /**
     * The original size of the content, it is usually set by [SubsamplingState] after parsing the original size of the image
     */
    var contentOriginSize: IntSize by mutableStateOf(zoomableCore.contentOriginSize.toPlatform())


    /* *********************************** Properties configured by the user ****************************** */

    /**
     * The scale of the content, usually set by ZoomImage component
     */
    var contentScale: ContentScale by mutableStateOf(zoomableCore.contentScale.toPlatform())

    /**
     * The alignment of the content, usually set by ZoomImage component
     */
    var alignment: Alignment by mutableStateOf(zoomableCore.alignment.toPlatform())

    /**
     * The layout direction of the content, usually set by ZoomImage component
     */
    var layoutDirection: LayoutDirection by mutableStateOf(if (zoomableCore.rtlLayoutDirection) LayoutDirection.Rtl else LayoutDirection.Ltr)

    /**
     * Setup whether to enable read mode and configure read mode
     */
    var readMode: ReadMode? by mutableStateOf(zoomableCore.readMode)

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    var scalesCalculator: ScalesCalculator by mutableStateOf(zoomableCore.scalesCalculator)

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    var threeStepScale: Boolean by mutableStateOf(zoomableCore.threeStepScale)

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    var rubberBandScale: Boolean by mutableStateOf(zoomableCore.rubberBandScale)

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    var oneFingerScaleSpec: OneFingerScaleSpec by mutableStateOf(zoomableCore.oneFingerScaleSpec)

    /**
     * The animation configuration for the zoom animation
     */
    var animationSpec: ZoomAnimationSpec by mutableStateOf(ZoomAnimationSpec.Default)

    /**
     * If true, when the user offset to the bounds through a gesture,
     * continuing to offset will have a rubber band effect, and when the hand is released,
     * it will rollback to the bounds
     */
    var rubberBandOffset: Boolean by mutableStateOf(zoomableCore.rubberBandOffset)

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    var limitOffsetWithinBaseVisibleRect: Boolean by mutableStateOf(zoomableCore.limitOffsetWithinBaseVisibleRect)

    /**
     * Add whitespace around containers based on container size
     */
    var containerWhitespaceMultiple: Float by mutableStateOf(zoomableCore.containerWhitespaceMultiple)

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultiple]
     */
    var containerWhitespace: ContainerWhitespace by mutableStateOf(zoomableCore.containerWhitespace)

    /**
     * Transform are keep when content with the same aspect ratio is switched
     */
    var keepTransformWhenSameAspectRatioContentSizeChanged: Boolean by mutableStateOf(zoomableCore.keepTransformWhenSameAspectRatioContentSizeChanged)

    /**
     * Disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.GestureType
     */
    var disabledGestureTypes: Int by mutableIntStateOf(0)

    /**
     * Whether to reverse the scale of the mouse wheel, the default is false
     */
    var reverseMouseWheelScale: Boolean by mutableStateOf(false)

    /**
     * Zoom increment converter when zooming with mouse wheel
     */
    @Deprecated("Use mouseWheelScaleCalculator instead")
    var mouseWheelScaleScrollDeltaConverter: ((Float) -> Float)? = null

    /**
     * Calculate the scaling factor based on the increment of the mouse wheel scroll
     */
    var mouseWheelScaleCalculator: MouseWheelScaleCalculator = MouseWheelScaleCalculator.Default


    /* *********************************** Properties readable by the user ******************************* */

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
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of [baseTransform] and [userTransform]
     */
    var transform: Transform by mutableStateOf(zoomableCore.transform.toPlatform())
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


    /* *********************************** Interactive with user ******************************* */

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

    override fun onRemembered() {
        // Since ZoomableState2 is annotated with @Stable, onRemembered will be executed multiple times,
        // but we only need execute it once
        rememberedCount++
        if (rememberedCount != 1) return

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope

        bindProperties(coroutineScope)
    }

    override fun onAbandoned() = onForgotten()
    override fun onForgotten() {
        // Since ZoomableState2 is annotated with @Stable, onForgotten will be executed multiple times,
        // but we only need execute it once
        if (rememberedCount <= 0) return
        rememberedCount--
        if (rememberedCount != 0) return

        val coroutineScope = this.coroutineScope ?: return

        coroutineScope.cancel("onForgotten")
        this.coroutineScope = null
    }

    private fun bindProperties(coroutineScope: CoroutineScope) {
        /*
         * Must be immediate, otherwise the user will see the image move quickly from the top to the center
         */
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { containerSize }.collect {
                zoomableCore.setContainerSize(it.toCompat())
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { contentSize }.collect {
                zoomableCore.setContentSize(it.toCompat())
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { contentOriginSize }.collect {
                zoomableCore.setContentOriginSize(it.toCompat())
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { contentScale }.collect {
                zoomableCore.setContentScale(it.toCompat())
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { alignment }.collect {
                zoomableCore.setAlignment(it.toCompat())
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { layoutDirection }.collect {
                zoomableCore.setRtlLayoutDirection(layoutDirection == LayoutDirection.Rtl)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { readMode }.collect {
                zoomableCore.setReadMode(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { scalesCalculator }.collect {
                zoomableCore.setScalesCalculator(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { threeStepScale }.collect {
                zoomableCore.setThreeStepScale(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { rubberBandScale }.collect {
                zoomableCore.setRubberBandScale(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { oneFingerScaleSpec }.collect {
                zoomableCore.setOneFingerScaleSpec(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { animationSpec }.collect {
                zoomableCore.setAnimationSpec(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { rubberBandOffset }.collect {
                zoomableCore.setRubberBandOffset(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { limitOffsetWithinBaseVisibleRect }.collect {
                zoomableCore.setLimitOffsetWithinBaseVisibleRect(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { containerWhitespaceMultiple }.collect {
                zoomableCore.setContainerWhitespaceMultiple(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { containerWhitespace }.collect {
                zoomableCore.setContainerWhitespace(it)
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { keepTransformWhenSameAspectRatioContentSizeChanged }.collect {
                zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(it)
            }
        }
    }

    suspend fun reset() = zoomableCore.reset(caller = "fromUser", force = true)

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