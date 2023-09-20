/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

import com.github.panpf.zoomimage.Edge
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.isEmpty
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
 * @see [com.github.panpf.zoomimage.core.test.zoom.CoreZoomUtilsTest.testCalculateRotatedContentRect]
 */
fun calculateRotatedContentRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    rotation: Int,
): RectCompat {
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
 * @see [com.github.panpf.zoomimage.core.test.zoom.CoreZoomUtilsTest.testCalculateRotatedContentMoveToTopLeftOffset]
 */
fun calculateRotatedContentMoveToTopLeftOffset(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    rotation: Int,
): OffsetCompat {
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
 * @see [com.github.panpf.zoomimage.core.test.zoom.CoreZoomUtilsTest.testCalculateContentRotateOrigin]
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
 * @see [com.github.panpf.zoomimage.core.test.zoom.CoreZoomUtilsTest.testCalculateBaseTransform]
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

    if (readMode == null) return null
    if (contentScale == ContentScaleCompat.FillBounds) return null

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


/* ******************************************* Rect ***************************************** */

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
    // TODO It can be calculated directly based on baseDisplay

    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
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
    val horizontalSpace = (scaledRotatedContentSize.width - containerSize.width) / 2f
    if (scaledRotatedContentSize.width.roundToInt() <= containerSize.width) {
        left = 0f
        right = scaledRotatedContentSize.width
    } else if (alignment.isStart) {
        left = 0f
        right = containerSize.width.toFloat()
    } else if (alignment.isHorizontalCenter) {
        left = horizontalSpace
        right = horizontalSpace + containerSize.width
    } else {   // alignment.isEnd
        left = scaledRotatedContentSize.width - containerSize.width
        right = scaledRotatedContentSize.width
    }

    val top: Float
    val bottom: Float
    val verticalSpace = (scaledRotatedContentSize.height - containerSize.height) / 2f
    if (scaledRotatedContentSize.height.roundToInt() <= containerSize.height) {
        top = 0f
        bottom = scaledRotatedContentSize.height
    } else if (alignment.isTop) {
        top = 0f
        bottom = containerSize.height.toFloat()
    } else if (alignment.isVerticalCenter) {
        top = verticalSpace
        bottom = verticalSpace + containerSize.height
    } else {   // alignment.isBottom
        top = scaledRotatedContentSize.height - containerSize.height
        bottom = scaledRotatedContentSize.height
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

fun calculateContentBaseInsideDisplayRect(
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
    return baseTransformHelper.insideDisplayRect
}

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

fun calculateUserOffsetBounds(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    limitBaseVisibleRect: Boolean,
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
    val rotatedContentSize = contentSize.rotate(rotation)
    val scaledContainerSize = containerSize.toSize() * userScale
    val rotatedContentBaseDisplayRect = if (limitBaseVisibleRect) {
        calculateContentBaseInsideDisplayRect(
            containerSize = containerSize,
            contentSize = rotatedContentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = 0,
        )
    } else {
        calculateContentBaseDisplayRect(
            containerSize = containerSize,
            contentSize = rotatedContentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = 0,
        )
    }
    val scaledRotatedContentBaseDisplayRect = rotatedContentBaseDisplayRect * userScale

    val horizontalBounds =
        if (scaledRotatedContentBaseDisplayRect.width.roundToInt() >= containerSize.width) {
            ((scaledRotatedContentBaseDisplayRect.right - containerSize.width) * -1)..
                    (scaledRotatedContentBaseDisplayRect.left * -1)
        } else if (alignment.isStart) {
            0f..0f
        } else if (alignment.isHorizontalCenter) {
            val horizontalSpace = (scaledContainerSize.width - containerSize.width) / 2f * -1
            horizontalSpace..horizontalSpace
        } else {   // alignment.isEnd
            val horizontalSpace = (scaledContainerSize.width - containerSize.width) * -1
            horizontalSpace..horizontalSpace
        }

    val verticalBounds =
        if (scaledRotatedContentBaseDisplayRect.height.roundToInt() >= containerSize.height) {
            ((scaledRotatedContentBaseDisplayRect.bottom - containerSize.height) * -1)..
                    (scaledRotatedContentBaseDisplayRect.top * -1)
        } else if (alignment.isTop) {
            0f..0f
        } else if (alignment.isVerticalCenter) {
            val verticalSpace = (scaledContainerSize.height - containerSize.height) / 2f * -1
            verticalSpace..verticalSpace
        } else {   // alignment.isBottom
            val verticalSpace = (scaledContainerSize.height - containerSize.height) * -1
            verticalSpace..verticalSpace
        }

    val offsetBounds = RectCompat(
        left = horizontalBounds.start,
        top = verticalBounds.start,
        right = horizontalBounds.endInclusive,
        bottom = verticalBounds.endInclusive
    )
    return offsetBounds
}

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
    return locateOffset
}

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
    return targetOffset
}

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

fun limitScaleWithRubberBand(
    currentScale: Float,
    targetScale: Float,
    minScale: Float,
    maxScale: Float,
    rubberBandRatio: Float = 2f
): Float = when {
    targetScale > maxScale -> {
        val addScale = targetScale - currentScale
        val rubberBandMaxScale = maxScale * rubberBandRatio
        val overScale = targetScale - maxScale
        val overMaxScale = rubberBandMaxScale - maxScale
        val progress = overScale / overMaxScale
        // Multiplying by 0.5f is to be a little slower
        val limitedAddScale = addScale * (1 - progress) * 0.5f
        currentScale + limitedAddScale
    }

    targetScale < minScale -> {
        val addScale = targetScale - currentScale
        val rubberBandMinScale = minScale / rubberBandRatio
        val overScale = targetScale - minScale
        val overMinScale = rubberBandMinScale - minScale
        val progress = overScale / overMinScale
        // Multiplying by 0.5f is to be a little slower
        val limitedAddScale = addScale * (1 - progress) * 0.5f
        currentScale + limitedAddScale
    }

    else -> targetScale
}

fun calculateNextStepScale(
    stepScales: FloatArray,
    currentScale: Float,
    rangeOfError: Float = 0.1f
): Float {
    if (stepScales.isEmpty()) return currentScale
    val formattedCurrentScale = currentScale.format(1)
    return stepScales
        .find { it.format(1) > formattedCurrentScale + rangeOfError }
        ?: stepScales.first()
}


/* ******************************************* Point ***************************************** */

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

fun containerPointToTouchPoint(
    containerSize: IntSizeCompat,
    userScale: Float,
    userOffset: OffsetCompat,
    containerPoint: OffsetCompat
): OffsetCompat {
    if (containerSize.isEmpty()) {
        return OffsetCompat.Zero
    }

    val scaledContainerPoint = containerPoint * userScale
    val touchPoint = scaledContainerPoint + userOffset
    return touchPoint
}

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