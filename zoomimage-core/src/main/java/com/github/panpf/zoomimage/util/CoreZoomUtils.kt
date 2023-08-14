@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.util

import com.github.panpf.zoomimage.Edge
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.util.internal.format
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/* ******************************************* ContentInContainer ***************************************** */

fun computeContentInContainerRect(
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

fun computeContentInContainerRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
): RectCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledContentSize = contentSize.toSize().times(contentScaleFactor)
    val alignmentOffset = alignment.align(
        size = scaledContentSize.round(),
        space = containerSize,
        ltrLayout = true,
    )
    val contentInContainerRect = RectCompat(
        left = alignmentOffset.x.toFloat(),
        top = alignmentOffset.y.toFloat(),
        right = alignmentOffset.x + scaledContentSize.width,
        bottom = alignmentOffset.y + scaledContentSize.height,
    )
    return contentInContainerRect
}

// todo 考虑这个函数的作用
fun computeContentInContainerInnerRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
): RectCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    val contentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    val boundsRect = RectCompat(
        left = 0f,
        top = 0f,
        right = containerSize.width.toFloat(),
        bottom = containerSize.height.toFloat(),
    )
    val contentInContainerInnerRect =
        contentInContainerRect.limitTo(boundsRect)
    return contentInContainerInnerRect
}


/* ******************************************* initial ***************************************** */

@Suppress("MemberVisibilityCanBePrivate")
class BaseTransformInfo(
    val containerSize: IntSizeCompat,
    val contentSize: IntSizeCompat,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val rotation: Int,
    val ltrLayout: Boolean = true,
) {

    val rotatedContentSize: IntSizeCompat by lazy {
        contentSize.rotate(rotation)
    }

    val rotatedContentScaleFactor: ScaleFactorCompat by lazy {
        contentScale.computeScaleFactor(
            srcSize = rotatedContentSize.toSize(),
            dstSize = containerSize.toSize()
        )
    }

    val scaledRotatedContentSize: IntSizeCompat by lazy {
        rotatedContentSize.times(rotatedContentScaleFactor)
    }

    val alignmentOffset: IntOffsetCompat by lazy {
        alignment.align(
            size = scaledRotatedContentSize,
            space = containerSize,
            ltrLayout = ltrLayout,
        )
    }

    val rotateOffset: OffsetCompat by lazy {
        val rotatedContentInContainerRect = computeContentInContainerRect(
            containerSize = containerSize,
            contentSize = contentSize,
            rotation = rotation,
        )
        val moveRotatedContentToTopLeftOffset =
            IntOffsetCompat.Zero - rotatedContentInContainerRect.topLeft
        moveRotatedContentToTopLeftOffset * rotatedContentScaleFactor
    }

    val baseTransform: TransformCompat by lazy {
        val offset = rotateOffset + alignmentOffset
        val rotationOrigin = if (rotation != 0) {
            val center = contentSize.toSize().center
            TransformOriginCompat(
                pivotFractionX = center.x / containerSize.width,
                pivotFractionY = center.y / containerSize.height
            )
        } else {
            TransformOriginCompat.TopStart
        }
        TransformCompat(
            scale = rotatedContentScaleFactor,
            scaleOrigin = TransformOriginCompat.TopStart,
            offset = offset,
            rotation = rotation.toFloat(),
            rotationOrigin = rotationOrigin,
        )
    }
}

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

    val baseTransformInfo = BaseTransformInfo(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation,
    )
    return baseTransformInfo.baseTransform
}

const val DefaultMediumScaleMinMultiple: Float = 3f

fun computeUserScales(
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    containerSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    baseScale: ScaleFactorCompat,
    mediumScaleMinMultiple: Float
): FloatArray {
    if (contentSize.isEmpty() || containerSize.isEmpty()) {
        return floatArrayOf(1.0f, 1.0f, 1.0f)
    } else if (contentScale == ContentScaleCompat.FillBounds
        || baseScale.scaleX.format(2) != baseScale.scaleY.format(2)
    ) {
        val minScale = 1.0f
        val mediumScale = minScale * mediumScaleMinMultiple
        return floatArrayOf(minScale, mediumScale, mediumScale * 2f)
    } else {
        // The width and height of content fill the container at the same time
        val fillContainerScale = max(
            containerSize.width / contentSize.width.toFloat(),
            containerSize.height / contentSize.height.toFloat()
        )
        // Enlarge content to the same size as its original
        val contentOriginScale = if (contentOriginSize.isNotEmpty) {
            val widthScale = contentOriginSize.width / contentSize.width.toFloat()
            val heightScale = contentOriginSize.height / contentSize.height.toFloat()
            max(widthScale, heightScale)
        } else {
            1.0f
        }
        val minScale = baseScale.scaleX
        val mediumScale = floatArrayOf(
            contentOriginScale,
            fillContainerScale,
            minScale * mediumScaleMinMultiple
        ).maxOrNull()!!
        val maxScale = mediumScale * 2f
        return floatArrayOf(minScale, mediumScale, maxScale)
            .map { it / baseScale.scaleX }
            .toFloatArray()
    }
}

// todo 拆分三个方法
fun computeZoomInitialConfig(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    contentAlignment: AlignmentCompat,
    rotation: Int,
    readMode: ReadMode?,
    mediumScaleMinMultiple: Float,
): InitialConfig {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return InitialConfig(
            minScale = 1.0f,
            mediumScale = 1.0f,
            maxScale = 1.0f,
            baseTransform = TransformCompat.Origin,
            userTransform = TransformCompat.Origin
        )
    }

    val rotatedContentSize = contentSize.rotate(rotation)
    val rotatedContentOriginSize = contentOriginSize.rotate(rotation)

    val baseTransform = computeBaseTransform(
        contentSize = contentSize,
        containerSize = containerSize,
        contentScale = contentScale,
        alignment = contentAlignment,
        rotation = rotation,
    )

    val userStepScales = computeUserScales(
        contentSize = rotatedContentSize,
        contentOriginSize = rotatedContentOriginSize,
        containerSize = containerSize,
        contentScale = contentScale,
        baseScale = contentScale.computeScaleFactor(
            srcSize = rotatedContentSize.toSize(),
            dstSize = containerSize.toSize()
        ),
        mediumScaleMinMultiple = mediumScaleMinMultiple,
    )
    val minScale = userStepScales[0] * baseTransform.scaleX
    val mediumScale = userStepScales[1] * baseTransform.scaleX
    val maxScale = userStepScales[2] * baseTransform.scaleX

    val readModeTransform = readMode
        ?.takeIf { contentScale != ContentScaleCompat.FillBounds }
        ?.takeIf {
            it.accept(
                srcSize = rotatedContentSize,
                dstSize = containerSize
            )
        }?.computeTransform(
            containerSize = containerSize,
            contentSize = rotatedContentSize,
            baseTransform = baseTransform
        )
    val userTransform = readModeTransform?.split(baseTransform)

    return InitialConfig(
        minScale = minScale,
        mediumScale = mediumScale,
        maxScale = maxScale,
        baseTransform = baseTransform,
        userTransform = userTransform ?: TransformCompat.Origin
    )
}

class InitialConfig(
    val minScale: Float,
    val mediumScale: Float,
    val maxScale: Float,
    val baseTransform: TransformCompat,
    val userTransform: TransformCompat,
)


/* ******************************************* VisibleRect ***************************************** */

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
    return containerVisibleRect
}

// todo 考虑这个函数的作用
fun computeContentInContainerVisibleRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
): RectCompat {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    val baseTransformInfo = BaseTransformInfo(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = rotation
    )
    val scaledContentSize = baseTransformInfo.scaledRotatedContentSize

    val left: Float
    val right: Float
    val horizontalSpace = (scaledContentSize.width - containerSize.width) / 2f
    if (scaledContentSize.width <= containerSize.width) {
        left = 0f
        right = scaledContentSize.width.toFloat()
    } else if (alignment.isStart) {
        left = 0f
        right = containerSize.width.toFloat()
    } else if (alignment.isHorizontalCenter) {
        left = horizontalSpace
        right = horizontalSpace + containerSize.width
    } else {   // contentAlignment.isEnd
        left = (scaledContentSize.width - containerSize.width).toFloat()
        right = scaledContentSize.width.toFloat()
    }

    val top: Float
    val bottom: Float
    val verticalSpace = (scaledContentSize.height - containerSize.height) / 2f
    if (scaledContentSize.height <= containerSize.height) {
        top = 0f
        bottom = scaledContentSize.height.toFloat()
    } else if (alignment.isTop) {
        top = 0f
        bottom = containerSize.height.toFloat()
    } else if (alignment.isVerticalCenter) {
        top = verticalSpace
        bottom = verticalSpace + containerSize.height
    } else {   // contentAlignment.isBottom
        top = (scaledContentSize.height - containerSize.height).toFloat()
        bottom = scaledContentSize.height.toFloat()
    }

    val scaledContentInContainerVisibleRect =
        RectCompat(left = left, top = top, right = right, bottom = bottom)
    val contentInContainerVisibleRect =
        scaledContentInContainerVisibleRect.restoreScale(baseTransformInfo.rotatedContentScaleFactor)
            .limitTo(baseTransformInfo.rotatedContentSize.toSize())
    val reverseRotateContentInContainerVisibleRect =
        contentInContainerVisibleRect.reverseRotateInSpace(contentSize.toSize(), rotation)
            .limitTo(contentSize.toSize())
    return reverseRotateContentInContainerVisibleRect
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
    val containerVisibleRect = computeContainerVisibleRect(containerSize, userScale, userOffset)
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    if (!containerVisibleRect.overlaps(contentInContainerInnerRect)) {
        return RectCompat.Zero
    }

    val contentRect = RectCompat(
        left = (containerVisibleRect.left - contentInContainerInnerRect.left),
        top = (containerVisibleRect.top - contentInContainerInnerRect.top),
        right = (containerVisibleRect.right - contentInContainerInnerRect.left),
        bottom = (containerVisibleRect.bottom - contentInContainerInnerRect.top)
    )
    val visibleBoundsRect = RectCompat(
        left = 0f,
        top = 0f,
        right = contentInContainerInnerRect.width,
        bottom = contentInContainerInnerRect.height
    )
    val limitedVisibleRect = contentRect.limitTo(visibleBoundsRect)
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = rotatedContentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledLimitedVisibleRect = limitedVisibleRect.restoreScale(contentScaleFactor)
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
        rotation = 0,
    )
    val contentVisibleRect = scaledLimitedVisibleRect.translate(
        translateX = contentInContainerVisibleRect.left,
        translateY = contentInContainerVisibleRect.top
    )
    val reversedRotateContentVisibleRect =
        contentVisibleRect.reverseRotateInSpace(contentSize.toSize(), rotation)
    return reversedRotateContentVisibleRect
}


/* ******************************************* Function ***************************************** */

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

fun computeUserOffsetBounds(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    alignment: AlignmentCompat,
    rotation: Int,
    userScale: Float,
): RectCompat {
    // based on the top left zoom
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return RectCompat.Zero
    }
    val rotatedContentSize = contentSize.rotate(rotation)
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    val scaledContentInContainerInnerRect = contentInContainerInnerRect.scale(userScale)

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
        left = horizontalBounds.start.roundToInt().toFloat(),
        top = verticalBounds.start.roundToInt().toFloat(),
        right = horizontalBounds.endInclusive.roundToInt().toFloat(),
        bottom = verticalBounds.endInclusive.roundToInt().toFloat()
    )
    return offsetBounds
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
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = contentAlignment,
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
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
        x = containerPoint.x - contentInContainerInnerRect.left,
        y = containerPoint.y - contentInContainerInnerRect.top,
    )
    val contentPoint = IntOffsetCompat(
        x = (scaledContentPointOffset.x / contentScaleFactor.scaleX + contentInContainerVisibleRect.left).roundToInt(),
        y = (scaledContentPointOffset.y / contentScaleFactor.scaleY + contentInContainerVisibleRect.top).roundToInt(),
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
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = rotatedContentSize,
        contentScale = contentScale,
        alignment = contentAlignment
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
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
        x = rotatedContentPoint.x - contentInContainerVisibleRect.left,
        y = rotatedContentPoint.y - contentInContainerVisibleRect.top,
    )
    val scaledContentPointOffset = OffsetCompat(
        x = contentPointOffset.x * contentScaleFactor.scaleX,
        y = contentPointOffset.y * contentScaleFactor.scaleY,
    )
    val containerPoint = IntOffsetCompat(
        x = (contentInContainerInnerRect.left + scaledContentPointOffset.x).roundToInt(),
        y = (contentInContainerInnerRect.top + scaledContentPointOffset.y).roundToInt(),
    )
    val limitedContainerPoint = IntOffsetCompat(
        x = containerPoint.x.coerceIn(0, containerSize.width),
        y = containerPoint.y.coerceIn(0, containerSize.height),
    )
    return limitedContainerPoint
}
