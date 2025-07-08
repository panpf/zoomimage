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
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.format
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
import kotlinx.coroutines.coroutineScope

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
    var scrollEdge: ScrollEdge = ScrollEdge.Default
        private set
    var userOffsetBoundsRect: RectCompat = RectCompat.Zero
        private set

    @ContinuousTransformType
    var continuousTransformType: Int = ContinuousTransformType.NONE
        private set

    private var resetParams: ResetParams? = null


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
            currentUserScale = currentUserTransform.scaleX,
            currentUserOffset = currentUserTransform.offset,
            targetUserScale = limitedTargetUserScale,
            centroid = touchPoint,
        )
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            userScale = limitedTargetUserScale,
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
            userScale = currentUserScale,
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
            userScale = limitedTargetUserScale,
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

    fun canScroll(horizontal: Boolean, direction: Int): Boolean {
        return canScrollByEdge(scrollEdge, horizontal, direction)
    }


    suspend fun setContainerSize(containerSize: IntSizeCompat) {
        if (this.containerSize != containerSize) {
            this.containerSize = containerSize
            reset("containerSizeChanged")
        }
    }

    suspend fun setContentSize(contentSize: IntSizeCompat) {
        if (this.contentSize != contentSize) {
            this.contentSize = contentSize
            reset("contentSizeChanged")
        }
    }

    suspend fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
        if (this.contentOriginSize != contentOriginSize) {
            this.contentOriginSize = contentOriginSize
            reset("contentOriginSizeChanged")
        }
    }

    suspend fun setContentScale(contentScale: ContentScaleCompat) {
        if (this.contentScale != contentScale) {
            this.contentScale = contentScale
            reset("contentScaleChanged")
        }
    }

    suspend fun setAlignment(alignment: AlignmentCompat) {
        if (this.alignment != alignment) {
            this.alignment = alignment
            reset("alignmentChanged")
        }
    }

    suspend fun setRtlLayoutDirection(rtlLayoutDirection: Boolean) {
        if (this.rtlLayoutDirection != rtlLayoutDirection) {
            this.rtlLayoutDirection = rtlLayoutDirection
            reset("rtlLayoutDirectionChanged")
        }
    }

    suspend fun setReadMode(readMode: ReadMode?) {
        if (this.readMode != readMode) {
            this.readMode = readMode
            reset("readModeChanged")
        }
    }

    suspend fun setScalesCalculator(scalesCalculator: ScalesCalculator) {
        if (this.scalesCalculator != scalesCalculator) {
            this.scalesCalculator = scalesCalculator
            reset("scalesCalculatorChanged")
        }
    }

    fun setThreeStepScale(threeStepScale: Boolean) {
        this.threeStepScale = threeStepScale
    }

    fun setRubberBandScale(rubberBandScale: Boolean) {
        this.rubberBandScale = rubberBandScale
    }

    fun setOneFingerScaleSpec(oneFingerScaleSpec: OneFingerScaleSpec) {
        this.oneFingerScaleSpec = oneFingerScaleSpec
    }

    fun setAnimationSpec(animationSpec: BaseZoomAnimationSpec) {
        this.animationSpec = animationSpec
    }

    suspend fun setLimitOffsetWithinBaseVisibleRect(limitOffsetWithinBaseVisibleRect: Boolean) {
        if (this.limitOffsetWithinBaseVisibleRect != limitOffsetWithinBaseVisibleRect) {
            this.limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
            reset("limitOffsetWithinBaseVisibleRectChanged")
        }
    }

    suspend fun setContainerWhitespaceMultiple(containerWhitespaceMultiple: Float) {
        if (this.containerWhitespaceMultiple != containerWhitespaceMultiple) {
            this.containerWhitespaceMultiple = containerWhitespaceMultiple
            reset("containerWhitespaceMultipleChanged")
        }
    }

    suspend fun setContainerWhitespace(containerWhitespace: ContainerWhitespace) {
        if (this.containerWhitespace != containerWhitespace) {
            this.containerWhitespace = containerWhitespace
            reset("containerWhitespaceChanged")
        }
    }

    fun setKeepTransformWhenSameAspectRatioContentSizeChanged(keep: Boolean) {
        this.keepTransformWhenSameAspectRatioContentSizeChanged = keep
    }

    suspend fun reset(caller: String, force: Boolean = false) {
        requiredMainThread()

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

        stopAllAnimation("reset:$caller")

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

        val lastUserTransform = userTransform
        val hasUserActions = !transformAboutEquals(
            one = lastUserTransform,
            two = TransformCompat.Origin
        )
        val mode: String
        val newUserTransform = if (
            !force
            && hasUserActions
            && diffResult.isOnlyContainerSizeChanged
            && lastResetParams != null
            && lastResetParams.containerSize.isNotEmpty()
            && newResetParams.containerSize.isNotEmpty()
        ) {
            mode = "RestoreVisibleCenter"
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
                userScale = restoreUserTransform.scaleX,
            )
            restoreUserTransform.copy(offset = limitUserOffset)
        } else if (
            !force
            && hasUserActions
            && keepTransformWhenSameAspectRatioContentSizeChanged
            && diffResult.isOnlyContentSizeOrContentOriginSizeChanged
            && lastResetParams != null
            && lastResetParams.contentSize.isNotEmpty()
            && newResetParams.contentSize.isNotEmpty()
            && shouldRestoreVisibleRect(
                oldContentSize = lastResetParams.contentSize,
                newContentSize = newResetParams.contentSize,
                diffResult = diffResult
            )
        ) {
            mode = "RestoreVisibleRect"
            val restoreTransform =
                calculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged(
                    oldContentSize = lastResetParams.contentSize,
                    newContentSize = newResetParams.contentSize,
                    transform = transform,
                ).copy(rotationOrigin = newBaseTransform.rotationOrigin)
            val restoreUserTransform = restoreTransform - newBaseTransform
            val limitUserOffset = limitUserOffset(
                newUserOffset = restoreUserTransform.offset,
                userScale = restoreUserTransform.scaleX,
            )
            restoreUserTransform.copy(offset = limitUserOffset)
        } else {
            mode = "Reset"
            newInitialZoom.userTransform
        }

        logger.d {
            val transform = newBaseTransform + newUserTransform
            "$module. reset:$caller. $mode. " +
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
                    "diffResult=${diffResult}. " +
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
        resetParams = newResetParams
    }

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

    suspend fun rollbackScale(focus: OffsetCompat? = null): Boolean = coroutineScope {
        val containerSize =
            containerSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false
        contentSize.takeIf { it.isNotEmpty() } ?: return@coroutineScope false

        val currentScale = transform.scaleX
        val minScale = minScale
        val maxScale = maxScale
        val targetScale = when {
            currentScale.format(2) > maxScale.format(2) -> maxScale
            currentScale.format(2) < minScale.format(2) -> minScale
            else -> null
        }
        if (targetScale != null) {
            val startScale = currentScale
            val endScale = targetScale
            val finalFocus = focus ?: containerSize.toSize().center
            val centroidContentPoint = touchPointToContentPoint(finalFocus)
            logger.d {
                "$module. rollbackScale. " +
                        "focus=${focus?.toShortString()}. " +
                        "startScale=${startScale.format(4)}, " +
                        "endScale=${endScale.format(4)}"
            }
            scale(
                targetScale = endScale,
                centroidContentPoint = centroidContentPoint,
                animated = true
            )
        }
        targetScale != null
    }

    suspend fun gestureTransform(
        centroid: OffsetCompat,
        panChange: OffsetCompat,
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
            currentOffset = currentUserOffset,
            targetScale = limitedTargetUserScale,
            centroid = centroid,
            pan = panChange,
            gestureRotate = 0f,
        )
        val limitedTargetUserOffset = limitUserOffset(
            newUserOffset = targetUserOffset,
            userScale = limitedTargetUserScale,
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
            "$module. gestureTransform. " +
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

    suspend fun fling(velocity: OffsetCompat, extras: Map<String, Any>): Boolean = coroutineScope {
        val containerSize = containerSize.takeIf { it.isNotEmpty() }
            ?: return@coroutineScope false
        val contentSize = contentSize.takeIf { it.isNotEmpty() }
            ?: return@coroutineScope false
        val contentScale = contentScale
        val alignment = alignment
        val rtlLayoutDirection = rtlLayoutDirection
        val rotation = rotation
        val currentUserTransform = userTransform
        val limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect

        stopAllAnimation("fling")

        val startUserOffset = currentUserTransform.offset
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = currentUserTransform.scaleX,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(rtlLayoutDirection),
        )

        setContinuousTransformType(ContinuousTransformType.FLING)
        try {
            logger.d {
                "$module. fling. start. " +
                        "startUserOffset=${startUserOffset.toShortString()}, " +
                        "userOffsetBounds=${userOffsetBounds.toShortString()}, " +
                        "velocity=${velocity.toShortString()}"
            }
            animationAdapter.startFlingAnimation(
                startUserOffset = startUserOffset,
                userOffsetBounds = userOffsetBounds,
                velocity = velocity,
                extras = extras,
                onUpdateValue = { newUserOffset ->
                    val currentUserTransform2 = this@ZoomableCore.userTransform
                    val limitedTargetUserOffset = limitUserOffset(
                        newUserOffset = newUserOffset,
                        userScale = currentUserTransform2.scaleX,
                    )
                    val continue1 = limitedTargetUserOffset != currentUserTransform2.offset
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
                        "bounds=${userOffsetBounds.toShortString()}, " +
                        "velocity=${velocity.toShortString()}"
            }
        } finally {
            setContinuousTransformType(ContinuousTransformType.NONE)
        }
        true
    }

    fun setContinuousTransformType(@ContinuousTransformType continuousTransformType: Int) {
        this.continuousTransformType = continuousTransformType
        onTransformChanged(this@ZoomableCore)
    }

    fun checkSupportGestureType(disabledGestureTypes: Int, @GestureType gestureType: Int): Boolean =
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

    private fun limitUserOffset(
        newUserOffset: OffsetCompat,
        userScale: Float,
    ): OffsetCompat {
        val userOffsetBounds = calculateUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = userScale,
            limitBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = calculateContainerWhitespace().rtlFlipped(rtlLayoutDirection),
        ).round().toRect()      // round() makes sense
        return newUserOffset.limitTo(userOffsetBounds)
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