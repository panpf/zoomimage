@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.util

import com.github.panpf.zoomimage.Edge
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.util.internal.BaseTransformHelper
import com.github.panpf.zoomimage.util.internal.format
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin


const val DefaultMediumScaleMinMultiple: Float = 3f


/* ******************************************* initial ***************************************** */

fun computeBaseTransform(
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

fun computeInitialUserTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    readMode: ReadMode?,
    baseTransform: TransformCompat,
): TransformCompat? {
    if (readMode == null) return null
    if (contentScale == ContentScaleCompat.FillBounds) return null
    val rotatedContentSize = contentSize.rotate(rotation)
    val accept = readMode.accept(
        srcSize = rotatedContentSize,
        dstSize = containerSize
    )
    if (!accept) return null
    val widthScale = containerSize.width / rotatedContentSize.width.toFloat()
    val heightScale = containerSize.height / rotatedContentSize.height.toFloat()
    val fillScale = max(widthScale, heightScale)
    val baseTransformHelper = BaseTransformHelper(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    val baseScaleFactor = baseTransformHelper.scaleFactor
    val alignmentOffset = baseTransformHelper.alignmentOffset
    val addScale = fillScale / baseScaleFactor.scaleX
    val scaleX = baseScaleFactor.scaleX * addScale
    val scaleY = baseScaleFactor.scaleY * addScale
    val translateX = if (alignmentOffset.x < 0f)
        alignmentOffset.x * addScale else 0f
    val translateY = if (alignmentOffset.y < 0f)
        alignmentOffset.y * addScale else 0f
// todo 这里有错误，需要修复
    val readModeTransform = TransformCompat(
        scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
        offset = OffsetCompat(x = translateX, y = translateY)
    )
    return readModeTransform.split(baseTransform)
}

fun computeStepScales(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    rotation: Int,
    mediumScaleMinMultiple: Float,
): FloatArray {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return floatArrayOf(1.0f, 1.0f, 1.0f)
    }

    val rotatedContentSize = contentSize.rotate(rotation)
    val baseScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )

    val minScale = baseScaleFactor.scaleX
    val minMediumScale = minScale * mediumScaleMinMultiple
    val mediumScale = if (contentScale != ContentScaleCompat.FillBounds) {
        // The width and height of content fill the container at the same time
        val fillContainerScale = max(
            containerSize.width / rotatedContentSize.width.toFloat(),
            containerSize.height / rotatedContentSize.height.toFloat()
        )
        // Enlarge content to the same size as its original
        val contentOriginScale = if (contentOriginSize.isNotEmpty) {
            val rotatedContentOriginSize = contentOriginSize.rotate(rotation)
            val widthScale = rotatedContentOriginSize.width / rotatedContentSize.width.toFloat()
            val heightScale = rotatedContentOriginSize.height / rotatedContentSize.height.toFloat()
            max(widthScale, heightScale)
        } else {
            1.0f
        }
        floatArrayOf(minMediumScale, fillContainerScale, contentOriginScale).maxOrNull()!!
    } else {
        minMediumScale
    }
    val maxScale = mediumScale * 2f
    return floatArrayOf(minScale, mediumScale, maxScale)
}

fun computeInitialZoom(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    contentAlignment: AlignmentCompat,
    rotation: Int,
    readMode: ReadMode?,
    mediumScaleMinMultiple: Float,
): InitialZoom {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return InitialZoom.Origin
    }
    val stepScales = computeStepScales(
        containerSize = containerSize,
        contentSize = contentSize,
        contentOriginSize = contentOriginSize,
        contentScale = contentScale,
        rotation = rotation,
        mediumScaleMinMultiple = mediumScaleMinMultiple,
    )
    val baseTransform = computeBaseTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = rotation,
    )
    val userTransform = computeInitialUserTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = rotation,
        readMode = readMode,
        baseTransform = baseTransform,
    )
    return InitialZoom(
        minScale = stepScales[0],
        mediumScale = stepScales[1],
        maxScale = stepScales[2],
        baseTransform = baseTransform,
        userTransform = userTransform ?: TransformCompat.Origin
    )
}


/* ******************************************* Rect ***************************************** */

fun computeContainerVisibleRect(
    containerSize: IntSizeCompat,
    userScale: Float,
    userOffset: OffsetCompat
): RectCompat {
    if (containerSize.isEmpty()) {
        return RectCompat.Zero
    }
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val topLeft = OffsetCompat(x = userOffset.x * -1, y = userOffset.y * -1)
    val scaledContainerVisibleRect = RectCompat(offset = topLeft, size = containerSize.toSize())
    val boundsRect = RectCompat(offset = OffsetCompat(0f, 0f), size = scaledContainerSize)
    val limitedScaledContainerVisibleRect = scaledContainerVisibleRect.limitTo(boundsRect)
    val filteredEmptyRect =
        limitedScaledContainerVisibleRect.takeIf { !it.isEmpty } ?: RectCompat.Zero
    val containerVisibleRect = filteredEmptyRect.restoreScale(userScale)
    val limitedContainerVisibleRect = containerVisibleRect.limitTo(containerSize.toSize())
    return limitedContainerVisibleRect
}

fun computeContentBaseDisplayRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): RectCompat {
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

fun computeContentBaseVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): RectCompat {
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
    val scaledContentSize = baseTransformHelper.scaledRotatedContentSize

    val left: Float
    val right: Float
    val horizontalSpace = (scaledContentSize.width - containerSize.width) / 2f
    if (scaledContentSize.width.roundToInt() <= containerSize.width) {
        left = 0f
        right = scaledContentSize.width
    } else if (alignment.isStart) {
        left = 0f
        right = containerSize.width.toFloat()
    } else if (alignment.isHorizontalCenter) {
        left = horizontalSpace
        right = horizontalSpace + containerSize.width
    } else {   // contentAlignment.isEnd
        left = scaledContentSize.width - containerSize.width
        right = scaledContentSize.width
    }

    val top: Float
    val bottom: Float
    val verticalSpace = (scaledContentSize.height - containerSize.height) / 2f
    if (scaledContentSize.height.roundToInt() <= containerSize.height) {
        top = 0f
        bottom = scaledContentSize.height
    } else if (alignment.isTop) {
        top = 0f
        bottom = containerSize.height.toFloat()
    } else if (alignment.isVerticalCenter) {
        top = verticalSpace
        bottom = verticalSpace + containerSize.height
    } else {   // contentAlignment.isBottom
        top = scaledContentSize.height - containerSize.height
        bottom = scaledContentSize.height
    }

    val scaledContentInContainerVisibleRect =
        RectCompat(left = left, top = top, right = right, bottom = bottom)
    val contentInContainerVisibleRect =
        scaledContentInContainerVisibleRect.restoreScale(baseTransformHelper.scaleFactor)
            .limitTo(baseTransformHelper.rotatedContentSize.toSize())
    val reverseRotateContentInContainerVisibleRect =
        contentInContainerVisibleRect.reverseRotateInSpace(contentSize.toSize(), rotation)
            .limitTo(contentSize.toSize())
    val limitedContentBaseVisibleRect =
        reverseRotateContentInContainerVisibleRect.limitTo(contentSize.toSize())
    return limitedContentBaseVisibleRect
}

fun computeContentBaseInsideDisplayRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): RectCompat {
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

fun computeContentDisplayRect(
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

    val scaledRotatedContentSize = rotatedContentSize.times(rotatedContentScaleFactor)
    val scaledRotatedContentAlignmentOffset = alignment.align(
        size = scaledRotatedContentSize,
        space = containerSize,
        ltrLayout = true,
    )

    val baseRect = IntRectCompat(scaledRotatedContentAlignmentOffset, scaledRotatedContentSize)
    val scaledBaseRect = baseRect.toRect().scale(userScale)
    val contentDisplayRect = scaledBaseRect.translate(userOffset)
    return contentDisplayRect
}

fun computeContentVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
    userOffset: OffsetCompat,
): RectCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }

    val rotatedContentSize = contentSize.rotate(rotation)
    val containerVisibleRect =
        computeContainerVisibleRect(containerSize, userScale, userOffset)
    val contentBaseInsideDisplayRect = computeContentBaseInsideDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    if (!containerVisibleRect.overlaps(contentBaseInsideDisplayRect)) {
        return RectCompat.Zero
    }

    val contentRect = RectCompat(
        left = (containerVisibleRect.left - contentBaseInsideDisplayRect.left),
        top = (containerVisibleRect.top - contentBaseInsideDisplayRect.top),
        right = (containerVisibleRect.right - contentBaseInsideDisplayRect.left),
        bottom = (containerVisibleRect.bottom - contentBaseInsideDisplayRect.top)
    )
    val visibleBoundsRect = RectCompat(
        left = 0f,
        top = 0f,
        right = contentBaseInsideDisplayRect.width,
        bottom = contentBaseInsideDisplayRect.height
    )
    val limitedVisibleRect = contentRect.limitTo(visibleBoundsRect)
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledLimitedVisibleRect = limitedVisibleRect.restoreScale(contentScaleFactor)
    val contentBaseVisibleRect = computeContentBaseVisibleRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    val contentVisibleRect =
        scaledLimitedVisibleRect.translate(contentBaseVisibleRect.topLeft)
    val reversedRotateContentVisibleRect =
        contentVisibleRect.reverseRotateInSpace(contentSize.toSize(), rotation)
    val limitedContentVisibleRect = reversedRotateContentVisibleRect.limitTo(contentSize.toSize())
    return limitedContentVisibleRect
}


/* ******************************************* Offset ***************************************** */

fun computeUserOffsetBounds(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
): RectCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    val rotatedContentSize = contentSize.rotate(rotation)
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val contentBaseInsideDisplayRect = computeContentBaseInsideDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    val scaledContentInContainerInnerRect = contentBaseInsideDisplayRect.scale(userScale)

    val horizontalBounds =
        if (scaledContentInContainerInnerRect.width.roundToInt() >= containerSize.width) {
            ((scaledContentInContainerInnerRect.right - containerSize.width) * -1)..(scaledContentInContainerInnerRect.left * -1)
        } else if (alignment.isStart) {
            0f..0f
        } else if (alignment.isHorizontalCenter) {
            val horizontalSpace = (scaledContainerSize.width - containerSize.width) / 2f * -1
            horizontalSpace..horizontalSpace
        } else {   // contentAlignment.isEnd
            val horizontalSpace = (scaledContainerSize.width - containerSize.width) * -1
            horizontalSpace..horizontalSpace
        }

    val verticalBounds =
        if (scaledContentInContainerInnerRect.height.roundToInt() >= containerSize.height) {
            ((scaledContentInContainerInnerRect.bottom - containerSize.height) * -1)..(scaledContentInContainerInnerRect.top * -1)
        } else if (alignment.isTop) {
            0f..0f
        } else if (alignment.isVerticalCenter) {
            val verticalSpace = (scaledContainerSize.height - containerSize.height) / 2f * -1
            verticalSpace..verticalSpace
        } else {   // contentAlignment.isBottom
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

fun computeLocationUserOffset(
    containerSize: IntSizeCompat,
    containerPoint: IntOffsetCompat,
    userScale: Float,
): OffsetCompat {
    if (containerSize.isEmpty()) {
        return OffsetCompat.Zero
    }
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val scaledContainerPoint = containerPoint.toOffset().times(userScale)
    val containerCenter = containerSize.center.toOffset()
    val originMoveToCenterOffset = scaledContainerPoint - containerCenter
    val locationOffset = OffsetCompat(
        x = (originMoveToCenterOffset.x * -1).coerceIn(-scaledContainerSize.width, 0f),
        y = (originMoveToCenterOffset.y * -1).coerceIn(-scaledContainerSize.height, 0f)
    )
    return locationOffset
}

fun computeTransformOffset(
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

fun computeScrollEdge(
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
 * @param direction Negative to check scrolling left, positive to check scrolling right.
 */
fun canScroll(horizontal: Boolean, direction: Int, scrollEdge: ScrollEdge): Boolean {
    return if (horizontal) {
        if (direction < 0) {
            scrollEdge.horizontal != Edge.START && scrollEdge.horizontal != Edge.BOTH
        } else {
            scrollEdge.horizontal != Edge.END && scrollEdge.horizontal != Edge.BOTH
        }
    } else {
        if (direction < 0) {
            scrollEdge.vertical != Edge.START && scrollEdge.vertical != Edge.BOTH
        } else {
            scrollEdge.vertical != Edge.END && scrollEdge.vertical != Edge.BOTH
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
): Float {
    return when {
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
): IntOffsetCompat {
    if (containerSize.isEmpty()) {
        return IntOffsetCompat.Zero
    }
    val scaledContainerPoint = touchPoint - userOffset
    val containerPoint = IntOffsetCompat(
        x = (scaledContainerPoint.x / userScale).roundToInt(),
        y = (scaledContainerPoint.y / userScale).roundToInt(),
    )
    val limitedContainerPoint = IntOffsetCompat(
        x = containerPoint.x.coerceIn(0, containerSize.width),
        y = containerPoint.y.coerceIn(0, containerSize.height),
    )
    return limitedContainerPoint
}

fun containerPointToContentPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    contentAlignment: AlignmentCompat,
    rotation: Int,
    containerPoint: IntOffsetCompat
): IntOffsetCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntOffsetCompat.Zero
    }
    val rotatedContentSize = contentSize.rotate(rotation)
    val contentBaseInsideDisplayRect = computeContentBaseInsideDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = 0,
    )
    val contentBaseVisibleRect = computeContentBaseVisibleRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = 0,
    )
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledContentPointOffset = OffsetCompat(
        x = containerPoint.x - contentBaseInsideDisplayRect.left,
        y = containerPoint.y - contentBaseInsideDisplayRect.top,
    )
    val contentPoint = IntOffsetCompat(
        x = (scaledContentPointOffset.x / contentScaleFactor.scaleX + contentBaseVisibleRect.left).roundToInt(),
        y = (scaledContentPointOffset.y / contentScaleFactor.scaleY + contentBaseVisibleRect.top).roundToInt(),
    )
    val limitedContentPoint = IntOffsetCompat(
        x = contentPoint.x.coerceIn(0, rotatedContentSize.width),
        y = contentPoint.y.coerceIn(0, rotatedContentSize.height)
    )
    val reversedRotatedContentPoint =
        limitedContentPoint.reverseRotateInSpace(contentSize, rotation)
    return reversedRotatedContentPoint
}

fun contentPointToContainerPoint(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    contentAlignment: AlignmentCompat,
    rotation: Int,
    contentPoint: IntOffsetCompat
): IntOffsetCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntOffsetCompat.Zero
    }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentPoint =
        contentPoint.rotateInSpace(contentSize, rotation)
    val contentBaseInsideDisplayRect = computeContentBaseInsideDisplayRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = 0,
    )
    val contentBaseVisibleRect = computeContentBaseVisibleRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = 0,
    )
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val contentPointOffset = OffsetCompat(
        x = rotatedContentPoint.x - contentBaseVisibleRect.left,
        y = rotatedContentPoint.y - contentBaseVisibleRect.top,
    )
    val scaledContentPointOffset = OffsetCompat(
        x = contentPointOffset.x * contentScaleFactor.scaleX,
        y = contentPointOffset.y * contentScaleFactor.scaleY,
    )
    val containerPoint = IntOffsetCompat(
        x = (contentBaseInsideDisplayRect.left + scaledContentPointOffset.x).roundToInt(),
        y = (contentBaseInsideDisplayRect.top + scaledContentPointOffset.y).roundToInt(),
    )
    val limitedContainerPoint = IntOffsetCompat(
        x = containerPoint.x.coerceIn(0, containerSize.width),
        y = containerPoint.y.coerceIn(0, containerSize.height),
    )
    return limitedContainerPoint
}
