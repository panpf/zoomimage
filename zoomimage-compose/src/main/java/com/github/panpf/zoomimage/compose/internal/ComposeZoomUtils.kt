@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.compose.internal

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.core.Origin
import kotlin.math.roundToInt


/* ******************************************* Offset ***************************************** */

internal fun computeAlignmentOffset(
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
    val scaledContentSize = contentSize.times(contentScaleFactor)
    val alignmentOffset = alignment.align(
        size = scaledContentSize,
        space = containerSize,
        layoutDirection = LayoutDirection.Ltr
    )
    return alignmentOffset
}

internal fun computeLocationOffset(
    containerSize: IntSize,
    scale: Float,
    containerOrigin: Origin,
): Offset {
    if (containerSize.isEmpty()) {
        return Offset.Zero
    }
    val scaledContainerSize = containerSize.times(scale)
    val originInScaledContainerLocation = Offset(
        x = scaledContainerSize.width * containerOrigin.x,
        y = scaledContainerSize.height * containerOrigin.y,
    )
    val containerCenter = Offset(x = containerSize.width / 2f, y = containerSize.height / 2f)
    val originMoveToCenterOffset = originInScaledContainerLocation - containerCenter
    val locationOffset = Offset(
        x = (originMoveToCenterOffset.x * -1).coerceIn(-scaledContainerSize.width.toFloat(), 0f),
        y = (originMoveToCenterOffset.y * -1).coerceIn(-scaledContainerSize.height.toFloat(), 0f)
    )
    return locationOffset
}


/* ******************************************* In Rect ***************************************** */

internal fun computeContentInContainerRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): IntRect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntRect.Zero
    }
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
    val contentInContainerRect = IntRect(
        left = alignmentOffset.x,
        top = alignmentOffset.y,
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
): IntRect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntRect.Zero
    }
    val contentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    val boundsRect = IntRect(
        left = 0,
        top = 0,
        right = containerSize.width,
        bottom = containerSize.height,
    )
    val contentInContainerInnerRect = contentInContainerRect.limitTo(boundsRect)
    return contentInContainerInnerRect
}


/* ******************************************* VisibleRect ***************************************** */

internal fun computeContainerVisibleRect(
    containerSize: IntSize,
    scale: Float,
    offset: Offset
): IntRect {
    if (containerSize.isEmpty()) {
        return IntRect.Zero
    }
    val scaledContainerSize = containerSize.times(scale)
    val topLeft = IntOffset(x = (offset.x * -1).roundToInt(), y = (offset.y * -1).roundToInt())
    val scaledContainerVisibleRect = IntRect(offset = topLeft, size = containerSize)
    val boundsRect = IntRect(offset = IntOffset(0, 0), size = scaledContainerSize)
    val limitedScaledContainerVisibleRect = scaledContainerVisibleRect.limitTo(boundsRect)
    val filteredEmptyRect = limitedScaledContainerVisibleRect.takeIf { !it.isEmpty } ?: IntRect.Zero
    val containerVisibleRect = filteredEmptyRect.restoreScale(scale)
    return containerVisibleRect
}

internal fun computeContentInContainerVisibleRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
): IntRect {
    // todo 测试，优化
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntRect.Zero
    }
    val contentScaleFactor = contentScale
        .computeScaleFactor(srcSize = contentSize.toSize(), dstSize = containerSize.toSize())
    val scaledContentSize = contentSize.times(contentScaleFactor)

    val left: Int
    val right: Int
    val horizontalSpace = (scaledContentSize.width - containerSize.width) / 2
    if (scaledContentSize.width <= containerSize.width) {
        left = 0
        right = scaledContentSize.width
    } else if (alignment.isStart) {
        left = 0
        right = containerSize.width
    } else if (alignment.isHorizontalCenter) {
        left = horizontalSpace
        right = horizontalSpace + containerSize.width
    } else {   // contentAlignment.isEnd
        left = scaledContentSize.width - containerSize.width
        right = scaledContentSize.width
    }

    val top: Int
    val bottom: Int
    val verticalSpace = (scaledContentSize.height - containerSize.height) / 2
    if (scaledContentSize.height <= containerSize.height) {
        top = 0
        bottom = scaledContentSize.height
    } else if (alignment.isTop) {
        top = 0
        bottom = containerSize.height
    } else if (alignment.isVerticalCenter) {
        top = verticalSpace
        bottom = verticalSpace + containerSize.height
    } else {   // contentAlignment.isBottom
        top = scaledContentSize.height - containerSize.height
        bottom = scaledContentSize.height
    }

    val scaledContentInContainerVisibleRect =
        IntRect(left = left, top = top, right = right, bottom = bottom)
    val contentInContainerVisibleRect =
        scaledContentInContainerVisibleRect.restoreScale(contentScaleFactor)
    return contentInContainerVisibleRect
}

internal fun computeContentVisibleRect(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
    scale: Float,
    offset: Offset,
): IntRect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntRect.Zero
    }
    val containerVisibleRect = computeContainerVisibleRect(containerSize, scale, offset)
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment,
    )
    if (!containerVisibleRect.overlaps(contentInContainerInnerRect)) {
        return IntRect.Zero
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
    val contentRect = IntRect(
        left = (containerVisibleRect.left - contentInContainerInnerRect.left),
        top = (containerVisibleRect.top - contentInContainerInnerRect.top),
        right = (containerVisibleRect.right - contentInContainerInnerRect.left),
        bottom = (containerVisibleRect.bottom - contentInContainerInnerRect.top)
    )
    val visibleBoundsRect = IntRect(
        left = 0,
        top = 0,
        right = contentInContainerInnerRect.width,
        bottom = contentInContainerInnerRect.height
    )
    val limitedVisibleRect = contentRect.limitTo(visibleBoundsRect)
    val scaledLimitedVisibleRect = limitedVisibleRect.restoreScale(contentScaleFactor)
    val contentVisibleRect = scaledLimitedVisibleRect.translate(
        translateX = contentInContainerVisibleRect.left,
        translateY = contentInContainerVisibleRect.top
    )
    // todo 会超出边界，需要修复  图片高度只有 874，但是 contentVisibleRect.bottom = 875
    return contentVisibleRect
}


/* ******************************************* Origin ***************************************** */

internal fun computeContainerOriginByTouchPosition(
    containerSize: IntSize,
    scale: Float,
    offset: Offset,
    touch: Offset
): Origin {
    if (containerSize.isEmpty()) {
        return Origin.Zero
    }
    val touchOfContainer = touch - offset
    val restoreScaledTouchOfContainer = Offset(
        x = touchOfContainer.x / scale,
        y = touchOfContainer.y / scale,
    )
    val containerOrigin = Origin(
        x = restoreScaledTouchOfContainer.x / containerSize.width,
        y = restoreScaledTouchOfContainer.y / containerSize.height,
    )
    val limitedContainerOrigin = Origin(
        x = containerOrigin.x.coerceIn(0f, 1f),
        y = containerOrigin.y.coerceIn(0f, 1f),
    )
    return limitedContainerOrigin
}

internal fun containerOriginToContentOrigin(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    containerOrigin: Origin
): Origin {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Origin.Zero
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
    val containerOriginOffset = Offset(
        x = containerSize.width * containerOrigin.x,
        y = containerSize.height * containerOrigin.y
    )
    val contentScaledContentOriginOffset = Offset(
        x = containerOriginOffset.x - contentInContainerInnerRect.left,
        y = containerOriginOffset.y - contentInContainerInnerRect.top,
    )
    val contentOriginOffset = Offset(
        x = contentScaledContentOriginOffset.x / contentScaleFactor.scaleX,
        y = contentScaledContentOriginOffset.y / contentScaleFactor.scaleY,
    ).let {
        Offset(
            x = it.x + contentInContainerVisibleRect.left,
            y = it.y + contentInContainerVisibleRect.top
        )
    }.let {
        Offset(
            x = it.x.coerceIn(0f, contentSize.width.toFloat()),
            y = it.y.coerceIn(0f, contentSize.height.toFloat())
        )
    }
    return Origin(
        x = contentOriginOffset.x / contentSize.width,
        y = contentOriginOffset.y / contentSize.height
    )
}

internal fun contentOriginToContainerOrigin(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    contentOrigin: Origin
): Origin {
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return Origin.Zero
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
    val contentOriginOffset = Offset(
        x = contentSize.width * contentOrigin.x,
        y = contentSize.height * contentOrigin.y,
    ).let {
        Offset(
            x = it.x - contentInContainerVisibleRect.left,
            y = it.y - contentInContainerVisibleRect.top,
        )
    }
    val contentScaledContentOriginOffset = Offset(
        x = contentOriginOffset.x * contentScaleFactor.scaleX,
        y = contentOriginOffset.y * contentScaleFactor.scaleY,
    )
    val containerOriginOffset = Offset(
        x = contentInContainerInnerRect.left + contentScaledContentOriginOffset.x,
        y = contentInContainerInnerRect.top + contentScaledContentOriginOffset.y,
    ).let {
        Offset(
            x = it.x.coerceIn(0f, containerSize.width.toFloat()),
            y = it.y.coerceIn(0f, containerSize.height.toFloat()),
        )
    }
    return Origin(
        x = containerOriginOffset.x / containerSize.width,
        y = containerOriginOffset.y / containerSize.height
    )
}


/* ******************************************* Other ***************************************** */

internal fun computeOffsetBounds(
    containerSize: IntSize,
    contentSize: IntSize,
    contentScale: ContentScale,
    alignment: Alignment,
    scale: Float
): IntRect {
    // based on the top left zoom
    if (containerSize.isEmpty() || contentSize.isEmpty()) {
        return IntRect.Zero
    }
    val scaledContainerSize = containerSize.times(scale)
    val contentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        alignment = alignment
    )
    val scaledContentInContainerInnerRect = contentInContainerInnerRect.scale(scale)

    val horizontalBounds: IntRange =
        if (scaledContentInContainerInnerRect.width > containerSize.width) {
            ((scaledContentInContainerInnerRect.right - containerSize.width) * -1)..(scaledContentInContainerInnerRect.left * -1)
        } else if (alignment.isStart) {
            0..0
        } else if (alignment.isHorizontalCenter) {
            val horizontalSpace = (scaledContainerSize.width - containerSize.width) / 2 * -1
            horizontalSpace..horizontalSpace
        } else {   // contentAlignment.isEnd
            val horizontalSpace = (scaledContainerSize.width - containerSize.width) * -1
            horizontalSpace..horizontalSpace
        }

    val verticalBounds: IntRange =
        if (scaledContentInContainerInnerRect.height > containerSize.height) {
            ((scaledContentInContainerInnerRect.bottom - containerSize.height) * -1)..(scaledContentInContainerInnerRect.top * -1)
        } else if (alignment.isTop) {
            0..0
        } else if (alignment.isVerticalCenter) {
            val verticalSpace = (scaledContainerSize.height - containerSize.height) / 2 * -1
            verticalSpace..verticalSpace
        } else {   // contentAlignment.isBottom
            val verticalSpace = (scaledContainerSize.height - containerSize.height) * -1
            verticalSpace..verticalSpace
        }

    val offsetBounds = IntRect(
        left = horizontalBounds.first,
        top = verticalBounds.first,
        right = horizontalBounds.last,
        bottom = verticalBounds.last
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

internal fun computeScaleOffsetByCentroid(
    currentScale: Float,
    currentOffset: Offset,
    targetScale: Float,
    centroid: Offset,
    gestureRotate: Float,
): Offset {
    // copied https://github.com/androidx/androidx/blob/643b1cfdd7dfbc5ccce1ad951b6999df049678b3/compose/foundation/foundation/samples/src/main/java/androidx/compose/foundation/samples/TransformGestureSamples.kt
    @Suppress("UnnecessaryVariable") val oldScale = currentScale
    @Suppress("UnnecessaryVariable") val newScale = targetScale
    var contentOffset = currentOffset / currentScale * -1f
    // For natural zooming and rotating, the centroid of the gesture should
    // be the fixed point where zooming and rotating occurs.
    // We compute where the centroid was (in the pre-transformed coordinate
    // space), and then compute where it will be after this delta.
    // We then compute what the new offset should be to keep the centroid
    // visually stationary for rotating and zooming, and also apply the pan.
    contentOffset =
        (contentOffset + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale)
    return contentOffset * newScale * -1f
}