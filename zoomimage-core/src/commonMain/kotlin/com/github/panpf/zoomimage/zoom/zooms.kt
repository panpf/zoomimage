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

package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.aboutEquals
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.filterNegativeZeros
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toRect
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.internal.BaseTransformHelper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin


/* ******************************************* initial ***************************************** */

/**
 * Calculate the position of content after rotate in the container
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateRotatedContentRect
 */
fun calculateRotatedContentRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    rotation: Int,
): RectCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    if (rotation % 180 == 0) {
        return RectCompat(
            left = 0f,
            top = 0f,
            right = containerSize.width.toFloat(),
            bottom = containerSize.height.toFloat(),
        )
    } else {
        val contentCenter = contentSize.toSize().center
        val left = contentCenter.x - contentCenter.y
        val top = contentCenter.y - contentCenter.x
        return RectCompat(
            left = left,
            top = top,
            right = left + contentSize.height,
            bottom = top + contentSize.width,
        )
    }
}

/**
 * Calculate the offset of the content after it is rotated in the container and then moved back to the upper left corner
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateRotatedContentMoveToTopLeftOffset
 */
fun calculateRotatedContentMoveToTopLeftOffset(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    rotation: Int,
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    val rotatedContentRect = calculateRotatedContentRect(
        containerSize = containerSize,
        contentSize = contentSize,
        rotation = rotation,
    )
    return IntOffsetCompat.Zero - rotatedContentRect.topLeft
}

/**
 * Calculate the rotation origin of content, usually the percentage of the center of content relative to container
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateContentRotateOrigin
 */
fun calculateContentRotateOrigin(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat
): TransformOriginCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    val center = contentSize.toSize().center
    return TransformOriginCompat(
        pivotFractionX = center.x / containerSize.width,
        pivotFractionY = center.y / containerSize.height
    )
}

/**
 * Calculate the basic transformation of content. The basic transformation is affected by contentScale, alignment, and rotation.
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateBaseTransform
 */
fun calculateBaseTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): TransformCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return TransformCompat.Origin
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val baseTransformHelper = BaseTransformHelper(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    return baseTransformHelper.transform
}

/**
 * Calculate the transformation based on the reading mode under the basic transformation.
 * The reading mode will make the picture fill the screen and move to the starting position,
 * allowing the user to read the picture content immediately
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateReadModeTransform
 */
fun calculateReadModeTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    readMode: ReadMode?,
): TransformCompat? {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()
        || readMode == null || contentScale == ContentScaleCompat.FillBounds
    ) {
        return null
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val baseTransformHelper = BaseTransformHelper(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    val rotatedContentSize = baseTransformHelper.rotatedContentSize
    if (!readMode.accept(contentSize = rotatedContentSize, containerSize = containerSize)) {
        return null
    }

    val widthScale = containerSize.width / rotatedContentSize.width.toFloat()
    val heightScale = containerSize.height / rotatedContentSize.height.toFloat()
    val fillScale = max(widthScale, heightScale)
    val readModeScale = ScaleFactorCompat(fillScale)

    val baseTransform = baseTransformHelper.transform
    val addScale = fillScale / baseTransform.scaleX
    val alignmentMoveToStartOffset = baseTransformHelper.alignmentOffset.let {
        OffsetCompat(it.x.coerceAtMost(0f), it.y.coerceAtMost(0f))
    }
    val readModeOffset =
        (alignmentMoveToStartOffset + baseTransformHelper.rotateRectifyOffset) * addScale

    val rotationOrigin = calculateContentRotateOrigin(
        containerSize = containerSize,
        contentSize = contentSize
    )
    val readModeTransform = TransformCompat(
        scale = readModeScale,
        offset = readModeOffset,
        rotation = rotation.toFloat(),
        rotationOrigin = rotationOrigin,
    )
    return readModeTransform
}

/**
 * Calculate the minimum, medium, and maximum scale factor
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateScales
 */
fun calculateScales(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    rotation: Int,
    initialScale: Float,
    calculator: ScalesCalculator,
): FloatArray {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return floatArrayOf(1.0f, 1.0f, 1.0f)
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentOriginSize = contentOriginSize.rotate(rotation)
    val baseScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val minScale = baseScaleFactor.scaleX
    val result = calculator.calculate(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentOriginSize = rotatedContentOriginSize,
        contentScale = contentScale,
        minScale = minScale,
        initialScale = initialScale,
    )
    return floatArrayOf(minScale, result.mediumScale, result.maxScale)
}

/**
 * Calculate initial zoom information, including minimum, medium, maximum scale factor, base transformation and user transformation
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest.testCalculateInitialZoom
 */
fun calculateInitialZoom(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    readMode: ReadMode?,
    scalesCalculator: ScalesCalculator,
): InitialZoom {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return InitialZoom.Origin
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val baseTransform = calculateBaseTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    val readModeTransform = calculateReadModeTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
        readMode = readMode,
    )
    val userTransform = if (readModeTransform != null) {
        readModeTransform - baseTransform
    } else {
        TransformCompat.Origin
    }
    val scales = calculateScales(
        containerSize = containerSize,
        contentSize = contentSize,
        contentOriginSize = contentOriginSize,
        contentScale = contentScale,
        rotation = rotation,
        initialScale = readModeTransform?.scaleX ?: baseTransform.scaleX,
        calculator = scalesCalculator,
    )
    return InitialZoom(
        minScale = scales[0],
        mediumScale = scales[1],
        maxScale = scales[2],
        baseTransform = baseTransform,
        userTransform = userTransform
    )
}

data class InitialZoom(
    val minScale: Float,
    val mediumScale: Float,
    val maxScale: Float,
    val baseTransform: TransformCompat,
    val userTransform: TransformCompat,
) {
    companion object {
        val Origin = InitialZoom(
            minScale = 1.0f,
            mediumScale = 1.0f,
            maxScale = 1.0f,
            baseTransform = TransformCompat.Origin,
            userTransform = TransformCompat.Origin,
        )
    }
}

/**
 * Check whether the parameters have changed
 *
 * @return 0: All unchanged; 1: Only containerSize changes; -1: More changes
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest5.testCheckParamsChanges
 */
fun checkParamsChanges(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    readMode: ReadMode?,
    scalesCalculator: ScalesCalculator,
    limitOffsetWithinBaseVisibleRect: Boolean,
    containerWhitespace: ContainerWhitespace,
    lastContainerSize: IntSizeCompat,
    lastContentSize: IntSizeCompat,
    lastContentOriginSize: IntSizeCompat,
    lastContentScale: ContentScaleCompat,
    lastAlignment: AlignmentCompat,
    lastRotation: Int,
    lastReadMode: ReadMode?,
    lastScalesCalculator: ScalesCalculator,
    lastLimitOffsetWithinBaseVisibleRect: Boolean,
    lastContainerWhitespace: ContainerWhitespace,
): Int {
    return if (
        lastContainerSize.isNotEmpty()
        && lastContentSize.isNotEmpty()
        && containerSize.isNotEmpty()
        && contentSize.isNotEmpty()
        && contentSize == lastContentSize
        && lastContentOriginSize == contentOriginSize
        && lastContentScale == contentScale
        && lastAlignment == alignment
        && lastRotation == rotation
        && lastReadMode == readMode
        && lastScalesCalculator == scalesCalculator
        && lastLimitOffsetWithinBaseVisibleRect == limitOffsetWithinBaseVisibleRect
        && lastContainerWhitespace == containerWhitespace
    ) {
        if (lastContainerSize == containerSize) 0 else 1
    } else {
        -1
    }
}

/**
 * Calculates the user transform required to restore the last content-visible hub
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest5.testCalculateRestoreContentVisibleCenterUserTransform
 */
fun calculateRestoreContentVisibleCenterUserTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    newBaseTransform: TransformCompat,
    lastTransform: TransformCompat,
    lastContentVisibleCenter: IntOffsetCompat,
): TransformCompat {
    val contentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedLastContentVisibleCenter =
        lastContentVisibleCenter.rotateInSpace(contentSize, rotation)
    val baseScaledRotatedContentSize = rotatedContentSize.toSize() * newBaseTransform.scale
    val rotatedCenterProportion = ScaleFactorCompat(
        scaleX = rotatedLastContentVisibleCenter.x.toFloat() / rotatedContentSize.width,
        scaleY = rotatedLastContentVisibleCenter.y.toFloat() / rotatedContentSize.height,
    )

    val sizeCompat = baseScaledRotatedContentSize * rotatedCenterProportion
    val contentVisibleCenterOnBaseDisplay =
        contentBaseDisplayRect.topLeft + sizeCompat.let { OffsetCompat(it.width, it.height) }
    // The purpose of the user to expand the window is to see more content, so keep the total zoom factor unchanged, and more content can be displayed
//    val newUserScale = lastUserTransform.scale    // This causes the window to always show the contents of a fixed area and not see more
    val newUserScale = lastTransform.scale / newBaseTransform.scale
    val scaledContentVisibleCenterOnBaseDisplay = contentVisibleCenterOnBaseDisplay * newUserScale
    val containerSizeCenter = containerSize.center
    val newUserOffset = containerSizeCenter - scaledContentVisibleCenterOnBaseDisplay
    return TransformCompat(scale = newUserScale, offset = newUserOffset)
}


/* ******************************************* Rect ***************************************** */

/**
 * Calculate the base display area of content in the container,
 * controlled by basic parameters such as [contentScale], [alignment], [rotation]
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest2.testCalculateContentBaseDisplayRect
 */
fun calculateContentBaseDisplayRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): RectCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val baseTransformHelper = BaseTransformHelper(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    return baseTransformHelper.displayRect
}

/**
 * The base visible area for calculating content,
 * controlled by basic parameters such as [contentScale], [alignment], [rotation]
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest2.testCalculateContentBaseVisibleRect
 */
fun calculateContentBaseVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): RectCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val baseTransformHelper = BaseTransformHelper(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation
    )
    val scaledRotatedContentSize = baseTransformHelper.scaledRotatedContentSize

    val left: Float
    val right: Float
    if (scaledRotatedContentSize.width.roundToInt() <= containerSize.width) {
        left = 0f
        right = scaledRotatedContentSize.width
    } else if (alignment.isStart) {
        left = 0f
        right = containerSize.width.toFloat()
    } else if (alignment.isEnd) {
        left = scaledRotatedContentSize.width - containerSize.width
        right = scaledRotatedContentSize.width
    } else {   // horizontal center
        val horizontalSpace = (scaledRotatedContentSize.width - containerSize.width) / 2f
        left = horizontalSpace
        right = horizontalSpace + containerSize.width
    }

    val top: Float
    val bottom: Float
    if (scaledRotatedContentSize.height.roundToInt() <= containerSize.height) {
        top = 0f
        bottom = scaledRotatedContentSize.height
    } else if (alignment.isTop) {
        top = 0f
        bottom = containerSize.height.toFloat()
    } else if (alignment.isBottom) {
        top = scaledRotatedContentSize.height - containerSize.height
        bottom = scaledRotatedContentSize.height
    } else {   // vertical center
        val verticalSpace = (scaledRotatedContentSize.height - containerSize.height) / 2f
        top = verticalSpace
        bottom = verticalSpace + containerSize.height
    }

    val scaledRotatedContentBaseVisibleRect =
        RectCompat(left = left, top = top, right = right, bottom = bottom)
    val rotatedContentBaseVisibleRect =
        scaledRotatedContentBaseVisibleRect / baseTransformHelper.scaleFactor
    val limitedRotatedContentBaseVisibleRect =
        rotatedContentBaseVisibleRect.limitTo(baseTransformHelper.rotatedContentSize.toSize())
    val contentBaseVisibleRect =
        limitedRotatedContentBaseVisibleRect.reverseRotateInSpace(contentSize.toSize(), rotation)
    val limitedContentBaseVisibleRect = contentBaseVisibleRect.limitTo(contentSize.toSize())
    return limitedContentBaseVisibleRect
}

/**
 * Calculating the display area of content in the container,
 * controlled by basic parameters such as [contentScale], [alignment], [rotation],
 * and user behavior parameters such as [userScale], [userOffset].
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest2.testCalculateContentDisplayRect
 */
fun calculateContentDisplayRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    userOffset: OffsetCompat
): RectCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )

    val scaledRotatedContentSize = rotatedContentSize * rotatedContentScaleFactor
    val scaledRotatedContentAlignmentOffset = alignment.align(
        size = scaledRotatedContentSize,
        space = containerSize,
        ltrLayout = true,
    )

    val baseRect = IntRectCompat(scaledRotatedContentAlignmentOffset, scaledRotatedContentSize)
    val scaledBaseRect = baseRect.toRect() * userScale
    val contentDisplayRect = scaledBaseRect.translate(userOffset)
    return contentDisplayRect
}

/**
 * The visible area in which content is calculated,
 * controlled by basic parameters such as [contentScale], [alignment], [rotation],
 * and user behavior parameters such as [userScale], [userOffset].
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest2.testCalculateContentVisibleRect
 */
fun calculateContentVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    userOffset: OffsetCompat,
): RectCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val topLeft = OffsetCompat(x = userOffset.x * -1, y = userOffset.y * -1)
    val scaledContainerVisibleRect = RectCompat(offset = topLeft, size = containerSize.toSize())
    val containerDisplayRect = scaledContainerVisibleRect / userScale

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    if (!containerDisplayRect.overlaps(rotatedContentBaseDisplayRect)) {
        return RectCompat.Zero
    }

    val impreciseScaledRotatedContentVisibleRect = RectCompat(
        left = (containerDisplayRect.left - rotatedContentBaseDisplayRect.left),
        top = (containerDisplayRect.top - rotatedContentBaseDisplayRect.top),
        right = (containerDisplayRect.right - rotatedContentBaseDisplayRect.left),
        bottom = (containerDisplayRect.bottom - rotatedContentBaseDisplayRect.top)
    )
    val scaledRotatedContentVisibleRect = impreciseScaledRotatedContentVisibleRect
        .limitTo(rotatedContentBaseDisplayRect.size)
    val rotatedContentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val rotatedContentVisibleRect = scaledRotatedContentVisibleRect / rotatedContentScaleFactor
    val contentVisibleRect =
        rotatedContentVisibleRect.reverseRotateInSpace(contentSize.toSize(), rotation)
    val limitedContentVisibleRect = contentVisibleRect.limitTo(contentSize.toSize())
    return limitedContentVisibleRect
}


/* ******************************************* Offset ***************************************** */

/**
 * Calculates the bounds of the user offset at the specified [userScale],
 * and can only be offset within the underlying visible area when the [limitBaseVisibleRect] parameter is true
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCalculateUserOffsetBounds
 */
fun calculateUserOffsetBounds(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    limitBaseVisibleRect: Boolean,
    containerWhitespace: ContainerWhitespace
): RectCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90, rotation=$rotation" }
    require(containerWhitespace.check()) {
        "containerWhitespace must be greater than or equal to 0f, containerWhitespace=$containerWhitespace"
    }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    ).let {
        if (limitBaseVisibleRect) it.limitTo(containerSize.toSize()) else it
    }

    val containerWidth = containerSize.width
    val containerHeight = containerSize.height

    val scaledRotatedContentBaseDisplayRect = (rotatedContentBaseDisplayRect * userScale)
    val scaledRotatedContentBaseWidth = scaledRotatedContentBaseDisplayRect.width
    val scaledRotatedContentBaseHeight = scaledRotatedContentBaseDisplayRect.height

    val horizontalBounds = if (scaledRotatedContentBaseWidth.roundToInt() >= containerWidth) {
        val leftBounds =
            (scaledRotatedContentBaseDisplayRect.right - containerWidth) * -1
        val rightBounds = scaledRotatedContentBaseDisplayRect.left * -1
        val correctLeftBounds = leftBounds.coerceAtMost(rightBounds)
        correctLeftBounds..rightBounds
    } else if (alignment.isStart) {
        0f..0f
    } else if (alignment.isEnd) {
        val leftBounds = (scaledRotatedContentBaseDisplayRect.right - containerWidth) * -1
        leftBounds..leftBounds
    } else {   // horizontal center
        val horizontalSpace = (containerWidth - scaledRotatedContentBaseWidth) / 2f
        val leftBounds = (scaledRotatedContentBaseDisplayRect.left - horizontalSpace) * -1
        leftBounds..leftBounds
    }

    val verticalBounds = if (scaledRotatedContentBaseHeight.roundToInt() >= containerHeight) {
        val topBounds = (scaledRotatedContentBaseDisplayRect.bottom - containerHeight) * -1
        val bottomBounds = scaledRotatedContentBaseDisplayRect.top * -1
        val correctTopBounds = topBounds.coerceAtMost(bottomBounds)
        correctTopBounds..bottomBounds
    } else if (alignment.isTop) {
        0f..0f
    } else if (alignment.isBottom) {
        val topBounds = (scaledRotatedContentBaseDisplayRect.bottom - containerHeight) * -1
        topBounds..topBounds
    } else {
        // vertical center
        val verticalSpace = (containerHeight - scaledRotatedContentBaseHeight) / 2f
        val topBounds = (scaledRotatedContentBaseDisplayRect.top - verticalSpace) * -1
        topBounds..topBounds
    }

    val offsetBounds = RectCompat(
        left = horizontalBounds.start.filterNegativeZeros(),
        top = verticalBounds.start.filterNegativeZeros(),
        right = horizontalBounds.endInclusive.filterNegativeZeros(),
        bottom = verticalBounds.endInclusive.filterNegativeZeros(),
    )

    val horPaddingSpace = (containerWidth - scaledRotatedContentBaseWidth).coerceAtLeast(0f) / 2
    val verPaddingSpace = (containerHeight - scaledRotatedContentBaseHeight).coerceAtLeast(0f) / 2
    val leftWhitespace = (containerWhitespace.right - horPaddingSpace).coerceAtLeast(0f)
    val topWhitespace = (containerWhitespace.bottom - verPaddingSpace).coerceAtLeast(0f)
    val rightWhitespace = (containerWhitespace.left - horPaddingSpace).coerceAtLeast(0f)
    val bottomWhitespace = (containerWhitespace.top - verPaddingSpace).coerceAtLeast(0f)
    val containerWhitespaceOffsetBounds = RectCompat(
        left = (offsetBounds.left - leftWhitespace).filterNegativeZeros(),
        top = (offsetBounds.top - topWhitespace).filterNegativeZeros(),
        right = (offsetBounds.right + rightWhitespace).filterNegativeZeros(),
        bottom = (offsetBounds.bottom + bottomWhitespace).filterNegativeZeros()
    )
    return containerWhitespaceOffsetBounds
}

/**
 * Calculates the user offset required to locate to the specified point of the container at the specified user scaling
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCalculateLocateUserOffset
 */
fun calculateLocateUserOffset(
    containerSize: IntSizeCompat,
    containerPoint: OffsetCompat,
    userScale: Float,
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty()) {
        return OffsetCompat.Zero
    }

    val scaledContainerPoint = containerPoint * userScale
    val containerCenter = containerSize.center.toOffset()
    val toCenterScaledContainerPoint = scaledContainerPoint - containerCenter
    val locateOffset = toCenterScaledContainerPoint * -1f
    return locateOffset.filterNegativeZeros()
}

/**
 * Calculates the user offset required to maintain the centroid when scaling to a specified user scaling factor
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCalculateScaleUserOffset
 */
fun calculateScaleUserOffset(
    currentUserScale: Float,
    currentUserOffset: OffsetCompat,
    targetUserScale: Float,
    centroid: OffsetCompat,
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    return calculateTransformOffset(
        currentScale = currentUserScale,
        currentOffset = currentUserOffset,
        targetScale = targetUserScale,
        centroid = centroid,
        pan = OffsetCompat.Zero,
        gestureRotate = 0f,
    )
}

/**
 * Calculates the offset required to keep the centroid unchanged when the user gesture transforms
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCalculateTransformOffset
 */
fun calculateTransformOffset(
    currentScale: Float,
    currentOffset: OffsetCompat,
    targetScale: Float,
    centroid: OffsetCompat,
    pan: OffsetCompat,
    gestureRotate: Float,
): OffsetCompat {
    /**
     * Rotates the given offset around the origin by the given angle in degrees.
     *
     * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
     * coordinate system.
     *
     * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
     */
    fun OffsetCompat.rotateBy(angle: Float): OffsetCompat {
        val angleInRadians = angle * kotlin.math.PI / 180
        return OffsetCompat(
            x = (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
            y = (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
        )
    }

    // copied https://github.com/androidx/androidx/blob/643b1cfdd7dfbc5ccce1ad951b6999df049678b3/compose/foundation/foundation/samples/src/main/java/androidx/compose/foundation/samples/TransformGestureSamples.kt
    val oldScale = currentScale
    val newScale = targetScale
    val restoreScaleCurrentOffset = currentOffset / currentScale * -1f
    // For natural zooming and rotating, the centroid of the gesture should
    // be the fixed point where zooming and rotating occurs.
    // We compute where the centroid was (in the pre-transformed coordinate
    // space), and then compute where it will be after this delta.
    // We then compute what the new offset should be to keep the centroid
    // visually stationary for rotating and zooming, and also apply the pan.
    val targetRestoreScaleCurrentOffset =
        (restoreScaleCurrentOffset + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale + pan / oldScale)
    val targetOffset = targetRestoreScaleCurrentOffset * newScale * -1f
    return targetOffset.filterNegativeZeros()
}

/**
 * Calculate scroll edge
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCalculateScrollEdge
 */
fun calculateScrollEdge(
    userOffsetBounds: RectCompat,
    userOffset: OffsetCompat,
): ScrollEdge {
    val leftFormatted = userOffsetBounds.left.roundToInt()
    val rightFormatted = userOffsetBounds.right.roundToInt()
    val xFormatted = userOffset.x.roundToInt()
    val horizontal = when {
        leftFormatted == rightFormatted -> Edge.BOTH
        xFormatted <= leftFormatted -> Edge.END
        xFormatted >= rightFormatted -> Edge.START
        else -> Edge.NONE
    }

    val topFormatted = userOffsetBounds.top.roundToInt()
    val bottomFormatted = userOffsetBounds.bottom.roundToInt()
    val yFormatted = userOffset.y.roundToInt()
    val vertical = when {
        topFormatted == bottomFormatted -> Edge.BOTH
        yFormatted <= topFormatted -> Edge.END
        yFormatted >= bottomFormatted -> Edge.START
        else -> Edge.NONE
    }
    return ScrollEdge(horizontal = horizontal, vertical = vertical)
}

/**
 * Whether you can scroll horizontally or vertical in the specified direction
 *
 * @param direction positive means scroll to the right or scroll down, negative means scroll to the left or scroll up
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCanScrollByEdge
 */
fun canScrollByEdge(scrollEdge: ScrollEdge, horizontal: Boolean, direction: Int): Boolean {
    return if (horizontal) {
        if (direction > 0) {
            scrollEdge.horizontal != Edge.END && scrollEdge.horizontal != Edge.BOTH
        } else {
            scrollEdge.horizontal != Edge.START && scrollEdge.horizontal != Edge.BOTH
        }
    } else {
        if (direction > 0) {
            scrollEdge.vertical != Edge.END && scrollEdge.vertical != Edge.BOTH
        } else {
            scrollEdge.vertical != Edge.START && scrollEdge.vertical != Edge.BOTH
        }
    }
}


/* ******************************************* Scale ***************************************** */

/**
 * Limiting the scaling factor by means of a rubber band effect exceeds the maximum scaling limit, which has a damping effect when exceeded
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testLimitScaleWithRubberBand
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testLimitScaleWithRubberBand2
 */
fun limitScaleWithRubberBand(
    currentScale: Float,
    targetScale: Float,
    minScale: Float,
    maxScale: Float,
    rubberBandRatio: Float
): Float = when {
    targetScale > maxScale -> {
        val addScale = targetScale - currentScale
        val rubberBandMaxScale = maxScale * rubberBandRatio
        if (targetScale < rubberBandMaxScale) {
            val overScale = targetScale - maxScale
            val overMaxScale = rubberBandMaxScale - maxScale
            val progress = overScale / overMaxScale
            // Multiplying by 0.5f is to be a little slower
            val limitedAddScale = addScale * (1 - progress) * 0.5f
            currentScale + limitedAddScale
        } else {
            rubberBandMaxScale
        }
    }

    targetScale < minScale -> {
        val addScale = targetScale - currentScale
        val rubberBandMinScale = minScale / rubberBandRatio
        if (targetScale > rubberBandMinScale) {
            val overScale = targetScale - minScale
            val overMinScale = rubberBandMinScale - minScale
            val progress = overScale / overMinScale
            // Multiplying by 0.5f is to be a little slower
            val limitedAddScale = addScale * (1 - progress) * 0.5f
            currentScale + limitedAddScale
        } else {
            rubberBandMinScale
        }
    }

    else -> targetScale
}

/**
 * Calculates the next scale factor for double-click scaling
 *
 * @param delta – the maximum delta between expected and actual for which both numbers are still considered equal.
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest3.testCalculateNextStepScale
 */
fun calculateNextStepScale(
    stepScales: FloatArray,
    currentScale: Float,
    delta: Float = 0.1f
): Float {
    if (stepScales.isEmpty()) return currentScale
    val formattedCurrentScale = currentScale.format(1)
    return stepScales
        .find { it.format(1) > formattedCurrentScale + delta }
        ?: stepScales.first()
}


/* ******************************************* Point ***************************************** */

/**
 * Converts on-screen touch points to points on a container, supports scaling, panning, and ignoring rotation of the container
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest4.testTouchPointToContainerPoint
 */
fun touchPointToContainerPoint(
    containerSize: IntSizeCompat,
    userScale: Float,
    userOffset: OffsetCompat,
    touchPoint: OffsetCompat
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty()) {
        return OffsetCompat.Zero
    }

    val scaledContainerPoint = touchPoint - userOffset
    val containerPoint = scaledContainerPoint / userScale
    return containerPoint
}

/**
 * Converts points on a container to on-screen touch points, supports zooming, panning, and ignoring rotation of the container
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest4.testContainerPointToTouchPoint
 */
fun containerPointToTouchPoint(
    containerSize: IntSizeCompat,
    userScale: Float,
    userOffset: OffsetCompat,
    containerPoint: OffsetCompat
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty()) {
        return OffsetCompat.Zero
    }
    val scaledContainerPoint = containerPoint * userScale
    val touchPoint = scaledContainerPoint + userOffset
    return touchPoint
}

/**
 * Convert points on containers to points on content
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest4.testContainerPointToContentPoint
 */
fun containerPointToContentPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    containerPoint: OffsetCompat
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return OffsetCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    val scaledRotatedContentPointOffset = containerPoint - rotatedContentBaseDisplayRect.topLeft
    val rotatedContentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val rotatedContentPoint = (scaledRotatedContentPointOffset / rotatedContentScaleFactor)
    val limitedRotatedContentPoint = rotatedContentPoint.limitTo(rotatedContentSize.toSize())
    val contentPoint =
        limitedRotatedContentPoint.reverseRotateInSpace(contentSize.toSize(), rotation)
    return contentPoint
}

/**
 * Converts points on content to points on containers
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest4.testContentPointToContainerPoint
 */
fun contentPointToContainerPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    contentPoint: OffsetCompat
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return OffsetCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentPoint = contentPoint.rotateInSpace(contentSize.toSize(), rotation)
    val rotatedContentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledRotatedContentPoint = rotatedContentPoint * rotatedContentScaleFactor
    val rotatedContentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    val containerPoint = scaledRotatedContentPoint + rotatedContentBaseDisplayRect.topLeft
    return containerPoint
}

/**
 * Converts touch points on the screen into points on content, and supports scaling, panning, and rotating containers
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest4.testTouchPointToContentPoint
 */
fun touchPointToContentPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    userOffset: OffsetCompat,
    touchPoint: OffsetCompat,
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return OffsetCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val containerPoint = touchPointToContainerPoint(
        containerSize = containerSize,
        userScale = userScale,
        userOffset = userOffset,
        touchPoint = touchPoint
    )
    return containerPointToContentPoint(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
        containerPoint = containerPoint
    )
}

/**
 * Converts points on content into touch points on the screen, and supports scaling, panning, and rotating containers
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest4.testContentPointToTouchPoint
 */
fun contentPointToTouchPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    userOffset: OffsetCompat,
    contentPoint: OffsetCompat,
): OffsetCompat {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return OffsetCompat.Zero
    }
    require(rotation % 90 == 0) { "rotation must be multiple of 90" }

    val containerPoint = contentPointToContainerPoint(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
        contentPoint = contentPoint
    )
    val touchPoint = containerPointToTouchPoint(
        containerSize = containerSize,
        userScale = userScale,
        userOffset = userOffset,
        containerPoint = containerPoint
    )
    return touchPoint
}

/**
 * Rough comparison TransformCompat
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest5.testTransformAboutEquals
 */
fun transformAboutEquals(one: TransformCompat, two: TransformCompat): Boolean {
    return one.scaleX.aboutEquals(two.scaleX, delta = 0.1f, scale = 2)
            && one.scaleY.aboutEquals(two.scaleY, delta = 0.1f, scale = 2)
            && one.offsetX.aboutEquals(two.offsetX, delta = 1f, scale = 2)
            && one.offsetY.aboutEquals(two.offsetY, delta = 1f, scale = 2)
}