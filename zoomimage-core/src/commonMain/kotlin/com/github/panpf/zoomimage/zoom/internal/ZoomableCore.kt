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

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.Origin
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.containsWithDelta
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.isInRangeWithScale
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.isThumbnailWithSize
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.requiredMainThread
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
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
import com.github.panpf.zoomimage.zoom.isEmpty
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.rtlFlipped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Core that control scale, pan, rotation, ZoomableState and ZoomableEngine are its UI wrappers
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomableCoreTest
 */
class ZoomableCore constructor(
    val logger: Logger,
    val module: String,
    val animationAdapter: AnimationAdapter,
    val onTransformChanged: (zoomableCore: ZoomableCore) -> Unit,
) {

    var rotation: Int = 0
        private set

    var containerSize: IntSizeCompat = IntSizeCompat.Zero
        private set
    var contentSize: IntSizeCompat = IntSizeCompat.Zero
        private set
    var contentOriginSize: IntSizeCompat = IntSizeCompat.Zero
        private set

    var contentScale: ContentScaleCompat = ContentScaleCompat.Fit
        private set
    var alignment: AlignmentCompat = AlignmentCompat.Center
        private set
    var rtlLayoutDirection: Boolean = false
        private set
    var readMode: ReadMode? = null
        private set
    var scalesCalculator: ScalesCalculator = ScalesCalculator.Dynamic
        private set
    var threeStepScale: Boolean = false
        private set
    var rubberBandScale: Boolean = true
        private set
    var oneFingerScaleSpec: OneFingerScaleSpec = OneFingerScaleSpec.Default
        private set
    var animationSpec: BaseZoomAnimationSpec? = null
        private set

    var limitOffsetWithinBaseVisibleRect: Boolean = false
        private set
    var containerWhitespaceMultiple: Float = 0f
        private set
    var containerWhitespace: ContainerWhitespace = ContainerWhitespace.Zero
        private set
    var keepTransformWhenSameAspectRatioContentSizeChanged: Boolean = false
        private set

    var baseTransform: TransformCompat = TransformCompat.Origin
        private set
    var userTransform: TransformCompat = TransformCompat.Origin
        private set
    var transform: TransformCompat = TransformCompat.Origin
        private set
    var minScale: Float = 1.0f
        private set
    var mediumScale: Float = 1.0f
        private set
    var maxScale: Float = 1.0f
        private set
    var contentBaseDisplayRect: RectCompat = RectCompat.Zero
        private set
    var contentBaseVisibleRect: RectCompat = RectCompat.Zero
        private set
    var contentDisplayRect: RectCompat = RectCompat.Zero
        private set
    var contentVisibleRect: RectCompat = RectCompat.Zero
        private set
    var sourceScaleFactor: ScaleFactorCompat = ScaleFactorCompat.Origin
        private set
    var sourceVisibleRect: RectCompat = RectCompat.Zero
        private set
    var scrollEdge: ScrollEdge = ScrollEdge.Default
        private set
    var userOffsetBoundsRect: RectCompat = RectCompat.Zero
        private set

    @ContinuousTransformType
    var continuousTransformType: Int = ContinuousTransformType.NONE
        private set

    private var resetParams: ResetParams? = null
    private var initialZoom: InitialZoom? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: OffsetCompat = contentVisibleRect.center,
        animated: Boolean = false,
        animationSpec: BaseZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        requiredMainThread()
        val containerSize =
            containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform
        val contentScale = contentScale
        val alignment = alignment
        val rtlLayoutDirection = rtlLayoutDirection
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
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = currentUserScale,
            userOffset = currentUserOffset,
            contentPoint = centroidContentPoint,
        )
        val targetUserOffset = calculateScaleUserOffset(
            currentUserScale = currentUserScale,
            currentUserOffset = currentUserOffset,
            targetUserScale = limitedTargetUserScale,
            centroid = touchPoint,
        )
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            newUserScale = limitedTargetUserScale,
        )
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

    suspend fun switchScale(
        centroidContentPoint: OffsetCompat = contentVisibleRect.center,
        animated: Boolean = false,
        animationSpec: BaseZoomAnimationSpec? = null,
    ): Float? {
        requiredMainThread()
        val nextScale = getNextStepScale()
        val scaleResult = scale(
            targetScale = nextScale,
            centroidContentPoint = centroidContentPoint,
            animationSpec = animationSpec,
            animated = animated
        )
        return if (scaleResult) nextScale else null
    }

    suspend fun offset(
        targetOffset: OffsetCompat,
        animated: Boolean = false,
        animationSpec: BaseZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        requiredMainThread()
        containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform

        stopAllAnimation("offset")

        val scaledBaseOffset = currentBaseTransform.offset.times(currentUserTransform.scale)
        val targetUserOffset = targetOffset - scaledBaseOffset
        val currentUserScale = currentUserTransform.scaleX
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            newUserScale = currentUserScale,
        )
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

    suspend fun locate(
        contentPoint: OffsetCompat,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
        animationSpec: BaseZoomAnimationSpec? = null,
    ): Boolean = coroutineScope {
        requiredMainThread()
        val containerSize =
            containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize =
            contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentScale = contentScale
        val alignment = alignment
        val rtlLayoutDirection = rtlLayoutDirection
        val rotation = rotation
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform

        stopAllAnimation("locate")

        val containerPoint = contentPointToContainerPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            contentPoint = contentPoint,
        )

        val targetUserScale = targetScale / currentBaseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)

        val targetUserOffset = calculateLocateUserOffset(
            containerSize = containerSize,
            containerPoint = containerPoint,
            userScale = limitedTargetUserScale,
        )
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            newUserScale = limitedTargetUserScale,
        )
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

    suspend fun rotate(targetRotation: Int): Unit = coroutineScope {
        requiredMainThread()
        require(targetRotation % 90 == 0) { "rotation must be in multiples of 90: $targetRotation" }
        val limitedTargetRotation = (targetRotation % 360).let { if (it < 0) 360 + it else it }
        val currentRotation = rotation
        if (currentRotation == limitedTargetRotation) return@coroutineScope
        rotation = limitedTargetRotation
        reset("rotate")
    }

    fun getNextStepScale(): Float {
        requiredMainThread()
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

    fun touchPointToContentPoint(touchPoint: OffsetCompat): OffsetCompat {
        requiredMainThread()
        val containerSize =
            containerSize.takeIf { it.isNotEmpty() } ?: return OffsetCompat.Zero
        val contentSize =
            contentSize.takeIf { it.isNotEmpty() } ?: return OffsetCompat.Zero
        val currentUserTransform = userTransform
        val contentScale = contentScale
        val alignment = alignment
        val rtlLayoutDirection = rtlLayoutDirection
        val rotation = rotation
        val contentPoint = touchPointToContentPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            userOffset = currentUserTransform.offset,
            touchPoint = touchPoint
        )
        return contentPoint
    }

    fun sourceToDraw(point: OffsetCompat): OffsetCompat {
        val contentSize = this@ZoomableCore.contentSize
        val contentOriginSize =
            this@ZoomableCore.contentOriginSize.takeIf { it.isNotEmpty() } ?: contentSize
        val transform = this@ZoomableCore.transform
        val rotation = rotation
        return sourceToDraw(
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            rotation = rotation,
            scale = transform.scale,
            offset = transform.offset,
            point = point
        )
    }

    fun sourceToDraw(rect: RectCompat): RectCompat {
        val contentSize = this@ZoomableCore.contentSize
        val contentOriginSize =
            this@ZoomableCore.contentOriginSize.takeIf { it.isNotEmpty() } ?: contentSize
        val transform = this@ZoomableCore.transform
        val rotation = rotation
        return sourceToDraw(
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            rotation = rotation,
            scale = transform.scale,
            offset = transform.offset,
            rect = rect
        )
    }

    fun canScroll(horizontal: Boolean, direction: Int): Boolean {
        return canScrollByEdge(scrollEdge, horizontal, direction)
    }


    fun setContainerSize(containerSize: IntSizeCompat) {
        requiredMainThread()
        if (this.containerSize != containerSize) {
            this.containerSize = containerSize
            logger.d { "$module. containerSize=$containerSize" }
            reset("containerSizeChanged")
        }
    }

    fun setContentSize(contentSize: IntSizeCompat) {
        requiredMainThread()
        if (this.contentSize != contentSize) {
            this.contentSize = contentSize
            logger.d { "$module. contentSize=$contentSize" }
            reset("contentSizeChanged")
        }
    }

    fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
        requiredMainThread()
        if (this.contentOriginSize != contentOriginSize) {
            this.contentOriginSize = contentOriginSize
            reset("contentOriginSizeChanged")
        }
    }

    fun setContentScale(contentScale: ContentScaleCompat) {
        requiredMainThread()
        if (this.contentScale != contentScale) {
            this.contentScale = contentScale
            logger.d { "$module. contentScale=${contentScale.name}" }
            reset("contentScaleChanged")
        }
    }

    fun setAlignment(alignment: AlignmentCompat) {
        requiredMainThread()
        if (this.alignment != alignment) {
            this.alignment = alignment
            logger.d { "$module. alignment=${alignment.name}" }
            reset("alignmentChanged")
        }
    }

    fun setRtlLayoutDirection(rtlLayoutDirection: Boolean) {
        requiredMainThread()
        if (this.rtlLayoutDirection != rtlLayoutDirection) {
            this.rtlLayoutDirection = rtlLayoutDirection
            logger.d { "$module. rtlLayoutDirection=$rtlLayoutDirection" }
            reset("rtlLayoutDirectionChanged")
        }
    }

    fun setReadMode(readMode: ReadMode?) {
        requiredMainThread()
        if (this.readMode != readMode) {
            this.readMode = readMode
            logger.d { "$module. readMode=$readMode" }
            reset("readModeChanged")
        }
    }

    fun setScalesCalculator(scalesCalculator: ScalesCalculator) {
        requiredMainThread()
        if (this.scalesCalculator != scalesCalculator) {
            this.scalesCalculator = scalesCalculator
            logger.d { "$module. scalesCalculator=$scalesCalculator" }
            reset("scalesCalculatorChanged")
        }
    }

    fun setThreeStepScale(threeStepScale: Boolean) {
        requiredMainThread()
        if (this.threeStepScale != threeStepScale) {
            this.threeStepScale = threeStepScale
            logger.d { "$module. threeStepScale=$threeStepScale" }
        }
    }

    fun setRubberBandScale(rubberBandScale: Boolean) {
        requiredMainThread()
        if (this.rubberBandScale != rubberBandScale) {
            this.rubberBandScale = rubberBandScale
            logger.d { "$module. rubberBandScale=$rubberBandScale" }
        }
    }

    fun setOneFingerScaleSpec(oneFingerScaleSpec: OneFingerScaleSpec) {
        requiredMainThread()
        if (this.oneFingerScaleSpec != oneFingerScaleSpec) {
            this.oneFingerScaleSpec = oneFingerScaleSpec
            logger.d { "$module. oneFingerScaleSpec=$oneFingerScaleSpec" }
        }
    }

    fun setAnimationSpec(animationSpec: BaseZoomAnimationSpec) {
        requiredMainThread()
        if (this.animationSpec != animationSpec) {
            this.animationSpec = animationSpec
            logger.d { "$module. animationSpec=$animationSpec" }
        }
    }

    fun setLimitOffsetWithinBaseVisibleRect(limitOffsetWithinBaseVisibleRect: Boolean) {
        requiredMainThread()
        if (this.limitOffsetWithinBaseVisibleRect != limitOffsetWithinBaseVisibleRect) {
            this.limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
            logger.d { "$module. limitOffsetWithinBaseVisibleRect=$limitOffsetWithinBaseVisibleRect" }
            reset("limitOffsetWithinBaseVisibleRectChanged")
        }
    }

    fun setContainerWhitespaceMultiple(containerWhitespaceMultiple: Float) {
        requiredMainThread()
        if (this.containerWhitespaceMultiple != containerWhitespaceMultiple) {
            this.containerWhitespaceMultiple = containerWhitespaceMultiple
            logger.d { "$module. containerWhitespaceMultiple=$containerWhitespaceMultiple" }
            reset("containerWhitespaceMultipleChanged")
        }
    }

    fun setContainerWhitespace(containerWhitespace: ContainerWhitespace) {
        requiredMainThread()
        if (this.containerWhitespace != containerWhitespace) {
            this.containerWhitespace = containerWhitespace
            logger.d { "$module. containerWhitespace=$containerWhitespace" }
            reset("containerWhitespaceChanged")
        }
    }

    fun setKeepTransformWhenSameAspectRatioContentSizeChanged(keepTransform: Boolean) {
        requiredMainThread()
        if (this.keepTransformWhenSameAspectRatioContentSizeChanged != keepTransform) {
            this.keepTransformWhenSameAspectRatioContentSizeChanged = keepTransform
            logger.d { "$module. keepTransformWhenSameAspectRatioContentSizeChanged=$keepTransform" }
        }
    }

    fun reset(caller: String, force: Boolean = false) {
        requiredMainThread()

        val lastInitialZoom = initialZoom
        val lastResetParams = resetParams
        val newResetParams = ResetParams(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            rotation = rotation,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespaceMultiple = containerWhitespaceMultiple,
            containerWhitespace = containerWhitespace,
        )
        val diffResult = newResetParams.diff(lastResetParams)
        if (!force && diffResult.isNotChanged) {
            logger.d { "$module. reset:$caller. skipped. All parameters unchanged" }
            return
        }

        val newInitialZoom = calculateInitialZoom(
            containerSize = newResetParams.containerSize,
            contentSize = newResetParams.contentSize,
            contentOriginSize = newResetParams.contentOriginSize,
            contentScale = newResetParams.contentScale,
            alignment = newResetParams.alignment,
            rtlLayoutDirection = newResetParams.rtlLayoutDirection,
            rotation = newResetParams.rotation,
            readMode = newResetParams.readMode,
            scalesCalculator = newResetParams.scalesCalculator,
        )

        val newBaseTransform = newInitialZoom.baseTransform

        val currentUserTransform = userTransform
        val hasUserActions = lastInitialZoom != null && !transformAboutEquals(
            one = currentUserTransform,
            two = lastInitialZoom.userTransform
        )
        val (newUserTransform, mode) = if (
            !force
            && hasUserActions
            && diffResult.isOnlyContainerSizeChanged
            && lastResetParams != null
            && lastResetParams.containerSize.isNotEmpty()
            && newResetParams.containerSize.isNotEmpty()
        ) {
            val restoreTransform =
                calculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged(
                    oldContainerSize = lastResetParams.containerSize,
                    newContainerSize = newResetParams.containerSize,
                    contentSize = newResetParams.contentSize,
                    contentScale = newResetParams.contentScale,
                    alignment = newResetParams.alignment,
                    rtlLayoutDirection = newResetParams.rtlLayoutDirection,
                    rotation = newResetParams.rotation,
                    transform = transform,
                )
            val restoreUserTransform = restoreTransform - newBaseTransform
            val limitUserOffset = limitUserOffset(
                newUserOffset = restoreUserTransform.offset,
                newUserScale = restoreUserTransform.scaleX,
            )
            restoreUserTransform.copy(offset = limitUserOffset) to "restoreVisibleCenter"
        } else if (
            !force
            && hasUserActions
            && keepTransformWhenSameAspectRatioContentSizeChanged
            && diffResult.isOnlyContentSizeChanged
            && lastResetParams != null
            && lastResetParams.contentSize.isNotEmpty()
            && newResetParams.contentSize.isNotEmpty()
            && shouldRestoreVisibleRect(
                oldContentSize = lastResetParams.contentSize,
                newContentSize = newResetParams.contentSize,
                diffResult = diffResult
            )
        ) {
            val restoreTransform =
                calculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged(
                    oldContentSize = lastResetParams.contentSize,
                    newContentSize = newResetParams.contentSize,
                    transform = transform,
                ).copy(rotationOrigin = newBaseTransform.rotationOrigin)
            val restoreUserTransform = restoreTransform - newBaseTransform
            val limitUserOffset = limitUserOffset(
                newUserOffset = restoreUserTransform.offset,
                newUserScale = restoreUserTransform.scaleX,
            )
            restoreUserTransform.copy(offset = limitUserOffset) to "restoreVisibleRect"
        } else {
            newInitialZoom.userTransform to "reset"
        }

        // contentOriginSize usually only affects minScale, mediumScale, maxScale,
        // Therefore, when the user has an operation or is in the animation,
        // you can skip resetting and only set minScale, mediumScale, maxScale to avoid interrupting the user's operation
        val skipResetTransform = !force
                && diffResult.isOnlyContentOriginSizeChanged
                && lastInitialZoom != null
                && newInitialZoom.baseTransform == lastInitialZoom.baseTransform
                && newInitialZoom.userTransform == lastInitialZoom.userTransform
                && (hasUserActions || animationAdapter.isRunning() || animationAdapter.isFlingRunning())
        logger.d {
            val transform = newBaseTransform + newUserTransform
            "$module. reset:$caller. $mode. ${if (skipResetTransform) "skip. " else ""}" +
                    "containerSize=${newResetParams.containerSize.toShortString()}, " +
                    "contentSize=${newResetParams.contentSize.toShortString()}, " +
                    "contentOriginSize=${newResetParams.contentOriginSize.toShortString()}, " +
                    "contentScale=${newResetParams.contentScale.name}, " +
                    "alignment=${newResetParams.alignment.name}, " +
                    "rtlLayoutDirection=${newResetParams.rtlLayoutDirection}, " +
                    "rotation=${newResetParams.rotation}, " +
                    "scalesCalculator=${newResetParams.scalesCalculator}, " +
                    "readMode=${newResetParams.readMode}. " +
                    "keepTransform=${keepTransformWhenSameAspectRatioContentSizeChanged}. " +
                    "hasUserActions=${hasUserActions}. " +
                    "diffResult=${diffResult}, " +
                    "animationRunning=(${animationAdapter.isRunning()},${animationAdapter.isFlingRunning()}). " +
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

        if (!skipResetTransform) {
            coroutineScope.launch(Dispatchers.Main.immediate) {
                stopAllAnimation(caller)
            }
            contentBaseDisplayRect = calculateContentBaseDisplayRect(
                containerSize = newResetParams.containerSize,
                contentSize = newResetParams.contentSize,
                contentScale = newResetParams.contentScale,
                alignment = newResetParams.alignment,
                rtlLayoutDirection = newResetParams.rtlLayoutDirection,
                rotation = newResetParams.rotation,
            )
            contentBaseVisibleRect = calculateContentBaseVisibleRect(
                containerSize = newResetParams.containerSize,
                contentSize = newResetParams.contentSize,
                contentScale = newResetParams.contentScale,
                alignment = newResetParams.alignment,
                rtlLayoutDirection = newResetParams.rtlLayoutDirection,
                rotation = newResetParams.rotation,
            )
            baseTransform = newBaseTransform
            updateUserTransform(newUserTransform)
        }

        initialZoom = newInitialZoom
        resetParams = newResetParams
    }


    /* *************************************** Internal ***************************************** */

    private fun shouldRestoreVisibleRect(
        oldContentSize: IntSizeCompat,
        newContentSize: IntSizeCompat,
        diffResult: ResetParamsDiffResult,
    ): Boolean {
        // Relax epsilonPixels to 2f to avoid errors caused by image scaling
        val isThumbnail = isThumbnailWithSize(
            size = oldContentSize,
            otherSize = newContentSize,
            epsilonPixels = 2f,
        )
        if (diffResult.isContentSizeChanged && isThumbnail) {
            return true
        }
        return diffResult.isContentOriginSizeChanged
    }

    suspend fun stopAllAnimation(caller: String) {
        requiredMainThread()
        if (animationAdapter.stopAnimation()) {
            logger.d { "$module. stopTransformAnimation:$caller" }
        }

        if (animationAdapter.stopFlingAnimation()) {
            logger.d { "$module. stopFlingAnimation:$caller" }
        }

        val lastContinuousTransformType = continuousTransformType
        if (lastContinuousTransformType != 0) {
            setContinuousTransformType(ContinuousTransformType.NONE)
        }
    }

    suspend fun gestureTransform(
        centroid: OffsetCompat,
        panChange: OffsetCompat,
        zoomChange: Float,
        rotationChange: Float
    ): Unit = coroutineScope {
        containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope

        val currentScale = transform.scaleX
        val baseScale = baseTransform.scaleX
        val targetScale = currentScale * zoomChange
        val targetUserScale = targetScale / baseScale
        val limitedTargetUserScale = limitUserScale(
            targetUserScale = targetUserScale,
            rubberBandMode = rubberBandScale
        )

        val currentUserTransform = userTransform
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
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            newUserScale = limitedTargetUserScale,
        )
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactorCompat(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        // @formatter:off. Please turn "Editor | Code Style | Formatter | Turn formatter on/off with markers in code comments" configuration item of IDEA
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "$module. gestureTransform. " +
                    "centroid=${centroid.toShortString()}, " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "userScale=${currentUserScale.format(4)} " +
                    "-> ${targetUserScale.format(4)}(${targetAddUserScale.format(4)}) " +
                    "-> ${limitedTargetUserScale.format(4)}(${limitedAddUserScale.format(4)}), " +
                    "panChange=${panChange.toShortString()}, " +
                    "userOffset=${currentUserOffset.toShortString()} " +
                    "-> ${targetUserOffset.toShortString()}(${targetAddUserOffset.toShortString()}) " +
                    "-> ${limitedTargetUserOffset.toShortString()}(${limitedTargetAddOffset.toShortString()}), " +
                    "rotationChange=${rotationChange.format(4)}. " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }
        // @formatter:on. Please turn "Editor | Code Style | Formatter | Turn formatter on/off with markers in code comments" configuration item of IDEA

        updateUserTransform(limitedTargetUserTransform)
    }

    suspend fun rollback(focus: OffsetCompat? = null): Boolean = coroutineScope {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        val currentScale = transform.scaleX
        val currentBaseTransform = baseTransform
        val currentUserTransform = userTransform
        val currentUserOffset = currentUserTransform.offset
        val currentUserScale = currentUserTransform.scale.scaleX
        val userOffsetBoundsRect = userOffsetBoundsRect
        val minScale = minScale
        val maxScale = maxScale

        val scaleInRange =
            currentScale.isInRangeWithScale(min = minScale, max = maxScale, scale = 2)
        val userOffsetInRange =
            userOffsetBoundsRect.containsWithDelta(offset = currentUserOffset, delta = 1f)
        if (scaleInRange && userOffsetInRange) {
            return@coroutineScope false
        }

        val finalFocus = focus ?: containerSize.toSize().center
        val centroidContentPoint = touchPointToContentPoint(finalFocus)
        val targetScale = currentScale.coerceIn(minScale, maxScale)
        val targetUserScale = targetScale / currentBaseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)
        val touchPoint = contentPointToTouchPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = currentUserScale,
            userOffset = currentUserOffset,
            contentPoint = centroidContentPoint,
        )
        val targetUserOffset = calculateScaleUserOffset(
            currentUserScale = currentUserTransform.scaleX,
            currentUserOffset = currentUserTransform.offset,
            targetUserScale = limitedTargetUserScale,
            centroid = touchPoint,
        )
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            newUserScale = limitedTargetUserScale,
        )
        val newUserTransform = currentUserTransform.copy(
            scale = ScaleFactorCompat(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )

        logger.d {
            "$module. rollback. " +
                    "focus=${focus?.toShortString()}. " +
                    "currentScale=${currentScale.format(4)}, " +
                    "minScale=${minScale.format(4)}, " +
                    "maxScale=${maxScale.format(4)}, " +
                    "userOffsetBoundsRect=${userOffsetBoundsRect.toShortString()}, " +
                    "currentUserOffset=${currentUserOffset.toShortString()}, " +
                    "currentUserTransform=${currentUserTransform.toShortString()}, " +
                    "newUserTransform=${newUserTransform.toShortString()}"
        }
        animatedUpdateUserTransform(
            targetUserTransform = newUserTransform,
            newContinuousTransformType = ContinuousTransformType.ROLLBACK,
            animationSpec = animationSpec,
            caller = "rollback"
        )
        return@coroutineScope true
    }

    @Deprecated(message = "Use `rollback()` instead", replaceWith = ReplaceWith("rollback(focus)"))
    suspend fun rollbackScale(focus: OffsetCompat? = null): Boolean = rollback(focus)

    suspend fun fling(velocity: OffsetCompat, extras: Map<String, Any>): Boolean = coroutineScope {
        containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false

        stopAllAnimation("fling")

        setContinuousTransformType(ContinuousTransformType.FLING)
        try {
            val startUserOffset = userTransform.offset
            val userOffsetBoundsRect = this@ZoomableCore.userOffsetBoundsRect
            logger.d {
                "$module. fling. start. " +
                        "startUserOffset=${startUserOffset.toShortString()}, " +
                        "userOffsetBounds=${userOffsetBoundsRect.toShortString()}, " +
                        "velocity=${velocity.toShortString()}"
            }
            animationAdapter.startFlingAnimation(
                startUserOffset = startUserOffset,
                userOffsetBounds = userOffsetBoundsRect,
                velocity = velocity,
                extras = extras,
                onUpdateValue = { newUserOffset ->
                    val nowUserTransform = this@ZoomableCore.userTransform
                    val limitedTargetUserOffset = limitUserOffset(
                        newUserOffset = newUserOffset,
                        newUserScale = nowUserTransform.scaleX,
                    )
                    val continue1 = limitedTargetUserOffset != nowUserTransform.offset
                    if (continue1) {
                        val newUserTransform = this@ZoomableCore.userTransform
                            .copy(offset = limitedTargetUserOffset)
                        logger.v {
                            "$module. fling. running. " +
                                    "velocity=$velocity. " +
                                    "startUserOffset=${startUserOffset.toShortString()}, " +
                                    "currentUserOffset=${newUserTransform.toShortString()}, " +
                                    "continue1=${continue1}"
                        }
                        updateUserTransform(newUserTransform)
                    }
                    continue1
                },
                onEnd = {
                }
            )
            logger.d {
                "$module. fling. end. " +
                        "offset=${userTransform.offset.toShortString()}, " +
                        "bounds=${userOffsetBoundsRect.toShortString()}, " +
                        "velocity=${velocity.toShortString()}"
            }
        } finally {
            setContinuousTransformType(ContinuousTransformType.NONE)
        }
        true
    }

    fun setContinuousTransformType(@ContinuousTransformType continuousTransformType: Int) {
        requiredMainThread()
        this.continuousTransformType = continuousTransformType
        onTransformChanged(this@ZoomableCore)
    }

    fun checkSupportGestureType(disabledGestureTypes: Int, @GestureType gestureType: Int): Boolean =
        disabledGestureTypes.and(gestureType) == 0

    private fun limitUserScale(targetUserScale: Float, rubberBandMode: Boolean = false): Float {
        val minUserScale = minScale / baseTransform.scaleX
        val maxUserScale = maxScale / baseTransform.scaleX
        return if (rubberBandMode) {
            val currentScale = userTransform.scaleX
            limitScaleWithRubberBand(
                currentScale = currentScale,
                targetScale = targetUserScale,
                minScale = minUserScale,
                maxScale = maxUserScale,
                rubberBandRatio = 2f,
            )
        } else {
            targetUserScale.coerceIn(minimumValue = minUserScale, maximumValue = maxUserScale)
        }
    }

    private fun limitUserOffset(
        newUserOffset: OffsetCompat,
        newUserScale: Float,
    ): OffsetCompat {
        val userOffsetBoundsRect = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = newUserScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(rtlLayoutDirection),
        ).round().toRect()      // round() makes sense
        return newUserOffset.limitTo(userOffsetBoundsRect)
    }

    private suspend fun animatedUpdateUserTransform(
        targetUserTransform: TransformCompat,
        newContinuousTransformType: Int?,
        animationSpec: BaseZoomAnimationSpec?,
        caller: String
    ) {
        val finalAnimationSpec = animationSpec ?: this.animationSpec
        val startUserTransform = userTransform
        if (newContinuousTransformType != null) {
            setContinuousTransformType(newContinuousTransformType)
        }
        try {
            logger.d {
                "$module. $caller. animated started. transform=${transform.toShortString()}, userTransform=${userTransform.toShortString()}"
            }
            animationAdapter.startAnimation(
                animationSpec = finalAnimationSpec,
                onProgress = { value ->
                    val userTransform = lerp(
                        start = startUserTransform,
                        stop = targetUserTransform,
                        fraction = value
                    )
                    logger.v {
                        "$module. $caller. animated running. fraction=$value, userTransform=${userTransform.toShortString()}"
                    }
                    updateUserTransform(userTransform)
                },
                onEnd = {
                }
            )
            logger.d {
                "$module. $caller. animated end. transform=${transform.toShortString()}, userTransform=${userTransform.toShortString()}"
            }
        } finally {
            if (newContinuousTransformType != null) {
                setContinuousTransformType(ContinuousTransformType.NONE)
            }
        }
    }

    private fun updateUserTransform(targetUserTransform: TransformCompat) {
        this.userTransform = targetUserTransform
        updateTransform()
    }

    private fun updateTransform() {
        val userTransform = userTransform
        transform = baseTransform + userTransform

        contentDisplayRect = calculateContentDisplayRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        )
        contentVisibleRect = calculateContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        )

        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = userTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(rtlLayoutDirection),
        )
        this.userOffsetBoundsRect = userOffsetBounds

        scrollEdge = calculateScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset,
        )

        sourceScaleFactor = calculateSourceScaleFactor(
            contentSize = contentSize,
            contentOriginSize = contentOriginSize.takeIf { it.isNotEmpty() } ?: contentSize,
            scale = transform.scale,
        )

        sourceVisibleRect = calculateSourceVisibleRect(
            contentSize = contentSize,
            contentOriginSize = contentOriginSize.takeIf { it.isNotEmpty() } ?: contentSize,
            contentVisibleRect = contentVisibleRect,
        )

        onTransformChanged(this@ZoomableCore)
    }

    private fun calculateContainerWhitespace(): ContainerWhitespace {
        val containerWhitespace = containerWhitespace
        val containerSize = containerSize
        val containerWhitespaceMultiple = containerWhitespaceMultiple
        return if (!containerWhitespace.isEmpty()) {
            containerWhitespace
        } else if (containerSize.isNotEmpty() && containerWhitespaceMultiple > 0f) {
            ContainerWhitespace(
                horizontal = containerSize.width * containerWhitespaceMultiple,
                vertical = containerSize.height * containerWhitespaceMultiple
            )
        } else {
            ContainerWhitespace.Zero
        }
    }
}