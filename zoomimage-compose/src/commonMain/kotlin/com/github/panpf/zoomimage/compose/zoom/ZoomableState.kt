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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
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
import androidx.compose.ui.geometry.center
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.util.ScaleFactor
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.util.isNotEmpty
import com.github.panpf.zoomimage.compose.util.limitTo
import com.github.panpf.zoomimage.compose.util.name
import com.github.panpf.zoomimage.compose.util.roundToPlatform
import com.github.panpf.zoomimage.compose.util.rtlFlipped
import com.github.panpf.zoomimage.compose.util.times
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toCompatOffset
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.util.toPlatformRect
import com.github.panpf.zoomimage.compose.util.toShortString
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
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
import com.github.panpf.zoomimage.zoom.touchPointToContentPoint
import com.github.panpf.zoomimage.zoom.transformAboutEquals
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Creates and remember a [ZoomableState] that can be used to control the scale, pan, rotation of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ZoomableStateTest.testRememberZoomableState
 */
@Composable
fun rememberZoomableState(logger: Logger = rememberZoomImageLogger()): ZoomableState {
    val layoutDirection = LocalLayoutDirection.current
    val zoomableState = remember(logger, layoutDirection) {
        ZoomableState(logger, layoutDirection)
    }
    return zoomableState
}

/**
 * A state object that can be used to control the scale, pan, rotation of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ZoomableStateTest
 */
@Stable
class ZoomableState(val logger: Logger, val layoutDirection: LayoutDirection) : RememberObserver {

    private var coroutineScope: CoroutineScope? = null
    private var lastScaleAnimatable: Animatable<*, *>? = null
    private var lastFlingAnimatable: Animatable<*, *>? = null
    private var lastInitialUserTransform: Transform = Transform.Origin
    private var rotation: Int by mutableIntStateOf(0)
    private var rememberedCount = 0

    /**
     * The size of the container that holds the content, this is usually the size of the ZoomImage component
     */
    var containerSize: IntSize by mutableStateOf(IntSize.Zero)

    /**
     * The size of the content, usually Painter.intrinsicSize.round(), setup by the ZoomImage component
     */
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)

    /**
     * The original size of the content, it is usually set by [SubsamplingState] after parsing the original size of the image
     */
    var contentOriginSize: IntSize by mutableStateOf(IntSize.Zero)


    /* *********************************** Configurable properties ****************************** */

    /**
     * The scale of the content, usually set by ZoomImage component
     */
    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)

    /**
     * The alignment of the content, usually set by ZoomImage component
     */
    var alignment: Alignment by mutableStateOf(Alignment.Center)

    /**
     * Setup whether to enable read mode and configure read mode
     */
    var readMode: ReadMode? by mutableStateOf(null)

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    var scalesCalculator: ScalesCalculator by mutableStateOf(ScalesCalculator.Dynamic)

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    var threeStepScale: Boolean by mutableStateOf(false)

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    var rubberBandScale: Boolean by mutableStateOf(true)

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    var oneFingerScaleSpec: OneFingerScaleSpec by mutableStateOf(OneFingerScaleSpec.Default)

    /**
     * The animation configuration for the zoom animation
     */
    var animationSpec: ZoomAnimationSpec by mutableStateOf(ZoomAnimationSpec.Default)

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    var limitOffsetWithinBaseVisibleRect: Boolean by mutableStateOf(false)

    /**
     * Add whitespace around containers based on container size
     */
    var containerWhitespaceMultiple: Float by mutableStateOf(0f)

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultiple]
     */
    var containerWhitespace: ContainerWhitespace by mutableStateOf(ContainerWhitespace.Zero)

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
    var mouseWheelScaleScrollDeltaConverter: (Float) -> Float = { it * 0.33f }


    /* *********************************** Information properties ******************************* */

    /**
     * Base transformation, include the base scale, offset, rotation,
     * which is affected by [contentScale], [alignment] properties and [rotate] method
     */
    var baseTransform: Transform by mutableStateOf(Transform.Origin)
        private set

    /**
     * User transformation, include the user scale, offset, rotation,
     * which is affected by the user's gesture, [readMode] properties and [scale], [offset], [locate] method
     */
    var userTransform: Transform by mutableStateOf(Transform.Origin)
        private set

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of [baseTransform] and [userTransform]
     */
    var transform: Transform by mutableStateOf(Transform.Origin)
        private set

    /**
     * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    var minScale: Float by mutableFloatStateOf(1f)
        private set

    /**
     * Medium scale factor, only as a target value for one of when switch scale
     */
    var mediumScale: Float by mutableFloatStateOf(1f)
        private set

    /**
     * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    var maxScale: Float by mutableFloatStateOf(1f)
        private set

    /**
     * The content region in the container after the baseTransform transformation
     */
    var contentBaseDisplayRect: IntRect by mutableStateOf(IntRect.Zero)
        private set

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    var contentBaseVisibleRect: IntRect by mutableStateOf(IntRect.Zero)
        private set

    /**
     * The content region in the container after the final transform transformation
     */
    var contentDisplayRect: IntRect by mutableStateOf(IntRect.Zero)
        private set

    /**
     * The content is visible region to the user after the final transform transformation
     */
    var contentVisibleRect: IntRect by mutableStateOf(IntRect.Zero)
        private set

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    var userOffsetBounds: IntRect by mutableStateOf(IntRect.Zero)
        private set

    /**
     * Edge state for the current offset
     */
    var scrollEdge: ScrollEdge by mutableStateOf(ScrollEdge.Default)
        private set

    /**
     * The type of transformation currently in progress
     *
     * @see ContinuousTransformType
     */
    var continuousTransformType: Int by mutableIntStateOf(0)
        internal set

    private var lastContainerSize: IntSize = containerSize
    private var lastContentSize: IntSize = contentSize
    private var lastContentOriginSize: IntSize = contentOriginSize
    private var lastContentScale: ContentScale = contentScale
    private var lastAlignment: Alignment = alignment
    private var lastRotation: Int = rotation
    private var lastReadMode: ReadMode? = readMode
    private var lastScalesCalculator: ScalesCalculator = scalesCalculator
    private var lastLimitOffsetWithinBaseVisibleRect: Boolean = limitOffsetWithinBaseVisibleRect
    private var lastContainerWhitespace: ContainerWhitespace = calculateContainerWhitespace()


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Reset [transform] and [minScale], [mediumScale], [maxScale], automatically called when [containerSize],
     * [contentSize], [contentOriginSize], [contentScale], [alignment], [rotate], [scalesCalculator], [readMode] changes
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun reset(caller: String): Unit = coroutineScope {
        stopAllAnimation("reset:$caller")

        val containerSize = containerSize
        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val contentScale = contentScale
        val alignment = alignment
        val readMode = readMode
        val rotation = rotation
        val scalesCalculator = scalesCalculator
        val limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
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
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentOriginSize = contentOriginSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.toCompat(),
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = lastContainerSize.toCompat(),
            lastContentSize = lastContentSize.toCompat(),
            lastContentOriginSize = lastContentOriginSize.toCompat(),
            lastContentScale = lastContentScale.toCompat(),
            lastAlignment = lastAlignment.toCompat(),
            lastRotation = lastRotation,
            lastReadMode = lastReadMode,
            lastScalesCalculator = lastScalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = lastLimitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = lastContainerWhitespace,
        )
        if (paramsChanges == 0) {
            logger.d { "ZoomableState. reset:$caller. skipped. All parameters unchanged" }
            return@coroutineScope
        }

        val newInitialZoom = calculateInitialZoom(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentOriginSize = contentOriginSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
        )
        val newBaseTransform = newInitialZoom.baseTransform

        val onlyContainerSizeChanged = paramsChanges == 1
        val lastInitialUserTransform = lastInitialUserTransform
        val lastUserTransform = userTransform
        val thereAreUserActions = !transformAboutEquals(
            one = lastInitialUserTransform.toCompat(),
            two = lastUserTransform.toCompat()
        )
        val lastContentVisibleCenter = contentVisibleRect.center
        val newUserTransform = if (onlyContainerSizeChanged && thereAreUserActions) {
            val lastTransform = transform
            calculateRestoreContentVisibleCenterUserTransform(
                containerSize = containerSize.toCompat(),
                contentSize = contentSize.toCompat(),
                contentScale = contentScale.toCompat(),
                alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
                rotation = rotation,
                newBaseTransform = newBaseTransform,
                lastTransform = lastTransform.toCompat(),
                lastContentVisibleCenter = lastContentVisibleCenter.toCompat(),
            ).let {
                val limitUserOffset = limitUserOffset(
                    userOffset = it.offset.toPlatform(),
                    userScale = it.scaleX
                )
                it.copy(offset = limitUserOffset.toCompat())
            }
        } else {
            newInitialZoom.userTransform
        }

        logger.d {
            val transform = newBaseTransform + newUserTransform
            "ZoomableState. reset:$caller. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "rotation=${rotation}, " +
                    "scalesCalculator=${scalesCalculator}, " +
                    "readMode=${readMode}. " +
                    "lastContentVisibleCenter=${lastContentVisibleCenter.toShortString()}. " +
                    "minScale=${newInitialZoom.minScale.format(4)}, " +
                    "mediumScale=${newInitialZoom.mediumScale.format(4)}, " +
                    "maxScale=${newInitialZoom.maxScale.format(4)}, " +
                    "baseTransform=${newBaseTransform.toShortString()}, " +
                    "userTransform=${newUserTransform.toShortString()}, " +
                    "transform=${transform.toShortString()}"
        }

        minScale = newInitialZoom.minScale
        mediumScale = newInitialZoom.mediumScale
        maxScale = newInitialZoom.maxScale
        contentBaseDisplayRect = calculateContentBaseDisplayRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
        ).roundToPlatform()
        contentBaseVisibleRect = calculateContentBaseVisibleRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
        ).roundToPlatform()
        baseTransform = newBaseTransform.toPlatform()
        updateUserTransform(newUserTransform.toPlatform())

        // TODO Improve it. Create a special parameter class to encapsulate it
        this@ZoomableState.lastInitialUserTransform = newInitialZoom.userTransform.toPlatform()
        this@ZoomableState.lastContainerSize = containerSize
        this@ZoomableState.lastContentSize = contentSize
        this@ZoomableState.lastContentOriginSize = contentOriginSize
        this@ZoomableState.lastContentScale = contentScale
        this@ZoomableState.lastAlignment = alignment
        this@ZoomableState.lastReadMode = readMode
        this@ZoomableState.lastRotation = rotation
        this@ZoomableState.lastScalesCalculator = scalesCalculator
        this@ZoomableState.lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
        this@ZoomableState.lastContainerWhitespace = containerWhitespace
    }

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffset = contentVisibleRect.center,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            userScale = currentUserScale,
            userOffset = currentUserOffset.toCompat(),
            contentPoint = centroidContentPoint.toCompatOffset(),
        ).toPlatform()
        val targetUserOffset = calculateScaleUserOffset(
            currentUserScale = currentUserTransform.scaleX,
            currentUserOffset = currentUserTransform.offset.toCompat(),
            targetUserScale = limitedTargetUserScale,
            centroid = touchPoint.toCompat(),
        ).toPlatform()
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "ZoomableState. scale. " +
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
     * If [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only cycle between [minScale] and [mediumScale]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun switchScale(
        centroidContentPoint: IntOffset = contentVisibleRect.center,
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
        targetOffset: Offset,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
            "ZoomableState. offset. " +
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
        contentPoint: IntOffset,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentScale = contentScale
        val alignment = alignment
        val rotation = rotation
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform

        stopAllAnimation("locate")

        val containerPoint = contentPointToContainerPoint(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            contentPoint = contentPoint.toCompatOffset(),
        )

        val targetUserScale = targetScale / currentBaseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)

        val targetUserOffset = calculateLocateUserOffset(
            containerSize = containerSize.toCompat(),
            containerPoint = containerPoint,
            userScale = limitedTargetUserScale,
        ).toPlatform()
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
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
            "ZoomableState. locate. " +
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

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: Offset): IntOffset {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return IntOffset.Zero
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return IntOffset.Zero
        val currentUserTransform = userTransform
        val contentScale = contentScale
        val alignment = alignment
        val rotation = rotation
        val contentPoint = touchPointToContentPoint(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            userOffset = currentUserTransform.offset.toCompat(),
            touchPoint = touchPoint.toCompat()
        ).toPlatform()
        return contentPoint.round()
    }

    /**
     * If true is returned, scrolling can continue on the specified axis and direction
     *
     * @param horizontal Whether to scroll horizontally
     * @param direction positive means scroll to the right or scroll down, negative means scroll to the left or scroll up
     */
    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        canScrollByEdge(scrollEdge, horizontal, direction)


    /* *************************************** Internal ***************************************** */

    override fun onRemembered() {
        // Since ZoomableState is annotated with @Stable, onRemembered will be executed multiple times,
        // but we only need execute it once
        rememberedCount++
        if (rememberedCount != 1) return

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope

        // Must be immediate, otherwise the user will see the image move quickly from the top to the center
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { containerSize }.collect {
                reset("containerSizeChanged")
            }
        }
        // Must be immediate, otherwise the user will see the image move quickly from the top to the center
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { contentSize }.collect {
                reset("contentSizeChanged")
            }
        }
        // Must be immediate, otherwise the user will see the image move quickly from the top to the center
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { contentOriginSize }.collect {
                reset("contentOriginSizeChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { contentScale }.collect {
                reset("contentScaleChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { alignment }.collect {
                reset("alignmentChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { readMode }.collect {
                reset("readModeChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { scalesCalculator }.collect {
                reset("scalesCalculatorChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { limitOffsetWithinBaseVisibleRect }.collect {
                reset("limitOffsetWithinBaseVisibleRectChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { containerWhitespaceMultiple }.collect {
                reset("containerWhitespaceMultipleChanged")
            }
        }
        coroutineScope.launch {
            snapshotFlow { containerWhitespace }.collect {
                reset("containerWhitespaceChanged")
            }
        }
    }

    override fun onAbandoned() = onForgotten()
    override fun onForgotten() {
        // Since ZoomableState is annotated with @Stable, onForgotten will be executed multiple times,
        // but we only need execute it once
        if (rememberedCount <= 0) return
        rememberedCount--
        if (rememberedCount != 0) return

        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) {
            coroutineScope.cancel("onForgotten")
            this.coroutineScope = null
        }
    }

    /**
     * Stop all animations immediately
     */
    internal suspend fun stopAllAnimation(caller: String) {
        val lastScaleAnimatable = lastScaleAnimatable
        if (lastScaleAnimatable?.isRunning == true) {
            lastScaleAnimatable.stop()
            logger.d { "ZoomableState. stopScaleAnimation:$caller" }
        }

        val lastFlingAnimatable = lastFlingAnimatable
        if (lastFlingAnimatable?.isRunning == true) {
            lastFlingAnimatable.stop()
            logger.d { "ZoomableState. stopFlingAnimation:$caller" }
        }

        val lastContinuousTransformType = continuousTransformType
        if (lastContinuousTransformType != 0) {
            continuousTransformType = 0
        }
    }

    internal suspend fun rollbackScale(centroid: Offset? = null): Boolean = coroutineScope {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
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
                "ZoomableState. rollbackScale. " +
                        "centroid=${centroid?.toShortString()}. " +
                        "startScale=${startScale.format(4)}, " +
                        "endScale=${endScale.format(4)}"
            }
            val finalCentroid = centroid ?: containerSize.toSize().center
            val updateAnimatable = Animatable(0f)
            this@ZoomableState.lastScaleAnimatable = updateAnimatable
            continuousTransformType = ContinuousTransformType.SCALE
            try {
                updateAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animationSpec.durationMillis,
                        easing = animationSpec.easing
                    ),
                    initialVelocity = animationSpec.initialVelocity,
                ) {
                    val frameScale = androidx.compose.ui.util.lerp(
                        start = startScale,
                        stop = endScale,
                        fraction = value
                    )
                    val nowScale = this@ZoomableState.transform.scaleX
                    val addScale = frameScale / nowScale
                    coroutineScope?.launch {
                        gestureTransform(
                            centroid = finalCentroid,
                            panChange = Offset.Zero,
                            zoomChange = addScale,
                            rotationChange = 0f
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                continuousTransformType = 0
            }
        }
        targetScale != null
    }

    internal suspend fun gestureTransform(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ): Unit = coroutineScope {
        containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope
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
            currentOffset = currentUserOffset.toCompat(),
            targetScale = limitedTargetUserScale,
            centroid = centroid.toCompat(),
            pan = panChange.toCompat(),
            gestureRotate = 0f,
        ).toPlatform()
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "ZoomableState. transform. " +
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

    internal suspend fun fling(velocity: Velocity, density: Density): Boolean = coroutineScope {
        containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val currentUserTransform = userTransform

        stopAllAnimation("fling")

        val startUserOffset = currentUserTransform.offset
        logger.d {
            "ZoomableState. fling. start. " +
                    "start=${startUserOffset.toShortString()}, " +
                    "bounds=${userOffsetBounds.toShortString()}, " +
                    "velocity=${velocity}"
        }
        val flingAnimatable = Animatable(
            initialValue = startUserOffset,
            typeConverter = Offset.VectorConverter,
        )
        this@ZoomableState.lastFlingAnimatable = flingAnimatable
        var job: Job? = null
        job = coroutineScope {
            launch {
                continuousTransformType = ContinuousTransformType.FLING
                try {
                    val initialVelocity = Offset.VectorConverter
                        .convertFromVector(AnimationVector(velocity.x, velocity.y))
                    flingAnimatable.animateDecay(
                        initialVelocity = initialVelocity,
                        animationSpec = splineBasedDecay(density)
                    ) {
                        val currentUserTransform2 = this@ZoomableState.userTransform
                        val targetUserOffset = this.value
                        val limitedTargetUserOffset =
                            limitUserOffset(targetUserOffset, currentUserTransform2.scaleX)
                        if (limitedTargetUserOffset != currentUserTransform2.offset) {
                            logger.d {
                                "ZoomableState. fling. running. " +
                                        "velocity=$velocity. " +
                                        "startUserOffset=${startUserOffset.toShortString()}, " +
                                        "currentUserOffset=${limitedTargetUserOffset.toShortString()}"
                            }
                            val newUserOffset =
                                currentUserTransform2.copy(offset = limitedTargetUserOffset)
                            updateUserTransform(newUserOffset)
                        } else {
                            // SubsamplingState(line 87) relies on the fling state to refresh tiles,
                            // so you need to end the fling animation as soon as possible
                            job?.cancel("reachBounds")
                            continuousTransformType = 0
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } finally {
                    continuousTransformType = 0
                }
            }
        }
        true
    }

    internal fun checkSupportGestureType(@GestureType gestureType: Int): Boolean =
        disabledGestureTypes.and(gestureType) == 0

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
            maxScale = maxUserScale,
            rubberBandRatio = 2f,
        )
    }

    private fun limitUserOffset(userOffset: Offset, userScale: Float): Offset {
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            userScale = userScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(layoutDirection),
        ).round().toPlatformRect()    // round() makes sense
        return userOffset.limitTo(userOffsetBounds)
    }

    private suspend fun animatedUpdateUserTransform(
        targetUserTransform: Transform,
        @ContinuousTransformType newContinuousTransformType: Int?,
        animationSpec: ZoomAnimationSpec?,
        caller: String
    ) {
        val currentUserTransform = userTransform
        val updateAnimatable = Animatable(0f)
        this.lastScaleAnimatable = updateAnimatable
        if (newContinuousTransformType != null) {
            continuousTransformType = newContinuousTransformType
        }
        val finalAnimationSpec = animationSpec ?: this@ZoomableState.animationSpec
        try {
            updateAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = finalAnimationSpec.durationMillis,
                    easing = finalAnimationSpec.easing
                ),
                initialVelocity = finalAnimationSpec.initialVelocity,
            ) {
                val userTransform = lerp(
                    start = currentUserTransform,
                    stop = targetUserTransform,
                    fraction = value
                )
                logger.d {
                    "ZoomableState. $caller. animated running. transform=${userTransform.toShortString()}"
                }
                this@ZoomableState.userTransform = userTransform
                updateTransform()
            }
        } catch (e: CancellationException) {
            throw e
        } finally {
            if (newContinuousTransformType != null) {
                continuousTransformType = 0
            }
        }
    }

    private fun updateUserTransform(targetUserTransform: Transform) {
        this.userTransform = targetUserTransform
        updateTransform()
    }

    private fun updateTransform() {
        val userTransform = userTransform
        transform = baseTransform + userTransform

        contentDisplayRect = calculateContentDisplayRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset.toCompat(),
        ).roundToPlatform()
        contentVisibleRect = calculateContentVisibleRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset.toCompat(),
        ).roundToPlatform()

        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = alignment.rtlFlipped(layoutDirection).toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(layoutDirection),
        )
        this.userOffsetBounds = userOffsetBounds.roundToPlatform()

        scrollEdge = calculateScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset.toCompat(),
        )
    }

    private fun calculateContainerWhitespace(): ContainerWhitespace {
        val containerWhitespace = containerWhitespace
        val containerSize = containerSize
        val containerWhitespaceMultiple = containerWhitespaceMultiple
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
        "ZoomableState(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentOriginSize=${contentOriginSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}, " +
                "minScale=${minScale.format(4)}, " +
                "mediumScale=${mediumScale.format(4)}, " +
                "maxScale=${maxScale.format(4)}, " +
                "transform=${transform.toShortString()}" +
                ")"
}