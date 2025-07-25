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

package com.github.panpf.zoomimage.zoom.internal

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
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toRect
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.Edge
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import com.github.panpf.zoomimage.zoom.check
import com.github.panpf.zoomimage.zoom.isBottom
import com.github.panpf.zoomimage.zoom.isEnd
import com.github.panpf.zoomimage.zoom.isStart
import com.github.panpf.zoomimage.zoom.isTop
import com.github.panpf.zoomimage.zoom.rtlFlipped
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin


/* ******************************************* initial ***************************************** */

/**
 * Calculate the position of content after rotate in the container
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateRotatedContentRect
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateRotatedContentMoveToTopLeftOffset
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateContentRotateOrigin
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateBaseTransform
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateBaseTransformWithRTL
 */
fun calculateBaseTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        ltrLayout = !rtlLayoutDirection,
        rotation = rotation,
    )
    return baseTransformHelper.transform
}

/**
 * Calculate the transformation required to restore the content to its original position
 */
fun calculateRestoreContentBaseTransformTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
): TransformCompat {
    val baseTransform = calculateBaseTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = 0
    )
    val scaleX = 1f / baseTransform.scaleX
    val scaleY = 1f / baseTransform.scaleY
    val scaledBaseOffsetX = baseTransform.offsetX * scaleX
    val scaledBaseOffsetY = baseTransform.offsetY * scaleY
    val translationX = 0f - scaledBaseOffsetX
    val translationY = 0f - scaledBaseOffsetY
    val transformOrigin = TransformOriginCompat(0f, 0f)
    return TransformCompat(
        scale = ScaleFactorCompat(scaleX, scaleY),
        offset = OffsetCompat(translationX, translationY),
        rotation = 0f,
        rotationOrigin = transformOrigin
    )
}

/**
 * Calculate the transformation based on the reading mode under the basic transformation.
 * The reading mode will make the picture fill the screen and move to the starting position,
 * allowing the user to read the picture content immediately
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateReadModeTransform
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateReadModeTransformWithRTL
 */
fun calculateReadModeTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        ltrLayout = !rtlLayoutDirection,
        rotation = rotation,
    )
    val rotatedContentSize = baseTransformHelper.rotatedContentSize
    if (!readMode.accept(contentSize = rotatedContentSize, containerSize = containerSize)) {
        return null
    }

    val widthScale = containerSize.width / rotatedContentSize.width.toFloat()
    val heightScale = containerSize.height / rotatedContentSize.height.toFloat()
    val fillScale = max(widthScale, heightScale)

    val moveToLeftOffset =
        baseTransformHelper.rotateRectifyOffset / baseTransformHelper.scaleFactor.scaleX * fillScale
    val readModeOffset = if (rtlLayoutDirection) {
        val scaledRotatedContentSize = rotatedContentSize * fillScale
        if (scaledRotatedContentSize.width > containerSize.width) {
            // If the content is wider than the container, align it to the end
            val x = containerSize.width.toFloat() - scaledRotatedContentSize.width
            moveToLeftOffset + OffsetCompat(x, 0f)
        } else {
            moveToLeftOffset
        }
    } else {
        moveToLeftOffset
    }
    val rotationOrigin = calculateContentRotateOrigin(
        containerSize = containerSize,
        contentSize = contentSize
    )
    val readModeTransform = TransformCompat(
        scale = ScaleFactorCompat(fillScale),
        offset = readModeOffset,
        rotation = rotation.toFloat(),
        rotationOrigin = rotationOrigin,
    )
    return readModeTransform
}

/**
 * Calculate the minimum, medium, and maximum scale factor
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateScales
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
    val baseScale = baseScaleFactor.scaleX
    val result = calculator.calculateWithBase(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentOriginSize = rotatedContentOriginSize,
        contentScale = contentScale,
        baseScale = baseScale,
        initialScale = initialScale,
    )
    return floatArrayOf(result.minScale, result.mediumScale, result.maxScale)
}

/**
 * Calculate the minimum scale factor
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateMinScale
 */
fun calculateMinScale(baseScale: Float, initialScale: Float): Float =
    if (initialScale > 0f && initialScale < baseScale) {
        initialScale
    } else if (
        initialScale > 0f && initialScale > baseScale
        && initialScale / baseScale < 1.5f && abs(initialScale - baseScale) < 1.5f
    ) {
        // If the difference is too small, use the initial proportion as the minimum proportion directly
        initialScale
    } else {
        baseScale
    }


/**
 * Calculate initial zoom information, including minimum, medium, maximum scale factor, base transformation and user transformation
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateInitialZoom
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateInitialZoomWithLong
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest.testCalculateInitialZoomWithRTL
 */
fun calculateInitialZoom(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = rotation,
    )
    val readModeTransform = calculateReadModeTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = rtlLayoutDirection,
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

/**
 * Calculates the user transform required to restore the last content-visible hub
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testCalculateRestoreContentVisibleCenterUserTransform
 */
@Deprecated("Please use calculateRestoreContentVisibleCenterUserTransform instead")
fun calculateRestoreContentVisibleCenterUserTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    newBaseTransform: TransformCompat,
    lastTransform: TransformCompat,
    lastContentVisibleCenter: OffsetCompat,
): TransformCompat {
    val contentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = false,
        rotation = rotation,
    )
    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedLastContentVisibleCenter =
        lastContentVisibleCenter.rotateInSpace(contentSize.toSize(), rotation)
    val baseScaledRotatedContentSize = rotatedContentSize.toSize() * newBaseTransform.scale
    val rotatedCenterProportion = ScaleFactorCompat(
        scaleX = rotatedLastContentVisibleCenter.x / rotatedContentSize.width,
        scaleY = rotatedLastContentVisibleCenter.y / rotatedContentSize.height,
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

/**
 * Calculates the user transform required to restore the last content-visible hub
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testCalculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testCalculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChangedWithRTL
 */
fun calculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged(
    oldContainerSize: IntSizeCompat,
    newContainerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
    rotation: Int,
    transform: TransformCompat,
): TransformCompat {
    val oldBaseTransform = calculateBaseTransform(
        containerSize = oldContainerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = rotation,
    )
    val oldUserTransform = transform - oldBaseTransform
    val contentVisibleRect = calculateContentVisibleRect(
        containerSize = oldContainerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = rotation,
        userScale = oldUserTransform.scaleX,
        userOffset = oldUserTransform.offset,
    )
    val contentVisibleCenter = contentVisibleRect.center
    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedLastContentVisibleCenter =
        contentVisibleCenter.rotateInSpace(contentSize.toSize(), rotation)
    val rotatedCenterProportion = ScaleFactorCompat(
        scaleX = rotatedLastContentVisibleCenter.x / rotatedContentSize.width,
        scaleY = rotatedLastContentVisibleCenter.y / rotatedContentSize.height,
    )

    val newBaseTransform = calculateBaseTransform(
        containerSize = newContainerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = rotation,
    )
    val newContentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = newContainerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = rotation,
    )
    val newBaseScaledRotatedContentSize = rotatedContentSize.toSize() * newBaseTransform.scale
    val sizeCompat = newBaseScaledRotatedContentSize * rotatedCenterProportion
    val contentVisibleCenterOnBaseDisplay =
        newContentBaseDisplayRect.topLeft + sizeCompat.let { OffsetCompat(it.width, it.height) }
    // The purpose of the user to expand the window is to see more content, so keep the total zoom factor unchanged, and more content can be displayed
//    val newUserScale = oldUserTransform.scale    // This causes the window to always show the contents of a fixed area and not see more
    val newUserScale = transform.scale / newBaseTransform.scale
    val scaledContentVisibleCenterOnBaseDisplay = contentVisibleCenterOnBaseDisplay * newUserScale
    val containerSizeCenter = newContainerSize.center
    val newUserOffset = containerSizeCenter - scaledContentVisibleCenterOnBaseDisplay
    val newUserTransform = TransformCompat(scale = newUserScale, offset = newUserOffset)
    val newTransform = newBaseTransform + newUserTransform
    return newTransform
}

/**
 * Calculate the restore of content transformation when only content size changes
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testCalculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged
 */
fun calculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged(
    oldContentSize: IntSizeCompat,
    newContentSize: IntSizeCompat,
    transform: TransformCompat,
): TransformCompat {
    val scaleFactor = if (oldContentSize.width > oldContentSize.height) {
        (oldContentSize.width * transform.scaleX) / newContentSize.width
    } else {
        (oldContentSize.height * transform.scaleY) / newContentSize.height
    }
    return transform.copy(scale = ScaleFactorCompat(scaleFactor), offset = transform.offset)
}


/* ******************************************* Rect ***************************************** */

/**
 * Calculate the base display area of content in the container,
 * controlled by basic parameters such as [contentScale], [alignment], [rotation]
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentBaseDisplayRect
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentBaseDisplayRectWithRTL
 */
fun calculateContentBaseDisplayRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        ltrLayout = !rtlLayoutDirection,
        rotation = rotation,
    )
    return baseTransformHelper.displayRect
}

/**
 * The base visible area for calculating content,
 * controlled by basic parameters such as [contentScale], [alignment], [rotation]
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentBaseVisibleRect
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentBaseVisibleRectWithVER
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentBaseVisibleRectWithRTL
 */
fun calculateContentBaseVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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

    val flippedAlignment = alignment.rtlFlipped(rtlLayoutDirection)
    val baseTransformHelper = BaseTransformHelper(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = flippedAlignment,
        ltrLayout = true,
        rotation = rotation
    )
    val scaledRotatedContentSize = baseTransformHelper.scaledRotatedContentSize

    val left: Float
    val right: Float
    if (scaledRotatedContentSize.width.roundToInt() <= containerSize.width) {
        left = 0f
        right = scaledRotatedContentSize.width
    } else if (flippedAlignment.isStart) {
        left = 0f
        right = containerSize.width.toFloat()
    } else if (flippedAlignment.isEnd) {
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
    } else if (flippedAlignment.isTop) {
        top = 0f
        bottom = containerSize.height.toFloat()
    } else if (flippedAlignment.isBottom) {
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentDisplayRect
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentDisplayRectWithRTL
 */
fun calculateContentDisplayRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        ltrLayout = !rtlLayoutDirection,
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentVisibleRect
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest2.testCalculateContentVisibleRectWithRTL
 */
fun calculateContentVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        rtlLayoutDirection = rtlLayoutDirection,
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateUserOffsetBounds
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateUserOffsetBoundsWithHOR
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateUserOffsetBoundsWithRTL
 */
fun calculateUserOffsetBounds(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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

    val flippedAlignment = alignment.rtlFlipped(rtlLayoutDirection)
    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentBaseDisplayRect = calculateContentBaseDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = flippedAlignment,
        rtlLayoutDirection = false,
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
    } else if (flippedAlignment.isStart) {
        0f..0f
    } else if (flippedAlignment.isEnd) {
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
    } else if (flippedAlignment.isTop) {
        0f..0f
    } else if (flippedAlignment.isBottom) {
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateLocateUserOffset
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateScaleUserOffset
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateTransformOffset
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateScrollEdge
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCanScrollByEdge
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testLimitScaleWithRubberBand
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testLimitScaleWithRubberBand2
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
            val progress = overScale / overMaxScale   // TODO progress adds interpolation
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
            val progress = overScale / overMinScale   // TODO progress adds interpolation
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest3.testCalculateNextStepScale
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testTouchPointToContainerPoint
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContainerPointToTouchPoint
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContainerPointToContentPoint
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContainerPointToContentPointWithRTL
 */
fun containerPointToContentPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        rtlLayoutDirection = rtlLayoutDirection,
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContentPointToContainerPoint
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContentPointToContainerPointWithRTL
 */
fun contentPointToContainerPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = 0,
    )
    val containerPoint = scaledRotatedContentPoint + rotatedContentBaseDisplayRect.topLeft
    return containerPoint
}

/**
 * Converts touch points on the screen into points on content, and supports scaling, panning, and rotating containers
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testTouchPointToContentPoint
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testTouchPointToContentPointWithRTL
 */
fun touchPointToContentPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        rtlLayoutDirection = rtlLayoutDirection,
        rotation = rotation,
        containerPoint = containerPoint
    )
}

/**
 * Converts points on content into touch points on the screen, and supports scaling, panning, and rotating containers
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContentPointToTouchPoint
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest4.testContentPointToTouchPointWithRTL
 */
fun contentPointToTouchPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rtlLayoutDirection: Boolean,
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
        rtlLayoutDirection = rtlLayoutDirection,
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
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testTransformAboutEquals
 */
fun transformAboutEquals(one: TransformCompat, two: TransformCompat): Boolean {
    return one.scaleX.aboutEquals(two.scaleX, delta = 0.1f, scale = 2)
            && one.scaleY.aboutEquals(two.scaleY, delta = 0.1f, scale = 2)
            && one.offsetX.aboutEquals(two.offsetX, delta = 1f, scale = 2)
            && one.offsetY.aboutEquals(two.offsetY, delta = 1f, scale = 2)
}

/**
 * Calculate elastic effects based on the longest distance
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testRubberBandWithDistance
 */
fun rubberBandWithDistance(
    currentValue: Float,
    newValue: Float,
    minValue: Float,
    maxValue: Float,
    maxDistance: Float,
): Float {
    if (maxDistance == 0f) {
        return newValue.coerceIn(minValue, maxValue)
    }
    if (newValue < minValue) {
        val add = newValue - currentValue
        val overflow = minValue - newValue
        val progress = overflow / maxDistance   // TODO progress adds interpolation
        return currentValue + add * (1f - progress)
    }
    if (newValue > maxValue) {
        val add = newValue - currentValue
        val overflow = newValue - maxValue
        val progress = overflow / maxDistance   // TODO progress adds interpolation
        return currentValue + add * (1f - progress)
    }
    return newValue
}

/**
 * Limits the user offset within the specified bounds, with an elastic effect when exceeding the bounds.
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.ZoomsTest5.testLimitOffsetWithRubberBand
 */
fun limitOffsetWithRubberBand(
    currentUserOffset: OffsetCompat,
    newUserOffset: OffsetCompat,
    userOffsetBoundsRect: RectCompat,
    maxDistance: OffsetCompat,
    alwaysCanDragAtEdge: Boolean,
): OffsetCompat {
    return if (
        alwaysCanDragAtEdge
        && userOffsetBoundsRect.left == userOffsetBoundsRect.right
        && userOffsetBoundsRect.top == userOffsetBoundsRect.bottom
    ) {
        OffsetCompat(
            x = rubberBandWithDistance(
                currentValue = currentUserOffset.x,
                newValue = newUserOffset.x,
                minValue = userOffsetBoundsRect.left,
                maxValue = userOffsetBoundsRect.right,
                maxDistance = maxDistance.x
            ),
            y = rubberBandWithDistance(
                currentValue = currentUserOffset.y,
                newValue = newUserOffset.y,
                minValue = userOffsetBoundsRect.top,
                maxValue = userOffsetBoundsRect.bottom,
                maxDistance = maxDistance.y
            ),
        )
    } else {
        OffsetCompat(
            x = if (userOffsetBoundsRect.left != userOffsetBoundsRect.right) {
                rubberBandWithDistance(
                    currentValue = currentUserOffset.x,
                    newValue = newUserOffset.x,
                    minValue = userOffsetBoundsRect.left,
                    maxValue = userOffsetBoundsRect.right,
                    maxDistance = maxDistance.x
                )
            } else {
                newUserOffset.x.coerceIn(userOffsetBoundsRect.left, userOffsetBoundsRect.right)
            },
            y = if (userOffsetBoundsRect.top != userOffsetBoundsRect.bottom) {
                rubberBandWithDistance(
                    currentValue = currentUserOffset.y,
                    newValue = newUserOffset.y,
                    minValue = userOffsetBoundsRect.top,
                    maxValue = userOffsetBoundsRect.bottom,
                    maxDistance = maxDistance.y
                )
            } else {
                newUserOffset.y.coerceIn(userOffsetBoundsRect.top, userOffsetBoundsRect.bottom)
            },
        )
    }
}