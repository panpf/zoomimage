@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.compose.zoom.internal

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isHorizontalCenter
import com.github.panpf.zoomimage.compose.internal.isStart
import com.github.panpf.zoomimage.compose.internal.isTop
import com.github.panpf.zoomimage.compose.internal.isVerticalCenter
import com.github.panpf.zoomimage.compose.internal.limitTo
import com.github.panpf.zoomimage.compose.internal.restoreScale
import com.github.panpf.zoomimage.compose.internal.rotateBy
import com.github.panpf.zoomimage.compose.internal.roundToIntSize
import com.github.panpf.zoomimage.compose.internal.scale
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.zoom.Transform
import kotlin.math.roundToInt


/* ******************************************* Offset ***************************************** */

internal fun computeAlignmentIntOffset(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): IntOffset {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntOffset.Zero
    }
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledContentSize = contentSize.toSize().times(contentScaleFactor)
    val alignmentOffset = alignment.align(
        size = scaledContentSize.roundToIntSize(),
        space = containerSize,
        layoutDirection = LayoutDirection.Ltr
    )
    return alignmentOffset
}

internal fun computeLocationUserOffset(
    containerSize: IntSize,
    containerPoint: IntOffset,
    userScale: Float,
): Offset {
    if (containerSize.isEmpty()) {
        return Offset.Zero
    }
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val scaledContainerPoint = containerPoint.toOffset().times(userScale)
    val containerCenter = containerSize.center.toOffset()
    val originMoveToCenterOffset = scaledContainerPoint - containerCenter
    val locationOffset = Offset(
        x = (originMoveToCenterOffset.x * -1).coerceIn(-scaledContainerSize.width, 0f),
        y = (originMoveToCenterOffset.y * -1).coerceIn(-scaledContainerSize.height, 0f)
    )
    return locationOffset
}


/* ******************************************* In Rect ***************************************** */

internal fun computeContentInContainerRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): Rect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Rect.Zero
    }
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledContentSize = contentSize.toSize().times(contentScaleFactor)
    val alignmentOffset = alignment.align(
        size = scaledContentSize.roundToIntSize(),
        space = containerSize,
        layoutDirection = LayoutDirection.Ltr
    )
    val contentInContainerRect = Rect(
        left = alignmentOffset.x.toFloat(),
        top = alignmentOffset.y.toFloat(),
        right = alignmentOffset.x + scaledContentSize.width,
        bottom = alignmentOffset.y + scaledContentSize.height,
    )
    return contentInContainerRect
}

internal fun computeContentInContainerInnerRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): Rect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Rect.Zero
    }
    val contentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    val boundsRect = Rect(
        left = 0f,
        top = 0f,
        right = containerSize.width.toFloat(),
        bottom = containerSize.height.toFloat(),
    )
    val contentInContainerInnerRect = contentInContainerRect.limitTo(boundsRect)
    return contentInContainerInnerRect
}


/* ******************************************* VisibleRect ***************************************** */

internal fun computeContainerVisibleRect(
    containerSize: IntSize,
    userScale: Float,
    userOffset: Offset
): Rect {
    if (containerSize.isEmpty()) {
        return Rect.Zero
    }
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val topLeft = Offset(x = userOffset.x * -1, y = userOffset.y * -1)
    val scaledContainerVisibleRect = Rect(offset = topLeft, size = containerSize.toSize())
    val boundsRect = Rect(offset = Offset(0f, 0f), size = scaledContainerSize)
    val limitedScaledContainerVisibleRect = scaledContainerVisibleRect.limitTo(boundsRect)
    val filteredEmptyRect = limitedScaledContainerVisibleRect.takeIf { !it.isEmpty } ?: Rect.Zero
    val containerVisibleRect = filteredEmptyRect.restoreScale(userScale)
    return containerVisibleRect
}

internal fun computeContentInContainerVisibleRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): Rect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Rect.Zero
    }
    val contentScaleFactor = contentScale
        .computeScaleFactor(srcSize = contentSize.toSize(), dstSize = containerSize.toSize())
    val scaledContentSize = contentSize.toSize().times(contentScaleFactor)

    val left: Float
    val right: Float
    val horizontalSpace = (scaledContentSize.width - containerSize.width) / 2
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
    val verticalSpace = (scaledContentSize.height - containerSize.height) / 2
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
        Rect(left = left, top = top, right = right, bottom = bottom)
    val contentInContainerVisibleRect =
        scaledContentInContainerVisibleRect.restoreScale(contentScaleFactor)
    return contentInContainerVisibleRect
}

internal fun computeContentVisibleRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
    userScale: Float,
    userOffset: Offset,
): Rect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Rect.Zero
    }
    val containerVisibleRect = computeContainerVisibleRect(containerSize, userScale, userOffset)
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    if (!containerVisibleRect.overlaps(contentInContainerInnerRect)) {
        return Rect.Zero
    }
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    val contentRect = Rect(
        left = (containerVisibleRect.left - contentInContainerInnerRect.left),
        top = (containerVisibleRect.top - contentInContainerInnerRect.top),
        right = (containerVisibleRect.right - contentInContainerInnerRect.left),
        bottom = (containerVisibleRect.bottom - contentInContainerInnerRect.top)
    )
    val visibleBoundsRect = Rect(
        left = 0f,
        top = 0f,
        right = contentInContainerInnerRect.width,
        bottom = contentInContainerInnerRect.height
    )
    val limitedVisibleRect = contentRect.limitTo(visibleBoundsRect)
    val scaledLimitedVisibleRect = limitedVisibleRect.restoreScale(contentScaleFactor)
    val contentVisibleRect = scaledLimitedVisibleRect.translate(
        translateX = contentInContainerVisibleRect.left,
        translateY = contentInContainerVisibleRect.top
    )
    return contentVisibleRect
}


/* ******************************************* Point ***************************************** */

internal fun touchPointToContainerPoint(
    containerSize: IntSize,
    userScale: Float,
    userOffset: Offset,
    touchPoint: Offset
): IntOffset {
    if (containerSize.isEmpty()) {
        return IntOffset.Zero
    }
    val scaledContainerPoint = touchPoint - userOffset
    val containerPoint = IntOffset(
        x = (scaledContainerPoint.x / userScale).roundToInt(),
        y = (scaledContainerPoint.y / userScale).roundToInt(),
    )
    val limitedContainerPoint = IntOffset(
        x = containerPoint.x.coerceIn(0, containerSize.width),
        y = containerPoint.y.coerceIn(0, containerSize.height),
    )
    return limitedContainerPoint
}

internal fun containerPointToContentPoint(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    containerPoint: IntOffset
): IntOffset {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntOffset.Zero
    }
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = contentAlignment
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = contentAlignment
    )
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledContentPointOffset = Offset(
        x = containerPoint.x - contentInContainerInnerRect.left,
        y = containerPoint.y - contentInContainerInnerRect.top,
    )
    val contentPoint = IntOffset(
        x = (scaledContentPointOffset.x / contentScaleFactor.scaleX + contentInContainerVisibleRect.left).roundToInt(),
        y = (scaledContentPointOffset.y / contentScaleFactor.scaleY + contentInContainerVisibleRect.top).roundToInt(),
    )
    val limitedContentPoint = IntOffset(
        x = contentPoint.x.coerceIn(0, contentSize.width),
        y = contentPoint.y.coerceIn(0, contentSize.height)
    )
    return limitedContentPoint
}

internal fun contentPointToContainerPoint(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    contentPoint: IntOffset
): IntOffset {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntOffset.Zero
    }
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = contentAlignment
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = contentAlignment
    )
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val contentPointOffset = Offset(
        x = contentPoint.x - contentInContainerVisibleRect.left,
        y = contentPoint.y - contentInContainerVisibleRect.top,
    )
    val scaledContentPointOffset = Offset(
        x = contentPointOffset.x * contentScaleFactor.scaleX,
        y = contentPointOffset.y * contentScaleFactor.scaleY,
    )
    val containerPoint = IntOffset(
        x = (contentInContainerInnerRect.left + scaledContentPointOffset.x).roundToInt(),
        y = (contentInContainerInnerRect.top + scaledContentPointOffset.y).roundToInt(),
    )
    val limitedContainerPoint = IntOffset(
        x = containerPoint.x.coerceIn(0, containerSize.width),
        y = containerPoint.y.coerceIn(0, containerSize.height),
    )
    return limitedContainerPoint
}


/* ******************************************* Other ***************************************** */

internal fun computeUserOffsetBounds(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
    userScale: Float
): Rect {
    // based on the top left zoom
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Rect.Zero
    }
    val scaledContainerSize = containerSize.toSize().times(userScale)
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment
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

    val offsetBounds = Rect(
        left = horizontalBounds.start.roundToInt().toFloat(),
        top = verticalBounds.start.roundToInt().toFloat(),
        right = horizontalBounds.endInclusive.roundToInt().toFloat(),
        bottom = verticalBounds.endInclusive.roundToInt().toFloat()
    )
    return offsetBounds
}

internal fun computeTransform(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): Transform {
    val scaleFactor = contentScale.computeScaleFactor(contentSize.toSize(), containerSize.toSize())
    val contentScaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize.toSize(),
        dstSize = containerSize.toSize()
    )
    val scaledContentSize = contentSize.times(contentScaleFactor)
    val alignmentOffset = alignment.align(
        size = scaledContentSize,
        space = containerSize,
        layoutDirection = LayoutDirection.Ltr
    )
    return Transform(scale = scaleFactor, offset = alignmentOffset.toOffset())
}

internal fun computeReadModeTransform(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): Transform {
    val baseTransform = computeTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment
    )
    return com.github.panpf.zoomimage.core.internal.computeReadModeTransform(
        srcSize = contentSize.toCompatIntSize(),
        dstSize = containerSize.toCompatIntSize(),
        baseTransform = baseTransform.toCompatTransform()
    ).toTransform()
}

internal fun ContentScale.supportReadMode(): Boolean = this != ContentScale.FillBounds

internal fun computeTransformOffset(
    currentScale: Float,
    currentOffset: Offset,
    targetScale: Float,
    centroid: Offset,
    pan: Offset,
    gestureRotate: Float,
): Offset {
    // copied https://github.com/androidx/androidx/blob/643b1cfdd7dfbc5ccce1ad951b6999df049678b3/compose/foundation/foundation/samples/src/main/java/androidx/compose/foundation/samples/TransformGestureSamples.kt
    @Suppress("UnnecessaryVariable") val oldScale = currentScale
    @Suppress("UnnecessaryVariable") val newScale = targetScale
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

internal fun IntOffset.rotateInContainer(
    containerSize: IntSize,
    rotate: Int,
): IntOffset {
    require(rotate % 90 == 0) { "rotate must be an integer multiple of 90" }
    return when (rotate) {
        90 -> {
            IntOffset(
                x = containerSize.height - y,
                y = x
            )
        }

        180 -> {
            IntOffset(
                x = containerSize.width - x,
                y = containerSize.height - y
            )
        }

        270 -> {
            IntOffset(
                x = y,
                y = containerSize.width - x
            )
        }

        else -> this
    }
}